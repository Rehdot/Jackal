package me.redot.jackal.pattern;

import lombok.experimental.UtilityClass;
import me.redot.jackal.JackalSystem;
import me.redot.jackal.pattern.node.*;
import me.redot.jackal.token.Token;
import me.redot.jackal.token.TokenStream;
import me.redot.jackal.token.type.GroupToken;
import me.redot.jackal.token.type.GroupType;
import me.redot.jackal.token.type.TokenType;

import java.util.List;

/// Parse logic for pattern declarations
@UtilityClass
public class PatternParser {

    public static Pattern parse(List<Token> tokens, GroupType groupType) {
        TokenStream stream = new TokenStream(tokens);
        Pattern pattern = new Pattern(groupType);

        while (stream.hasNext()) {
            Token token = stream.peek();

            if (token.is(JackalSystem.VARIABLE_SPECIFIER)) {
                if (stream.peek(1) instanceof GroupToken) { // repetition
                    pattern.addNode(parseRepetition(stream));
                    continue;
                }

                pattern.addNode(parseVariable(stream)); // variable
                continue;
            }

            if (token instanceof GroupToken) {
                pattern.addNode(parseGroup(stream));
                continue;
            }

            pattern.addNode(new LiteralPattern(stream.advance().getContent()));
        }

        createLookaheads(pattern);
        return pattern;
    }

    private static void createLookaheads(Pattern pattern) {
        createLookaheads(pattern.getNodes(), null);
    }

    private static void createLookaheads(List<PatternNode> nodes, PatternNode outerLookahead) {
        RepetitionPattern last = null;

        for (int i = 0; i < nodes.size(); i++) {
            PatternNode node = nodes.get(i);

            if (last != null && last.getLookahead() == null && node instanceof LiteralPattern) {
                last.setLookahead(node);
            }

            if (node instanceof RepetitionPattern rep) {
                if (rep == getLastNode(nodes) && rep.getLookahead() == null) {
                    rep.setLookahead(outerLookahead);
                }
                // recurse with rep's own lookahead as the outer context for its children
                createLookaheads(rep.getPattern(), rep.getLookahead());
                last = rep;
            } else if (node instanceof GroupPattern group) {
                createLookaheads(group.getChildren(), null);
                last = null;
            } else {
                last = null;
            }
        }
    }

    private static PatternNode getLastNode(List<PatternNode> nodes) {
        return nodes.isEmpty() ? null : nodes.get(nodes.size() - 1);
    }

    public static GroupPattern parseGroup(TokenStream stream) {
        Token groupToken = stream.expectType(TokenType.GROUP);

        if (!(groupToken instanceof GroupToken group)) {
            return null;
        }

        Pattern inner = parse(group.getSubtokens(), group.getGroupType());
        return new GroupPattern(group.getGroupType(), inner.getNodes());
    }

    public static VariablePattern parseVariable(TokenStream stream) {
        stream.expect(JackalSystem.VARIABLE_SPECIFIER);

        Token name = stream.expectType(TokenType.IDENT);

        stream.expect(":");

        Token type = stream.expectType(TokenType.IDENT);
        MatcherType matcherType = MatcherType.resolve(type.getContent());

        return new VariablePattern(name.getContent(), matcherType);
    }

    public static RepetitionPattern parseRepetition(TokenStream stream) {
        stream.expect(JackalSystem.VARIABLE_SPECIFIER);

        GroupToken group = stream.expectGroup(GroupType.PAREN);
        Pattern inner = parse(group.getSubtokens(), group.getGroupType());
        String separator = null;
        Token next = stream.peek();

        if (next != null) {
            switch (next.getContent()) {
                case "*", "+", "?" -> {}
                default -> separator = stream.advance().getContent();
            }
        }

        Token repSpecifier = stream.advance();
        RepetitionType type = RepetitionType.resolve(repSpecifier.getContent());

        return new RepetitionPattern(inner.getNodes(), type, separator);
    }

}
