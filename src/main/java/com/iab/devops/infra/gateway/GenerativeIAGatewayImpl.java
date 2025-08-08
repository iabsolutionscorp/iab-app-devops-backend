package com.iab.devops.infra.gateway;

import com.iab.devops.application.gateway.GenerativeIAGateway;
import com.iab.devops.domain.enums.IACType;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.time.LocalTime;
import java.util.List;

@RequiredArgsConstructor
@Component
public class GenerativeIAGatewayImpl implements GenerativeIAGateway {


    @Value("${iab.devops.aiac.workdir:#{systemProperties['user.dir'] + '/local'}}")
    private Path workDir;

    @Override
    public String generateCode(IACType type, String prompt) {
        try {
            Process process = generateCodeWithAI(type, prompt);

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream())
            );

            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }

            int exit = process.waitFor();

            if (exit != 0) {
                throw new RuntimeException("Erro ao executar aiac, exitCode=" + exit + ", output:\n" + output);
            }
            return output.toString().trim();

        } catch (Exception ex) {
            throw new RuntimeException("Erro ao executar aiac: " + ex.getMessage(), ex);
        }
    }

    private Process generateCodeWithAI(IACType type, String prompt) throws IOException {
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

        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.directory(new File(workDir.toString()));
        pb.redirectErrorStream(true);

        return pb.start();
    }
}
