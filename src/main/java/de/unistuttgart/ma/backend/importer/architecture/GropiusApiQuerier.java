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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import de.unistuttgart.gropius.api.Mutation;
import de.unistuttgart.gropius.api.MutationQuery;
import de.unistuttgart.gropius.api.Query;
import de.unistuttgart.gropius.api.QueryQuery;
import de.unistuttgart.ma.backend.exceptions.IssueCreationFailedException;
import de.unistuttgart.ma.backend.exceptions.IssueLinkageFailedException;

/**
 * A{@code GropiusApiQuerier} queries the Gropius back end for a {@link Query}
 * or a {@link Mutation}.
 * 
 * @author maumau
 *
 */
public class GropiusApiQuerier {

	private final URI apiUri;
	private final Gson gsonInstance;
	private final HttpClient httpClient;

	private final Logger logger = LoggerFactory.getLogger(getClass());

	/**
	 * Create new querier to query the gropius backend at the specified uri.
	 * 
	 * @param apiUri uri of the gropius backend
	 */
	public GropiusApiQuerier(final String apiUri) {

		this.apiUri = URI.create(apiUri);
		this.httpClient = HttpClient.newBuilder().build();

		this.gsonInstance = new GsonBuilder().registerTypeAdapter(Mutation.class, new MutationDeserializer())
				.registerTypeAdapter(Query.class, new QueryDeserializer()).create();

	}

	/**
	 * Queries the Gropius back end for the given query.
	 * 
	 * @param query the graphql query for the query
	 * @return the query, that was queried for
	 * @throws IOException          if the query failed
	 * @throws InterruptedException if the query failed
	 */
	public Query queryQuery(QueryQuery query) throws IOException, InterruptedException {

		String queryString = "query=" + URLEncoder.encode(query.toString(), "UTF-8");
		URI requestUri = URI.create(apiUri.toString() + "?" + queryString);
		HttpRequest request = HttpRequest.newBuilder().GET().uri(requestUri).build();

		logger.debug(String.format("Send %s query request to %s.", request.method(), request.uri().toString()));
		String body = request(request);

		return gsonInstance.fromJson(body, Query.class);
	}

	/**
	 * Queries the Gropius back end for the given mutation.
	 * 
	 * @param query the graphql query for the mutation
	 * @return the mutation, that was queried for as json
	 * @throws IOException          if the query failed
	 * @throws InterruptedException if the query failed
	 */
	public String queryMutation(MutationQuery query) throws IOException, InterruptedException {

		String escaped = StringEscapeUtils.escapeJson(query.toString());
		String full = "{\"query\":\"" + escaped + "\",\"variables\":null}";

		URI requestUri = URI.create(apiUri.toString());
		HttpRequest request = HttpRequest.newBuilder().POST(BodyPublishers.ofString(full)).uri(requestUri)
				.header("Content-Type", "application/json").build();

		logger.debug(String.format("Send %s mutation request to %s.", request.method(), request.uri().toString()));
		logger.debug(String.format("Body : %s ", full));

		return request(request);
	}

	/**
	 * Queries the Gropius back end with the given mutation to link two issues.
	 * 
	 * @param query the query for the mutation to link issue
	 * @return the mutation that was queried for
	 * @throws IssueLinkageFailedException if the linkage failed
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
	 * @param query the query for the mutation to create a new issue
	 * @return the mutation that was queried for
	 * @throws IssueCreationFailedException if the creation failed
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

	/**
	 * Send a http request to the Gropius back end and return the response's body.
	 * 
	 * Removes additional clutter from the response body, such that the returned
	 * body may be deserialised without further ado.
	 * 
	 * 
	 * @param request the http request to send
	 * @return string representation of the body, with all clutter removed
	 * @throws IOException          if sending the request failed, or the response
	 *                              has errors.
	 * @throws InterruptedException if sending the request failed
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

		logger.debug(String.format("Response: %s ", body));

		return body;
	}
}
