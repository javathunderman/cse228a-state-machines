package fsm.outputs
import chisel3._
import chisel3.util._

object HostGenOptState extends ChiselEnum {
	val ENTRY, EstablishedCommunication, Standby, Service, HVPowerOn, HVTesting, Initialize, Pressurize, SteeringAdjustments, BorePhaseI, BorePhaseII, SystemPause, FinalRoutineStop, FinalESTOP, Nonsenseunreachablestate, nonsenseunreachablestate2, accessiblebutdeadstate = Value
}

object HostGenOptTransition extends ChiselEnum {
	val S10toS11, INIT, EnterService, ReverttoStandby, StartHV, ToInitialize, ToHVTesting, StartPressurizing, MainLineat3000PSI, BeginBorePhaseI, Jacksat1foot, PauseSystem, ResumeBoringPhaseI, ResumeBoringPhaseII, ActuatorsFullyExtended, deadtransition, FaultEncountered = Value
}

class HostGenOpt extends Module {
	val io = IO(new Bundle {
		val transition = Input(HostGenOptTransition())
		val state = Output(HostGenOptState())
	})
	val state = RegInit(HostGenOptState.ENTRY)
	switch(state) {
	 is(HostGenOptState.ENTRY) {
		when(io.transition === HostGenOptTransition.S10toS11) {
			state := HostGenOptState.EstablishedCommunication
		}
		.elsewhen(io.transition === HostGenOptTransition.deadtransition) {
			state := HostGenOptState.accessiblebutdeadstate
		}
	}
	 is(HostGenOptState.EstablishedCommunication) {
		when(io.transition === HostGenOptTransition.INIT) {
			state := HostGenOptState.Standby
		}
		.elsewhen(io.transition === HostGenOptTransition.FaultEncountered) {
			state := HostGenOptState.FinalESTOP
		}
	}
	 is(HostGenOptState.Standby) {
		when(io.transition === HostGenOptTransition.EnterService) {
			state := HostGenOptState.Service
		}
		.elsewhen(io.transition === HostGenOptTransition.StartHV) {
			state := HostGenOptState.HVPowerOn
		}
		.elsewhen(io.transition === HostGenOptTransition.FaultEncountered) {
			state := HostGenOptState.FinalESTOP
		}
	}
	 is(HostGenOptState.Service) {
		when(io.transition === HostGenOptTransition.ReverttoStandby) {
			state := HostGenOptState.Standby
		}
		.elsewhen(io.transition === HostGenOptTransition.FaultEncountered) {
			state := HostGenOptState.FinalESTOP
		}
	}
	 is(HostGenOptState.HVPowerOn) {
		when(io.transition === HostGenOptTransition.ToInitialize) {
			state := HostGenOptState.Initialize
		}
		.elsewhen(io.transition === HostGenOptTransition.FaultEncountered) {
			state := HostGenOptState.FinalESTOP
		}
	}
	 is(HostGenOptState.HVTesting) {
		when(io.transition === HostGenOptTransition.ToInitialize) {
			state := HostGenOptState.Initialize
		}
		.elsewhen(io.transition === HostGenOptTransition.FaultEncountered) {
			state := HostGenOptState.FinalESTOP
		}
	}
	 is(HostGenOptState.Initialize) {
		when(io.transition === HostGenOptTransition.ToHVTesting) {
			state := HostGenOptState.HVTesting
		}
		.elsewhen(io.transition === HostGenOptTransition.StartPressurizing) {
			state := HostGenOptState.Pressurize
		}
		.elsewhen(io.transition === HostGenOptTransition.FaultEncountered) {
			state := HostGenOptState.FinalESTOP
		}
	}
	 is(HostGenOptState.Pressurize) {
		when(io.transition === HostGenOptTransition.MainLineat3000PSI) {
			state := HostGenOptState.SteeringAdjustments
		}
		.elsewhen(io.transition === HostGenOptTransition.FaultEncountered) {
			state := HostGenOptState.FinalESTOP
		}
	}
	 is(HostGenOptState.SteeringAdjustments) {
		when(io.transition === HostGenOptTransition.BeginBorePhaseI) {
			state := HostGenOptState.BorePhaseI
		}
		.elsewhen(io.transition === HostGenOptTransition.FaultEncountered) {
			state := HostGenOptState.FinalESTOP
		}
	}
	 is(HostGenOptState.BorePhaseI) {
		when(io.transition === HostGenOptTransition.Jacksat1foot) {
			state := HostGenOptState.BorePhaseII
		}
		.elsewhen(io.transition === HostGenOptTransition.PauseSystem) {
			state := HostGenOptState.SystemPause
		}
		.elsewhen(io.transition === HostGenOptTransition.FaultEncountered) {
			state := HostGenOptState.FinalESTOP
		}
	}
	 is(HostGenOptState.BorePhaseII) {
		when(io.transition === HostGenOptTransition.PauseSystem) {
			state := HostGenOptState.SystemPause
		}
		.elsewhen(io.transition === HostGenOptTransition.ActuatorsFullyExtended) {
			state := HostGenOptState.FinalRoutineStop
		}
		.elsewhen(io.transition === HostGenOptTransition.FaultEncountered) {
			state := HostGenOptState.FinalESTOP
		}
	}
	 is(HostGenOptState.SystemPause) {
		when(io.transition === HostGenOptTransition.ResumeBoringPhaseI) {
			state := HostGenOptState.BorePhaseI
		}
		.elsewhen(io.transition === HostGenOptTransition.ResumeBoringPhaseII) {
			state := HostGenOptState.BorePhaseII
		}
		.elsewhen(io.transition === HostGenOptTransition.FaultEncountered) {
			state := HostGenOptState.FinalESTOP
		}
	}
	 is(HostGenOptState.FinalRoutineStop) {
		when(io.transition === HostGenOptTransition.StartHV) {
			state := HostGenOptState.HVPowerOn
		}
		.elsewhen(io.transition === HostGenOptTransition.FaultEncountered) {
			state := HostGenOptState.FinalESTOP
		}
	}
	 is(HostGenOptState.FinalESTOP) {
		when(io.transition === HostGenOptTransition.EnterService) {
			state := HostGenOptState.Service
		}
	}

	}
	io.state := state
}
