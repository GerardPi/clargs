package io.github.gerardpi.clargs;

import java.util.*;

public class Argument {
    private final Key key;
    private final boolean required;
    private final ValueType valueType;
    private final String description;
    private final String value;
    private final ArgumentError error;

    private Argument(Key key, boolean required, ValueType valueType, String value, String description, ArgumentError error) {
        this.key = key;
        this.required = required;
        this.valueType = valueType;
        this.value = value;
        this.description = description;
        this.error = error;
    }

    public static Builder builder() {
        return new Builder();
    }

    public boolean isRequired() {
        return required;
    }

    public ValueType getValueType() {
        return valueType;
    }

    public boolean isSuccess() {
        return this.error == null;
    }

    public ArgumentError getError() {
        return error;
    }

    public String getDescription() {
        return description;
    }

    public String getValue() {
        return value;
    }

    public boolean matches(String key) {
        return this.key.matches(key);
    }

    public boolean containsValue() {
        return this.value != null;
    }

    public Key getKey() {
        return key;
    }

    public Argument withValue(String value) {
        return new Argument(key, required, valueType, value, description, null);
    }
    public Argument withError(ArgumentError error) {
        return new Argument(key, required, valueType, value, description, error);
    }

    public Argument withMissingArgumentError() {
        return withError(new ArgumentError(ArgumentError.Type.MISSING_ARGUMENT, "Missing argument " + this.displayValue()));
    }

    public Argument withMissingArgumentValueError() {
        return withError(new ArgumentError(ArgumentError.Type.MISSING_ARGUMENT_VALUE, "Missing argument value " + this.displayValue()));
    }

    public Argument withNoArgumentValueExpectedError() {
        return withError(new ArgumentError(ArgumentError.Type.NO_ARGUMENT_VALUE_EXPECTED, "No argument value expected " + this.displayValue()));
    }

    static class ArgumentError {
        static enum Type {
            MISSING_ARGUMENT_VALUE,
            MISSING_ARGUMENT,
            NO_ARGUMENT_VALUE_EXPECTED
        }
        private final String message;
        private final Type type;

        public ArgumentError(Type type, String message) {
            this.type = type;
            this.message = message;
        }

        public String getMessage() {
            return message;
        }

        public Type getType() {
            return type;
        }

        @Override
        public String toString() {
            return new StringJoiner(", ",
                    ArgumentError.class.getSimpleName() + "[", "]")
                    .add("type=" + type)
                    .add("message=" + message)
                    .toString();
        }
    }


    @Override
    public String toString() {
        return new StringJoiner(", ", Argument.class.getSimpleName() + "[", "]")
                .add("key=" + key)
                .add("required=" + required)
                .add("valueType=" + valueType)
                .add("description='" + description + "'")
                .add("value='" + value + "'")
                .add("error=" + error)
                .toString();
    }

    public String displayValue() {
        return new StringJoiner("; ", "", "")
                .add("-" + key.getShortKey() + " or --" + getKey().getLongKey())
                .add("required: " + (required ? "yes" : "no"))
                .add(valueType.expectsValue() ? "a value is " + valueType.getDisplayValue() : "no value is expected")
                .add("description: '" + description + "'")
                .toString();
    }

    static enum ValueType {
        NO_VALUE("none"),
        OPTIONAL_VALUE("optional"),
        REQUIRED_VALUE("required");
        private final String displayValue;

        ValueType(String displayValue) {
            this.displayValue = displayValue;
        }

        public boolean requiresValue() {
            return this == REQUIRED_VALUE;
        }
        public boolean expectsValue() {
            return this != NO_VALUE;
        }

        public String getDisplayValue() {
            return displayValue;
        }
    }

    public static class Builder {
        private Key key;
        private boolean required;
        private ValueType valueType = ValueType.NO_VALUE;
        private String description;
        private String value;

        public Builder setKey(Key key) {
            this.key = key;
            return this;
        }

        public Builder setRequired(boolean required) {
            this.required = required;
            return this;
        }

        public Builder setValueRequired() {
            return setValueType(ValueType.REQUIRED_VALUE);
        }

        public Builder setValueOptional() {
            return setValueType(ValueType.OPTIONAL_VALUE);
        }

        public Builder setValueType(ValueType valueType) {
            this.valueType = valueType;
            return this;
        }

        public Builder setDescription(String description) {
            this.description = description;
            return this;
        }

        public Builder setValue(String value) {
            this.value = value;
            return this;
        }

        public Argument build() {
            return new Argument(key, required, valueType, value, description, null);
        }
    }
}
