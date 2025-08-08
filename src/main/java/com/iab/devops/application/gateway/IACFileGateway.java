package com.iab.devops.application.gateway;

import com.iab.devops.domain.entity.IACFile;

import java.util.Optional;

public interface IACFileGateway {
    IACFile save(IACFile iacFile);
    Optional<IACFile> getById(Long id);
}
