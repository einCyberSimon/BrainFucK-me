package einsimon.de

import einsimon.de.execution.Executor
import einsimon.de.other.Error
import einsimon.de.other.Success
import einsimon.de.other.map
import einsimon.de.other.onSuccessAlso
import einsimon.de.parsing.Lexer
import einsimon.de.parsing.Parser
import java.io.File
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    val arguments = argparse(args.toList())

    val files =
        (arguments["f"].orEmpty() + arguments["file"].orEmpty())
            .map { filePath -> File(filePath) }
            .filter { file -> file.exists() }
    if (files.isEmpty()) {
        println("No existing files found")
        exitProcess(1)
    }

    val inputs =
        (arguments["i"].orEmpty() + arguments["input"].orEmpty())
            .ifEmpty { listOf("") }

    val lexer = Lexer()
    val tokenizers = files.associateWith { file -> lexer.readFile(file) }
    val parsedInstructions =
        tokenizers.map { (file, tokens) ->
            val fileName = file.path.split("/").last()
            println("Parsing $fileName")
            fileName to Parser(tokens).parse().onSuccessAlso { println("Parsed successfully") }
        }

    parsedInstructions.forEach { (fileName, instructions) ->
        instructions.map { instr ->
            inputs.forEach { input ->
                println("Executing $fileName with input '$input'")
                val result = Executor(instr).runCode(input)
                when (result) {
                    is Success -> println("Execution Successful")
                    is Error -> println("Execution Failed with ${result.error}")
                }
            }
        }
    }
}

fun argparse(args: List<String>): Map<String, List<String>> =
    args.fold(Pair(emptyMap<String, List<String>>(), "")) { (map, lastKey), argString ->
        if (argString.startsWith("-")) {
            Regex("-{1,2}").let { regex ->
                Pair(map + (argString.split(regex)[1] to emptyList()), argString.split(regex)[1])
            }
        } else {
            Pair(map + (lastKey to map.getOrDefault(lastKey, emptyList()) + argString), lastKey)
        }
    }.first
