package me.redot.jackal.expansion;

import lombok.experimental.UtilityClass;
import me.redot.jackal.JackalSystem;
import me.redot.jackal.expansion.node.*;
import me.redot.jackal.pattern.RepetitionType;
import me.redot.jackal.token.Token;
import me.redot.jackal.token.TokenModifier;
import me.redot.jackal.token.TokenStream;
import me.redot.jackal.token.type.CharToken;
import me.redot.jackal.token.type.GroupToken;
import me.redot.jackal.token.type.GroupType;
import me.redot.jackal.token.type.TokenType;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/// Parse logic for expansion declarations
@UtilityClass
public class ExpansionParser {

    public static Expansion parse(List<Token> tokens) {
        TokenStream stream = new TokenStream(tokens);
        Expansion expansion = new Expansion();

        while (stream.hasNext()) {
            Token token = stream.peek();

            if (token.is(JackalSystem.VARIABLE_SPECIFIER)) {
                Token peek = stream.peek(1);

                if (peek instanceof GroupToken group) {
                    if (group.getGroupType() == GroupType.BRACKET) {
                        expansion.addNode(parseModifier(stream));
                    } else {
                        expansion.addNode(parseRepetition(stream));
                    }
                } else {
                    VariableExpansion variable = parseVariable(stream);
                    expansion.addNode(variable);
                }
                continue;
            }

            switch (token.getType()) {
                case GROUP -> {
                    expansion.addNode(parseGroup(stream));
                    continue;
                }
                case STRING -> {
                    String content = token.getContent();

                    if (content.contains(JackalSystem.VARIABLE_SPECIFIER)) {
                        stream.advance();
                        expansion.addNode(new StringExpansion(content));
                    } else {
                        expansion.addNode(new LiteralExpansion(stream.advance()));
                    }
                    continue;
                }
                case CHAR -> expansion.addNode(new CharExpansion((CharToken) token));
            }

            expansion.addNode(new LiteralExpansion(stream.advance()));
        }

        return expansion;
    }

    public static ModifierExpansion parseModifier(TokenStream stream) {
        stream.expect(JackalSystem.VARIABLE_SPECIFIER);

        GroupToken group = stream.expectGroup(GroupType.BRACKET);
        TokenStream inner = new TokenStream(group.getSubtokens());
        List<TokenModifier> modifiers = new ArrayList<>();

        while (inner.hasNext()) {
            Token token = inner.advance();
            if (token.is(",")) continue;
            modifiers.add(TokenModifier.resolve(token.getContent()));
        }

        return new ModifierExpansion(modifiers);
    }

    public static GroupExpansion parseGroup(TokenStream stream) {
        Token token = stream.expectType(TokenType.GROUP);

        if (!(token instanceof GroupToken group)) {
            throw new IllegalStateException("Expected GroupToken for ExpansionParser.parseGroup()");
        }

        Expansion inner = parse(group.getSubtokens());
        return new GroupExpansion(group.getGroupType(), inner.getNodes());
    }

    public static VariableExpansion parseVariable(TokenStream stream) {
        stream.expect(JackalSystem.VARIABLE_SPECIFIER);

        Token ident = stream.expectType(TokenType.IDENT);
        return new VariableExpansion(ident.getContent());
    }

    public static ExpansionNode parseRepetition(TokenStream stream) {
        stream.expect(JackalSystem.VARIABLE_SPECIFIER);

        GroupToken group = stream.expectGroup(GroupType.PAREN);
        Expansion inner = parse(group.getSubtokens());
        String separator = null;
        Token next = stream.peek();

        switch (next.getContent()) {
            case "*", "+", "?" -> {}
            default -> separator = stream.advance().getContent();
        }

        RepetitionType type = RepetitionType.resolve(stream.advance().getContent());
        String name = findFirstVariable(inner.getNodes())
                .orElseThrow(() -> new IllegalStateException(
                        "Repetition expansion must contain at least one variable"
                ));

        if (type == RepetitionType.ZERO_OR_ONE) {
            return new OptionalExpansion(inner.getNodes(), name);
        }

        return new RepetitionExpansion(inner.getNodes(), name, separator, type);
    }

    private static Optional<String> findFirstVariable(List<ExpansionNode> nodes) {
        for (ExpansionNode node : nodes) {
            if (node instanceof VariableExpansion variable) {
                return Optional.of(variable.getName());
            }
            if (node instanceof GroupExpansion group) {
                Optional<String> found = findFirstVariable(group.getNodes());
                if (found.isPresent()) return found;
            }
            if (node instanceof RepetitionExpansion repetition) {
                return Optional.of(repetition.getName());
            }
            if (node instanceof OptionalExpansion optional) {
                return Optional.of(optional.getGuardian());
            }
        }
        return Optional.empty();
    }

}
