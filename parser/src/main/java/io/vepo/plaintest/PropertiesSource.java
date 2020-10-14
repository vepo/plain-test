package io.vepo.plaintest;

import static java.util.Objects.isNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((file == null) ? 0 : file.hashCode());
        result = prime * result + ((headers == null) ? 0 : headers.hashCode());
        result = prime * result + ((separator == null) ? 0 : separator.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        PropertiesSource other = (PropertiesSource) obj;
        if (file == null) {
            if (other.file != null) {
                return false;
            }
        } else if (!file.equals(other.file)) {
            return false;
        }
        if (headers == null) {
            if (other.headers != null) {
                return false;
            }
        } else if (!headers.equals(other.headers)) {
            return false;
        }
        if (separator == null) {
            if (other.separator != null) {
                return false;
            }
        } else if (!separator.equals(other.separator)) {
            return false;
        }
        if (type != other.type) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "PropertiesSource [type=" + type + ", file=" + file + ", separator=" + separator + ", headers=" + headers
                + "]";
    }

}
