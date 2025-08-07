package com.iab.devops.config;

import com.iab.devops.application.GenerateIACCodeUseCase;
import com.iab.devops.application.gateway.GenerativeIAGateway;
import com.iab.devops.infra.gateway.GenerativeIAGatewayImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class IACConfig {

    @Bean
    GenerativeIAGateway generativeIAGateway(){
        return new GenerativeIAGatewayImpl();
    }

    @Bean
    GenerateIACCodeUseCase generateIACCodeUseCase(GenerativeIAGateway generativeIAGateway){
        return new GenerateIACCodeUseCase(generativeIAGateway);
    }



}
