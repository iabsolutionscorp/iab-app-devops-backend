package com.iab.devops.infra.controller;


import com.iab.devops.application.GenerateIACCodeUseCase;
import com.iab.devops.infra.controller.request.IACRequest;
import com.iab.devops.mapper.IACCodeMapper;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.ResponseEntity.ok;

@RestController
@RequestMapping("/v1/terraform")
public class IACController {

    private final GenerateIACCodeUseCase generateIACCodeUseCase;
    private final IACCodeMapper iacCodeMapper;

    public IACController(GenerateIACCodeUseCase generateIACCodeUseCase, IACCodeMapper iacCodeMapper) {
        this.generateIACCodeUseCase = generateIACCodeUseCase;
        this.iacCodeMapper = iacCodeMapper;
    }

    @PostMapping
    public ResponseEntity<InputStreamResource> generateCode(@RequestBody IACRequest request) {
        String code = generateIACCodeUseCase.execute(request.prompt());
        return ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=infra.tf")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(iacCodeMapper.toTerraformFile(code));
    }


}
