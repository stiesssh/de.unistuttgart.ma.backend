package de.unistuttgart.ma.backend.importer.slo;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

/**
 * A {@code SolomonApiQuerier} queries the Solomon API at the given
 * {@code apiUri} for Slo rules.
 * 
 * @author maumau
 *
 */
public class SolomonApiQuerier {
	private URI apiUri;
	private Gson gsonInstance;
	private HttpClient httpClient;

	/**
	 * Create a new querier to query the Solomon tool at the given uri.
	 * 
	 * @param apiUri uri of the Solomon tool
	 */
	public SolomonApiQuerier(final String apiUri) {
		this.gsonInstance = new GsonBuilder().create();
		this.apiUri = URI.create(apiUri);
		this.httpClient = HttpClient.newBuilder().build();
	}

	/**
	 * Query the Solomon Tool and return the SLO rules in the response.
	 * 
	 * Query for all rules in the given deployment environment. It is up to the
	 * caller, to figure out which rules they actually need.
	 * 
	 * @param queryParamEnvironment the environment to query for.
	 * @return the SLO rules
	 * @throws IOException          if sending the request failed, or the response
	 *                              has errors.
	 * @throws InterruptedException if sending the request failed
	 */
	public Set<FlatSolomonRule> querySolomon(String queryParamEnvironment) throws IOException, InterruptedException {

		URI requestUri = URI.create(apiUri.toString() + queryParamEnvironment);

		HttpRequest request = HttpRequest.newBuilder().GET().uri(requestUri).build();
		HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());

		if (response.statusCode() != 200) {
			throw new IOException("Reuqest failed with " + response.statusCode() + ". Body:\n" + response.body());
		}

		String body = response.body();

		Set<FlatSolomonRule> rules = gsonInstance.fromJson(body, new TypeToken<Set<FlatSolomonRule>>() {
		}.getType());

		return rules;
	}
}
