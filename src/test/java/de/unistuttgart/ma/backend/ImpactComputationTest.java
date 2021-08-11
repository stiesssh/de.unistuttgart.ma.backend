package de.unistuttgart.ma.backend;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.util.Set;

import org.eclipse.bpmn2.FlowElement;
import org.eclipse.bpmn2.Task;
import org.eclipse.emf.ecore.EObject;
import org.junit.jupiter.api.Test;
import org.mockito.exceptions.verification.MoreThanAllowedActualInvocations;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import de.unistuttgart.gropius.ComponentInterface;
import de.unistuttgart.gropius.slo.SloRule;
import de.unistuttgart.ma.impact.Impact;
import de.unistuttgart.ma.impact.ImpactFactory;
import de.unistuttgart.ma.impact.Notification;
import de.unistuttgart.ma.impact.Violation;
import de.unistuttgart.ma.saga.SagaStep;

@ContextConfiguration(classes = TestContext.class)
@DataMongoTest
@ActiveProfiles("test")
public class ImpactComputationTest extends TestWithRepo{

	@Test
	public void computeNotificationTest() throws IOException {
		loadSystem();
		
		ComponentInterface creditInstituteFace = system.getComponentInterfaceById("5e8cf780c585a029"); // ci-face
		SloRule rule = system.getSloForNode(creditInstituteFace).iterator().next();

		
		Violation violation = ImpactFactory.eINSTANCE.createViolation();
		violation.setViolatedRule(rule);
		
		Set<Notification> actuals = computationService.calculateImpacts(violation);
		
		//Set<Impact> actuals = notificationRepoProxy.findBySystemId(systemId);
		assertNotNull(actuals);
		assertEquals(2, actuals.size());
		
		for (Notification actual : actuals) {
			assertNotNull(actual);
			assertEquals(rule, actual.getRootCause().getViolatedRule());
			Impact top = actual.getTopLevelImpact();
			assertNotNull(top);
			assertTrue(top.getLocation() instanceof Task);
			if (((Task)top.getLocation()).getId().equals("Task_4")) {
				assertPaymentImpact(actual);
			} else if (((Task)top.getLocation()).getId().equals("Task_5")) {
				assertInventoryImpact(actual);
			} else {
				fail();
			}
		}
	}
	
	// 	pay -> step -> task_pay  // 5e8cf760d345a028 // paymentStep // Task_4
	private void assertPaymentImpact(Notification note) {
		Impact current = note.getTopLevelImpact();
		assertImpact(current, "Task_4", false);
		current = current.getCause();
		
		assertImpact(current, "paymentStep", false);
		current = current.getCause();

		assertImpact(current, "5e8cf760d345a028", false);
		current = current.getCause();
		
		assertImpact(current, "5e8cf780c585a029", true);
	}
	
	
	// 	other -> another -> inventory -> task_inv          // 5e94539417ca7005 // 5e94553f2a4a7006 // 5e8cf74541c5a026 // inventoryStep // Task_5
	private void assertInventoryImpact(Notification note) {
		Impact current = note.getTopLevelImpact();
		assertImpact(current, "Task_5", false);
		current = current.getCause();
		
		assertImpact(current, "inventoryStep", false);
		current = current.getCause();

		assertImpact(current, "5e8cf74541c5a026", false);
		current = current.getCause();
		
		assertImpact(current, "5e94553f2a4a7006", false);
		current = current.getCause();
		
		assertImpact(current, "5e94539417ca7005", false);
		current = current.getCause();
		
		assertImpact(current, "5e8cf780c585a029", true);
	}
	
	private void assertImpact(Impact impact, String id, boolean last) {
		assertNotNull(impact);
		EObject location = impact.getLocation();
		if (location instanceof ComponentInterface) {
			assertEquals(id, ((ComponentInterface) location).getId());
		}
		if (location instanceof FlowElement) {
			assertEquals(id, ((FlowElement) location).getId());
		}
		if (location instanceof SagaStep) {
			assertEquals(id, ((SagaStep) location).getId());
		}
		if (!last) {
			assertNotNull(impact.getCause());
		}
	}
}
