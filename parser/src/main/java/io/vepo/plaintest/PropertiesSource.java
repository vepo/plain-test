package io.vepo.plaintest;

import static java.util.Objects.isNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import io.vepo.plaintest.Suite.SuiteBuilder;

public class PropertiesSource extends SuiteChild {
    public enum SourceType {
        CSV
    };

    public static class PropertiesSourceBuilder {
        private int index;
        private SuiteBuilder parent;
        private PropertiesSource instance;
        private SourceType type;
        private File file;
        private String separator;
        private List<String> headers;

        private PropertiesSourceBuilder() {
            headers = new ArrayList<>();
        }

        public PropertiesSourceBuilder index(int index) {
            this.index = index;
            return this;
        }

        public PropertiesSourceBuilder type(SourceType type) {
            this.type = type;
            return this;
        }

        public PropertiesSourceBuilder parent(SuiteBuilder parent) {
            this.parent = parent;
            return this;
        }

        public PropertiesSourceBuilder file(File file) {
            this.file = file;
            return this;
        }

        public PropertiesSourceBuilder separator(String separator) {
            this.separator = separator;
            return this;
        }

        public PropertiesSourceBuilder headers(List<String> headers) {
            this.headers.addAll(headers);
            return this;
        }

        public PropertiesSource build() {
            if (isNull(instance)) {
                instance = new PropertiesSource(this);
            }
            return instance;
        }

    }

    public static final PropertiesSourceBuilder builder() {
        return new PropertiesSourceBuilder();
    }

    private final SourceType type;
    private final File file;
    private final String separator;
    private final List<String> headers;

    private PropertiesSource(PropertiesSourceBuilder builder) {
        super(builder.index, Optional.ofNullable(builder.parent).map(SuiteBuilder::build).orElse(null));
        type = builder.type;
        file = builder.file;
        separator = builder.separator;
        headers = builder.headers;
    }

    public SourceType getType() {
        return type;
    }

    public File getFile() {
        return file;
    }

    public String getSeparator() {
        return separator;
    }

    public List<String> getHeaders() {
        return headers;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(type).append(file).append(separator)
                .append(headers).hashCode();
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
        PropertiesSource other = (PropertiesSource) obj;
        return new EqualsBuilder().appendSuper(super.equals(obj)).append(type, other.type).append(file, other.file)
                .append(separator, other.separator).append(headers, other.headers).isEquals();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).appendSuper(super.toString())
                .append("type", type).append("file", file).append("separator", separator).append("headers", headers)
                .toString();
    }

}
