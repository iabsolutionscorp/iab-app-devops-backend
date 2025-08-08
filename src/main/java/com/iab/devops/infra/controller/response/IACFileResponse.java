package com.iab.devops.infra.controller.response;

import com.iab.devops.domain.entity.IACFile;
import com.iab.devops.domain.enums.IACType;

import java.net.URL;

public record IACFileResponse(Long id,
                              String name,
                              IACType type,
                              URL url) {
}
