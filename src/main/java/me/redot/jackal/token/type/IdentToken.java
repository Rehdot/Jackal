package me.redot.jackal.token.type;

import me.redot.jackal.token.Token;

public class IdentToken extends Token {

    public IdentToken(String content) {
        super(content);
    }

    @Override
    public TokenType getType() {
        return TokenType.IDENT;
    }

    @Override
    public Token copy() {
        return new IdentToken(this.content);
    }

}
