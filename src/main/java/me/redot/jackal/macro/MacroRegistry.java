package me.redot.jackal.macro;

import lombok.Data;
import me.redot.jackal.token.Token;
import me.redot.jackal.token.TokenStream;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class MacroRegistry {

    private final Map<String, Macro> macros = new HashMap<>();

    private MacroRegistry() {}

    public Macro getMacro(String name) {
        return this.macros.get(name);
    }

    public void addMacro(Macro macro) {
        String name = macro.getName();
        Macro existing = this.macros.get(name);

        if (existing != null) {
            existing.addRules(macro.getRules());
        } else {
            this.macros.put(name, macro);
        }
    }

    public boolean hasMacro(Macro macro) {
        return this.hasMacro(macro.getName());
    }

    public boolean hasMacro(String name) {
        return this.macros.containsKey(name);
    }

    public static MacroRegistry from(List<Token> jfTokens) {
        MacroRegistry registry = new MacroRegistry();
        TokenStream stream = new TokenStream(jfTokens);

        while (stream.hasNext()) {
            Token token = stream.peek();

            if (!token.is("macro")) {
                stream.advance();
                continue;
            }

            Macro macro = MacroParser.parse(stream);
            registry.addMacro(macro);
        }

        return registry;
    }

}
