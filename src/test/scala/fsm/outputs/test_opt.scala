package fsm.outputs
import chisel3._
import chisel3.util._

object FSMGenOptState extends ChiselEnum {
	val ENTRY, Intermediate, Final = Value
}

object FSMGenOptTransition extends ChiselEnum {
	val moveToIntermediate, repeatFinal = Value
}

class FSMGenOpt extends Module {
	val io = IO(new Bundle {
		val transition = Input(FSMGenOptTransition())
		val state = Output(FSMGenOptState())
	})
	val state = RegInit(FSMGenOptState.ENTRY)
	switch(state) {
	 is(FSMGenOptState.ENTRY) {
		when(io.transition === FSMGenOptTransition.moveToIntermediate) {
			state := FSMGenOptState.Intermediate
		}
	}

	}
	io.state := state
}
