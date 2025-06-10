package fsm.outputs
import chisel3._
import chisel3.util._

object FSMGenUnoptState extends ChiselEnum {
	val ENTRY, Intermediate, Final = Value
}

object FSMGenUnoptTransition extends ChiselEnum {
	val moveToIntermediate, repeatFinal = Value
}

class FSMGenUnopt extends Module {
	val io = IO(new Bundle {
		val transition = Input(FSMGenUnoptTransition())
		val state = Output(FSMGenUnoptState())
	})
	val state = RegInit(FSMGenUnoptState.ENTRY)
	switch(state) {
	 is(FSMGenUnoptState.ENTRY) {
		when(io.transition === FSMGenUnoptTransition.moveToIntermediate) {
			state := FSMGenUnoptState.Intermediate
		}
	}
	 is(FSMGenUnoptState.Final) {
		when(io.transition === FSMGenUnoptTransition.repeatFinal) {
			state := FSMGenUnoptState.Final
		}
	}

	}
	io.state := state
}
