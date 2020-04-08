package io.vepo.plaintest.runner.executor;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Arrays;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.vepo.plaintest.SuiteFactory;
import io.vepo.plaintest.runner.utils.MockOs;

public class FailsTest {
	@BeforeEach
	public void setup() {
		MockOs.mockUnix();
	}

	@AfterEach
	public void shutdown() {
		MockOs.clear();
	}

	@Test
	public void unknownPluginTest() {
		var suite = SuiteFactory.parseSuite("""
				Suite UnknownTest {
				        Unknown DoNothing {
				            cmd    : "xyz"
				            timeout: 500
				            assert stdout Equals "other string"
				       }
				    }
				""");
		var executor = new PlainTestExecutor();
		Result result = executor.execute(suite);
		Result doNothingResult = (Result) result.results().stream().filter(r -> ((Result) r).name().equals("DoNothing"))
				.findFirst().orElse(null);
		assertNotNull(doNothingResult);
		assertEquals(Arrays.asList(new Fail(FailReason.PLUGIN_NOT_FOUND, "Could not find plugin: Unknown")),
				doNothingResult.fails());
	}

	@Test
	public void unknownCommandPluginTest() {
		var suite = SuiteFactory.parseSuite("""
				Suite UnknownTest {
				        CMD DoNothing {
				            cmd    : "unknownCommand"
				            timeout: 500
				       }
				    }
				""");
		var executor = new PlainTestExecutor();
		Result result = executor.execute(suite);
		Result doNothingResult = (Result) result.results().stream().filter(r -> ((Result) r).name().equals("DoNothing"))
				.findFirst().orElse(null);
		assertNotNull(doNothingResult);
		assertEquals(doNothingResult.fails().size(), 1);
		assertEquals(FailReason.FAILED, ((Fail) doNothingResult.fails().get(0)).reason());
	}
}
