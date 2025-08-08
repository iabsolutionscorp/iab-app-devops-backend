package com.iab.devops.config;

import com.iab.devops.application.usecase.iacfile.DeployIACFileUseCase;
import com.iab.devops.application.gateway.FilePersistenceGateway;
import com.iab.devops.application.gateway.IACFileGateway;
import com.iab.devops.application.gateway.InfrastructureProvisionerGateway;
import com.iab.devops.domain.enums.IACType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.core.task.TaskExecutor;

import java.util.EnumMap;
import java.util.Map;

@Configuration
public class AsyncConfig {

    @Bean
    public TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor exec = new ThreadPoolTaskExecutor();
        exec.setCorePoolSize(5);
        exec.setMaxPoolSize(10);
        exec.setQueueCapacity(25);
        exec.initialize();
        return exec;
    }

    @Bean
    @Primary
    public DeployIACFileUseCase deployIacFileUseCaseAsync(TaskExecutor taskExecutor,
                                                          IACFileGateway iacFileGateway,
                                                          FilePersistenceGateway filePersistenceGateway,
                                                          Map<IACType, InfrastructureProvisionerGateway> provisioners
    ) {
        return new DeployIACFileUseCase(iacFileGateway, filePersistenceGateway, provisioners) {
            @Override public void execute(Long id) {taskExecutor.execute(() -> super.execute(id));}
        };
    }
}
