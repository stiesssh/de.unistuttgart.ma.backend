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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import de.unistuttgart.gropius.api.Mutation;
import de.unistuttgart.gropius.api.MutationQuery;
import de.unistuttgart.gropius.api.Query;
import de.unistuttgart.gropius.api.QueryQuery;

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
	 * @return the mutation, that was queried for
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public Mutation queryMutation(MutationQuery query) throws IOException, InterruptedException {

		String escaped = StringEscapeUtils.escapeJson(query.toString());
		String full = "{\"query\":\"" + escaped + "\",\"variables\":null}";

		URI requestUri = URI.create(apiUri.toString());
		HttpRequest request = HttpRequest.newBuilder().POST(BodyPublishers.ofString(full)).uri(requestUri)
				.header("Content-Type", "application/json").build();

		String body = request(request);

		return gsonInstance.fromJson(body, Mutation.class);
	}
}
