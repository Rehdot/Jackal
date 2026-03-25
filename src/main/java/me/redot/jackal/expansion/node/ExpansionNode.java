package me.redot.jackal.expansion.node;

import me.redot.jackal.pattern.PatternMatch;
import me.redot.jackal.token.Token;

import java.util.List;

public interface ExpansionNode {

    List<Token> expand(PatternMatch match);

}
