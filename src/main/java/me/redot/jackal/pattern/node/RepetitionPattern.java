package me.redot.jackal.pattern.node;

import lombok.Data;
import me.redot.jackal.binding.Binding;
import me.redot.jackal.pattern.PatternMatch;
import me.redot.jackal.pattern.RepetitionType;
import me.redot.jackal.token.TokenStream;

import java.util.*;

@Data
public class RepetitionPattern implements PatternNode {

    private final List<PatternNode> pattern;
    private final RepetitionType type;
    private final String separator;
    private PatternNode lookahead;

    @Override
    public boolean conforms(TokenStream stream, PatternMatch match) {
        Map<String, List<Binding>> bindings = new LinkedHashMap<>();
        int count = 0;

        // initializing all variables here with empty lists
        // makes it so zero-match repetitions still have an entry
        for (String name : this.collectVariableNames(this.pattern)) {
            bindings.put(name, new ArrayList<>());
        }

        while (true) {
            int start = stream.position();
            PatternMatch iterMatch = new PatternMatch(match.getMacroRule());

            // ensures we don't consume explicitly specified tokens
            if (this.lookahead != null) {
                PatternMatch probe = new PatternMatch(match.getMacroRule());
                int pos = stream.position();

                if (this.lookahead.conforms(stream, probe)) {
                    stream.reset(pos);
                    break;
                }
                stream.reset(pos);
            }

            if (!this.matchPatternSequence(stream, iterMatch)) {
                stream.reset(start);
                break;
            }
            if (stream.position() == start) break;

            count++;

            for (var entry : iterMatch.getBindings().entrySet()) {
                bindings.computeIfAbsent(entry.getKey(), k -> new ArrayList<>())
                        .add(entry.getValue());
            }

            if (this.type == RepetitionType.ZERO_OR_ONE) break;

            if (this.separator != null) {
                if (stream.peek() == null || !stream.peek().is(this.separator)) break;
                stream.advance();
            }
        }

        if (this.type == RepetitionType.ONE_OR_MORE && count == 0) return false;

        // write sequence bindings into match
        for (var entry : bindings.entrySet()) {
            match.bindSequence(entry.getKey(), entry.getValue());
        }

        return true;
    }

    private Set<String> collectVariableNames(List<PatternNode> nodes) {
        Set<String> names = new LinkedHashSet<>();

        for (PatternNode node : nodes) {
            if (node instanceof VariablePattern variable) {
                names.add(variable.getName());
            } else if (node instanceof GroupPattern group) {
                names.addAll(this.collectVariableNames(group.getChildren()));
            } else if (node instanceof RepetitionPattern repetition) {
                names.addAll(this.collectVariableNames(repetition.getPattern()));
            }
        }

        return names;
    }

    private boolean matchPatternSequence(TokenStream stream, PatternMatch match) {
        int start = stream.position();

        for (PatternNode node : this.pattern) {
            if (!node.conforms(stream, match)) {
                stream.reset(start);
                return false;
            }
        }

        return true;
    }

}
