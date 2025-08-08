package com.iab.devops.infra.gateway;

import com.iab.devops.application.gateway.IACFileGateway;
import com.iab.devops.domain.entity.IACFile;
import com.iab.devops.infra.persistence.entity.IACFileEntity;
import com.iab.devops.infra.persistence.repository.IACFileRepository;
import com.iab.devops.mapper.IACFileMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@RequiredArgsConstructor
@Component
public class IACFileGatewayImpl implements IACFileGateway {

    private final IACFileRepository repository;
    private final IACFileMapper mapper;

    @Override
    public IACFile save(IACFile iacFile) {
        IACFileEntity entity =  mapper.toEntity(iacFile);
        IACFileEntity saved = repository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<IACFile> getById(Long id) {
        return repository.findById(id).map(mapper::toDomain);
    }
}
