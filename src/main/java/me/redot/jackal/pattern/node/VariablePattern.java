package me.redot.jackal.pattern.node;

import lombok.Data;
import me.redot.jackal.binding.Binding;
import me.redot.jackal.binding.TokenBinding;
import me.redot.jackal.pattern.MatcherType;
import me.redot.jackal.pattern.PatternMatch;
import me.redot.jackal.token.Token;
import me.redot.jackal.token.TokenStream;
import me.redot.jackal.token.type.GroupToken;
import me.redot.jackal.token.type.GroupType;
import me.redot.jackal.token.type.TokenType;
import me.redot.jackal.util.Constants;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class VariablePattern implements PatternNode {

    private final String name;
    private final MatcherType matcher;

    @Override
    public boolean conforms(TokenStream stream, PatternMatch match) {
        Token token = stream.peek();
        if (token == null) return false;

        switch (this.matcher) {
            case IDENT -> {
                if (token.getType() != TokenType.IDENT) return false;
                match.bindToken(this.name, token.copy());
                stream.advance();
                return true;
            }
            case TT -> {
                match.bindToken(this.name, token.copy());
                stream.advance();
                return true;
            }
            case BLOCK -> {
                if (!(token instanceof GroupToken g)
                        || g.getGroupType() != GroupType.BRACE) return false;
                match.bindToken(this.name, token.copy());
                stream.advance();
                return true;
            }
            case EXPR -> {
                List<Token> captured = new ArrayList<>();

                while (stream.peek() != null
                        && !stream.peek().is(",") && !stream.peek().is(";")) {
                    captured.add(stream.advance());
                }

                if (captured.isEmpty()) return false;
                List<Binding> elements = captured.stream()
                        .map(TokenBinding::new)
                        .collect(Collectors.toList());
                match.bindSequence(this.name, elements);
                return true;
            }
            case MOD -> {
                if (token.getType() != TokenType.IDENT
                        || !Constants.MODIFIERS.contains(token.getContent())) {
                    return false;
                }

                match.bindToken(this.name, token.copy());
                stream.advance();
                return true;
            }
        }

        return false;
    }

}
