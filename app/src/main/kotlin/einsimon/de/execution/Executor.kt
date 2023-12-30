package einsimon.de.execution

import einsimon.de.other.Result
import einsimon.de.other.Success
import einsimon.de.other.flatMap

class Executor(private val instructions: List<Instruction>) {
    fun runCode(input: String): Result<State, Throwable> {
        val currentState: Result<State, Throwable> = Success(StartingState(input))
        return instructions.fold(currentState) { state, instruction ->
            state.flatMap { s -> instruction.execute(s).also { s.dumpState() } }
        }
    }
}
