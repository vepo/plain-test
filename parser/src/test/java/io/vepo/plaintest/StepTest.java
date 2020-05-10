package io.vepo.plaintest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class StepTest {

	@Test
	@DisplayName("It should return the property according with the type")
	public void attributeTest() {
		assertTrue(Step.builder().attribute("booleanProperty", "true").build().requiredAttribute("booleanProperty",
				Boolean.class));
		assertThat(Step.builder().attribute("booleanProperty", "true").build().optionalAttribute("booleanProperty",
				Boolean.class)).isNotEmpty().contains(true);
	}

}
