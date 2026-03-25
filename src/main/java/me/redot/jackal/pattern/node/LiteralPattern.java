package me.redot.jackal.pattern.node;

import lombok.Data;
import me.redot.jackal.pattern.PatternMatch;
import me.redot.jackal.token.Token;
import me.redot.jackal.token.TokenStream;

@Data
public class LiteralPattern implements PatternNode {

    private final String text;

    @Override
    public boolean conforms(TokenStream stream, PatternMatch match) {
        Token token = stream.peek();

        if (token == null || !token.is(this.text)) {
            return false;
        }

        stream.advance();
        return true;
    }

}
