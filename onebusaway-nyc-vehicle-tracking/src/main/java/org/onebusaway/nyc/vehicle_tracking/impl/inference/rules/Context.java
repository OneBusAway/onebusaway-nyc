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
package org.onebusaway.nyc.vehicle_tracking.impl.inference.rules;

import org.onebusaway.nyc.vehicle_tracking.impl.inference.Observation;
import org.onebusaway.nyc.vehicle_tracking.impl.inference.state.VehicleState;

public class Context {

  private final VehicleState parentState;

  private final VehicleState state;

  private final Observation observation;

  public Context(VehicleState parentState, VehicleState state,
      Observation observation) {
    this.parentState = parentState;
    this.state = state;
    this.observation = observation;
  }

  public VehicleState getParentState() {
    return parentState;
  }

  public VehicleState getState() {
    return state;
  }

  public Observation getObservation() {
    return observation;
  }
}
