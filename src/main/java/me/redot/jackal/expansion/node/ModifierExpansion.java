package me.redot.jackal.expansion.node;

import lombok.Data;
import me.redot.jackal.pattern.PatternMatch;
import me.redot.jackal.token.Token;
import me.redot.jackal.token.TokenModifier;
import me.redot.jackal.token.type.ModifierToken;

import java.util.List;

@Data
public class ModifierExpansion implements ExpansionNode {

    private final List<TokenModifier> modifiers;

    @Override
    public List<Token> expand(PatternMatch match) {
        return List.of(new ModifierToken(this.modifiers));
    }

}
