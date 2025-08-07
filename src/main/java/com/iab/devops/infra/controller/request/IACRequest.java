package com.iab.devops.infra.controller.request;

import jakarta.validation.constraints.NotBlank;

public record IACRequest(
        @NotBlank
        String prompt) {
}
