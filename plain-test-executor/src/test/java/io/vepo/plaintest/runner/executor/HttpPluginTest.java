package io.vepo.plaintest.runner.executor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.mockserver.verify.VerificationTimes.atLeast;

import java.net.InetSocketAddress;
import java.util.concurrent.Callable;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockserver.client.MockServerClient;
import org.mockserver.junit.jupiter.MockServerExtension;
import org.mockserver.model.HttpRequest;

import io.vepo.plaintest.Suite;
import io.vepo.plaintest.SuiteFactory;

@ExtendWith(MockServerExtension.class)
public class HttpPluginTest extends AbstractTest {
	private static final String HTTP_TEST_SUITE = "Suite HttpGet {\n" + //
			"\n" + //
			"    HTTP GetRequest {\n" + //
			"        url: \"http://${host}:${port}/defaultGet\"\n" + //
			"        method: \"GET\"\n" + //
			"    }\n" + //
			"}";
	private MockServerClient client;

	@BeforeEach
	public void setup(MockServerClient client) {
		this.client = client;
	}

	@AfterEach
	public void tearDown() {
		this.client.close();
	}

	@Test
	public void voidTest() {
		InetSocketAddress remoteAddress = client.remoteAddress();
		validateHttp("/defaultGet", "GET", 200, "{\"response\":\"OK\"}", 1, () -> {

			Suite suite = SuiteFactory.parseSuite(HTTP_TEST_SUITE.replace("${host}", remoteAddress.getHostName())
					.replace("${port}", Integer.toString(remoteAddress.getPort())));
			PlainTestExecutor executor = new PlainTestExecutor();

			assertThat(executor.execute(suite)).satisfies(result -> assertTrue(result.isSuccess()))
					.satisfies(result -> assertThat(find(result, "HttpGet")).isPresent().get()
							.satisfies(r -> assertTrue(r.isSuccess())))
					.satisfies(result -> assertThat(find(result, "GetRequest")).isPresent().get()
							.satisfies(r -> assertTrue(r.isSuccess())));
		});

	}

	private void validateHttp(String path, String method, int statusCode, String body, int times, Runnable code) {
		HttpRequest serverRequest = request().withMethod(method).withPath(path);

		client.when(serverRequest).respond(response().withStatusCode(statusCode).withBody(body));

		code.run();

		client.verify(serverRequest, atLeast(times));
	}
}
