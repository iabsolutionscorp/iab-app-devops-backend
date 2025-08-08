package com.iab.devops.infra.gateway;

import com.iab.devops.application.gateway.InfrastructureProvisionerGateway;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Map;

@Slf4j
@Service
public class TerraformProvisioner implements InfrastructureProvisionerGateway {

    @Value("${iac.terraform.baseWorkingDir:/tmp/terraform}")
    private Path baseWorkingDir;

    @Value("${aws.accessKeyId}")
    private String awsAccessKeyId;

    @Value("${aws.secretAccessKey}")
    private String awsSecretAccessKey;

    /**
     * Opcional: só necessário se você obtiver token via STS
     */
    @Value("${aws.sessionToken:}")
    private String awsSessionToken;

    @Override
    public void deploy(InputStream iacFileStream) {
        try {
            // 1. Cria diretório único para esta execução
            Files.createDirectories(baseWorkingDir);
            Path runDir = Files.createTempDirectory(baseWorkingDir, "iac-");

            // 2. Copia o conteúdo do main.tf
            Path mainTf = runDir.resolve("main.tf");
            Files.copy(iacFileStream, mainTf);

            // 3. Executa init e apply
            runCommand(runDir, "terraform", "init", "-input=false", "-no-color");
            runCommand(runDir, "terraform", "apply", "-auto-approve", "-input=false", "-no-color");

            // 4. Limpa temp
            deleteTempFile(runDir);

        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Erro ao provisionar infraestrutura via Terraform", e);
        }
    }

    private void runCommand(Path dir, String... command) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.directory(dir.toFile());
        pb.redirectErrorStream(true);
        injectAWSCredentials(pb);
        Process process = pb.start();
        logProcessOutput(process);

        int exitCode = process.waitFor();
        if (exitCode != 0) throw new RuntimeException("Comando falhou (exit code " + exitCode + "): " + String.join(" ", command));
    }

    private void injectAWSCredentials(ProcessBuilder pb) {
        Map<String, String> env = pb.environment();
        env.put("AWS_ACCESS_KEY_ID", awsAccessKeyId);
        env.put("AWS_SECRET_ACCESS_KEY", awsSecretAccessKey);
        if (!awsSessionToken.isBlank()) {
            env.put("AWS_SESSION_TOKEN", awsSessionToken);
        }
    }

    private void logProcessOutput(Process process) throws IOException {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()))) {
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
}
