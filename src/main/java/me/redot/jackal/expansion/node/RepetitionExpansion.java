package me.redot.jackal.expansion.node;

import lombok.Data;
import me.redot.jackal.binding.Binding;
import me.redot.jackal.binding.SequenceBinding;
import me.redot.jackal.binding.TokenBinding;
import me.redot.jackal.pattern.PatternMatch;
import me.redot.jackal.pattern.RepetitionType;
import me.redot.jackal.token.Token;
import me.redot.jackal.token.type.SymbolToken;

import java.util.ArrayList;
import java.util.List;

@Data
public class RepetitionExpansion implements ExpansionNode {

    private final List<ExpansionNode> nodes;
    private final String name;
    private final String separator;
    private final RepetitionType type;

    @Override
    public List<Token> expand(PatternMatch match) {
        Binding binding = match.getBinding(this.name);
        if (binding == null) return List.of();

        if (!(binding instanceof SequenceBinding sequence)) {
            throw new IllegalStateException(
                    "Variable '" + this.name + "' is not a sequence binding."
            );
        }

        int count = sequence.getElements().size();
        List<Token> output = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            PatternMatch single = new PatternMatch(match.getMacroRule());

            // inherit everything from outer scope
            for (var entry : match.getBindings().entrySet()) {
                single.getBindings().put(entry.getKey(), entry.getValue());
            }

            // bind ALL sequence variables at index i, not just the driven one
            for (var entry : match.getBindings().entrySet()) {
                String key = entry.getKey();
                Binding value = entry.getValue();

                if (value instanceof SequenceBinding sequenceBinding) {
                    if (i < sequenceBinding.getElements().size()) {
                        Binding element = sequenceBinding.getElements().get(i);

                        // could be a TokenBinding or a nested SequenceBinding
                        if (element instanceof TokenBinding tb) {
                            single.bindToken(key, tb.getToken());
                        } else if (element instanceof SequenceBinding nested) {
                            single.bindSequence(key, nested.getElements());
                        }
                    }
                }
                // non-sequence bindings are passed through as-is so inner expansions can reference them
                else if (value instanceof TokenBinding tokenBinding) {
                    single.bindToken(key, tokenBinding.getToken());
                }
            }

            for (ExpansionNode node : this.nodes) {
                output.addAll(node.expand(single));
            }

            if (this.separator != null && i < count - 1) {
                output.add(new SymbolToken(this.separator));
            }
        }

        return output;
    }

}
