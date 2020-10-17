package io.vepo.plaintest;

import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

import java.util.Optional;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import io.vepo.plaintest.exceptions.PropertyNotDefinedException;

public abstract class TestItem implements PropertiesResolver {
    private final int index;
    private TestItem parent;

    private transient PropertiesResolver propertiesResolver;

    protected TestItem(int index) {
        this.index = index;
    }

    public int getIndex() {
        return index;
    }

    public TestItem getParent() {
        return parent;
    }

    public void setParent(TestItem parent) {
        this.parent = parent;
    }

    public void setPropertiesResolver(PropertiesResolver propertiesResolver) {
        this.propertiesResolver = propertiesResolver;
    }

    @Override
    public <T> T findRequiredPropertyValue(String key) {
        if (nonNull(parent)) {
            for (int currIndex = index - 1; currIndex >= 0; --currIndex) {
                TestItem curr = parent.getChild(currIndex);
                if (curr instanceof Properties && ((Properties) curr).hasValue(key)) {
                    return ((Properties) curr).getValue(key);
                }
            }

            return Optional.ofNullable(propertiesResolver)
                    .map(resolver -> resolver.<T>findRequiredPropertyValue(key))
                    .orElseGet(() -> parent.<T>findRequiredPropertyValue(key));
        }
        return Optional.ofNullable(propertiesResolver)
                .map(resolver -> resolver.<T>findRequiredPropertyValue(key))
                .orElseThrow(() -> new PropertyNotDefinedException("Could not find property: " + key));
    }

    protected TestItem getChild(int currIndex) {
        throw new IllegalStateException("TreeItem does not accet children!");
    }

    @Override
    public <T> Optional<T> findOptionalPropertyValue(String key) {
        if (nonNull(parent)) {
            for (int currIndex = index - 1; currIndex >= 0; --currIndex) {
                TestItem curr = parent.getChild(currIndex);
                if (curr instanceof Properties && ((Properties) curr).hasValue(key)) {
                    return Optional.of(((Properties) curr).getValue(key));
                }
            }

            return Optional.ofNullable(propertiesResolver)
                    .map(resolver -> resolver.<T>findOptionalPropertyValue(key))
                    .orElseGet(() -> parent.<T>findOptionalPropertyValue(key));
        }
        return Optional.ofNullable(propertiesResolver).flatMap(resolver -> resolver.<T>findOptionalPropertyValue(key));
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(index).hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        requireNonNull(obj, "Null should be checked on parent class");
        if (getClass() != obj.getClass()) {
            return false;
        }
        TestItem other = (TestItem) obj;
        return new EqualsBuilder().append(index, other.index).isEquals();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, SHORT_PREFIX_STYLE).append("index", index).toString();
    }

}
