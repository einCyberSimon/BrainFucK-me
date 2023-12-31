package einsimon.de.execution

import einsimon.de.other.Error
import einsimon.de.other.Result
import einsimon.de.other.Success

sealed interface State {
    val pointer: Int
    val input: String
    val tape: Map<Int, Int>
    val currentCell: Int

    fun move(amount: Int): Result<State, Throwable>

    fun modifyTape(value: Int): Result<State, Throwable>

    fun setTapeValue(value: Int): Result<State, Throwable>

    fun readInput(): Result<Pair<Int, State>, Throwable>

    fun dumpState()
}

data class SimpleState(
    override val pointer: Int,
    override val tape: Map<Int, Int>,
    override val input: String,
) : State {
    override val currentCell: Int
        get() = tape.withDefault { 0 }.getValue(pointer)

    override fun move(amount: Int): Result<State, Throwable> {
        return if (pointer + amount >= 0) {
            Success(SimpleState(pointer + amount, tape, input))
        } else {
            Error(IllegalStateException("Cannot move pointer left by $amount"))
        }
    }

    override fun modifyTape(value: Int): Result<State, Throwable> {
        val newTape = tape.toMutableMap().apply { merge(pointer, value) { old, new -> old + new % 256 } }
        return Success(SimpleState(pointer, newTape, input))
    }

    override fun setTapeValue(value: Int): Result<State, Throwable> {
        val newTape = tape + mapOf(pointer to value % 256)
        return Success(SimpleState(pointer, newTape, input))
    }

    override fun readInput(): Result<Pair<Int, State>, Throwable> {
        val readChar = input.firstOrNull()?.code ?: 0
        return Success(readChar to SimpleState(pointer, tape, input.drop(1)))
    }

    override fun dumpState() {
        println()
        println("Dumping current state:")
        println("Pointer: $pointer")
        println("Current Cell: $currentCell (char Value: ${currentCell.toChar()})")
        println("Filtered ASCII values from tape:")
        println(tape.toSortedMap().filterValues { v -> v in 32..126 }.map { (_, v) -> v.toChar() }.joinToString(""))
        println()
    }
}

data class StartingState(override val input: String) : State {
    override val pointer = 0
    override val tape = mapOf<Int, Int>().withDefault { 0 }
    override val currentCell = 0

    override fun move(amount: Int): Result<State, Throwable> {
        return if (amount >= 0) {
            Success(SimpleState(amount, tape, input))
        } else {
            Error(IllegalStateException("Cannot move pointer left by $amount"))
        }
    }

    override fun modifyTape(value: Int): Result<State, Throwable> {
        return Success(SimpleState(pointer, mapOf(pointer to value), input))
    }

    override fun setTapeValue(value: Int): Result<State, Throwable> {
        return Success(SimpleState(pointer, mapOf(pointer to value), input))
    }

    override fun readInput(): Result<Pair<Int, State>, Throwable> {
        val readChar = input.firstOrNull()?.code ?: 0
        return Success(readChar to SimpleState(pointer, tape, input.drop(1)))
    }

    override fun dumpState() {
        println("Skipping Dumping Starting State.")
    }
}
