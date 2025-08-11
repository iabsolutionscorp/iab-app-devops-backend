package com.iab.devops.infra.gateway;

import com.iab.devops.application.gateway.InfrastructureProvisionerGateway;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class TerraformProvisioner implements InfrastructureProvisionerGateway {

    @Value("${iac.terraform.baseWorkingDir:/tmp/terraform}")
    private Path baseWorkingDir;

    @Value("${aws.accessKeyId}")
    private String awsAccessKeyId;

    @Value("${aws.secretAccessKey}")
    private String awsSecretAccessKey;

    /** Opcional: só necessário se você obtiver token via STS */
    @Value("${aws.sessionToken:}")
    private String awsSessionToken;

    /** Opcional: útil pra LocalStack (mostrado só em log) */
    @Value("${aws.region:us-east-1}")
    private String awsRegion;

    /** Timeout padrão por comando Terraform */
    private static final Duration DEFAULT_TIMEOUT = Duration.ofMinutes(10);

    @Override
    public void deploy(InputStream iacFileStream) {
        Path runDir = null;
        try {
            // 1. Cria diretório único para esta execução
            Files.createDirectories(baseWorkingDir);
            runDir = Files.createTempDirectory(baseWorkingDir, "iac-");
            log.info("Terraform runDir criado: {}", runDir);

            // 2. Copia o conteúdo do main.tf
            Path mainTf = runDir.resolve("main.tf");
            Files.copy(iacFileStream, mainTf);
            log.info("Arquivo principal escrito em: {}", mainTf);

            // 3. Executa init, validate e apply (com logs detalhados)
            runCommand(runDir, DEFAULT_TIMEOUT, "terraform", "init", "-input=false", "-no-color");
            runCommand(runDir, Duration.ofMinutes(2), "terraform", "validate", "-no-color");
            runCommand(runDir, DEFAULT_TIMEOUT, "terraform", "apply", "-auto-approve", "-input=false", "-no-color");

        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Execução interrompida durante provisionamento Terraform", ie);
        } catch (IOException e) {
            throw new RuntimeException("Erro de I/O ao provisionar infraestrutura via Terraform", e);
        } finally {
            // 4. Limpa temp
            if (runDir != null) {
                try {
                    deleteTempFile(runDir);
                } catch (IOException e) {
                    log.warn("Falha ao deletar diretório temporário {}: {}", runDir, e.getMessage());
                }
            }
        }
    }

    private void runCommand(Path dir, Duration timeout, String... command) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.directory(dir.toFile());
        // NÃO redireciona stderr -> queremos separar para logar bonito
        injectAWSCredentials(pb);

        Map<String, String> env = pb.environment();
        env.putIfAbsent("TF_IN_AUTOMATION", "1");

        // Log inicial
        log.info("Executando: '{}'", String.join(" ", command));
        log.info("Workdir: {}", dir);
        log.info("Env (parcial): TF_IN_AUTOMATION={}, AWS_ACCESS_KEY_ID={}, AWS_REGION={}, AWS_SESSION_TOKEN_SET={}",
                env.get("TF_IN_AUTOMATION"),
                mask(env.get("AWS_ACCESS_KEY_ID")),
                awsRegion,
                (env.get("AWS_SESSION_TOKEN") != null && !env.get("AWS_SESSION_TOKEN").isBlank()));

        Process process = pb.start();

        // Captura stdout e stderr em paralelo
        StreamGobbler out = new StreamGobbler(process.getInputStream(), l -> log.info("[tf-out] {}", l));
        StreamGobbler err = new StreamGobbler(process.getErrorStream(), l -> log.warn("[tf-err] {}", l));
        out.start();
        err.start();

        boolean finished = process.waitFor(timeout.toMillis(), TimeUnit.MILLISECONDS);
        if (!finished) {
            process.destroyForcibly();
            throw new RuntimeException("Timeout executando: " + String.join(" ", command));
        }

        int exitCode = process.exitValue();
        if (exitCode != 0) {
            String lastErr = err.tail();
            String lastOut = out.tail();
            throw new RuntimeException(
                    "Comando falhou (exit code " + exitCode + "): " + String.join(" ", command) +
                            "\n--- STDERR (tail) ---\n" + lastErr +
                            "\n--- STDOUT (tail) ---\n" + lastOut
            );
        }
    }

    private void injectAWSCredentials(ProcessBuilder pb) {
        Map<String, String> env = pb.environment();
        env.put("AWS_ACCESS_KEY_ID", awsAccessKeyId);
        env.put("AWS_SECRET_ACCESS_KEY", awsSecretAccessKey);
        env.putIfAbsent("AWS_REGION", awsRegion);
        if (!awsSessionToken.isBlank()) {
            env.put("AWS_SESSION_TOKEN", awsSessionToken);
        }
    }

    private void logProcessOutput(Process process) throws IOException {
        // (não usado mais; mantido só se você quiser versão que junta stdout/stderr)
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                log.info(line);
            }
        }
    }

    private static void deleteTempFile(Path runDir) throws IOException {
        Files.walk(runDir)
                .sorted(Comparator.reverseOrder())
                .forEach(path -> {
                    try {
                        Files.deleteIfExists(path);
                    } catch (IOException e) {
                        log.warn("Falha ao deletar {}: {}", path, e.getMessage());
                    }
                });
    }

    /** Utilitário para mascarar credenciais em logs */
    private static String mask(String s) {
        if (s == null || s.isBlank()) return "(vazio)";
        if (s.length() <= 4) return "****";
        return s.substring(0, 2) + "****" + s.substring(s.length() - 2);
    }

    /** Gobbler com ring buffer para incluir tail na exception */
    private static class StreamGobbler extends Thread {
        private final InputStream is;
        private final java.util.function.Consumer<String> consumer;
        private final Deque<String> ring = new ArrayDeque<>();
        private final int max = 200;

        StreamGobbler(InputStream is, java.util.function.Consumer<String> consumer) {
            this.is = is;
            this.consumer = consumer;
            setName("tf-stream-" + getId());
            setDaemon(true);
        }

        @Override public void run() {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
                String line;
                while ((line = br.readLine()) != null) {
                    consumer.accept(line);
                    ring.addLast(line);
                    if (ring.size() > max) ring.removeFirst();
                }
            } catch (IOException ignored) {}
        }

        String tail() {
            return String.join("\n", ring);
        }
    }
}
