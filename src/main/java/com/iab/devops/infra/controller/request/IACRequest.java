package com.iab.devops.infra.controller.request;

import com.iab.devops.domain.enums.IACType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record IACRequest(
        @NotBlank
        String prompt,
        @NotNull
        IACType type) {
}
