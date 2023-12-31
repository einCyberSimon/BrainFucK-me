# BrainFucK-me

A simple brainfuck interpreter written in Kotlin.

## Features

The interpreter can interpret any valid brainfuck program.
It will parse the symbols `>`, `<`, `+`, `-`, `.`, `,`, `[`, `]` as their corresponding instruction and other symbols as `NOP` statements that will be ignored during the execution.

Additionally, there is the `#` symbol, which will be interpreted as _debug_ instruction.
Once such a symbol is encountered, the interpreter will dump the current pointer position, cell value and print all tape values within the ASCII range as string.

## Usage

The basic usage using the Gradle build system is

```
gradle run --args="-f[ <file path>]+ [-i[ <input>]+]"
```

You can provide a list of file paths to be executed by passing them under the `-f` or `--file` argument and provide optional inputs via `-i` or `--input`.
Each of the parameters, i.e. filepath or input, should be whitespace separated.

## But Why?

It has become some sort of tradition, that we hide birthday gifts in some fun challenges.
One I received included a brainfuck program that upon execution decrypted some password.
Thus, I had to dump the state of the tape to retrieve said password and therefore I wrote this interpreter.
