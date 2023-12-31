package einsimon.de.execution

import einsimon.de.other.Error
import einsimon.de.other.Result
import einsimon.de.other.Success
import einsimon.de.other.flatMap

sealed interface Instruction {
    fun execute(state: State): Result<State, Throwable>
}

sealed class SimpleInstruction : Instruction

data object OutputSymbol : SimpleInstruction() {
    override fun execute(state: State): Result<State, Throwable> {
        print(state.currentCell.toChar())
        return Success(state)
    }
}

data object ReadSymbol : SimpleInstruction() {
    override fun execute(state: State): Result<State, Throwable> {
        return state.readInput().flatMap { (v, s) -> s.setTapeValue(v) }
    }
}

data object Nop : SimpleInstruction() {
    override fun execute(state: State): Result<State, Throwable> = Success(state)
}

data object DebugInstruction : SimpleInstruction() {
    override fun execute(state: State): Result<State, Throwable> {
        state.dumpState()
        return Success(state)
    }
}

sealed class RepeatedInstruction(var amount: Int) : Instruction {
    fun addInstruction() {
        amount++
    }
}

class MoveLeft(amount: Int) : RepeatedInstruction(amount) {
    override fun execute(state: State): Result<State, Throwable> {
        return state.move(-amount)
    }
}

class MoveRight(amount: Int) : RepeatedInstruction(amount) {
    override fun execute(state: State): Result<State, Throwable> {
        return state.move(amount)
    }
}

class Increment(amount: Int) : RepeatedInstruction(amount) {
    override fun execute(state: State): Result<State, Throwable> {
        return state.modifyTape(amount)
    }
}

class Decrement(amount: Int) : RepeatedInstruction(amount) {
    override fun execute(state: State): Result<State, Throwable> {
        return state.modifyTape(-amount)
    }
}

class Loop(val instructions: List<Instruction>) : Instruction {
    override tailrec fun execute(state: State): Result<State, Throwable> {
        if (state.currentCell == 0) return Success(state)
        val currentState =
            instructions.fold(Success(state) as Result<State, Throwable>) { currentState, instruction ->
                currentState.flatMap { s -> instruction.execute(s) }
            }
        return when (currentState) {
            is Success ->
                if (currentState.value.currentCell == 0) {
                    currentState
                } else {
                    execute(currentState.value)
                }
            is Error -> currentState
        }
    }
}
