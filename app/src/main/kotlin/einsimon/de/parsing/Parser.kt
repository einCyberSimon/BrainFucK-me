package einsimon.de.parsing

import einsimon.de.execution.Decrement
import einsimon.de.execution.Increment
import einsimon.de.execution.Instruction
import einsimon.de.execution.Loop
import einsimon.de.execution.MoveLeft
import einsimon.de.execution.MoveRight
import einsimon.de.execution.Nop
import einsimon.de.execution.OutputSymbol
import einsimon.de.execution.ReadSymbol
import einsimon.de.execution.RepeatedInstruction
import einsimon.de.other.Error
import einsimon.de.other.Result
import einsimon.de.other.Success
import einsimon.de.other.getOrElse

class Parser(private val tokenizer: TokenizedInput) {
    fun parse(): Result<List<Instruction>, Throwable> {
        val instructionList = mutableListOf<Instruction>()
        while (tokenizer.hasNext()) {
            val currentToken = tokenizer.next().getOrElse { return Error(it) }
            if (currentToken == Token.SQUARE_BRACKET_OPEN) {
                instructionList.add(parseLoop().getOrElse { return Error(it) })
            } else {
                instructionList.add(parseInstruction(currentToken).getOrElse { return Error(it) })
            }
        }
        return Success(reduceRepeatedInstructions(instructionList.toList()))
    }

    private fun parseInstruction(token: Token): Result<Instruction, Throwable> {
        return when (token) {
            Token.SMALLER_THAN -> MoveLeft(1)
            Token.GREATER_THAN -> MoveRight(1)
            Token.PLUS -> Increment(1)
            Token.MINUS -> Decrement(1)
            Token.DOT -> OutputSymbol
            Token.COMMA -> ReadSymbol
            Token.NOP -> Nop
            Token.SQUARE_BRACKET_OPEN, Token.SQUARE_BRACKET_CLOSE -> null
        }?.let { instruction: Instruction -> Success(instruction) } ?: Error(IllegalArgumentException("Parsed invalid token: $token"))
    }

    private fun parseLoop(): Result<Loop, Throwable> {
        val localInstructions = mutableListOf<Instruction>()
        while (tokenizer.hasNext()) {
            val token = tokenizer.next().getOrElse { return Error(it) }
            when (token) {
                Token.SQUARE_BRACKET_CLOSE -> return Success(Loop(localInstructions))
                Token.SQUARE_BRACKET_OPEN -> localInstructions.add(parseLoop().getOrElse { return Error(it) })
                else -> localInstructions.add(parseInstruction(token).getOrElse { return Error(it) })
            }
        }
        return Error(IllegalStateException("Expected end of loop"))
    }

    private fun reduceRepeatedInstructions(instructions: List<Instruction>): List<Instruction> {
        return instructions.fold(emptyList()) { acc, instruction ->
            when (instruction) {
                is Nop -> acc
                is Loop -> acc + listOf(Loop(reduceRepeatedInstructions(instruction.instructions)))
                is RepeatedInstruction -> {
                    val last = acc.lastOrNull()
                    if (last is RepeatedInstruction && last::class == instruction::class) {
                        last.addInstruction().let { acc }
                    } else {
                        acc + listOf(instruction)
                    }
                }
                else -> acc + listOf(instruction)
            }
        }
    }
}
