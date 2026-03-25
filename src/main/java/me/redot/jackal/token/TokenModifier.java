package me.redot.jackal.token;

public enum TokenModifier {

    CONCAT,
    CAP,
    UPPER,
    LOWER;

    public static TokenModifier resolve(String name) {
        return valueOf(name.toUpperCase());
    }

}
