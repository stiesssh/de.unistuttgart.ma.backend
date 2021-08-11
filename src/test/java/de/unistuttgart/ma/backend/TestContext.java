package de.unistuttgart.ma.backend;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import de.unistuttgart.ma.backend.repository.ImpactRepository;
import de.unistuttgart.ma.backend.repository.NotificationRepository;
import de.unistuttgart.ma.backend.repository.SystemRepository;

@Configuration
//@EnableAutoConfiguration(exclude = {...})
@EnableMongoRepositories(basePackageClasses = {SystemRepository.class, ImpactRepository.class})
@Profile("test")
public class TestContext {

}
