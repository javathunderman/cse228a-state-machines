package fsm.outputs
import chisel3._
import chisel3.util._

object HostGenUnoptState extends ChiselEnum {
	val ENTRY, EstablishedCommunication, Standby, Service, HVPowerOn, HVTesting, Initialize, Pressurize, SteeringAdjustments, BorePhaseI, BorePhaseII, SystemPause, FinalRoutineStop, FinalESTOP, Nonsenseunreachablestate, nonsenseunreachablestate2, accessiblebutdeadstate = Value
}

object HostGenUnoptTransition extends ChiselEnum {
	val S10toS11, INIT, EnterService, ReverttoStandby, StartHV, ToInitialize, ToHVTesting, StartPressurizing, MainLineat3000PSI, BeginBorePhaseI, Jacksat1foot, PauseSystem, ResumeBoringPhaseI, ResumeBoringPhaseII, ActuatorsFullyExtended, deadtransition, FaultEncountered = Value
}

class HostGenUnopt extends Module {
	val io = IO(new Bundle {
		val transition = Input(HostGenUnoptTransition())
		val state = Output(HostGenUnoptState())
	})
	val state = RegInit(HostGenUnoptState.ENTRY)
	switch(state) {
	 is(HostGenUnoptState.ENTRY) {
		when(io.transition === HostGenUnoptTransition.S10toS11) {
			state := HostGenUnoptState.EstablishedCommunication
		}
		.elsewhen(io.transition === HostGenUnoptTransition.deadtransition) {
			state := HostGenUnoptState.accessiblebutdeadstate
		}
	}
	 is(HostGenUnoptState.EstablishedCommunication) {
		when(io.transition === HostGenUnoptTransition.INIT) {
			state := HostGenUnoptState.Standby
		}
		.elsewhen(io.transition === HostGenUnoptTransition.FaultEncountered) {
			state := HostGenUnoptState.FinalESTOP
		}
	}
	 is(HostGenUnoptState.Standby) {
		when(io.transition === HostGenUnoptTransition.EnterService) {
			state := HostGenUnoptState.Service
		}
		.elsewhen(io.transition === HostGenUnoptTransition.StartHV) {
			state := HostGenUnoptState.HVPowerOn
		}
		.elsewhen(io.transition === HostGenUnoptTransition.FaultEncountered) {
			state := HostGenUnoptState.FinalESTOP
		}
	}
	 is(HostGenUnoptState.Service) {
		when(io.transition === HostGenUnoptTransition.ReverttoStandby) {
			state := HostGenUnoptState.Standby
		}
		.elsewhen(io.transition === HostGenUnoptTransition.FaultEncountered) {
			state := HostGenUnoptState.FinalESTOP
		}
	}
	 is(HostGenUnoptState.HVPowerOn) {
		when(io.transition === HostGenUnoptTransition.ToInitialize) {
			state := HostGenUnoptState.Initialize
		}
		.elsewhen(io.transition === HostGenUnoptTransition.FaultEncountered) {
			state := HostGenUnoptState.FinalESTOP
		}
	}
	 is(HostGenUnoptState.HVTesting) {
		when(io.transition === HostGenUnoptTransition.ToInitialize) {
			state := HostGenUnoptState.Initialize
		}
		.elsewhen(io.transition === HostGenUnoptTransition.FaultEncountered) {
			state := HostGenUnoptState.FinalESTOP
		}
	}
	 is(HostGenUnoptState.Initialize) {
		when(io.transition === HostGenUnoptTransition.ToHVTesting) {
			state := HostGenUnoptState.HVTesting
		}
		.elsewhen(io.transition === HostGenUnoptTransition.StartPressurizing) {
			state := HostGenUnoptState.Pressurize
		}
		.elsewhen(io.transition === HostGenUnoptTransition.FaultEncountered) {
			state := HostGenUnoptState.FinalESTOP
		}
	}
	 is(HostGenUnoptState.Pressurize) {
		when(io.transition === HostGenUnoptTransition.MainLineat3000PSI) {
			state := HostGenUnoptState.SteeringAdjustments
		}
		.elsewhen(io.transition === HostGenUnoptTransition.FaultEncountered) {
			state := HostGenUnoptState.FinalESTOP
		}
	}
	 is(HostGenUnoptState.SteeringAdjustments) {
		when(io.transition === HostGenUnoptTransition.BeginBorePhaseI) {
			state := HostGenUnoptState.BorePhaseI
		}
		.elsewhen(io.transition === HostGenUnoptTransition.FaultEncountered) {
			state := HostGenUnoptState.FinalESTOP
		}
	}
	 is(HostGenUnoptState.BorePhaseI) {
		when(io.transition === HostGenUnoptTransition.Jacksat1foot) {
			state := HostGenUnoptState.BorePhaseII
		}
		.elsewhen(io.transition === HostGenUnoptTransition.PauseSystem) {
			state := HostGenUnoptState.SystemPause
		}
		.elsewhen(io.transition === HostGenUnoptTransition.FaultEncountered) {
			state := HostGenUnoptState.FinalESTOP
		}
	}
	 is(HostGenUnoptState.BorePhaseII) {
		when(io.transition === HostGenUnoptTransition.PauseSystem) {
			state := HostGenUnoptState.SystemPause
		}
		.elsewhen(io.transition === HostGenUnoptTransition.ActuatorsFullyExtended) {
			state := HostGenUnoptState.FinalRoutineStop
		}
		.elsewhen(io.transition === HostGenUnoptTransition.FaultEncountered) {
			state := HostGenUnoptState.FinalESTOP
		}
	}
	 is(HostGenUnoptState.SystemPause) {
		when(io.transition === HostGenUnoptTransition.ResumeBoringPhaseI) {
			state := HostGenUnoptState.BorePhaseI
		}
		.elsewhen(io.transition === HostGenUnoptTransition.ResumeBoringPhaseII) {
			state := HostGenUnoptState.BorePhaseII
		}
		.elsewhen(io.transition === HostGenUnoptTransition.FaultEncountered) {
			state := HostGenUnoptState.FinalESTOP
		}
	}
	 is(HostGenUnoptState.FinalRoutineStop) {
		when(io.transition === HostGenUnoptTransition.StartHV) {
			state := HostGenUnoptState.HVPowerOn
		}
		.elsewhen(io.transition === HostGenUnoptTransition.FaultEncountered) {
			state := HostGenUnoptState.FinalESTOP
		}
	}
	 is(HostGenUnoptState.FinalESTOP) {
		when(io.transition === HostGenUnoptTransition.EnterService) {
			state := HostGenUnoptState.Service
		}
	}

	}
	io.state := state
}
