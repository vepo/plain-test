package io.vepo.plaintest.runner.executor;

import static io.vepo.plaintest.runner.executor.FailReason.RUNTIME_EXCEPTION;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.mockserver.verify.VerificationTimes.atLeast;

import java.net.InetSocketAddress;

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

	private MockServerClient client;

	@BeforeEach
	public void setup(MockServerClient client) {
		this.client = client;
	}

	@AfterEach
	public void tearDown() {
		this.client.reset();
	}

	private static final String HTTP_TEST_SUITE = "Suite HttpGet {\n" + //
			"\n" + //
			"    HTTP GetRequest {\n" + //
			"        url: \"http://${host}:${port}/defaultGet\"\n" + //
			"        method: \"GET\"\n" + //
			"    }\n" + //
			"}";

	@Test
	public void getTest() {
		InetSocketAddress remoteAddress = this.client.remoteAddress();
		this.validateHttp("/defaultGet", "GET", 200, "{\"response\":\"OK\"}", 1, () -> {

			Suite suite = SuiteFactory.parseSuite(HTTP_TEST_SUITE.replace("${host}", remoteAddress.getHostName())
					.replace("${port}", Integer.toString(remoteAddress.getPort())));
			PlainTestExecutor executor = new PlainTestExecutor();

			assertThat(executor.execute(suite)).satisfies(result -> assertTrue(result.isSuccess()))
					.satisfies(result -> assertThat(this.find(result, "HttpGet")).isPresent().get()
							.satisfies(r -> assertTrue(r.isSuccess())))
					.satisfies(result -> assertThat(this.find(result, "GetRequest")).isPresent().get()
							.satisfies(r -> assertTrue(r.isSuccess())));
		});

	}

	private static final String HTTP_INVALID_METHOD_TEST_SUITE = "Suite HttpGet {\n" + //
			"\n" + //
			"    HTTP GetRequest {\n" + //
			"        url: \"http://${host}:${port}/defaultGet\"\n" + //
			"        method: \"INVALID_METHOD\"\n" + //
			"    }\n" + //
			"}";

	@Test
	public void invalidMethodTest() {
		InetSocketAddress remoteAddress = this.client.remoteAddress();
		this.validateHttp("/defaultGet", "GET", 200, "{\"response\":\"OK\"}", 0, () -> {

			Suite suite = SuiteFactory
					.parseSuite(HTTP_INVALID_METHOD_TEST_SUITE.replace("${host}", remoteAddress.getHostName())
							.replace("${port}", Integer.toString(remoteAddress.getPort())));
			PlainTestExecutor executor = new PlainTestExecutor();

			assertThat(executor.execute(suite)).satisfies(result -> assertFalse(result.isSuccess()))
					.satisfies(result -> assertThat(this.find(result, "HttpGet")).isPresent().get()
							.satisfies(r -> assertFalse(r.isSuccess())))
					.satisfies(result -> assertThat(this.find(result, "GetRequest")).isPresent().get().satisfies(r -> {
						assertFalse(r.isSuccess());
						assertEquals(r.getFails(),
								asList(new Fail(RUNTIME_EXCEPTION, "Invalid Method: INVALID_METHOD")));
					}));
		});

	}

	private static final String HTTP_INVALID_URL_TEST_SUITE = "Suite HttpGet {\n" + //
			"\n" + //
			"    HTTP GetRequest {\n" + //
			"        url: \"${host}:${port}/defaultGet\"\n" + //
			"        method: \"GET\"\n" + //
			"    }\n" + //
			"}";

	@Test
	public void invalidUrlTest() {
		InetSocketAddress remoteAddress = this.client.remoteAddress();
		this.validateHttp("/defaultGet", "GET", 200, "{\"response\":\"OK\"}", 0, () -> {
			String port = Integer.toString(remoteAddress.getPort());
			Suite suite = SuiteFactory.parseSuite(
					HTTP_INVALID_URL_TEST_SUITE.replace("${host}", "THIS|IS|AN|INVALID|URL").replace("${port}", port));
			PlainTestExecutor executor = new PlainTestExecutor();

			assertThat(executor.execute(suite)).satisfies(result -> assertFalse(result.isSuccess()))
					.satisfies(result -> assertThat(this.find(result, "HttpGet")).isPresent().get()
							.satisfies(r -> assertFalse(r.isSuccess())))
					.satisfies(result -> assertThat(this.find(result, "GetRequest")).isPresent().get().satisfies(r -> {
						assertFalse(r.isSuccess());

						assertEquals(
								asList(new Fail(RUNTIME_EXCEPTION,
										String.format("Invalid URL: THIS|IS|AN|INVALID|URL:%s/defaultGet", port))),
								r.getFails());
					}));
		});

	}

	private void validateHttp(String path, String method, int statusCode, String body, int times, Runnable code) {
		HttpRequest serverRequest = request().withMethod(method).withPath(path);

		this.client.when(serverRequest).respond(response().withStatusCode(statusCode).withBody(body));

		code.run();

		this.client.verify(serverRequest, atLeast(times));
	}

}
