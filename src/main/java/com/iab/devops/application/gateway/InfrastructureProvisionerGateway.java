package com.iab.devops.application.gateway;

import java.io.InputStream;

public interface InfrastructureProvisionerGateway {
    void deploy(InputStream file);
}
