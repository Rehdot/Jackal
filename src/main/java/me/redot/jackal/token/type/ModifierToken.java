package me.redot.jackal.token.type;

import lombok.Getter;
import me.redot.jackal.token.Token;
import me.redot.jackal.token.TokenModifier;

import java.util.ArrayList;
import java.util.List;

@Getter
public class ModifierToken extends Token {

    private final List<TokenModifier> modifiers;

    public ModifierToken(List<TokenModifier> modifiers) {
        super("");
        this.modifiers = modifiers;
    }

    public boolean hasModifier(TokenModifier modifier) {
        return this.modifiers.contains(modifier);
    }

    @Override
    public TokenType getType() {
        return TokenType.MODIFIER;
    }

    @Override
    public Token copy() {
        return new ModifierToken(new ArrayList<>(this.modifiers));
    }

}
