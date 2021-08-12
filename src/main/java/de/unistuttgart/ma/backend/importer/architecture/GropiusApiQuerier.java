package de.unistuttgart.ma.backend.importer.architecture;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;

import org.apache.commons.lang3.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import de.unistuttgart.gropius.api.Mutation;
import de.unistuttgart.gropius.api.MutationQuery;
import de.unistuttgart.gropius.api.Query;
import de.unistuttgart.gropius.api.QueryQuery;
import de.unistuttgart.ma.backend.exceptions.IssueCreationFailedException;
import de.unistuttgart.ma.backend.exceptions.IssueLinkageFailedException;

/**
 * This class is responsible for querying the Gropius back end for Queries and Mutations. 
 * 
 * @author maumau
 *
 */
public class GropiusApiQuerier {
	private final URI apiUri;
	private final Gson gsonInstance;
	private final HttpClient httpClient;
	
	private final Logger logger = LoggerFactory.getLogger(getClass());

	public GropiusApiQuerier(final String apiUri) {
		this.gsonInstance = new GsonBuilder().registerTypeAdapter(Mutation.class, new MutationDeserializer())
				.registerTypeAdapter(Query.class, new QueryDeserializer()).create();
		this.apiUri = URI.create(apiUri);
		this.httpClient = HttpClient.newBuilder().build();
	}

	/**
	 * Queries the Gropius back end for the given query.
	 * 
	 * @param query the graphql query
	 * @return the query, that was queried for
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public Query queryQuery(QueryQuery query) throws IOException, InterruptedException {

		String queryString = "query=" + URLEncoder.encode(query.toString(), "UTF-8");
		URI requestUri = URI.create(apiUri.toString() + "?" + queryString);

		HttpRequest request = HttpRequest.newBuilder().GET().uri(requestUri).build();
		String body = request(request);

		return gsonInstance.fromJson(body, Query.class);
	}

	/**
	 * Sends the given http request to the Gropius back end and return the response
	 * body.
	 * 
	 * Removes additional clutter from the response body, such that the returned
	 * body may be deserialised without further ado.
	 * 
	 * 
	 * @param request the request to send
	 * @return string representation of the body, with all clutter removed
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public String request(final HttpRequest request) throws IOException, InterruptedException {
		HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());

		if (response.statusCode() != 200) {
			throw new IOException("Reuqest failed with " + response.statusCode() + ". Body:\n" + response.body());
		}

		String body = response.body();

		// Cut {"data": and }
		// This is probably a mismatch between the server and the apibindings generator
		body = body.substring(8, body.length() - 1);

		return body;
	}

	/**
	 * Queries the Gropius back end for the given mutation.
	 * 
	 * @param query the graphql mutation
	 * @return the mutation, as json
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public String queryMutation(MutationQuery query) throws IOException, InterruptedException {

		String escaped = StringEscapeUtils.escapeJson(query.toString());
		String full = "{\"query\":\"" + escaped + "\",\"variables\":null}";

		URI requestUri = URI.create(apiUri.toString());
		HttpRequest request = HttpRequest.newBuilder().POST(BodyPublishers.ofString(full)).uri(requestUri)
				.header("Content-Type", "application/json").build();
		
		return request(request);
	}
	
	/**
	 * Queries the Gropius back end with the given mutation to link two issues.
	 * 
	 * @param query the mutation
	 * @return the mutation
	 * @throws IssueLinkageFailedException 
	 */
	public Mutation queryLinkIssueMutation(MutationQuery query) throws IssueLinkageFailedException {

		String body;
		try {
			body = queryMutation(query);
		} catch (IOException | InterruptedException e) {
			throw new IssueLinkageFailedException("linking issue failed", e);
		}
		if (body.contains("errors")) {
			throw new IssueLinkageFailedException(body, null);
		}
	
		return gsonInstance.fromJson(body, Mutation.class);
	}
	
	/**
	 * Queries the Gropius back end with the given mutation to create a new issues.
	 * 
	 * @param query the mutation
	 * @return the mutation
	 * @throws IssueLinkageFailedException 
	 */
	public Mutation queryCreateIssueMutation(MutationQuery query) throws IssueCreationFailedException {

		String body;
		try {
			body = queryMutation(query);
		} catch (IOException | InterruptedException e) {
			throw new IssueCreationFailedException("creating issue failed", e);
		}
		if (body.contains("errors")) {
			throw new IssueCreationFailedException(body, null);
		}
	
		return gsonInstance.fromJson(body, Mutation.class);
	}
}
