package io.vepo.plaintest.runner.executor;

import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.mockserver.verify.VerificationTimes.atLeast;

import java.net.InetSocketAddress;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockserver.client.MockServerClient;
import org.mockserver.junit.jupiter.MockServerExtension;
import org.mockserver.model.Delay;
import org.mockserver.model.HttpRequest;

@ExtendWith(MockServerExtension.class)
public abstract class AbstractHttpTest extends AbstractTest {

	private MockServerClient client;

	@BeforeEach
	public void setup(MockServerClient client) {
		this.client = client;
	}

	@AfterEach
	public void tearDown() {
		client.reset();
	}

	protected InetSocketAddress remoteAddress() {
		return client.remoteAddress();
	}

	protected void validateHttp(String path, String method, int statusCode, String responseBody, int times, Delay delay,
			Runnable code) {
		HttpRequest serverRequest = request().withMethod(method).withPath(path);

		client.when(serverRequest)
				.respond(response().withStatusCode(statusCode).withBody(responseBody).withDelay(delay));

		code.run();

		client.verify(serverRequest, atLeast(times));
	}

	protected void validateHttp(String path, String method, int statusCode, String responseBody, int times,
			Runnable code) {
		HttpRequest serverRequest = request().withMethod(method).withPath(path);

		client.when(serverRequest).respond(response().withStatusCode(statusCode).withBody(responseBody));

		code.run();

		client.verify(serverRequest, atLeast(times));
	}

	protected void validateHttp(String path, String method, int statusCode, String requestBody, String responseBody,
			int times, Runnable code) {
		HttpRequest serverRequest = request().withMethod(method).withPath(path).withBody(requestBody);

		client.when(serverRequest).respond(response().withStatusCode(statusCode).withBody(responseBody));

		code.run();

		client.verify(serverRequest, atLeast(times));
	}

}
