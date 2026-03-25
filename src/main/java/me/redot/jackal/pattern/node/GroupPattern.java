package me.redot.jackal.pattern.node;

import lombok.Data;
import me.redot.jackal.pattern.PatternMatch;
import me.redot.jackal.token.Token;
import me.redot.jackal.token.TokenStream;
import me.redot.jackal.token.type.GroupToken;
import me.redot.jackal.token.type.GroupType;

import java.util.List;

@Data
public class GroupPattern implements PatternNode {

    private final GroupType groupType;
    private final List<PatternNode> children;

    @Override
    public boolean conforms(TokenStream stream, PatternMatch match) {
        Token token = stream.peek();

        if (!(token instanceof GroupToken group) || group.getGroupType() != this.groupType) {
            return false;
        }

        TokenStream inner = new TokenStream(group.getSubtokens());

        for (PatternNode node : this.children) {
            boolean result = node.conforms(inner, match);
            if (!result) {
                return false;
            }
        }

        if (inner.hasNext()) {
            while (inner.hasNext()) inner.advance();
            return false;
        }

        stream.advance();
        return true;
    }

}
