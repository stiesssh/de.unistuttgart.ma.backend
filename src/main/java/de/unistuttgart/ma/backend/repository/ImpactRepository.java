package de.unistuttgart.ma.backend.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import de.unistuttgart.ma.impact.Impact;

/**
 * A {@code ImpactRepository} is a repository that manages impacts. Each impact
 * is serialized into an {@link ImpactItem} because the default serialization
 * apparently can not handle the generated impacts ({@link Impact})
 * 
 * TODO : figure out why the default serialization fails on the generated
 * impacts (p.s. this does not only concern the impacts, but all generated
 * classes).
 *
 */
public interface ImpactRepository extends MongoRepository<ImpactItem, String> {
}
