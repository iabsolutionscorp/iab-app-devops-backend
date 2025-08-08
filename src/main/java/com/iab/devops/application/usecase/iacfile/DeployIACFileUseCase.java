package com.iab.devops.application.usecase.iacfile;

import com.iab.devops.application.gateway.FilePersistenceGateway;
import com.iab.devops.application.gateway.IACFileGateway;
import com.iab.devops.application.gateway.InfrastructureProvisionerGateway;
import com.iab.devops.domain.entity.IACFile;
import com.iab.devops.domain.enums.IACType;

import java.io.InputStream;
import java.util.Map;

public class DeployIACFileUseCase {

    private final IACFileGateway iacFileGateway;
    private final FilePersistenceGateway filePersistenceGateway;
    private final Map<IACType, InfrastructureProvisionerGateway> provisioners;

    public DeployIACFileUseCase(IACFileGateway iacFileGateway,
                                FilePersistenceGateway filePersistenceGateway,
                                Map<IACType, InfrastructureProvisionerGateway> provisioners) {
        this.iacFileGateway = iacFileGateway;
        this.filePersistenceGateway = filePersistenceGateway;
        this.provisioners = provisioners;
    }

    public void execute(Long id){
        IACFile iacFile = iacFileGateway.getById(id)
                .orElseThrow(RuntimeException::new);
        InputStream savedFile = filePersistenceGateway.download(iacFile.getLocation());
        InfrastructureProvisionerGateway provisioner = provisioners.get(iacFile.getType());
        provisioner.deploy(savedFile);
    }

}
