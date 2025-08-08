package com.iab.devops.application.usecase;


import com.iab.devops.application.gateway.GenerativeIAGateway;
import com.iab.devops.domain.enums.IACType;

public class GenerateIACCodeUseCase {

    private final GenerativeIAGateway generativeIAGateway;

    public GenerateIACCodeUseCase(GenerativeIAGateway generativeIAGateway) {
        this.generativeIAGateway = generativeIAGateway;
    }

    public String execute(IACType type, String prompt) {
        return generativeIAGateway.generateCode(type, prompt);
    }
}
