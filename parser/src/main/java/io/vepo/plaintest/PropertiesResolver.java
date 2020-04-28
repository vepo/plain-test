package io.vepo.plaintest;

import java.util.Optional;

public interface PropertiesResolver {
	public <T> T findRequiredPropertyValue(String key);

	public <T> Optional<T> findOptionalPropertyValue(String key);
}
