package com.iab.devops.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.springframework.core.io.InputStreamResource;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

@Mapper(componentModel = SPRING,
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface IACCodeMapper {

    default InputStreamResource toTerraformFile(String iacCode) {
        try {
            File tempFile = File.createTempFile("infra", ".tf");
            try (FileWriter fw = new FileWriter(tempFile)) {
                fw.write(iacCode);
            }
            tempFile.deleteOnExit();
            return new InputStreamResource(new FileInputStream(tempFile));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

}
