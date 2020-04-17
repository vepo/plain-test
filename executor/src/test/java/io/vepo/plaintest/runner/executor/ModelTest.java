package io.vepo.plaintest.runner.executor;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.jparams.verifier.tostring.ToStringVerifier;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

public class ModelTest {

	@ParameterizedTest
	@ValueSource(classes = { Result.class, Attribute.class, Fail.class })
	public void checkModelTest(Class<?> checkClass) {
		ToStringVerifier.forClass(checkClass).verify();
		EqualsVerifier.forClass(checkClass).usingGetClass().suppress(Warning.NONFINAL_FIELDS)
				.withPrefabValues(Result.class, Result.builder().name("step1").success(true).build(),
						Result.builder().name("step2").success(false).build())
				.verify();
	}
}
