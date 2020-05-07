package io.vepo.plaintest;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.jparams.verifier.tostring.ToStringVerifier;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.EqualsVerifierApi;
import nl.jqno.equalsverifier.Warning;

public class ModelTest {

	@ParameterizedTest
	@ValueSource(classes = { Suite.class, Step.class, Assertion.class, Properties.class, PropertyReference.class })
	public void checkModelTest(Class<?> checkClass) {
		ToStringVerifier toStringVerifier = ToStringVerifier.forClass(checkClass);
		if (SuiteChild.class.isAssignableFrom(checkClass)) {
			toStringVerifier.withIgnoredFields("parent", "propertiesResolver");
		}
		toStringVerifier.verify();

		EqualsVerifierApi<?> equalsVerifier = EqualsVerifier
				.forClass(checkClass).usingGetClass().withPrefabValues(Suite.class,
						Suite.builder().index(0).name("S1").build(), Suite.builder().index(1).name("S2").build())
				.suppress(Warning.NONFINAL_FIELDS);
		if (SuiteChild.class.isAssignableFrom(checkClass)) {
			equalsVerifier.withIgnoredFields("parent", "propertiesResolver");
		}
		equalsVerifier.verify();
	}

	@DisplayName("It should match correctly the PropertyReference")
	@Test
	public void checkPropertyReferenceTest() {
		assertFalse(PropertyReference.matches("${}"));
		assertTrue(PropertyReference.matches("${property}"));
		assertFalse(PropertyReference.matches("some field ${property} some data"));
	}
}
