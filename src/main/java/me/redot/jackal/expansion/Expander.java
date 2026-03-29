package me.redot.jackal.expansion;

import lombok.Data;
import me.redot.jackal.macro.MacroInvocation;
import me.redot.jackal.macro.MacroMatcher;
import me.redot.jackal.token.Token;
import me.redot.jackal.token.type.GroupToken;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Data
public class Expander {

    private final MacroMatcher matcher;

    public List<Token> expandTokens(List<Token> tokens, Set<String> importAccumulator) {
        for (Token token : tokens) {
            if (token instanceof GroupToken group) { // expand innermost macros first
                List<Token> subtokens = group.getSubtokens();
                List<Token> expanded = this.expandTokens(copy(subtokens), importAccumulator);

                subtokens.clear();
                subtokens.addAll(expanded);
            }
        }

        // find invocations
        List<MacroInvocation> invocations = this.matcher.findMacroInvocations(tokens);
        if (invocations.isEmpty()) {
            return tokens;
        }

        // replace invocations
        for (int i = invocations.size() - 1; i >= 0; i--) {
            MacroInvocation inv = invocations.get(i);
            List<Token> expansion = inv.getMatch().expand();

            tokens.subList(inv.getStartIndex(), inv.getEndIndex() + 1)
                    .clear();
            tokens.addAll(inv.getStartIndex(), expansion);
            importAccumulator.addAll(inv.getMacro().getImports());
        }

        // recurse until no more invocations exist
        return expandTokens(tokens, importAccumulator);
    }

    private static List<Token> copy(List<Token> list) {
        List<Token> copy = new ArrayList<>();

        for (Token token : list) {
            copy.add(token.copy());
        }

        return copy;
    }

}
