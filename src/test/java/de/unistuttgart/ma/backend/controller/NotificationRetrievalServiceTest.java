package de.unistuttgart.ma.backend.controller;
//package de.unistuttgart.ma.backend;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertNotNull;
//import static org.junit.jupiter.api.Assertions.fail;
//
//import java.io.ByteArrayInputStream;
//import java.io.IOException;
//import java.io.InputStream;
//
//import org.eclipse.emf.common.util.URI;
//import org.eclipse.emf.ecore.EObject;
//import org.eclipse.emf.ecore.resource.Resource;
//import org.junit.jupiter.api.Test;
//
//import de.unistuttgart.ma.backend.exceptions.MissingSystemModelException;
//import de.unistuttgart.ma.impact.Impact;
//import de.unistuttgart.ma.impact.Notification;
//
///**
// * 
// * TODO : DELETE ?? do i even need to save notifications my self now??
// * 
// * @author maumau
// *
// */
//public class NotificationRetrievalServiceTest extends TestWithRepo {
//	
//	@Test
//	public void retrieveSingelImpactTest() throws MissingSystemModelException, IOException {
//		loadSystem();
//		loadImpact();
//		
//		String xml = retrievalService.retrieveNotification(systemId);
//		Notification actual = deserialize(xml);
//		
//		assertNotNull(actual);
////		assertEquals(1,actual.getTopLevelImpacts().size());
////		assertEquals(2,actual.getImpacts().size());
//		
////		for (Impact impact : actual.getImpacts()) {
////			assertNotNull(impact);
////		}
//	}
//	
//	@Test
//	public void retrieveMultipleImpactsTest() throws MissingSystemModelException, IOException {
//		loadSystem();
//		loadImpact();
//		loadImpact();
//		
//		String xml = retrievalService.retrieveNotification(systemId);
//		Notification actual = deserialize(xml);
//		
//		assertNotNull(actual);
////		assertEquals(2,actual.getTopLevelImpacts().size());
////		assertEquals(4,actual.getImpacts().size());
////		
////		for (Impact impact : actual.getImpacts()) {
////			assertNotNull(impact);
////		}
//	}
//	
//	public Notification deserialize(String xml) throws IOException {
//		Resource res = set.createResource(URI.createPlatformResourceURI("test.impact", false));
//		
//		InputStream targetStream = new ByteArrayInputStream(xml.getBytes());
//		res.load(targetStream, null);
//		
//		for (EObject eObject : res.getContents()) {
//			if (eObject instanceof Notification) {
//				return ((Notification) eObject);
//			}
//		}
//		fail("deserialization failed.");
//		return null;
//	}
//}
