package fsm.outputs
import chisel3._
import chisel3.util._

object FSMGenState extends ChiselEnum {
	val ENTRY, EstablishedCommunication, Standby, Service, HVPowerOn, HVTesting, Initialize, Pressurize, SteeringAdjustments, BorePhaseI, BorePhaseII, SystemPause, FinalRoutineStop, FinalESTOP = Value
}

object FSMGenTransition extends ChiselEnum {
	val S10toS11, INIT, EnterService, ReverttoStandby, StartHV, ToInitialize, ToHVTesting, StartPressurizing, MainLineat3000PSI, BeginBorePhaseI, Jacksat1foot, PauseSystem, ResumeBoringPhaseI, ResumeBoringPhaseII, ActuatorsFullyExtended, FaultEncountered = Value
}

class FSMGen extends Module {
	val io = IO(new Bundle {
		val transition = Input(FSMGenTransition())
		val state = Output(FSMGenState())
	})
	val state = RegInit(FSMGenState.ENTRY)
	switch(state) {
	 is(FSMGenState.ENTRY) {
		when(io.transition === FSMGenTransition.S10toS11) {
			state := FSMGenState.EstablishedCommunication
		}
	}
	 is(FSMGenState.EstablishedCommunication) {
		when(io.transition === FSMGenTransition.INIT) {
			state := FSMGenState.Standby
		}
		.elsewhen(io.transition === FSMGenTransition.FaultEncountered) {
			state := FSMGenState.FinalESTOP
		}
	}
	 is(FSMGenState.Standby) {
		when(io.transition === FSMGenTransition.EnterService) {
			state := FSMGenState.Service
		}
		.elsewhen(io.transition === FSMGenTransition.StartHV) {
			state := FSMGenState.HVPowerOn
		}
		.elsewhen(io.transition === FSMGenTransition.FaultEncountered) {
			state := FSMGenState.FinalESTOP
		}
	}
	 is(FSMGenState.Service) {
		when(io.transition === FSMGenTransition.ReverttoStandby) {
			state := FSMGenState.Standby
		}
		.elsewhen(io.transition === FSMGenTransition.FaultEncountered) {
			state := FSMGenState.FinalESTOP
		}
	}
	 is(FSMGenState.HVPowerOn) {
		when(io.transition === FSMGenTransition.ToInitialize) {
			state := FSMGenState.Initialize
		}
		.elsewhen(io.transition === FSMGenTransition.FaultEncountered) {
			state := FSMGenState.FinalESTOP
		}
	}
	 is(FSMGenState.HVTesting) {
		when(io.transition === FSMGenTransition.ToInitialize) {
			state := FSMGenState.Initialize
		}
		.elsewhen(io.transition === FSMGenTransition.FaultEncountered) {
			state := FSMGenState.FinalESTOP
		}
	}
	 is(FSMGenState.Initialize) {
		when(io.transition === FSMGenTransition.ToHVTesting) {
			state := FSMGenState.HVTesting
		}
		.elsewhen(io.transition === FSMGenTransition.StartPressurizing) {
			state := FSMGenState.Pressurize
		}
		.elsewhen(io.transition === FSMGenTransition.FaultEncountered) {
			state := FSMGenState.FinalESTOP
		}
	}
	 is(FSMGenState.Pressurize) {
		when(io.transition === FSMGenTransition.MainLineat3000PSI) {
			state := FSMGenState.SteeringAdjustments
		}
		.elsewhen(io.transition === FSMGenTransition.FaultEncountered) {
			state := FSMGenState.FinalESTOP
		}
	}
	 is(FSMGenState.SteeringAdjustments) {
		when(io.transition === FSMGenTransition.BeginBorePhaseI) {
			state := FSMGenState.BorePhaseI
		}
		.elsewhen(io.transition === FSMGenTransition.FaultEncountered) {
			state := FSMGenState.FinalESTOP
		}
	}
	 is(FSMGenState.BorePhaseI) {
		when(io.transition === FSMGenTransition.Jacksat1foot) {
			state := FSMGenState.BorePhaseII
		}
		.elsewhen(io.transition === FSMGenTransition.PauseSystem) {
			state := FSMGenState.SystemPause
		}
		.elsewhen(io.transition === FSMGenTransition.FaultEncountered) {
			state := FSMGenState.FinalESTOP
		}
	}
	 is(FSMGenState.BorePhaseII) {
		when(io.transition === FSMGenTransition.PauseSystem) {
			state := FSMGenState.SystemPause
		}
		.elsewhen(io.transition === FSMGenTransition.ActuatorsFullyExtended) {
			state := FSMGenState.FinalRoutineStop
		}
		.elsewhen(io.transition === FSMGenTransition.FaultEncountered) {
			state := FSMGenState.FinalESTOP
		}
	}
	 is(FSMGenState.SystemPause) {
		when(io.transition === FSMGenTransition.ResumeBoringPhaseI) {
			state := FSMGenState.BorePhaseI
		}
		.elsewhen(io.transition === FSMGenTransition.ResumeBoringPhaseII) {
			state := FSMGenState.BorePhaseII
		}
		.elsewhen(io.transition === FSMGenTransition.FaultEncountered) {
			state := FSMGenState.FinalESTOP
		}
	}
	 is(FSMGenState.FinalRoutineStop) {
		when(io.transition === FSMGenTransition.StartHV) {
			state := FSMGenState.HVPowerOn
		}
		.elsewhen(io.transition === FSMGenTransition.FaultEncountered) {
			state := FSMGenState.FinalESTOP
		}
	}
	 is(FSMGenState.FinalESTOP) {
		when(io.transition === FSMGenTransition.EnterService) {
			state := FSMGenState.Service
		}
	}

	}
	io.state := state
}
