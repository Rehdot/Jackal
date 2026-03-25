package me.redot.jackal.util;

import lombok.experimental.UtilityClass;

import java.util.Set;

@UtilityClass
public class Constants {

    public static final Set<String> MODIFIERS = Set.of(
            "public", "private", "protected",
            "static", "final", "abstract",
            "synchronized", "volatile", "transient",
            "native", "strictfp", "sealed", "default"
    );

    public static final String[] SYMBOLS = {
            ">>>=", ">>=", "<<=", ">>>", "->", "++", "--", "==", "!=", ">=", "<=",
            "+=", "-=", "*=", "/=", "%=", "&=", "|=", "^=", "&&", "||", "<<",
            ">>", "+", "-", "*", "/", "%", "&", "|", "^", "!", "~", "?", ":",
            "=", "<", ">", ".", ",", ";", "(", ")", "{", "}", "[", "]"
    };

}
