package einsimon.de.parsing

import einsimon.de.other.Result
import einsimon.de.other.Success
import einsimon.de.other.withCatching
import einsimon.de.other.getOrElse
import einsimon.de.other.flatMap
import java.io.File

enum class Token {
    SMALLER_THAN,
    GREATER_THAN,
    PLUS,
    MINUS,
    DOT,
    COMMA,
    SQUARE_BRACKET_OPEN,
    SQUARE_BRACKET_CLOSE,
    NOP,
    ;

    companion object {
        fun parseChar(char: Char): Result<Token, Throwable> {
            return when(char) {
                '>' -> GREATER_THAN
                '<' -> SMALLER_THAN
                '-' -> MINUS
                '+' -> PLUS
                '.' -> DOT
                ',' -> COMMA
                '[' -> SQUARE_BRACKET_OPEN
                ']' -> SQUARE_BRACKET_CLOSE
                else -> NOP
            }.let { Success(it) }
        }
    }
}

class TokenizedInput(file: File): Iterator<Result<Token, Throwable>> {
    private val reader = file.reader()

    private val nextChars = ArrayDeque<Result<Int, Throwable>>()

    override fun hasNext(): Boolean { 
        if (nextChars.isNotEmpty()) return true
        val nextChar = withCatching { reader.read() }
        return if (nextChar.getOrElse { -1 } >= 0) {
            true.also { nextChars.add(nextChar) }
        } else {
            false
        }
    }

    override fun next(): Result<Token, Throwable> {
        return nextChars.removeFirst().flatMap { c -> Token.parseChar(c.toChar()) }
    }

}

class Lexer {
    fun readFile(file: File) = TokenizedInput(file)
}

