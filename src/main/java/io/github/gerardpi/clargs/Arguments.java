package io.github.gerardpi.clargs;

import com.google.common.collect.Iterators;
import com.google.common.collect.PeekingIterator;

import java.util.*;
import java.util.stream.Collectors;

/**
 * This class contains parameters to be used to parse a commaind line.
 */
public class Arguments {
    private final List<Argument> parameters;
    private final List<Argument> arguments;

    private Arguments(List<Argument> parameters, List<Argument> arguments) {
        this.parameters = parameters;
        this.arguments = arguments;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Arguments.class.getSimpleName() + "[", "]")
                .add("parameters=" + parameters)
                .add("arguments=" + arguments)
                .toString();
    }

    /**
     * @return When no command line was parsed, it will produce some information.
     * When a command line was parsed, and no errors were found, it will return a toString representation.
     */
    public String displayValue() {
        return parameters.stream().map(Argument::displayValue).collect(Collectors.joining(System.lineSeparator()));
    }

    public String errorsDisplayValue() {
        return getArgumentsWithErrors().stream().map(argument -> argument.getError().getMessage()).collect(Collectors.joining(System.lineSeparator()));
    }

    /**
     * Use this method to create parameters.
     *
     * @return Arguments that are actually still just parameters, waiting to be filled.
     * @see #parseArguments to fill the parameters to be arguments
     */
    public static Arguments create(List<Argument> parameters) {
        return new Arguments(parameters, Collections.emptyList());
    }

    /**
     * @return All the arguments found without errors.
     */
    public List<Argument> get() {
        return arguments.stream().filter(Argument::isSuccess).collect(Collectors.toList());
    }

    public boolean hasErrors() {
        return this.arguments.stream().anyMatch(argument -> !argument.isSuccess());
    }

    public List<Argument> getArgumentsWithErrors() {
        return this.arguments.stream().filter(argument -> !argument.isSuccess()).collect(Collectors.toList());
    }

    public boolean isFilled() {
        return !arguments.isEmpty();
    }

    /**
     * Use this method to parse the command line.
     * @return An Arguments object that contains parameters that are present in the command line.
     */
    public Arguments parseArgs(String[] args) {
        return new Arguments(parameters, parseArguments(this.parameters, args));
    }

    private static List<Argument> parseArguments(List<Argument> parameters, String[] args) {
        PeekingIterator<String> argIterator = Iterators.peekingIterator(Arrays.asList(args).iterator());
        List<Argument> result = new ArrayList<>();
        while (argIterator.hasNext()) {
            String arg = argIterator.next();
            for (Argument parameter : parameters) {
                if (parameter.matches(arg)) {
                    if (argIterator.hasNext()) {
                        String argPeeked = argIterator.peek();
                        if (parameter.getKey().isKey(argPeeked)) {
                            if (parameter.getValueType().requiresValue()) {
                                result.add(parameter.withMissingArgumentValueError());
                            }
                            result.add(parameter);
                        } else {
                            if (parameter.getValueType().expectsValue()) {
                                result.add(parameter.withValue(argPeeked));
                                argIterator.next();
                            } else {
                                result.add(parameter.withNoArgumentValueExpectedError());
                            }
                        }
                    } else {
                        result.add(parameter);
                    }
                }
            }
        }
        for (Argument parameter : parameters) {
            if (parameter.isRequired() && result.stream().noneMatch(argument -> argument.getKey().equals(parameter.getKey()))) {
                result.add(parameter.withMissingArgumentError());
            }
        }
        return Collections.unmodifiableList(result);
    }

    public boolean hasArgument(Key key) {
        return arguments.stream().anyMatch(argument -> argument.getKey().equals(key));
    }

    public String getRequiredValue(Key key) {
        return getArgument(key)
                .map(Argument::getValue)
                .orElseThrow(() -> new NoSuchElementException("There is no require value for argument '" + key + "'"));
    }


    public Optional<String> getValue(Key key) {
        return getArgument(key)
                .map(Argument::getValue);
    }

    public Optional<Argument> getArgument(Key key) {
        return arguments.stream()
                .filter(argument -> argument.getKey().equals(key))
                .findAny();
    }

    public Argument getRequiredArgument(Key key) {
        return getArgument(key).orElseThrow(() -> new NoSuchElementException("There is no required argument '" + key + "'"));
    }

    public Optional<Argument> getParameter(Key key) {
        return parameters.stream()
                .filter(argument -> argument.getKey().equals(key))
                .findAny();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final List<Argument> parameters;
        Builder() {
            this.parameters = new ArrayList<>();
        }
        public Builder add(Argument parameter) {
            parameters.add(parameter);
            return this;
        }
        public Arguments build() {
            return Arguments.create(parameters);
        }
    }
}
