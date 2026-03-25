package me.redot.jackal.expansion.node;

import lombok.Data;
import me.redot.jackal.pattern.PatternMatch;
import me.redot.jackal.token.Token;
import me.redot.jackal.token.type.GroupToken;
import me.redot.jackal.token.type.GroupType;

import java.util.ArrayList;
import java.util.List;

@Data
public class GroupExpansion implements ExpansionNode {

    private final GroupType groupType;
    private final List<ExpansionNode> nodes;

    public void addNode(ExpansionNode node) {
        this.nodes.add(node);
    }

    @Override
    public List<Token> expand(PatternMatch match) {
        List<Token> inner = new ArrayList<>();

        for (ExpansionNode node : this.nodes) {
            inner.addAll(node.expand(match));
        }

        if (inner.size() == 1 && inner.get(0) instanceof GroupToken) {
            return inner; // avoids double-wrapping
        }

        return List.of(new GroupToken(this.groupType, inner));
    }

}
