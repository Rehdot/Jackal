package me.redot.jackal.rules;

import lombok.Data;
import me.redot.jackal.token.Token;

import java.util.List;

@Data
public class Ruleset {

    // TODO: We will probably need this eventually.
    private Ruleset() { }

    public static Ruleset from(List<Token> jfTokens) {
        Ruleset ruleset = new Ruleset();

        // TODO: parse jackalFile for rules

        return ruleset;
    }

}
