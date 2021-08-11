package de.unistuttgart.ma.backend;

import java.io.IOException;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import de.unistuttgart.gropius.ComponentInterface;
import de.unistuttgart.gropius.slo.SloRule;
import de.unistuttgart.ma.backend.repository.ImpactRepositoryProxy;
import de.unistuttgart.ma.backend.repository.SystemRepositoryProxy;
import de.unistuttgart.ma.impact.Impact;
import de.unistuttgart.ma.impact.ImpactFactory;
import de.unistuttgart.ma.impact.Violation;

/**
 * TODO DELETE 
 * @author maumau
 *
 */
@Component
@Profile("test")
public class TestSetup {

	@Autowired
	SystemRepositoryProxy systemRepoProxy;
	@Autowired
	ImpactRepositoryProxy impactRepoProxy;

//	@PostConstruct
//	public void init(String systemId) throws IOException {
//
//		//systemRepoProxy.testInit();
//
//		de.unistuttgart.ma.saga.System system = systemRepoProxy.findById(systemId);
//
//		ComponentInterface creditInstituteFace = system.getComponentInterfaceById("5e8cf780c585a029");
//		ComponentInterface paymentFace = system.getComponentInterfaceById("5e8cf760d345a028");
//		SloRule rule = system.getSloForNode(creditInstituteFace).iterator().next();
//
//		{
//			Violation violation = ImpactFactory.eINSTANCE.createViolation();
//			violation.setViolatedRule(rule);
//			violation.setLocation(rule.getGropiusComponentInterface());
//
//			Impact impact = ImpactFactory.eINSTANCE.createImpact();
//			impact.setLocation(paymentFace);
//			impact.setCause(violation);
//
//			impactRepoProxy.save(impact, systemId);
//		}
//		{
//			Violation violation = ImpactFactory.eINSTANCE.createViolation();
//			violation.setViolatedRule(rule);
//			violation.setLocation(rule.getGropiusComponentInterface());
//
//			Impact impact = ImpactFactory.eINSTANCE.createImpact();
//			impact.setLocation(paymentFace);
//			impact.setCause(violation);
//
//			impactRepoProxy.save(impact, systemId);
//		}
//	}
}
