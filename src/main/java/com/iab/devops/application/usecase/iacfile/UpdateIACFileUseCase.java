package com.iab.devops.application.usecase.iacfile;

import com.iab.devops.application.gateway.FilePersistenceGateway;
import com.iab.devops.application.gateway.IACFileGateway;
import com.iab.devops.domain.entity.IACFile;

import java.io.InputStream;

public class UpdateIACFileUseCase {

    private final IACFileGateway iacFileGateway;
    private final FilePersistenceGateway filePersistenceGateway;

    public UpdateIACFileUseCase(IACFileGateway iacFileGateway, FilePersistenceGateway filePersistenceGateway) {
        this.iacFileGateway = iacFileGateway;
        this.filePersistenceGateway = filePersistenceGateway;
    }

    public void execute(Long id, InputStream file){
        IACFile iacFile = iacFileGateway.getById(id)
                .orElseThrow(RuntimeException::new);
        filePersistenceGateway.upload(file, iacFile.getLocation(), iacFile.getType().getContentType());
    }
}
