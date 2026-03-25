package me.redot.jackal.macro;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.*;

@Data
@RequiredArgsConstructor
public class Macro {

    private final String name;
    private final List<MacroRule> rules = new ArrayList<>();
    private final Set<String> imports = new HashSet<>();

    public void addRule(MacroRule rule) {
        this.rules.add(rule);
    }

    public void addRules(Collection<MacroRule> rule) {
        this.rules.addAll(rule);
    }

    public void addImport(String imp) {
        this.imports.add(imp);
    }

}
