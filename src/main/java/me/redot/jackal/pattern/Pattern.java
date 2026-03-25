package me.redot.jackal.pattern;

import lombok.Data;
import me.redot.jackal.pattern.node.PatternNode;

import java.util.ArrayList;
import java.util.List;

@Data
public class Pattern {

    private final List<PatternNode> nodes = new ArrayList<>();

    public void addNode(PatternNode node) {
        this.nodes.add(node);
    }

}
