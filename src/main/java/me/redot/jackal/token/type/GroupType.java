package me.redot.jackal.token.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum GroupType {

    PAREN("(", ")"),
    BRACE("{", "}"),
    BRACKET("[", "]");

    private final String open;
    private final String close;

    public static GroupType fromOpenChar(char c) {
        return switch (c) {
            case '(' -> PAREN;
            case '{' -> BRACE;
            case '[' -> BRACKET;
            default -> null;
        };
    }

}
