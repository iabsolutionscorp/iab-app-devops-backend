package com.iab.devops.application.gateway;

import com.iab.devops.domain.enums.IACType;

public interface GenerativeIAGateway {
    public String generateCode(IACType type, String prompt);
}
