package einsimon.de

import einsimon.de.execution.Executor
import einsimon.de.other.Error
import einsimon.de.other.Success
import einsimon.de.other.map
import einsimon.de.parsing.Lexer
import einsimon.de.parsing.Parser
import java.io.File

fun main() {
    val path = File("/home/einsimon/Documents/bd-present/stage3/cipherfuck.bf")
    val tokenizer = Lexer().readFile(path)
    val instructions = Parser(tokenizer).parse()
    val result =
        instructions.map { instr ->
            Executor(instr).runCode("")
        }
    when (result) {
        is Success -> println("Execution Successful")
        is Error -> println("Execution Failed with ${result.error}")
    }
}
