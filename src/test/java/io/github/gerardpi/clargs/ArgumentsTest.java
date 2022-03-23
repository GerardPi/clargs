package io.github.gerardpi.clargs;

import com.google.common.collect.ImmutableList;
import com.tngtech.jgiven.Stage;
import com.tngtech.jgiven.annotation.Quoted;
import com.tngtech.jgiven.junit5.SimpleScenarioTest;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class ArgumentsTest extends SimpleScenarioTest<ArgumentsTest.State> {

    public static void main(String[] args) {
        Arguments arguments = Arguments.builder()
                .add(Argument.builder()
                        .setKey(ArgumentKey.KAAS)
                        .setRequired(true)
                        .setValueRequired().build())
                .add(Argument.builder()
                        .setKey(ArgumentKey.WORST)
                        .setRequired(true)
                        .setValueOptional().build())
                .add(Argument.builder()
                        .setKey(ArgumentKey.TOPPING)
                        .setRequired(false)
                        .setValueOptional().build())
                .build().parseArgs(args);
        System.out.println(arguments.displayValue());
        System.out.println("There are " + arguments.getArgumentsWithErrors().size() + " arguments with errors");
        arguments.getArgumentsWithErrors().forEach(argument -> System.out.println(argument.getError().getMessage()));
        System.out.println("There are " + arguments.get().size() + " arguments");
        arguments.get().forEach(argument -> System.out.println(argument));
    }

    @Test
    void happy_flow() {
        given().expected_required_argument_$_that_has_a_required_value(ArgumentKey.KAAS)
                .and().expected_required_argument_$_that_has_an_optional_value(ArgumentKey.WORST)
                .and().expected_required_argument_$_that_has_no_value(ArgumentKey.TOPPING)
                .and().expected_optional_argument_$_that_has_no_value(ArgumentKey.SAUCE)
                .and().the_display_value_is_$(getText("expected-usage-happy-flow.txt"));
        when().command_line_arguments_$_are_parsed(ImmutableList.of("-k", "edammer", "-w", "knack", "-t"));
        then().$_arguments_were_found(3)
                .and().$_errors_were_found(0)
                .and().an_argument_with_key_$_and_required_value_$_is_found(ArgumentKey.KAAS, "edammer")
                .and().an_argument_with_key_$_and_optional_value_$_is_found(ArgumentKey.WORST, "knack")
                .and().an_argument_with_key_$_and_no_value_is_found(ArgumentKey.TOPPING);
    }

    @Test
    void error_required_argument_missing() {
        given().expected_required_argument_$_that_has_a_required_value(ArgumentKey.KAAS)
                .and().expected_required_argument_$_that_has_an_optional_value(ArgumentKey.WORST)
                .and().expected_required_argument_$_that_has_no_value(ArgumentKey.TOPPING);
        when().command_line_arguments_$_are_parsed(ImmutableList.of("-k", "edammer", "-w", "knack"));
        then().$_arguments_were_found(2)
                .and().$_errors_were_found(1)
                .and().an_argument_with_key_$_and_required_value_$_is_found(ArgumentKey.KAAS, "edammer")
                .and().an_argument_with_key_$_and_optional_value_$_is_found(ArgumentKey.WORST, "knack")
                .and().an_argument_with_key_$_and_with_error_message_$_is_found(ArgumentKey.TOPPING, "Missing argument -t or --topping; required: yes; no value is expected; description: 'null'")
                .and().the_errors_display_value_is_$(getText("expected-usage-error-1.txt"));
    }

    private String getText(String resourceName) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(Objects.requireNonNull(getClass().getResourceAsStream(resourceName), "Can not read file " + resourceName), StandardCharsets.UTF_8))) {
            return reader.lines().collect(Collectors.joining(System.lineSeparator()));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    static enum ArgumentKey implements Key {
        WORST("w", "worst"),
        KAAS("k", "kaas"),
        TOPPING("t", "topping"),
        SAUCE("s", "sauce");

        private final String shortKey;
        private final String longKey;

        ArgumentKey(String shortKey, String longKey) {
            this.shortKey = shortKey;
            this.longKey = longKey;
        }

        @Override
        public String getShortKey() {
            return shortKey;
        }

        @Override
        public String getLongKey() {
            return longKey;
        }
    }

    static class State extends Stage<State> {
        private final Arguments.Builder argumentsBuilder = Arguments.builder();
        private Arguments arguments;


        State expected_required_argument_$_that_has_a_required_value(@Quoted ArgumentKey key) {
            argumentsBuilder.add(Argument.builder().setRequired(true).setKey(key).setValueRequired().build());
            return self();
        }

        State expected_required_argument_$_that_has_an_optional_value(@Quoted ArgumentKey key) {
            argumentsBuilder.add(Argument.builder().setRequired(true).setKey(key).setValueOptional().build());
            return self();
        }

        State expected_required_argument_$_that_has_no_value(@Quoted ArgumentKey key) {
            argumentsBuilder.add(Argument.builder().setRequired(true).setKey(key).build());
            return self();
        }

        State expected_optional_argument_$_that_has_no_value(@Quoted ArgumentKey key) {
            argumentsBuilder.add(Argument.builder().setRequired(false).setKey(key).build());
            return self();
        }

        State command_line_arguments_$_are_parsed(@Quoted List<String> commandLineArguments) {
            String[] args = commandLineArguments.toArray(new String[0]);
            Arguments unparsedArguments = argumentsBuilder.build();
            assertThat(unparsedArguments.isFilled()).isFalse();
            this.arguments = unparsedArguments.parseArgs(args);
            assertThat(arguments.isFilled()).isTrue();
            return self();
        }

        State $_errors_were_found(int expectedErrorCount) {
            assertThat(arguments.getArgumentsWithErrors().size()).isEqualTo(expectedErrorCount);
            return self();
        }

        State an_argument_with_key_$_and_required_value_$_is_found(@Quoted ArgumentKey key, @Quoted String expectedValue) {
            assertThat(arguments.hasArgument(key)).isTrue();
            assertThat(arguments.getRequiredArgument(key).containsValue()).isTrue();
            assertThat(arguments.getRequiredValue(key)).isEqualTo(expectedValue);
            return self();
        }

        State an_argument_with_key_$_and_optional_value_$_is_found(@Quoted ArgumentKey key, @Quoted String expectedValue) {
            assertThat(arguments.hasArgument(key)).isTrue();
            assertThat(arguments.getArgument(key)).isPresent();
            assertThat(arguments.getArgument(key).get().containsValue()).isTrue();
            assertThat(arguments.getValue(key)).isNotEmpty();
            assertThat(arguments.getValue(key)).hasValue(expectedValue);
            return self();
        }

        State an_argument_with_key_$_and_no_value_is_found(@Quoted ArgumentKey key) {
            assertThat(arguments.hasArgument(key)).isTrue();
            assertThat(arguments.getArgument(key).get().isSuccess()).isTrue();
            return self();
        }

        State an_argument_with_key_$_and_with_error_message_$_is_found(@Quoted ArgumentKey key, @Quoted String expectedErrorMessage) {
            assertThat(arguments.hasArgument(key)).isTrue();
            assertThat(arguments.getArgument(key).get().isSuccess()).isFalse();
            assertThat(arguments.getArgument(key).get().getError().getMessage()).isEqualTo(expectedErrorMessage);
            return self();
        }

        State $_arguments_were_found(int expectedArgumentsCount) {
            assertThat(arguments.get().size()).isEqualTo(expectedArgumentsCount);
            return self();
        }

        State the_display_value_is_$(@Quoted String expectedDisplayValue) {
            assertThat(argumentsBuilder.build().displayValue()).isEqualTo(expectedDisplayValue);
            return self();
        }

        State the_errors_display_value_is_$(@Quoted String expectedDisplayValue) {
            assertThat(arguments.errorsDisplayValue()).isEqualTo(expectedDisplayValue);
            return self();
        }
    }
}
