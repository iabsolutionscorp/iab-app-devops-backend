package com.iab.devops.config;

import com.amazonaws.services.s3.AmazonS3;
import com.iab.devops.application.gateway.FilePersistenceGateway;
import com.iab.devops.application.gateway.IACFileGateway;
import com.iab.devops.application.gateway.InfrastructureProvisionerGateway;
import com.iab.devops.application.usecase.GenerateIACCodeUseCase;
import com.iab.devops.application.gateway.GenerativeIAGateway;
import com.iab.devops.application.usecase.iacfile.CreateIACFileUseCase;
import com.iab.devops.application.usecase.iacfile.GetIACFileByIdUseCase;
import com.iab.devops.application.usecase.iacfile.UpdateIACFileUseCase;
import com.iab.devops.domain.enums.IACType;
import com.iab.devops.infra.gateway.FilePersistenceGatewayImpl;
import com.iab.devops.infra.gateway.GenerativeIAGatewayImpl;
import com.iab.devops.infra.gateway.IACFileGatewayImpl;
import com.iab.devops.infra.gateway.TerraformProvisioner;
import com.iab.devops.infra.persistence.repository.IACFileRepository;
import com.iab.devops.mapper.IACFileMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

@Configuration
public class IACConfig {

    @Bean
    public Map<IACType, InfrastructureProvisionerGateway> provisioners(TerraformProvisioner terraformProvisioner) {
        EnumMap<IACType, InfrastructureProvisionerGateway> map = new EnumMap<>(IACType.class);
        map.put(IACType.TERRAFORM, terraformProvisioner);
        return Collections.unmodifiableMap(map);
    }

    @Bean
    GenerativeIAGateway generativeIAGateway() {
        return new GenerativeIAGatewayImpl();
    }

    @Bean
    GenerateIACCodeUseCase generateIACCodeUseCase(GenerativeIAGateway generativeIAGateway) {
        return new GenerateIACCodeUseCase(generativeIAGateway);
    }

    @Bean
    CreateIACFileUseCase createIACFileUseCase(FilePersistenceGateway filePersistenceGateway, IACFileGateway iacFileGateway) {
        return new CreateIACFileUseCase(filePersistenceGateway, iacFileGateway);
    }

    @Bean
    GetIACFileByIdUseCase getIACFileByIdUseCase(IACFileGateway fileGateway,
                                                FilePersistenceGateway gateway){
        return new GetIACFileByIdUseCase(fileGateway, gateway);
    }

    @Bean
    UpdateIACFileUseCase updateIACFileUseCase(IACFileGateway iacFileGateway,
                                              FilePersistenceGateway filePersistenceGateway){
        return new UpdateIACFileUseCase(iacFileGateway, filePersistenceGateway);
    }


}
