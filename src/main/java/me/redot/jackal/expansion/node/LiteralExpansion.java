package me.redot.jackal.expansion.node;

import lombok.Data;
import me.redot.jackal.pattern.PatternMatch;
import me.redot.jackal.token.Token;

import java.util.List;

@Data
public class LiteralExpansion implements ExpansionNode {

    private final Token token;

    @Override
    public List<Token> expand(PatternMatch match) {
        return List.of(this.token);
    }

}
