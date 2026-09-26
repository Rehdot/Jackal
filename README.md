## Jackal

### What is Jackal?
Jackal is a rust-inspired **macro programming language**, written for Java.
It operates on raw tokens to compose Java patterns that
the user wants to generate at compile-time.

---

### What Does This Enable?
Infinite compile-time metaprogramming patterns.
If there is a shorter way of representing some verbose
Java code, you can write a Jackal macro to 
have the compiler expand shorter code into more verbose, valid Java.

---

### Quick Info
- You can easily get started using Jackal, with the [Jackal Compiler Plugin](https://github.com/Rehdot/jackal-compiler-plugin).
- IntelliJ support for Jackal is still very limited and being worked on.

---

### Patterns
Jackal allows you to define what tokens you'd like to read and manipulate, 
and optionally bind variable names to them.
These definitions are called **patterns**, and they are defined inside 
any macro block before `-> { }`. 

This `() -> {}` group is called a **macro rule**.

```jackal
macro anyName {
    (/* Any pattern */) -> { }
    {/* Another pattern */} -> { }
    [/* Another pattern */] -> { }
}
```

The parenthesis, braces, or brackets, will determine how Jackal reads
a macro's **invocation** later on.

A macro's pattern can define any tokens it'd like to read.
It could be literal tokens:

```jackal
macro anyName {
    (public static void) -> { }
}
```

Or, it could read a token conforming to specific rules.
Jackal's recognized token rules are:
- **ident**: an identifier; tokens conforming to Java's naming patterns
- **expr**: an expression; any tokens until a comma or semicolon is read
- **tt**: a token tree; any kind of token
- **block**: any tokens surrounded by braces
- **mod**: any Java modifier keyword

You can write a binding inside a pattern using `&name:rule`, like:
```jackal
macro anyName {
    ( &type:ident ) -> { }
}
```
...This pattern would read **one** ident, such as:
- `Object`
- `int`
- `class`
- `$jackal`

**Sequence bindings** are how you tell Jackal to read multiple tokens.
Sequence bindings always have _repetition specifiers_, which describe 
how many tokens to read. Jackal's repetition specifiers are borrowed
from [regular expression quantifiers](https://en.wikipedia.org/wiki/Regular_expression#Quantification):
- `*` = Zero or more tokens ([Kleene star](https://en.wikipedia.org/wiki/Kleene_star))
- `+` = One or more tokens ([Kleene plus](https://en.wikipedia.org/wiki/Kleene_star#Kleene_plus))
- `?` = Zero or one tokens (optional)

Using these specifiers, you can write a sequence binding like:
- `&( word )*`: Read the token 'word' 0+ times
- `&( &token:tt )+`: Read ANY token 1+ times
- `&( &name:ident = )?`: Read a name and equals sign, if they exist

Sequence bindings can also be delimited by a single character, with
this kind of pattern:
- `&( hi ),*`: Read the token 'hi' and a comma, 0+ times.

---

### Expansions
Macro expansions define which tokens should be emitted, 
and are defined after the rule's `->`, always inside braces:
```jackal
macro anyName {
    () -> { /* Expansion definition */ }
}
```

Inside an expansion definition, you can access and unwrap the tokens that
you bound to names inside the rule's pattern. Take for example the following:

```jackal
macro println {
    ( &string:tt ) -> { 
        System.out.println( &string ); 
    }
}
```

And suddenly, you have a macro which would read any token,
such as a string literal, and reconstruct it into a print statement.

However, we can do better than this with a sequence binding:
```jackal
macro println {
    ( &( &token:tt )* ) -> { 
        System.out.println( &(&token)* ); 
    }
}
```
Note that we **must** wrap the `&token` reference in `&()*`, otherwise
we cannot access the sequence of tokens that it is bound to. This is
because of binding-depth conflicts; we must always access &token at its own
depth, or deeper.

With this macro, we now have the capability of expanding some tokens
like `"Hello, " + "World!"` into `System.out.println("Hello, " + "World!");`.

**Modifier tokens** are a built-in superpower for expansions. They give the macro
the opportunity to modify tokens right as they're being reconstructed into Java source.
A modifier token uses the pattern of `&[x,y,z]`, and will modify the token appearing directly
after it.

For example:
```jackal
macro upper {
    ( &name:ident ) -> {
        &[upper] &name
    }
}
```
Any ident fed into this macro would expand into its uppercase form.

A modifier token can take on as many modifiers as needed. The token
modifiers that Jackal expects are:
- `concat`: removes spacing between the previous and following tokens.
- `cap`: capitalizes the following token.
- `uncap`: un-capitalizes the following token.
- `upper`: uppercases the entire following token.
- `lower`: lowercases the entire following token.
- `newline`: puts a new line before the following token.
- `delete`: removes the following token entirely.

---

### Macro definitions
A macro is defined by its name and block:
```jackal
macro macroName {
    ( /* pattern */ ) -> {
        /* expansion */
    }
}
```

Macro definitions live inside any JackalFile, which uses the `.jf` extension. Definitions are separate from
Java source in the interest of enabling tools to compile a JackalFile
file into a JAR, and have that JackalFile and its macros available as
dependencies.

---

### Macro Invocations

Invocations live right inside Java files. A macro can be invoked with a tilde and its name, like so:
```java
public static void main(String[] args) {
    ~println("Hello, World!");
}
```

With the macro defined in the expansions section, Jackal would expand
this macro into the full source:
```java
public static void main(String[] args) {
    System.out.println("Hello, World!");
}
```

Invocations expect to use the grouping that the pattern of
the macro defines. For example, if you have a macro:
```jackal
macro loop {
    { &(&token:tt)* } -> {
        while (true) {
            &(&token)*
        }
    }
}
```
You **must** invoke it with `~loop { /* ... */ }`, not `~loop()` or `~loop[]`.

---

### Context Freedom

Macro invocations operate at the token level. This means
that an invocation can be anywhere inside a Java file, and operate
on any Java tokens that the programmer wants.

This is both a gift and a curse; Jackal does not attempt to make
sense of input tokens, it just generates expansions and moves on.
This keeps the tool fast, but Jackal macros are not what we would
call "procedural", where they can operate on the real abstract syntax tree.

However, it means that as a metaprogramming extension for Java,
we can operate at any level, such as the following:
```java
public class Test {
    ~getter {
        private Object obj;
        private final int abc = 12;
        
        private static Dog dog = new Dog();
    }
}
```
...if correctly defined, this macro could generate getter methods
for any of its enclosed tokens.

This is a double-edged sword. You have absolute power and freedom
to orchestrate valid Java tokens. However, you can always shoot yourself in the foot:
```java
public class Test {
    ~println("what???");
}
```
If println is defined as a standard print expansion, this code does not generate into the
correct context, and Jackal **will** just expand it.

---

### Import Injection
Jackal macros can safely inject imports into the top of a Java file.
A macro can define what imports that it wants to inject, inside its `imports` block:

```jackal
macro test {
    imports {
        java.util.List;
        java.util.ArrayList;
    }
    
    () -> {}
}
```
Jackal always expects imports to be delimited by a semicolon. 

`imports` blocks are entirely optional, but very useful for ensuring a file
which invokes a macro will always have the correct types.

---

### Macro Composure & Recursion

Macros can be composed in any order or enclosure. A macro can contain any inner macro:

```jackal
macro println {
    ( ... ) -> { ... }
}

macro loopAndPrint {
    () -> {
        for (int i = 1; i <= 100; i++) {
            ~println("Iteration: " + i);
        }
    }
}
```
This macro `loopAndPrint` will always expand into a statement which contains another macro to expand.
In this case, Jackal will first expand `loopAndPrint`, then check for more invocations, then expand `println`.

This also means that macros can be recursive!
Here's an example:

```jackal
macro printEveryToken {

    () -> {} // empty base case

    ( &first:tt &(&rest:tt)* ) -> {
        System.out.println(&first); // print first token,
        ~printEveryToken( &(&rest)* ) // then recurse!
    }

}
```
Jackal expands this code in order. For an invocation like `~printEveryToken("Hello" "World" a)`,
the expansion would be:
```java
System.out.println("Hello");
System.out.println("World");
System.out.println(a);
```
So essentially, Jackal can write programs, that write programs, that write programs.

A slightly different behavior occurs for nested macro invocations.
If we're in a Java file:
```java
public class Test {
    
    ~getter {
        private final Example example = new Example();
        
        ~annotate { @Inject,
            private Service service;
            private Instance instance;
        }
    }
    
}
```
In this example, the `annotate` macro expands first, then `getter`.
This is completely fine, because `getter` always expects the token patterns
that `annotate` will emit.

---

### Example Macros

<details>
<summary>Logger Macro</summary>

A simple macro to log things could be defined as such:
```jackal
macro log {
    imports {
        xxx.yyy.zzz.Logger;
    }

    ( &(&token:tt)* ) -> {
        Logger.getInstance().info( &(&token)* );
    }
}
```

And this logger macro could be used like so:
```java
public void method() {
    ~log("Entered method...");
}
```
</details>

<details>
<summary>Getter Macro</summary>

This is an example of a robust getter macro:
```jackal

macro getter {
    {} -> {} // base case

    { &(@ &ann:ident)* &(&mods1:mod)* static &(&mods2:mod)* &type:ident &field:ident &(= &(&def:tt)+)? ; &(&rest:tt)* } -> {
        &(@ &ann)* &(&mods1)* static &(&mods2)* &type &field &(= &(&def)+)?;
    
        public static &type get&[concat,cap]&field() {
            return &field;
        }

        ~getter { &(&rest)* }
    }

    { &(@ &ann:ident)* &(&mods1:mod)* &type:ident &field:ident &(= &(&def:tt)+)? ; &(&rest:tt)* } -> {
        &(@ &ann)* &(&mods1)* &type &field &(= &(&def)+)?;
            public &type get&[concat,cap]&field() {
            return this.&field;
        }

        ~getter { &(&rest)* }
    }
}

```
Notice its healthy usage of optional patterns such as `&(= &(&def:tt)+)?`,
and its usage of recursion. This macro's behavior is made possible by 
recursion, because it must match the static pattern in order to generate a
correct getter method for a static object.

Also note its usage of modifier tokens to compose the names of the methods, `&[concat,cap]&field`.
This is very important for Java metaprogramming in particular.

With these input tokens:
```java
public class Test {
    
    ~getter {
        private final Object obj = new Object();
        @Setter private Integer abc = 12;
        private static Example example;
    }
    
}
```
This is the expansion:
```java
public class Test {

    private final Object obj = new Object();
    public Object getObj() {
        return this.obj;
    }
    
    @Setter private Integer abc = 12;
    public Integer getAbc() {
        return this.abc;
    }

    private static Example example;
    public static Example getExample() {
        return example;
    }
    
}
```

</details>

<details>
<summary>Annotate Macro</summary>

If we take a macro defined like this:
```jackal
macro annotate {
    { &(@ &ann:ident),+ } -> { } // base case
    
    { &(@ &ann:ident),+ // read annotations
      &(@ &anno:ident)* &(&mods1:mod)* &type:ident &field:ident &(= &(&def:tt)+)? ; // read field
      &(&rest:tt)* // read rest
    } -> {
        &(@ &ann)+
        &(@ &anno)* &(&mods1)* &type &field &(= &(&def)+)? ;
        
        ~annotate { &(@ &ann),+ &(&rest)* } // recurse
    }
}
```

And feed it these tokens:
```java
public class Test {
    
    ~annotate { @Inject, @Getter,
        private Service service;
        private Instance instance;
        private Logger logger;
        private Util util;
    }
    
}
```
We see this expansion:
```java
public class Test {
    @Inject @Getter private Service service;
    @Inject @Getter private Instance instance;
    @Inject @Getter private Logger logger;
    @Inject @Getter private Util util;
}
```
This macro scales very nicely.

</details>

---

### Strengths
- Due to its overall simplicity, Jackal is typically _very fast_. Much faster than other Java metaprogramming tools.
Of course, this scales with code size and expansions performed.
- Since Jackal operates on raw tokens and pattern matching, a macro can expand into 
really anything that the programmer could possibly want it to.
- Jackal has very deterministic expansions.

---

### Weaknesses
- You can easily shoot yourself in the foot if you aren't careful.
This should become less of a problem with better IDE support, but for a tool like this, IDE support
is difficult.
- Macros become pretty difficult to write at scale, particularly when reading
a lot of Java grammar. I have some ideas to improve this in future
language versions.
