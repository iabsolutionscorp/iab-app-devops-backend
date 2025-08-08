package com.iab.devops.mapper;

import com.iab.devops.application.dto.IACFileDto;
import com.iab.devops.domain.entity.IACFile;
import com.iab.devops.domain.enums.IACType;
import com.iab.devops.infra.controller.response.IACFileResponse;
import com.iab.devops.infra.persistence.entity.IACFileEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

@Mapper(componentModel = SPRING,
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface IACFileMapper {

    @Mapping(target = "location", ignore = true)
    @Mapping(target = "id", ignore = true)
    IACFile toDomain(String name, IACType type);

    default InputStream toInputStream(MultipartFile file) {
        if (file == null) {
            throw new IllegalArgumentException("file must not be null");
        }
        try {
            return file.getInputStream();
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to open InputStream from MultipartFile", e);
        }
    }

    IACFileEntity toEntity(IACFile iacFile);

    IACFile toDomain(IACFileEntity saved);
}
