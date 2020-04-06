package io.vepo.plaintest.runner.executor;

import org.junit.jupiter.api.Test;

import io.vepo.plaintest.SuiteFactory;

public class BashCommandTest {

	@Test
	public void listCommandTest() {
		var suite = SuiteFactory.parseSuite("""
				Suite BashTest {
				    exec-dir: src

				    CMD EnterDir {
				        cmd    : "ls"
				        timeout: 500
				        assert returnCode: 0
				    }
				
				    Suite MainTest {
				        exec-dir: main
				        CMD EnterSubFolder {
				            cmd    : "ls"
				            timeout: 500
				            assert returnCode: 0
				        }
				    }
				}
								""");
		var executor = new PlainTestExecutor();
		executor.execute(suite);
	}
}
