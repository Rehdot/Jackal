package me.redot.jackal.macro;

import lombok.experimental.UtilityClass;
import me.redot.jackal.expansion.Expansion;
import me.redot.jackal.expansion.ExpansionParser;
import me.redot.jackal.pattern.Pattern;
import me.redot.jackal.pattern.PatternParser;
import me.redot.jackal.token.Token;
import me.redot.jackal.token.TokenStream;
import me.redot.jackal.token.type.GroupToken;
import me.redot.jackal.token.type.GroupType;
import me.redot.jackal.token.type.IdentToken;
import me.redot.jackal.token.type.TokenType;

import java.util.List;

/// Parse logic for Jackal macro definitions
@UtilityClass
public class MacroParser {

    public static Macro parse(TokenStream stream) {
        stream.expect("macro");

        Token nameToken = stream.expectType(TokenType.IDENT);
        String name = nameToken.getContent();
        GroupToken body = stream.expectGroup(GroupType.BRACE);

        Macro macro = new Macro(name);

        parseMacroBody(macro, body.getSubtokens());
        return macro;
    }

    public static void parseMacroBody(Macro macro, List<Token> tokens) {
        TokenStream stream = new TokenStream(tokens);

        while (stream.hasNext()) {
            Token token = stream.peek();

            if (token.is("imports")) {
                parseImports(stream, macro);
                continue;
            }

            MacroRule rule = parseRule(stream);
            macro.addRule(rule);
        }
    }

    public static void parseImports(TokenStream stream, Macro macro) {
        stream.expect("imports");

        GroupToken group = stream.expectGroup(GroupType.BRACE);
        TokenStream inner = new TokenStream(group.getSubtokens());
        StringBuilder builder = new StringBuilder();

        while (inner.hasNext()) {
            Token token = inner.advance();

            if (token.is(";")) {
                if (!builder.isEmpty()) {
                    macro.addImport(builder.toString());
                    builder.setLength(0);
                }
                continue;
            }

            if (token instanceof IdentToken || token.is(".")) {
                builder.append(token.getContent());
            }
        }

        if (!builder.isEmpty()) {
            macro.addImport(builder.toString());
        }
    }

    public static MacroRule parseRule(TokenStream stream) {
        GroupToken patternGroup = (GroupToken) stream.expectType(TokenType.GROUP);
        stream.expect("->");
        GroupToken expansionGroup = stream.expectGroup(GroupType.BRACE);

        Pattern pattern = PatternParser.parse(List.of(patternGroup), patternGroup.getGroupType());
        Expansion expansion = ExpansionParser.parse(expansionGroup.getSubtokens());

        return new MacroRule(pattern, expansion);
    }

}
