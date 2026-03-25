package me.redot.jackal.pattern;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.redot.jackal.binding.Binding;
import me.redot.jackal.binding.SequenceBinding;
import me.redot.jackal.binding.TokenBinding;
import me.redot.jackal.macro.MacroRule;
import me.redot.jackal.token.Token;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@RequiredArgsConstructor
public class PatternMatch {

    private final MacroRule macroRule;
    private final Map<String, Binding> bindings = new HashMap<>();

    public void bindToken(String name, Token token) {
        this.bindings.put(name, new TokenBinding(token));
    }

    public void bindSequence(String name, List<Binding> elements) {
        this.bindings.put(name, new SequenceBinding(elements));
    }

    public Binding getBinding(String name) {
        return this.bindings.get(name);
    }

    public List<Token> expand() {
        return this.macroRule.getExpansion().expand(this);
    }

}