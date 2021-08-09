package de.unistuttgart.ma.backend.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * 
 * assumption : each system / notification gets only ever written *once* and by
 * *one* service alone, thus a noSQL db should be enough
 * 
 * TODO : there is still some problem with uniqueness of system ids... 
 * 
 * @author maumau
 *
 */
public interface ImpactRepository extends MongoRepository<ImpactItem, String> {
}
