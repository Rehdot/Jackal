package me.redot.jackal.token;

public enum TokenModifier {

    CONCAT,
    CAP,
    UNCAP,
    UPPER,
    LOWER,
    NEWLINE,
    DELETE;

    public static TokenModifier resolve(String name) {
        return valueOf(name.toUpperCase());
    }

}
