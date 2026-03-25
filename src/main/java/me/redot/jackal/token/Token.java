package me.redot.jackal.token;

import lombok.Data;
import me.redot.jackal.token.type.TokenType;

@Data
public abstract class Token {

    protected final String content;

    public abstract TokenType getType();

    public abstract Token copy();

    public boolean is(String value) {
        return this.content.equals(value);
    }

}
