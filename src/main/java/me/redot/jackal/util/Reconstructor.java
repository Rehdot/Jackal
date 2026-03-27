package me.redot.jackal.util;

import lombok.experimental.UtilityClass;
import me.redot.jackal.token.Token;
import me.redot.jackal.token.TokenModifier;
import me.redot.jackal.token.type.GroupToken;
import me.redot.jackal.token.type.GroupType;
import me.redot.jackal.token.type.ModifierToken;
import me.redot.jackal.token.type.TokenType;

import javax.lang.model.SourceVersion;
import java.util.List;
import java.util.Set;

// This class is quick and dirty. To be honest, source reconstruction is not
// super important, but it is nicer to look at code that isn't so spaced out.
@UtilityClass
public class Reconstructor {

    public static String toSource(List<Token> tokens, Set<String> imports) {
        return toSource(tokens, imports, 0);
    }

    public static String toSource(List<Token> tokens, Set<String> imports, int depth) {
        StringBuilder builder = new StringBuilder();

        int importInsertIndex = (depth == 0 && imports != null && !imports.isEmpty())
                ? findImportStartIndex(tokens)
                : -1;

        if (importInsertIndex == 0) {
            for (String imp : imports) {
                builder.append("import ").append(imp).append(";\n");
            }
            builder.append("\n");
            importInsertIndex = -1;
        }

        String indent = "    ".repeat(depth);
        boolean atLineStart = depth > 0;

        for (int i = 0; i < tokens.size(); i++) {
            if (i == importInsertIndex) {
                builder.append("\n");
                for (String imp : imports) {
                    builder.append("import ").append(imp).append(";\n");
                }
                atLineStart = true;
            }

            Token current = tokens.get(i);
            Token next = i + 1 < tokens.size() ? tokens.get(i + 1) : null;

            if (current instanceof GroupToken group) {
                if (atLineStart) {
                    builder.append(indent);
                    atLineStart = false;
                }
                switch (group.getGroupType()) {
                    case BRACE -> {
                        builder.append("{\n");
                        builder.append(toSource(group.getSubtokens(), null, depth + 1));
                        builder.append(indent).append("}");
                    }
                    case PAREN, BRACKET -> {
                        GroupType type = group.getGroupType();
                        builder.append(type.getOpen());
                        builder.append(toSource(group.getSubtokens(), null, 0));
                        builder.append(type.getClose());
                    }
                }
            } else {
                if (atLineStart) {
                    builder.append(indent);
                    atLineStart = false;
                }

                String content = current.getContent();
                if (i > 0 && tokens.get(i - 1) instanceof ModifierToken mod) {
                    content = applyModifiers(content, mod.getModifiers());
                }

                builder.append(content);
            }

            String trailing = trailingString(current, next);
            builder.append(trailing);

            if (trailing.contains("\n")) {
                atLineStart = true;
            }
        }

        return builder.toString();
    }

    private static String applyModifiers(String content, List<TokenModifier> modifiers) {
        for (TokenModifier modifier : modifiers) {
            switch (modifier) {
                case LOWER -> content = content.toLowerCase();
                case UPPER -> content = content.toUpperCase();
                case DELETE -> content = "";
                case CAP -> {
                    if (content.isEmpty()) continue;
                    content = Character.toUpperCase(content.charAt(0)) + content.substring(1);
                }
                case UNCAP -> {
                    if (content.isEmpty()) continue;
                    content = Character.toLowerCase(content.charAt(0)) + content.substring(1);
                }
            }
        }
        return content;
    }

    // imports aren't something we need to spend a lot of time on,
    // because javac will yeet them anyway if they're unused.
    /// returns the index of the token AFTER the package declaration semicolon,
    /// or 0 if there is no package declaration.
    private static int findImportStartIndex(List<Token> tokens) {
        if (tokens.isEmpty() || !tokens.get(0).is("package")) {
            return 0;
        }

        for (int i = 1; i < tokens.size(); i++) {
            if (tokens.get(i).is(";")) {
                return i + 1;
            }
        }

        return 0;
    }

    private static String trailingString(Token current, Token next) {
        String a = current.getContent();
        String b = next != null ? next.getContent() : "";

        if (current instanceof ModifierToken) {
            if (next instanceof ModifierToken mod
                    && mod.hasModifier(TokenModifier.CONCAT)) return "";
            return " ";
        }
        if (next instanceof ModifierToken mod) {
            if (mod.hasModifier(TokenModifier.CONCAT)) return "";
            if (mod.hasModifier(TokenModifier.NEWLINE)) return "\n";
        }

        if (current instanceof GroupToken group) {
            if (group.getGroupType() == GroupType.BRACE) {
                return switch (b) {
                    case "else", "catch", "finally", "while" -> " ";
                    case "public", "private", "protected", "static" -> "\n\n";
                    default -> "\n";
                };
            }
        }

        switch (a) {
            case ";", "{" -> { return "\n"; }
            case "}" -> {
                return switch (b) {
                    case "else", "catch", "finally", "while" ->  " ";
                    case "public", "private", "protected", "static" -> "\n\n";
                    default -> "\n";
                };
            }
        }

        switch (a) {
            case ".", "@", "(", "[", "!", "~", "++", "--", "\\" -> { return ""; }
        }
        switch (b) {
            case ".", ",", ";", ")", "]", "++", "--", "" -> { return ""; }
        }

        if (SourceVersion.isKeyword(a)) {
            return " ";
        }

        if (current.getType() == TokenType.IDENT
                && next instanceof GroupToken group
                && group.getGroupType() == GroupType.PAREN) {
            return "";
        }
        if (a.equals("<") && (next.getType() == TokenType.IDENT || b.equals("<"))) {
            return "";
        }
        switch (b) {
            case ">", ">>", ">>>" -> { return ""; }
        }

        return " ";
    }

}
