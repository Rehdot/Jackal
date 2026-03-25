package me.redot.jackal.token;

import me.redot.jackal.token.type.GroupToken;
import me.redot.jackal.token.type.GroupType;
import me.redot.jackal.token.type.TokenType;

import java.util.List;

public class TokenStream {

    private final List<Token> tokens;
    private int index = 0;

    public TokenStream(List<Token> tokens) {
        this.tokens = tokens;
    }

    public Token peek() {
        return this.index < this.tokens.size()
                ? this.tokens.get(this.index)
                : null;
    }

    public Token peek(int offset) {
        int pos = this.index + offset;
        return pos < this.tokens.size()
                ? this.tokens.get(pos)
                : null;
    }

    public Token advance() {
        return this.index < this.tokens.size()
                ? this.tokens.get(this.index++)
                : null;
    }

    public boolean hasNext() {
        return this.index < this.tokens.size();
    }

    public int position() {
        return this.index;
    }

    public void reset(int position) {
        this.index = position;
    }

    public Token expect(String text) {
        Token token = this.advance();

        if (!token.is(text)) {
            throw new IllegalStateException("Expected '" + text + "', got '" + text + "'");
        }

        return token;
    }

    public Token expectType(TokenType type) {
        Token token = this.advance();

        if (token == null || token.getType() != type) {
            throw new IllegalStateException("Expected token type '" + type + "', got " + token);
        }

        return token;
    }

    public GroupToken expectGroup(GroupType type) {
        Token token = this.advance();

        if (!(token instanceof GroupToken group)) {
            throw new IllegalStateException("Expected group '" + type + "', got " + token);
        }

        if (group.getGroupType() != type) {
            throw new IllegalStateException(
                    "Expected group type '" + type + "', got '" + group.getGroupType() + "'"
            );
        }

        return group;
    }

}
