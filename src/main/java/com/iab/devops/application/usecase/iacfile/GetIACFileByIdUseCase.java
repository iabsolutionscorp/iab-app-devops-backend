package com.iab.devops.application.usecase.iacfile;

import com.iab.devops.application.dto.IACFileDto;
import com.iab.devops.application.gateway.FilePersistenceGateway;
import com.iab.devops.application.gateway.IACFileGateway;
import com.iab.devops.domain.entity.IACFile;

import java.net.URL;

public class GetIACFileByIdUseCase {

    private final IACFileGateway iacFileGateway;
    private final FilePersistenceGateway filePersistenceGateway;

    public GetIACFileByIdUseCase(IACFileGateway iacFileGateway, FilePersistenceGateway filePersistenceGateway) {
        this.iacFileGateway = iacFileGateway;
        this.filePersistenceGateway = filePersistenceGateway;
    }


    public IACFileDto execute(Long id) {
        IACFile iacFile = iacFileGateway.getById(id)
                .orElseThrow(RuntimeException::new);
        URL url = filePersistenceGateway.getUrl(iacFile.getLocation());

        return new IACFileDto(iacFile, url);
    }
}
