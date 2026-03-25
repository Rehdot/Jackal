package me.redot.jackal.token;

import me.redot.jackal.token.type.*;
import me.redot.jackal.util.Constants;

import java.util.ArrayList;
import java.util.List;

public class JackalTokenizer implements Tokenizer {

    private String source;
    private int pos;
    private int length;

    @Override
    public List<Token> tokenize(String source) {
        this.source = source;
        this.pos = 0;
        this.length = source.length();

        return this.readTokensUntil(null);
    }

    private List<Token> readTokensUntil(Character closingChar) {
        List<Token> tokens = new ArrayList<>();

        while (this.pos < this.length) {
            char c = this.source.charAt(this.pos);

            if (closingChar != null && c == closingChar) {
                this.pos++; // skip closing char
                return tokens;
            }

            if (Character.isWhitespace(c)) {
                this.pos++;
                continue;
            }

            if (c == '/' && this.pos + 1 < this.length) {
                char next = this.source.charAt(this.pos + 1);

                switch (next) { // never tokenize commentation
                    case '/' -> {
                        while (this.pos < this.length
                                && this.source.charAt(this.pos) != '\n') {
                            this.pos++;
                        }
                        continue;
                    }
                    case '*' -> {
                        this.pos += 2;
                        while (this.pos + 1 < this.length) {
                            if (this.source.charAt(this.pos) == '*'
                                    && this.source.charAt(this.pos + 1) == '/') {
                                this.pos += 2;
                                break;
                            }
                            this.pos++;
                        }
                        continue;
                    }
                    default -> {}
                }
            }

            if (c == '"') {
                tokens.add(this.readJavaString());
                continue;
            }

            GroupType groupType = GroupType.fromOpenChar(c);
            if (groupType != null) {
                this.pos++;
                List<Token> subtokens = this.readTokensUntil(groupType.getClose().charAt(0));
                tokens.add(new GroupToken(groupType, subtokens));
                continue;
            }

            if (Character.isJavaIdentifierStart(c)) {
                tokens.add(this.readIdent());
                continue;
            }

            if (Character.isDigit(c)) {
                tokens.add(this.readNumber());
                continue;
            }

            tokens.add(this.readSymbol());
        }

        if (closingChar != null) {
            throw new IllegalStateException("Expected closing character: " + closingChar);
        }

        return tokens;
    }

    private SymbolToken readSymbol() {
        for (String sym : Constants.SYMBOLS) {
            if (this.source.startsWith(sym, this.pos)) {
                this.pos += sym.length();
                return new SymbolToken(sym);
            }
        }

        // single char
        char c = this.source.charAt(this.pos);
        this.pos++;
        return new SymbolToken(String.valueOf(c));
    }

    private StringToken readJavaString() {
        int start = this.pos;
        boolean escaped = false;
        this.pos++; // opening quote

        while (this.pos < this.length) {
            char c = this.source.charAt(this.pos);
            if (escaped) {
                escaped = false;
            } else if (c == '\\') {
                escaped = true;
            } else if (c == '"') {
                this.pos++; // closing quote
                break;
            }
            this.pos++;
        }

        return new StringToken(this.source.substring(start, this.pos));
    }

    private IdentToken readIdent() {
        int start = this.pos;
        do {
            this.pos++;
        } while (this.pos < this.length && Character.isJavaIdentifierPart(this.source.charAt(this.pos)));
        return new IdentToken(this.source.substring(start, this.pos));
    }

    private NumberToken readNumber() {
        int start = this.pos;
        do {
            this.pos++;
        } while (this.pos < this.length && Character.isDigit(this.source.charAt(this.pos)));
        return new NumberToken(this.source.substring(start, this.pos));
    }

}
