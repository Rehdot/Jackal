package me.redot.jackal.expansion.node;

import lombok.Data;
import me.redot.jackal.pattern.PatternMatch;
import me.redot.jackal.token.Token;
import me.redot.jackal.token.type.CharToken;

import java.util.List;

@Data
public class CharExpansion implements ExpansionNode {

    /// a char literal like 'a' or '\n' including apostrophe
    private final CharToken token;

    @Override
    public List<Token> expand(PatternMatch match) {
        return List.of(this.token);
    }

}
