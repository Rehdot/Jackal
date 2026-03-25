package me.redot.jackal.pattern;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RepetitionType {

    ONE_OR_MORE("+"),
    ZERO_OR_MORE("*"),
    ZERO_OR_ONE("?");

    private final String specifier;

    public static RepetitionType resolve(String specifier) {
        return switch (specifier) {
            case "+" -> ONE_OR_MORE;
            case "*" -> ZERO_OR_MORE;
            case "?" -> ZERO_OR_ONE;
            default -> throw new IllegalStateException("Unexpected repetition specifier: " + specifier);
        };
    }

}
