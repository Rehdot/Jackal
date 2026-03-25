package me.redot.jackal.expansion.node;

import lombok.Data;
import me.redot.jackal.binding.Binding;
import me.redot.jackal.pattern.PatternMatch;
import me.redot.jackal.token.Token;

import java.util.List;

@Data
public class VariableExpansion implements ExpansionNode {

    private final String name;

    @Override
    public List<Token> expand(PatternMatch match) {
        Binding binding = match.getBinding(this.name);

        if (binding == null) {
            throw new IllegalStateException("Failed to resolve variable '" + this.name + "'.");
        }

        return binding.getTokens();
    }

}
