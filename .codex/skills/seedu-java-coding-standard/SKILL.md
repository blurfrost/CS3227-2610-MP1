---
name: seedu-java-coding-standard
description: Apply the SE-EDU Java coding standard to all Java production and test code in this project. Use whenever creating, modifying, generating, refactoring, or reviewing Java code.
---

# SE-EDU Java Coding Standard

Follow the complete SE-EDU Java coding standard at
<https://se-education.org/guides/conventions/java/index.html>. The rules below
are the project checklist derived from that standard. For Java topics not
covered here, follow the
[Google Java Style Guide](https://google.github.io/styleguide/javaguide.html).

Apply these rules to production code and test code. When making a focused
change, bring changed and newly added code into compliance, but do not reformat
or rewrite unrelated existing code unless the user asks for a standards cleanup.

## Naming

- Use all-lowercase package names, rooted in the project or group name and
  divided into logical packages. Do not use misleading institutional prefixes
  such as `edu.nus.comp`.
- Name classes and enums with English nouns in `PascalCase`.
- Name methods with English verbs in `camelCase`.
- Name variables in English using `camelCase`.
- Name constants in `SCREAMING_SNAKE_CASE`. Give associated constants a common
  prefix.
- Keep acronyms lowercase except for their initial letter when they are part of
  a name: use `exportHtmlSource`, not `exportHTMLSource`.
- Name booleans so they read as booleans, normally with prefixes such as `is`,
  `has`, `was`, `can`, or `should`. Name a boolean setter parameter similarly,
  for example `setFound(boolean isFound)`.
- Use plural names for collections and arrays.
- Match variable-name length to scope. Short scratch names such as `i`, `j`,
  and `k` are acceptable only in small scopes; reserve `j` and later letters
  for nested loops.
- Test method names may use
  `featureUnderTest_testScenario_expectedBehavior`; omit later parts when they
  add no useful distinction.

## Layout

- Indent with 4 spaces and never tabs. Indent wrapped lines by 8 additional
  spaces relative to the parent line.
- Keep lines below 110 characters when practical and never exceed 120
  characters.
- Wrap for readability: break after commas and before operators, including
  `.`, `&` in type bounds, and `|` in multi-catch clauses. Prefer higher-level
  breaks. Keep a method or constructor name attached to its opening `(`.
- Use K&R braces: place `{` at the end of the declaration or control-statement
  line and place `} else`, `} catch`, and `} finally` on one line.
- Always use braces around loop and conditional bodies, including one-line
  bodies. Put each conditional body on lines separate from its condition.
- Format `for`, `while`, `do-while`, `if-else`, `switch`, and `try-catch` in the
  conventional K&R forms shown by the source standard.
- In colon-style `switch` statements, indent case bodies once and include
  `// Fallthrough` whenever a case intentionally has no `break`. Arrow-style
  switch statements and expressions are allowed.
- Surround binary and ternary operators with spaces. Put spaces after Java
  keywords, commas, and semicolons in `for` headers.
- Separate logical units inside a block with one blank line. Do not add blank
  lines without a logical boundary.

## Packages And Imports

- Put every class in a package and group related classes in the same package.
- Keep import ordering consistent with the project. Group static imports first,
  then normal imports in stable package groups, separated by blank lines where
  appropriate.
- Import every class explicitly. Never use wildcard imports, and remove unused
  imports.

## Declarations

- Organize a class or interface as follows: class documentation; declaration;
  static variables ordered `public`, `protected`, package-private, `private`;
  instance variables in the same access order; constructors; methods.
- Put method modifiers in this order:
  `<access> static abstract synchronized <unusual> final native`, where unusual
  modifiers include `volatile` and `transient`. Most importantly, put the
  access modifier first.
- Attach array brackets to the type, as in `int[] values`, not `int values[]`.
- Initialize variables at declaration where a valid initial value exists, and
  declare them in the smallest practical scope. Do not use fake placeholder
  initial values merely to initialize a variable.
- Do not expose mutable class variables as `public`, except fields of a true
  behavior-free data class. Public constants are allowed.
- Use `this` for a field only when a parameter or local variable shadows that
  field.

## Comments And Javadoc

- Write comments in English using American spelling and avoid local slang.
- Add descriptive Javadoc to every public class and public method, except
  getters/setters, test code, and overrides whose inherited documentation
  applies exactly as written.
- Add Javadoc to every nontrivial private method.
- Start Javadoc with a short summary sentence in third-person verb form, such
  as `Returns ...`, `Adds ...`, or `Sends ...`.
- Use a multi-line Javadoc block for every class, method, and nontrivial field;
  do not use one-line Javadoc comments for these declarations.
- Put the opening `/**` on its own line, align each subsequent `*`, add one
  space after each `*`, and place no blank line between the Javadoc and its
  declaration.
- Write the first sentence as a short summary because Javadoc uses it in the
  method summary table and index. For methods, begin with `Returns ...`,
  `Sends ...`, `Adds ...`, or another third-person verb form.
- Separate the main description from block tags with one blank Javadoc line.
  End every parameter, return, and exception description with punctuation.
- Include either all useful `@param` tags or none. Omit them only when every
  parameter is self-explanatory or already explained in the description.
  Omit `@return` only for `void` methods or when the return value is obvious.
- Use `{@inheritDoc}` for overrides when the parent documentation applies, and
  add further details when the override changes the inherited behavior.
- Use the following form for methods with parameters, a return value, and an
  exception:

  ```java
  /**
   * Returns lateral location of the specified position.
   * If the position is unset, NaN is returned.
   *
   * @param x X coordinate of position.
   * @param y Y coordinate of position.
   * @param zone Zone of position.
   * @return Lateral location.
   * @throws IllegalArgumentException If zone is <= 0.
   */
  public double computeLocation(double x, double y, int zone)
          throws IllegalArgumentException {
      // ...
  }
  ```
- Indent comments with the code they describe. Use comments to explain intent,
  constraints, or non-obvious reasoning rather than restating the code.

## Compliance Workflow

1. Before editing, identify the Java files and declarations affected by the
   requested change.
2. Apply every relevant rule above while implementing the change.
3. Review changed Java lines for naming, wrapping, braces, imports, declaration
   order, scope, and Javadoc compliance.
4. Consult the linked SE-EDU source when a rule is ambiguous. Use Google Java
   Style only when SE-EDU does not address the topic.
5. Run the project's required tests and quality checks after application-code
   changes.
