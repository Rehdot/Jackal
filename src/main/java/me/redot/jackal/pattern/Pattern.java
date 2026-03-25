package me.redot.jackal.pattern;

import lombok.Data;
import me.redot.jackal.pattern.node.PatternNode;
import me.redot.jackal.token.type.GroupType;

import java.util.ArrayList;
import java.util.List;

@Data
public class Pattern {

    private final GroupType groupType;
    private final List<PatternNode> nodes = new ArrayList<>();

    public void addNode(PatternNode node) {
        this.nodes.add(node);
    }

}
