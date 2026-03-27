package me.redot.jackal.expansion.node;

import lombok.Data;
import me.redot.jackal.JackalSystem;
import me.redot.jackal.expansion.Expansion;
import me.redot.jackal.expansion.ExpansionParser;
import me.redot.jackal.pattern.PatternMatch;
import me.redot.jackal.token.Token;
import me.redot.jackal.token.type.StringToken;
import me.redot.jackal.util.Expander;
import me.redot.jackal.util.Reconstructor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static me.redot.jackal.JackalSystem.INVOKE_SPECIFIER;
import static me.redot.jackal.JackalSystem.VARIABLE_SPECIFIER;

@Data
public class StringExpansion implements ExpansionNode {

    private final StringToken stringToken;

    /// This expansion is kind-of a hack to our own system. In order to
    /// expand variables, repetitions and their inner variables, and
    /// also macros inside of strings, we need to get ahold of the
    /// encompassing JackalSystem.
    @Override
    public List<Token> expand(PatternMatch match) {
        String template = this.stringToken.getContent();

        // short-circuit so we don't need to expand inner macros and variables
        // most strings are going to be literals that we shouldn't expand anyway
        if (!template.contains(INVOKE_SPECIFIER) && !template.contains(VARIABLE_SPECIFIER)) {
            return List.of(this.stringToken.copy());
        }

        JackalSystem system = JackalSystem.getInstance();
        String content = template.substring(1, template.length() - 1);

        List<Token> tokens = system.getTokenizer().tokenize(content);

        // replace variable and repetition expansions inside of token list
        Expansion expansion = ExpansionParser.parse(tokens);
        List<Token> newTokens = new ArrayList<>();

        for (ExpansionNode node : expansion.getNodes()) {
            newTokens.addAll(node.expand(match));
        }

        // ...and then expand other patterns (macro invocations) inside the string
        List<Token> expandTokens = Expander.expandTokens(
                newTokens,
                new HashSet<>(), // not importing anything from macros expanded inside strings
                system.getMacroMatcher()
        );

        String result = Reconstructor.toSource(expandTokens, Set.of());
        return List.of(new StringToken("\"" + result + "\""));
    }

}
