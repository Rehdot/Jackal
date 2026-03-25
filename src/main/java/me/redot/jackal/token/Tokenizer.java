package me.redot.jackal.token;

import java.util.List;

public interface Tokenizer {

    List<Token> tokenize(String source);

}
