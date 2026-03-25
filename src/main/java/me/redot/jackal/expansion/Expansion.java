package me.redot.jackal.expansion;

import lombok.Data;
import me.redot.jackal.expansion.node.ExpansionNode;
import me.redot.jackal.pattern.PatternMatch;
import me.redot.jackal.token.Token;

import java.util.ArrayList;
import java.util.List;

@Data
public class Expansion {

    private final List<ExpansionNode> nodes = new ArrayList<>();

    public void addNode(ExpansionNode node) {
        this.nodes.add(node);
    }

    public List<Token> expand(PatternMatch match) {
        List<Token> output = new ArrayList<>();

        for (ExpansionNode node : this.nodes) {
            output.addAll(node.expand(match));
        }

        return output;
    }

}
