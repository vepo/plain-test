package io.vepo.plaintest.runner.executor;

import org.junit.jupiter.api.Test;

import io.vepo.plaintest.SuiteFactory;

public class BashCommandTest {

	@Test
	void listCommandTest() {
		var suite = SuiteFactory.parseSuite("""
				Suite BashTest {
				    CMD ListFolder {
				        cmd   : "ls"
				        assert returnCode: 0
				    }
				}
								""");
		var executor = new PlainTestExecutor();
		executor.execute(suite);
	}
}
