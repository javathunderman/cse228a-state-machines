package fsm.outputs
import chisel3._
import chisel3.util._

object HostGenState extends ChiselEnum {
	val ENTRY, EstablishedCommunication, Standby, Service, HVPowerOn, HVTesting, Initialize, Pressurize, SteeringAdjustments, BorePhaseI, BorePhaseII, SystemPause, FinalRoutineStop, FinalESTOP = Value
}

object HostGenTransition extends ChiselEnum {
	val S10toS11, INIT, EnterService, ReverttoStandby, StartHV, ToInitialize, ToHVTesting, StartPressurizing, MainLineat3000PSI, BeginBorePhaseI, Jacksat1foot, PauseSystem, ResumeBoringPhaseI, ResumeBoringPhaseII, ActuatorsFullyExtended, FaultEncountered = Value
}

class HostGen extends Module {
	val io = IO(new Bundle {
		val transition = Input(HostGenTransition())
		val state = Output(HostGenState())
	})
	val state = RegInit(HostGenState.ENTRY)
	switch(state) {
	 is(HostGenState.ENTRY) {
		when(io.transition === HostGenTransition.S10toS11) {
			state := HostGenState.EstablishedCommunication
		}
	}
	 is(HostGenState.EstablishedCommunication) {
		when(io.transition === HostGenTransition.INIT) {
			state := HostGenState.Standby
		}
		.elsewhen(io.transition === HostGenTransition.FaultEncountered) {
			state := HostGenState.FinalESTOP
		}
	}
	 is(HostGenState.Standby) {
		when(io.transition === HostGenTransition.EnterService) {
			state := HostGenState.Service
		}
		.elsewhen(io.transition === HostGenTransition.StartHV) {
			state := HostGenState.HVPowerOn
		}
		.elsewhen(io.transition === HostGenTransition.FaultEncountered) {
			state := HostGenState.FinalESTOP
		}
	}
	 is(HostGenState.Service) {
		when(io.transition === HostGenTransition.ReverttoStandby) {
			state := HostGenState.Standby
		}
		.elsewhen(io.transition === HostGenTransition.FaultEncountered) {
			state := HostGenState.FinalESTOP
		}
	}
	 is(HostGenState.HVPowerOn) {
		when(io.transition === HostGenTransition.ToInitialize) {
			state := HostGenState.Initialize
		}
		.elsewhen(io.transition === HostGenTransition.FaultEncountered) {
			state := HostGenState.FinalESTOP
		}
	}
	 is(HostGenState.HVTesting) {
		when(io.transition === HostGenTransition.ToInitialize) {
			state := HostGenState.Initialize
		}
		.elsewhen(io.transition === HostGenTransition.FaultEncountered) {
			state := HostGenState.FinalESTOP
		}
	}
	 is(HostGenState.Initialize) {
		when(io.transition === HostGenTransition.ToHVTesting) {
			state := HostGenState.HVTesting
		}
		.elsewhen(io.transition === HostGenTransition.StartPressurizing) {
			state := HostGenState.Pressurize
		}
		.elsewhen(io.transition === HostGenTransition.FaultEncountered) {
			state := HostGenState.FinalESTOP
		}
	}
	 is(HostGenState.Pressurize) {
		when(io.transition === HostGenTransition.MainLineat3000PSI) {
			state := HostGenState.SteeringAdjustments
		}
		.elsewhen(io.transition === HostGenTransition.FaultEncountered) {
			state := HostGenState.FinalESTOP
		}
	}
	 is(HostGenState.SteeringAdjustments) {
		when(io.transition === HostGenTransition.BeginBorePhaseI) {
			state := HostGenState.BorePhaseI
		}
		.elsewhen(io.transition === HostGenTransition.FaultEncountered) {
			state := HostGenState.FinalESTOP
		}
	}
	 is(HostGenState.BorePhaseI) {
		when(io.transition === HostGenTransition.Jacksat1foot) {
			state := HostGenState.BorePhaseII
		}
		.elsewhen(io.transition === HostGenTransition.PauseSystem) {
			state := HostGenState.SystemPause
		}
		.elsewhen(io.transition === HostGenTransition.FaultEncountered) {
			state := HostGenState.FinalESTOP
		}
	}
	 is(HostGenState.BorePhaseII) {
		when(io.transition === HostGenTransition.PauseSystem) {
			state := HostGenState.SystemPause
		}
		.elsewhen(io.transition === HostGenTransition.ActuatorsFullyExtended) {
			state := HostGenState.FinalRoutineStop
		}
		.elsewhen(io.transition === HostGenTransition.FaultEncountered) {
			state := HostGenState.FinalESTOP
		}
	}
	 is(HostGenState.SystemPause) {
		when(io.transition === HostGenTransition.ResumeBoringPhaseI) {
			state := HostGenState.BorePhaseI
		}
		.elsewhen(io.transition === HostGenTransition.ResumeBoringPhaseII) {
			state := HostGenState.BorePhaseII
		}
		.elsewhen(io.transition === HostGenTransition.FaultEncountered) {
			state := HostGenState.FinalESTOP
		}
	}
	 is(HostGenState.FinalRoutineStop) {
		when(io.transition === HostGenTransition.StartHV) {
			state := HostGenState.HVPowerOn
		}
		.elsewhen(io.transition === HostGenTransition.FaultEncountered) {
			state := HostGenState.FinalESTOP
		}
	}
	 is(HostGenState.FinalESTOP) {
		when(io.transition === HostGenTransition.EnterService) {
			state := HostGenState.Service
		}
	}

	}
	io.state := state
}
