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

public class SolomonApiQuerier {
	private URI apiUri;
	private Gson gsonInstance;
	private HttpClient httpClient;
		
	public SolomonApiQuerier(final String apiUri) {
		this.gsonInstance = new GsonBuilder().create();
		this.apiUri = URI.create(apiUri);
		this.httpClient = HttpClient.newBuilder().build();
	}

	public Set<SloFlatRule> query(String queryString ) throws IOException, InterruptedException {		
		
		URI requestUri = URI.create(apiUri.toString() + queryString);
		
		HttpRequest request = HttpRequest.newBuilder().GET().uri(requestUri).build();
		HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());
		
		if(response.statusCode() != 200) {
			throw new IOException("Reuqest failed with " + response.statusCode() + ". Body:\n" + response.body());
		}
		
		String body = response.body();
		
		Set<SloFlatRule> rules = gsonInstance.fromJson(body, new TypeToken<Set<SloFlatRule>>(){}.getType());
		
		return rules;
	}
}
