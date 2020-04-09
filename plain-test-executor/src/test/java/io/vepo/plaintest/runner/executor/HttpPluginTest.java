package io.vepo.plaintest.runner.executor;

import org.junit.jupiter.api.BeforeEach;

public class HttpPluginTest {
    private ClientAndServer mockServer;
	@BeforeEach
	public void setup() {
		mockServer = startClientAndServer(1080);
	}
}
