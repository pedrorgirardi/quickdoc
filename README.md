# Quickdoc

## API docs

See [API.md](API.md).

## Rationale

This library came out of the desire to have a fast and light weight tool that
produces API docs from any Clojure code (`.clj`, `.cljs`, `.cljc`, `.cljd`),
without executing that code. This tool produces pure Markdown and the output
does not need CSS or JavaScript.

Quickdoc's properties:

- Based on [clj-kondo static analysis](https://github.com/clj-kondo/clj-kondo/tree/master/analysis)
- Fast to run using [babashka](#babashka)

## Projects using quickdoc

- [process](https://github.com/babashka/process/blob/master/API.md)

## Babashka

Use as a babashka dependency. See this project's [bb.edn](bb.edn) how to use it as a task.
This project's [API.md](API.md) is generated by running `bb quickdoc` on the command line.

## Clojure tool

Quickdoc is also available as a [clj
tool](https://clojure.org/reference/deps_and_cli#_tool_usage). Note that this is
significantly slower than invocation via babashka.

On the command line, run:

```
clj -Ttools install io.github.borkdude/quickdoc '{:deps/root "jvm" :git/sha "228823e29f2d18bdc0f237d3f274b32f91283784"}' :as quickdoc
```

Then invoke quickdoc using:

```
clj -Tquickdoc quickdoc '{:github/repo "https://github.com/borkdude/quickdoc"}'
```

## License

See [LICENSE](LICENSE).
