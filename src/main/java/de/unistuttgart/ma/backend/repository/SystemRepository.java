package de.unistuttgart.ma.backend.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import de.unistuttgart.ma.saga.System;

public interface SystemRepository extends MongoRepository<SystemItem, String>{



	//List<System> findByArchitectureId(String id);


}
