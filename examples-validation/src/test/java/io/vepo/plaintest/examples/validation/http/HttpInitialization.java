package io.vepo.plaintest.examples.validation.http;

import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

import org.mockserver.client.MockServerClient;
import org.mockserver.client.initialize.PluginExpectationInitializer;

public class HttpInitialization implements PluginExpectationInitializer {

	@Override
	public void initializeExpectations(MockServerClient mockServerClient) {
		mockServerClient.when(request().withPath("/").withMethod("POST"))
				.respond(response().withStatusCode(200).withBody("OK"));
	}

}
