package io.vepo.plaintest.runner.executor;

import org.junit.jupiter.api.Test;

import io.vepo.plaintest.SuiteFactory;

public class BashCommandTest {

	@Test
	public void listCommandTest() {
		var suite = SuiteFactory.parseSuite("""
				Suite BashTest {
				    CMD EnterDir {
				        cmd    : "cd src"
				        timeout: 500
				        assert returnCode: 0
				    }
				    CMD EnterSubFolder {
				        cmd    : "cd main"
				        timeout: 500
				        assert returnCode: 0
				    }
				}
								""");
		var executor = new PlainTestExecutor();
		executor.execute(suite);
	}
}
