package de.unistuttgart.ma.backend.importer.architecture;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import de.unistuttgart.gropius.api.Query;
import de.unistuttgart.gropius.api.QueryQuery;

public class GropiusApiQuerier {
	private URI apiUri;
	private Gson gsonInstance;
	private HttpClient httpClient;
	
	public GropiusApiQuerier(final String apiUri) {
		this.gsonInstance = new GsonBuilder().registerTypeAdapter(Query.class, new QueryDeserializer()).create();
		this.apiUri = URI.create(apiUri);
		this.httpClient = HttpClient.newBuilder().build();
	}

	public Query query(QueryQuery query) throws IOException, InterruptedException {		
		
		String queryString = "query=" + URLEncoder.encode(query.toString(), "UTF-8");
		URI requestUri = URI.create(apiUri.toString() + "?" + queryString);
		
		HttpRequest request = HttpRequest.newBuilder().GET().uri(requestUri).build();
		HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());
		
		if(response.statusCode() != 200) {
			throw new IOException("Reuqest failed with " + response.statusCode() + ". Body:\n" + response.body());
		}
		
		String body = response.body();

		// Cut {"data": and }
		// This is probably a mismatch between the server and the apibindings generator
		body = body.substring(8, body.length() - 1);
		
		return gsonInstance.fromJson(body, Query.class);
	}
}
