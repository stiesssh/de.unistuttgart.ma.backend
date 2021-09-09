package de.unistuttgart.ma.backend.app;

import java.util.NoSuchElementException;

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

import de.unistuttgart.ma.backend.exceptions.ModelCreationFailedException;
import de.unistuttgart.ma.backend.rest.ImportRequest;

/**
 * Controller with end points to create, get and update the system models.
 */
@RestController
public class ModelController {

	private final ModelService modelService;

	public ModelController(@Autowired ModelService modelService) {
		assert (modelService != null);
		this.modelService = modelService;
	}

	/**
	 * Create a new model.
	 * 
	 * Imports architecture, slo rules and business process as specified in the
	 * import request.
	 * 
	 * @param request Request to create a new model
	 * @return xml representation of the newly created model
	 * @throws ModelCreationFailedException if the model creation failed
	 */
	@PostMapping("/api/model")
	public String createModel(@RequestBody ImportRequest request) throws ModelCreationFailedException {
		return modelService.createModel(request);
	}

	/**
	 * Update the model with the given id.
	 * 
	 * @param xml      the updated version of the model
	 * @param systemId Id of the model
	 */
	@PostMapping("/api/model/{id}")
	public void updateModel(@RequestBody String xml, @PathVariable String id) {
		modelService.updateModel(xml, id);
	}

	/**
	 * Get the model with the given id
	 * 
	 * @param systemId Id of the model
	 * @return xml representation of the newly created model
	 * @throws ModelCreationFailedException
	 */
	@GetMapping("/api/model/{id}")
	public String getModel(@PathVariable String id) throws ModelCreationFailedException {
		return modelService.getModel(id);
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
	
	@ExceptionHandler(NoSuchElementException.class)
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public ResponseEntity<String> noSuchElementException(NoSuchElementException exception) {
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(exception.getMessage());
	}
}
