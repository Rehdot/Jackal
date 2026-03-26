package me.redot.jackal.token.type;

import me.redot.jackal.token.Token;

public class CharToken extends Token {

    public CharToken(String content) {
        super(content);
    }

    @Override
    public TokenType getType() {
        return TokenType.CHAR;
    }

    @Override
    public Token copy() {
        return new CharToken(this.content);
    }

}
