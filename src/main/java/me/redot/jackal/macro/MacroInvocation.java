package me.redot.jackal.macro;

import lombok.Data;
import me.redot.jackal.pattern.PatternMatch;

@Data
public class MacroInvocation {

    private final Macro macro;
    private final PatternMatch match;
    private final int startIndex, endIndex;

}
