package me.redot.jackal.binding;

import lombok.Data;
import me.redot.jackal.token.Token;

import java.util.ArrayList;
import java.util.List;

@Data
public class SequenceBinding implements Binding {

    private final List<Binding> elements;

    @Override
    public List<Token> getTokens() {
        List<Token> tokens = new ArrayList<>();

        for (Binding binding : this.elements) {
            tokens.addAll(binding.getTokens());
        }

        return tokens;
    }

}
