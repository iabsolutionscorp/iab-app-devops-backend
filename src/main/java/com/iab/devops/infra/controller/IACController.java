package com.iab.devops.infra.controller;


import com.iab.devops.application.dto.IACFileDto;
import com.iab.devops.application.usecase.GenerateIACCodeUseCase;
import com.iab.devops.application.usecase.iacfile.CreateIACFileUseCase;
import com.iab.devops.application.usecase.iacfile.DeployIACFileUseCase;
import com.iab.devops.application.usecase.iacfile.GetIACFileByIdUseCase;
import com.iab.devops.application.usecase.iacfile.UpdateIACFileUseCase;
import com.iab.devops.domain.entity.IACFile;
import com.iab.devops.domain.enums.IACType;
import com.iab.devops.infra.controller.request.IACRequest;
import com.iab.devops.infra.controller.response.IACFileResponse;
import com.iab.devops.mapper.IACCodeMapper;
import com.iab.devops.mapper.IACFileMapper;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.net.URL;

import static com.iab.devops.infra.controller.ControllerUtils.buildLocationUri;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;
import static org.springframework.http.ResponseEntity.*;

@RestController
@RequestMapping("/v1/iac")
public class IACController {

    private final GenerateIACCodeUseCase generateIACCodeUseCase;
    private final CreateIACFileUseCase createIACFileUseCase;
    private final GetIACFileByIdUseCase getIACFileByIdUseCase;
    private final UpdateIACFileUseCase updateIACFileUseCase;
    private final DeployIACFileUseCase deployIACFileUseCase;
    private final IACCodeMapper iacCodeMapper;
    private final IACFileMapper iacFileMapper;

    public IACController(GenerateIACCodeUseCase generateIACCodeUseCase, CreateIACFileUseCase createIACFileUseCase, GetIACFileByIdUseCase getIACFileByIdUseCase, UpdateIACFileUseCase updateIACFileUseCase, DeployIACFileUseCase deployIACFileUseCase, IACCodeMapper iacCodeMapper, IACFileMapper iacFileMapper) {
        this.generateIACCodeUseCase = generateIACCodeUseCase;
        this.createIACFileUseCase = createIACFileUseCase;
        this.getIACFileByIdUseCase = getIACFileByIdUseCase;
        this.updateIACFileUseCase = updateIACFileUseCase;
        this.deployIACFileUseCase = deployIACFileUseCase;
        this.iacCodeMapper = iacCodeMapper;
        this.iacFileMapper = iacFileMapper;
    }


    @PostMapping("/generate")
    public ResponseEntity<InputStreamResource> generateCode(@RequestBody IACRequest request) {
        String code = generateIACCodeUseCase.execute(request.type(), request.prompt());
        return ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=infra.tf")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(iacCodeMapper.toTerraformFile(code));
    }

    @PostMapping("/{id}/deploy")
    public ResponseEntity<Void> deploy(@PathVariable Long id) {
        deployIACFileUseCase.execute(id);
        return accepted().build();
    }

    @PostMapping(consumes = MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> create(@RequestParam String fileName,
                                       @RequestParam IACType type,
                                       @RequestParam MultipartFile file) {

        IACFile iacFile = iacFileMapper.toDomain(fileName, type);
        InputStream inputStream = iacFileMapper.toInputStream(file);
        Long attachmentId = createIACFileUseCase.execute(iacFile, inputStream);
        return created(buildLocationUri(attachmentId)).build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<IACFileDto> getById(@PathVariable Long id) {
        IACFileDto dto = getIACFileByIdUseCase.execute(id);
        return ok(dto);
    }

    @PutMapping(path = "/{id}", consumes = MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> update(@PathVariable Long id, @RequestParam MultipartFile file) {
        InputStream inputStream = iacFileMapper.toInputStream(file);
        updateIACFileUseCase.execute(id, inputStream);
        return noContent().build();
    }


}
