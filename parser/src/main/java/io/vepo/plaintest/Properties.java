package io.vepo.plaintest;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class Properties extends TestItem {
    public static final class PropertiesBuilder {
        private int index;
        private Map<String, Object> values;

        private PropertiesBuilder() {
            values = new HashMap<>();
        }

        public PropertiesBuilder index(int index) {
            this.index = index;
            return this;
        }

        public PropertiesBuilder value(String key, Object value) {
            values.put(key, value);
            return this;
        }

        public PropertiesBuilder values(Map<String, Object> values) {
            this.values.putAll(values);
            return this;
        }

        public Properties build() {
            return new Properties(this);
        }
    }

    public static final PropertiesBuilder builder() {
        return new PropertiesBuilder();
    }

    private final Map<String, Object> values;

    private Properties(PropertiesBuilder builder) {
        super(builder.index);
        values = builder.values;
    }

    public boolean hasValue(String key) {
        return values.containsKey(key);
    }

    @SuppressWarnings("unchecked")
    public <T> T getValue(String key) {
        return (T) values.get(key);
    }

    public Map<String, Object> getValues() {
        return values;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(values).hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Properties other = (Properties) obj;
        return new EqualsBuilder().appendSuper(super.equals(obj)).append(values, other.values).isEquals();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).appendSuper(super.toString())
                .append("values", values).toString();
    }
}
