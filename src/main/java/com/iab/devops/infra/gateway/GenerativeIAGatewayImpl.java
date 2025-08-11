package com.iab.devops.infra.gateway;

import com.iab.devops.application.gateway.GenerativeIAGateway;
import com.iab.devops.domain.enums.IACType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@RequiredArgsConstructor
@Component
public class GenerativeIAGatewayImpl implements GenerativeIAGateway {

    @Value("${iab.devops.aiac.workdir:#{systemProperties['user.dir'] + '/local'}}")
    private Path workDir;

    @Override
    public String generateCode(IACType type, String prompt) {
        final String safePromptPreview = prompt == null ? "" :
                prompt.replaceAll("\\s+", " ").substring(0, Math.min(prompt.length(), 200));
        log.info("Iniciando geração com AIAC | type={} | workDir='{}' | prompt(200ch)='{}...'",
                type, workDir, safePromptPreview);

        Instant start = Instant.now();

        try {
            Process process = generateCodeWithAI(type, prompt);
            log.info("Processo iniciado. Comando: {}", pretty(process.info().commandLine().orElse("<desconhecido>")));

            // Lê saída em tempo real e já vai logando
            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {

                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append('\n');
                    log.debug("[AIAC] {}", line);
                }
            }

            // Timeout de segurança (um pouco acima do --timeout 120 do aiac)
            boolean finished = process.waitFor(3, TimeUnit.MINUTES);
            if (!finished) {
                process.destroyForcibly();
                Duration took = Duration.between(start, Instant.now());
                log.error("AIAC não finalizou no tempo esperado. Processo destruído. Decorrido: {}s",
                        took.toSeconds());
                throw new RuntimeException("Timeout ao executar aiac (3 min).");
            }

            int exit = process.exitValue();
            Duration took = Duration.between(start, Instant.now());
            log.info("Processo finalizado | exitCode={} | duração={}s", exit, took.toSeconds());

            if (exit != 0) {
                log.warn("AIAC retornou erro (exitCode={}). Saída:\n{}", exit, output);
                throw new RuntimeException("Erro ao executar aiac, exitCode=" + exit + ", veja logs.");
            }

            String result = output.toString().trim();
            log.info("Geração concluída com sucesso ({} chars).", result.length());
            return result;

        } catch (Exception ex) {
            log.error("Falha ao executar AIAC: {}", ex.getMessage(), ex);
            throw new RuntimeException("Erro ao executar aiac: " + ex.getMessage(), ex);
        }
    }

    private Process generateCodeWithAI(IACType type, String prompt) throws Exception {
        List<String> cmd = List.of(
                "docker", "compose",
                "exec", "-T", "aiac",
                "aiac",
                "-b", "gemini",
                "-q",
                "--timeout", "120",
                type.name().toLowerCase(), "for", "aws",
                prompt
        );

        File dir = new File(workDir.toString());
        if (!dir.exists()) {
            log.warn("WorkDir '{}' não existe. Tentando criar...", dir.getAbsolutePath());
            boolean created = dir.mkdirs();
            log.info("WorkDir criado? {}", created);
        }

        log.info("Executando em '{}' | CMD: {}", dir.getAbsolutePath(), quoteJoin(cmd));
        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.directory(dir);
        pb.redirectErrorStream(true); // junta stderr em stdout

        return pb.start();
    }

    private static String quoteJoin(List<String> parts) {
        return String.join(" ", parts.stream()
                .map(p -> p.contains(" ") ? "\"" + p + "\"" : p)
                .toList());
    }

    private static String pretty(String cmdline) {
        return cmdline == null ? "" : cmdline;
    }
}
