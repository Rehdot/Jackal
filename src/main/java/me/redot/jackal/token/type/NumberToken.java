package me.redot.jackal.token.type;

import me.redot.jackal.token.Token;

public class NumberToken extends Token {

    public NumberToken(String content) {
        super(content);
    }

    @Override
    public TokenType getType() {
        return TokenType.NUMBER;
    }

    @Override
    public Token copy() {
        return new NumberToken(this.content);
    }

}
