package me.redot.jackal.token.type;

import me.redot.jackal.token.Token;

public class SymbolToken extends Token {

    public SymbolToken(String content) {
        super(content);
    }

    @Override
    public TokenType getType() {
        return TokenType.SYMBOL;
    }

    @Override
    public Token copy() {
        return new SymbolToken(this.content);
    }

}
