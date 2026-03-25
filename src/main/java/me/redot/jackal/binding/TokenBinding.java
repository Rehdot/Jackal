package me.redot.jackal.binding;

import lombok.Data;
import me.redot.jackal.token.Token;

import java.util.List;

@Data
public class TokenBinding implements Binding {

    private final Token token;

    @Override
    public List<Token> getTokens() {
        return List.of(this.token);
    }

}
