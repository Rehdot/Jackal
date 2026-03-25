package me.redot.jackal.macro;

import lombok.Data;
import me.redot.jackal.expansion.Expansion;
import me.redot.jackal.pattern.Pattern;

@Data
public class MacroRule {

    private final Pattern pattern;
    private final Expansion expansion;

}
