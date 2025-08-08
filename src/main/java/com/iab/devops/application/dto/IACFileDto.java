package com.iab.devops.application.dto;

import com.iab.devops.domain.entity.IACFile;
import com.iab.devops.domain.enums.IACType;

import java.net.URL;

public record IACFileDto(Long id, String name, IACType type, URL url) {

    public IACFileDto(IACFile iacFile, URL url) {
        this(iacFile.getId(),
                iacFile.getName(),
                iacFile.getType(),
                url);
    }

}
