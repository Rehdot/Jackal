package me.redot.jackal.macro;

import lombok.RequiredArgsConstructor;
import me.redot.jackal.JackalSystem;
import me.redot.jackal.pattern.PatternMatch;
import me.redot.jackal.pattern.node.PatternNode;
import me.redot.jackal.token.Token;
import me.redot.jackal.token.TokenStream;
import me.redot.jackal.token.type.GroupToken;
import me.redot.jackal.token.type.GroupType;
import me.redot.jackal.token.type.TokenType;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class MacroMatcher {

    private final MacroRegistry registry;

    public List<MacroInvocation> findMacroInvocations(List<Token> tokens) {
        TokenStream stream = new TokenStream(tokens);
        List<MacroInvocation> invocations = new ArrayList<>();

        while (stream.hasNext()) {
            Token token = stream.peek();

            if (!token.is(JackalSystem.INVOKE_SPECIFIER)) {
                stream.advance();
                continue;
            }

            int startIndex = stream.position();
            stream.advance();

            if (!stream.hasNext()) {
                break;
            }

            Token nameToken = stream.peek();

            if (nameToken.getType() != TokenType.IDENT) {
                stream.advance();
                continue;
            }
            if (!this.registry.hasMacro(nameToken.getContent())) {
                throw new IllegalStateException("Failed to resolve macro '" + nameToken.getContent() + "'.");
            }

            stream.advance();

            Macro macro = this.registry.getMacro(nameToken.getContent());
            PatternMatch match = this.matchMacroPattern(macro, stream);

            int endIndex = stream.position() - 1;
            invocations.add(
                    new MacroInvocation(macro, match, startIndex, endIndex)
            );
        }

        return invocations;
    }

    public PatternMatch matchMacroPattern(Macro macro, TokenStream stream) {
        if (!(stream.peek() instanceof GroupToken groupToken)) {
            throw new IllegalStateException("Expected group for macro pattern, got " + stream.peek().getType());
        }

        GroupType groupType = groupToken.getGroupType();

        Outer:
        for (MacroRule rule : macro.getRules()) { // check every rule in the macro,
            if (rule.getPattern().getGroupType() != groupType) {
                // short circuit if group types aren't the same;
                // enforces the macro pattern and speeds up matching
                continue;
            }

            PatternMatch match = new PatternMatch(rule);
            int pos = stream.position();

            // if this TokenStream conforms to EVERY node in the pattern declaration,
            for (PatternNode node : rule.getPattern().getNodes()) {
                boolean result = node.conforms(stream, match);

                if (!result) {
                    stream.reset(pos);
                    continue Outer;
                }
            }

            return match; // select it as the pattern match.
        }

        throw new IllegalStateException("TokenStream does not conform to any rule for macro '"+macro.getName()+"'");
    }

}
