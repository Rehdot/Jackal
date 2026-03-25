package me.redot.jackal.pattern.node;

import me.redot.jackal.pattern.PatternMatch;
import me.redot.jackal.token.TokenStream;

public interface PatternNode {

    boolean conforms(TokenStream stream, PatternMatch match);

}
