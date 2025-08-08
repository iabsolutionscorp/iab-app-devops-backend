package com.iab.devops.application.usecase.iacfile;

import com.iab.devops.application.gateway.FilePersistenceGateway;
import com.iab.devops.application.gateway.IACFileGateway;
import com.iab.devops.domain.entity.IACFile;

import java.io.InputStream;

public class CreateIACFileUseCase {

    private final FilePersistenceGateway filePersistenceGateway;
    private final IACFileGateway iacFileGateway;

    public CreateIACFileUseCase(FilePersistenceGateway filePersistenceGateway,
                                IACFileGateway iacFileGateway) {
        this.filePersistenceGateway = filePersistenceGateway;
        this.iacFileGateway = iacFileGateway;
    }

    public Long execute(IACFile iacFile, InputStream inputStream) {
        IACFile saved = iacFileGateway.save(iacFile);
        filePersistenceGateway.upload(inputStream, saved.getLocation(), saved.getType().getContentType());
        return saved.getId();
    }
}
