package com.iab.devops.infra.controller;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@Component
public class ControllerUtils {

    private ControllerUtils() {
        // Construtor privado para evitar instância
    }

    public static URI buildLocationUri(Long id) {
        return ServletUriComponentsBuilder
                .fromCurrentRequestUri()
                .path("/{id}")
                .buildAndExpand(id)
                .toUri();
    }
}
