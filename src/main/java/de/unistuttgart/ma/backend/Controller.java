package de.unistuttgart.ma.backend;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import de.unistuttgart.ma.backend.exceptions.MissingSystemModelException;
import de.unistuttgart.ma.backend.exceptions.ModelCreationFailedException;
import de.unistuttgart.ma.backend.importer.SagaImporterService;
import de.unistuttgart.ma.backend.rest.ImportRequest;

/**
 * This controller provides the endpoints to be called by the sirius front end.
 *  
 * @author maumau
 *
 */
@RestController
public class Controller {

	
	private final NotificationRetrievalService service;
	private final SagaImporterService importService;	

	public Controller(@Autowired NotificationRetrievalService service, @Autowired SagaImporterService importService) {
		this.service = service;
		this.importService = importService;
	}

	/**
	 * Endpoint to update a saved model.
	 * 
	 * @param xml updated model as XML
	 */
	@PostMapping("/api/model/{systemId}")
	public void updateModel(@RequestBody String xml, @PathVariable String systemId) {
		importService.updateModel(xml, systemId);
	}
	
	@PostMapping("/api/model")
	public String createModel(@RequestBody ImportRequest request) throws ModelCreationFailedException {
		try {
			return importService.createModel(request);
		} catch (IOException e) {
			e.printStackTrace();
			throw new ModelCreationFailedException("model creation failed : " + e.getMessage(), e);
		}
	}

	
	@GetMapping("/")
	public String index() {
		return "Greetings :)";
	}

	@ExceptionHandler(ModelCreationFailedException.class)
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public ResponseEntity<String> modelCreationFailedException(ModelCreationFailedException exception) {
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(exception.getMessage());
	}
	
	@ExceptionHandler(MissingSystemModelException.class)
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public ResponseEntity<String> missingSystemModelException(MissingSystemModelException exception) {
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(exception.getMessage());
	}

}
