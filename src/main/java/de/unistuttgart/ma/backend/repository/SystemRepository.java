package de.unistuttgart.ma.backend.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * A {@code SystemRepository} is a repository that manages the system models. Each system model
 * is serialized into an {@link SystemItem} because the default serialization
 * apparently can not handle the system models (c.f. {@link ImpactRepository}})
 *
 */
public interface SystemRepository extends MongoRepository<SystemItem, String> {

}
