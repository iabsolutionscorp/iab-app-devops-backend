package com.iab.devops.application.gateway;

import com.iab.devops.domain.entity.IACFile;

import java.io.InputStream;
import java.net.URL;

public interface FilePersistenceGateway {
    void upload(InputStream file, String location, String contentType);
    InputStream download(String location);
    URL getUrl(String location);
    void delete(String location);
}
