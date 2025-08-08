package com.iab.devops.domain.entity;

import com.iab.devops.domain.enums.IACType;

import static java.lang.String.format;

public class IACFile {
    private Long id;
    private String name;
    private IACType type;
    private String location;

    public IACFile(Long id, String name, IACType type) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.location = String.format("%s/%d%s", type.name().toLowerCase(), id, type.getArchiveSuffix());
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public IACType getType() {
        return type;
    }

    public void setType(IACType type) {
        this.type = type;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}
