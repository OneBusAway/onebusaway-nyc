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
package org.onebusaway.nyc.vehicle_tracking.impl.sort;

import java.util.Comparator;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.onebusaway.nyc.vehicle_tracking.model.NycTestLocationRecord;

public class NycTestLocationRecordDestinationSignCodeComparator implements
    Comparator<NycTestLocationRecord> {

  @Override
  public int compare(NycTestLocationRecord o1, NycTestLocationRecord o2) {
    return new CompareToBuilder().append(o1.getDsc(), o2.getDsc()).toComparison();
  }
}
