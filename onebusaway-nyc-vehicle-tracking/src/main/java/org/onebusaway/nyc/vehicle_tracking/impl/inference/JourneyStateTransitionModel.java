/**
 * Copyright (c) 2011 Metropolitan Transportation Authority
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.onebusaway.nyc.vehicle_tracking.impl.inference;

import java.util.Arrays;
import java.util.List;

import org.onebusaway.nyc.vehicle_tracking.impl.inference.state.BlockState;
import org.onebusaway.nyc.vehicle_tracking.impl.inference.state.EdgeState;
import org.onebusaway.nyc.vehicle_tracking.impl.inference.state.JourneyPhaseSummary;
import org.onebusaway.nyc.vehicle_tracking.impl.inference.state.JourneyStartState;
import org.onebusaway.nyc.vehicle_tracking.impl.inference.state.JourneyState;
import org.onebusaway.nyc.vehicle_tracking.impl.inference.state.MotionState;
import org.onebusaway.nyc.vehicle_tracking.impl.inference.state.VehicleState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class JourneyStateTransitionModel {

  private JourneyPhaseSummaryLibrary _journeyStatePhaseLibrary = new JourneyPhaseSummaryLibrary();

  private BlockStateTransitionModel _blockStateTransitionModel;

  @Autowired
  public void setBlockStateTransitionModel(
      BlockStateTransitionModel blockStateTransitionModel) {
    _blockStateTransitionModel = blockStateTransitionModel;
  }

  /****
   * 
   * 
   ****/

  public void move(VehicleState parentState, EdgeState edgeState,
      MotionState motionState, Observation obs, List<VehicleState> results) {

    List<JourneyState> journeyStates = getTransitionJourneyStates(parentState,
        obs);

    generateVehicleStates(parentState, edgeState, motionState, journeyStates,
        obs, results);
  }

  public List<JourneyState> getTransitionJourneyStates(
      VehicleState parentState, Observation obs) {

    JourneyState parentJourneyState = parentState.getJourneyState();

    switch (parentJourneyState.getPhase()) {
      case AT_BASE:
        return moveAtBase(obs);
      case DEADHEAD_BEFORE:
        return moveDeadheadBefore(parentJourneyState);
      case LAYOVER_BEFORE:
        return moveLayoverBefore(obs);
      case IN_PROGRESS:
        return moveInProgress(obs);
      case DEADHEAD_DURING:
        return moveDeadheadDuring(obs, parentJourneyState);
      case LAYOVER_DURING:
        return moveLayoverDuring(obs);
      default:
        throw new IllegalStateException("unknown journey state: "
            + parentJourneyState.getPhase());
    }
  }

  private void generateVehicleStates(VehicleState parentState,
      EdgeState edgeState, MotionState motionState,
      List<JourneyState> journeyStates, Observation obs,
      List<VehicleState> results) {

    for (JourneyState journeyState : journeyStates) {

      BlockState blockState = _blockStateTransitionModel.transitionBlockState(
          parentState, motionState, journeyState, obs);

      List<JourneyPhaseSummary> summaries = _journeyStatePhaseLibrary.extendSummaries(
          parentState, blockState, journeyState, obs);

      VehicleState vehicleState = new VehicleState(edgeState, motionState,
          blockState, journeyState, summaries, obs);

      results.add(vehicleState);
    }
  }

  private List<JourneyState> moveAtBase(Observation obs) {

    return Arrays.asList(JourneyState.atBase(), JourneyState.layoverBefore(),
        JourneyState.deadheadBefore(obs.getLocation()),
        JourneyState.inProgress());
  }

  private List<JourneyState> moveDeadheadBefore(JourneyState parentJourneyState) {

    JourneyStartState start = parentJourneyState.getData();

    return Arrays.asList(JourneyState.atBase(), JourneyState.layoverBefore(),
        JourneyState.deadheadBefore(start.getJourneyStart()),
        JourneyState.inProgress());
  }

  private List<JourneyState> moveLayoverBefore(Observation obs) {

    return Arrays.asList(JourneyState.atBase(), JourneyState.layoverBefore(),
        JourneyState.deadheadBefore(obs.getLocation()),
        JourneyState.inProgress());
  }

  private List<JourneyState> moveInProgress(Observation obs) {

    return Arrays.asList(JourneyState.atBase(), JourneyState.inProgress(),
        JourneyState.deadheadDuring(obs.getLocation()),
        JourneyState.layoverDuring(),
        JourneyState.deadheadBefore(obs.getLocation()),
        JourneyState.layoverBefore());
  }

  private List<JourneyState> moveDeadheadDuring(Observation obs,
      JourneyState parentJourneyState) {

    JourneyStartState start = parentJourneyState.getData();

    return Arrays.asList(JourneyState.atBase(), JourneyState.inProgress(),
        JourneyState.deadheadDuring(start.getJourneyStart()),
        JourneyState.layoverDuring(),
        JourneyState.deadheadBefore(obs.getLocation()),
        JourneyState.layoverBefore());
  }

  private List<JourneyState> moveLayoverDuring(Observation obs) {

    return Arrays.asList(JourneyState.atBase(), JourneyState.inProgress(),
        JourneyState.deadheadDuring(obs.getLocation()),
        JourneyState.layoverDuring());
  }
}
