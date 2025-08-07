package com.iab.devops.application;


import com.iab.devops.application.gateway.GenerativeIAGateway;

public class GenerateIACCodeUseCase {

    private final GenerativeIAGateway generativeIAGateway;

    public GenerateIACCodeUseCase(GenerativeIAGateway generativeIAGateway) {
        this.generativeIAGateway = generativeIAGateway;
    }

    public String execute(String prompt) {
        return generativeIAGateway.generateCode(prompt);
    }
}
