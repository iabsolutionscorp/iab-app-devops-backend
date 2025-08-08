package com.iab.devops.infra.persistence.repository;

import com.iab.devops.infra.persistence.entity.IACFileEntity;
import org.springframework.data.repository.CrudRepository;

public interface IACFileRepository extends CrudRepository<IACFileEntity, Long> {
}
