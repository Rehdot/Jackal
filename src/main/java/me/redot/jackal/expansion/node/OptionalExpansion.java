package me.redot.jackal.expansion.node;

import lombok.Data;
import me.redot.jackal.binding.Binding;
import me.redot.jackal.binding.SequenceBinding;
import me.redot.jackal.pattern.PatternMatch;
import me.redot.jackal.token.Token;

import java.util.ArrayList;
import java.util.List;

/// An expansion node which only expands if its 'guardian' variable is present.
/// For ensuring ZERO_OR_ONE patterns coming before repetitions are only
/// ever emitted when something else is there to hold its hand.
@Data
public class OptionalExpansion implements ExpansionNode {

    private final List<ExpansionNode> nodes;
    private final String guardian;

    @Override
    public List<Token> expand(PatternMatch match) {
        Binding binding = match.getBinding(this.guardian);

        if (binding instanceof SequenceBinding seq && seq.getElements().isEmpty() || binding == null) {
            return List.of();
        }

        List<Token> output = new ArrayList<>();

        for (ExpansionNode node : this.nodes) {
            output.addAll(node.expand(match));
        }
        return output;
    }

}
