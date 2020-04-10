package io.vepo.plaintest;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.jparams.verifier.tostring.ToStringVerifier;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

public class ModelTest {

	@ParameterizedTest
	@ValueSource(classes = { Suite.class, Step.class, Assertion.class, })
	public void checkModelTest(Class<?> checkClass) {
		ToStringVerifier.forClass(checkClass).verify();
		EqualsVerifier.forClass(checkClass).usingGetClass()
				.withPrefabValues(Suite.class, new Suite(0, "S1", emptyList(), emptyList(), emptyMap()),
						new Suite(1, "S2", emptyList(), emptyList(), emptyMap()))
				.suppress(Warning.NONFINAL_FIELDS).verify();
	}
}
