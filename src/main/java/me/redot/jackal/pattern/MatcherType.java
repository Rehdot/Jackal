package me.redot.jackal.pattern;

public enum MatcherType {

    IDENT,
    EXPR,
    TT,
    BLOCK,
    MOD;

    public static MatcherType resolve(String type) {
        return valueOf(type.toUpperCase());
    }

}
