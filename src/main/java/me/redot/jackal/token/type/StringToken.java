package me.redot.jackal.token.type;

import me.redot.jackal.token.Token;

public class StringToken extends Token {

    public StringToken(String content) {
        super(content);
    }

    @Override
    public TokenType getType() {
        return TokenType.STRING;
    }

    @Override
    public Token copy() {
        return new StringToken(this.content);
    }

}
