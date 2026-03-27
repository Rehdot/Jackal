package me.redot.jackal;

import lombok.AccessLevel;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import me.redot.jackal.macro.MacroMatcher;
import me.redot.jackal.macro.MacroRegistry;
import me.redot.jackal.rules.Ruleset;
import me.redot.jackal.token.JackalTokenizer;
import me.redot.jackal.token.Token;
import me.redot.jackal.token.Tokenizer;
import me.redot.jackal.util.Expander;
import me.redot.jackal.util.Reconstructor;

import java.io.File;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class JackalSystem {

    private static JackalSystem INSTANCE;

    public static final String INVOKE_SPECIFIER = "~";
    public static final String VARIABLE_SPECIFIER = "&";

    private final MacroRegistry macroRegistry;
    private final Tokenizer tokenizer;
    private final Ruleset ruleset;
    private final MacroMatcher macroMatcher;

    private JackalSystem(String jackalFile) {
        this.tokenizer = new JackalTokenizer();

        List<Token> jfTokens = this.tokenizer.tokenize(jackalFile);

        this.macroRegistry = MacroRegistry.from(jfTokens);
        this.ruleset = Ruleset.from(jfTokens);
        this.macroMatcher = new MacroMatcher(this.macroRegistry);

        INSTANCE = this;
    }

    public String expand(String source) {
        int index = source.indexOf(INVOKE_SPECIFIER);

        if (index < 0) {
            return source;
        }

        List<Token> tokens = this.tokenizer.tokenize(source);
        Set<String> imports = new HashSet<>();
        List<Token> expanded = Expander.expandTokens(tokens, imports, this.macroMatcher);

        return Reconstructor.toSource(expanded, imports);
    }

    @SneakyThrows
    public String expand(File file) {
        return this.expand(Files.readString(file.toPath()));
    }

    @SneakyThrows
    public static JackalSystem fromJackalFile(File file) {
        return new JackalSystem(Files.readString(file.toPath()));
    }

    public static JackalSystem fromJackalFile(String jackalFile) {
        return new JackalSystem(jackalFile);
    }

    public static JackalSystem getInstance() {
        if (INSTANCE == null) {
            throw new IllegalStateException("No JackalSystem exists.");
        }

        return INSTANCE;
    }

}