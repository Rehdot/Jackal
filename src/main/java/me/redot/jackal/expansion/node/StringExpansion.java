package me.redot.jackal.expansion.node;

import lombok.Data;
import me.redot.jackal.binding.Binding;
import me.redot.jackal.binding.TokenBinding;
import me.redot.jackal.pattern.PatternMatch;
import me.redot.jackal.token.Token;
import me.redot.jackal.token.type.StringToken;

import java.util.List;

@Data
public class StringExpansion implements ExpansionNode {

    /// raw string content including quotes
    private final String template;

    @Override
    public List<Token> expand(PatternMatch match) {
        String content = this.template.substring(1, this.template.length() - 1);
        StringBuilder result = new StringBuilder();
        int i = 0;

        while (i < content.length()) {
            if (content.charAt(i) == '&') {
                int start = i + 1;
                int end = start;

                while (end < content.length()) {
                    char ch = content.charAt(end);
                    int ahead = end + 1;

                    if (ch == '&' || !Character.isJavaIdentifierPart(ch)
                            || ch == '_' && ahead < content.length()
                            && content.charAt(ahead) == '&') {
                        break;
                    }

                    end++;
                }

                String varName = content.substring(start, end);
                Binding binding = match.getBinding(varName);

                if (binding instanceof TokenBinding tb) {
                    result.append(tb.getToken().getContent());
                } else {
                    result.append("&").append(varName);
                }

                i = end;
            } else {
                result.append(content.charAt(i));
                i++;
            }
        }

        return List.of(new StringToken("\"" + result + "\""));
    }

}
