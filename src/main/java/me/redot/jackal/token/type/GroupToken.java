package me.redot.jackal.token.type;

import lombok.Getter;
import me.redot.jackal.token.Token;

import java.util.ArrayList;
import java.util.List;

@Getter
public class GroupToken extends Token {

    private final GroupType groupType;
    private final List<Token> subtokens;

    public GroupToken(GroupType groupType, List<Token> subtokens) {
        super("");
        this.groupType = groupType;
        this.subtokens = subtokens;
    }

    @Override
    public TokenType getType() {
        return TokenType.GROUP;
    }

    @Override
    public Token copy() {
        List<Token> copied = new ArrayList<>();

        for (Token sub : this.getSubtokens()) {
            copied.add(sub.copy());
        }

        return new GroupToken(this.groupType, copied);
    }

    // this was easier to do recursively here
    @Override
    public String getContent() {
        StringBuilder inner = new StringBuilder()
                .append(this.groupType.getOpen());

        for (Token subtoken : this.subtokens) {
            inner.append(subtoken.getContent()).append(" ");
        }

        return inner.append(this.groupType.getClose()).toString();
    }

}
