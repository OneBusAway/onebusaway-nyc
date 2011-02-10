/*
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

var OBA = window.OBA || {};

OBA.Sign = function() {
	var refreshInterval = 5;
	var configurableMessageHtml = null;
	var stopIdsToRequest = null;
	var vehiclesPerStop = 3;
	
	function getParameterByName(name, defaultValue) {
		name = name.replace(/[\[]/,"\\\[").replace(/[\]]/,"\\\]");
		var regexS = "[\\?&]"+name+"=([^&#]*)";
		var regex = new RegExp(regexS);
		var results = regex.exec(window.location.href);
		if(results == null) {
			return defaultValue;
		} else {
			return decodeURIComponent(results[1].replace(/\+/g, " "));
		}
	}

	function detectSize() {
		var w = jQuery(window).width();

		if (w < 800) {
			jQuery('body').removeClass();
		} else if (w >= 1800) {
			jQuery('body').removeClass().addClass('landscape-1800');
		} else if (w >= 1500) {
			jQuery('body').removeClass().addClass('landscape-1500');
		} else if (w >= 1200) {
			jQuery('body').removeClass().addClass('landscape-1200');
		} else if (w >= 1000) {
			jQuery('body').removeClass().addClass('landscape-1000');
		} else if (w >= 800) {
			jQuery('body').removeClass().addClass('landscape-800');
		}
	}
	
	function setupUI() {
		// configure interface with URL params
		refreshInterval = getParameterByName("refresh", refreshInterval);

		configurableMessageHtml = getParameterByName("message", null);

		if(configurableMessageHtml !== null) {
			var header = jQuery("#branding");

			jQuery("<p></p>")
					.attr("id", "message")
					.text(configurableMessageHtml)
					.appendTo(header);
		}

		var stopIds = getParameterByName("stopIds", null);
		
		if(stopIds !== null) {
			stopIdsToRequest = [];
			jQuery.each(stopIds.split(","), function(_, o) {
				stopIdsToRequest.push(OBA.Config.agencyId + "_" + o);
			});
		} else {
			showError("No stops are configured for display.");
		}

		// add event handlers
		detectSize();
		jQuery.event.add(window, "resize", detectSize);
		
		jQuery("#arrivals").empty();
	}

	function getNewTableForStop(stopId, name) {
		var table = jQuery("<table></table>")
						.addClass("stop" + stopId);
		
		jQuery('<thead>' + 
					'<tr>' + 
						'<th class="stop">' + 
							name + 
							' <span class="stop-id">Stop #' + stopId + '</span>' + 
						'</th>' + 
						'<th class="instructions">' + 
							OBA.Config.infoBubbleFooterFunction("sign", stopId) +
						'</th>' +
						'<th class="qr">' + 
							'<img src="http://transit.frumin.net/mta/qrcode/qr.bustime/' + stopId + '.png" alt="QR Code"/>' + 
						'</th>' +
					'</tr>' + 
				 '</thead>')
				 .appendTo(table);

		jQuery("<tbody></tbody>")
				 .appendTo(table);
		
		return table;
	}
	
	function updateTableForStop(stopTable, applicableSituations, routeToHeadsign, routeToVehicleInfo) {
		if(stopTable === null) {
			return;
		}
		
		var tableBody = stopTable.find("tbody");
		tableBody.empty();

		var tableHeader = stopTable.find("thead tr th");
		tableHeader.find("p.alert").remove();
		
		// situations
		jQuery.each(applicableSituations, function(_, situation) {
			var alert = jQuery("<p></p>")
							.addClass("alert")
							.text(situation);
			
			stopTable.find("thead tr th.stop").append(alert);
		});
		
		// arrivals
		jQuery.each(routeToVehicleInfo, function(routeId, distanceAways) {
			var headsign = routeToHeadsign[routeId];
			
			if(distanceAways.length === 0) {
				jQuery('<tr class="last">' + 
						'<td colspan="3">' + 
							'OneBusAway NYC is not tracking any buses en-route to this stop. Please check back shortly for an update.</li>' +
						'</td>' +
					   '</tr>')
					   .appendTo(tableBody);
			} else {			
				// sort based on distance
				distanceAways.sort(function(a, b) { return a.feet - b.feet; });
			
				jQuery.each(distanceAways, function(_, distanceAway) {
					if((_ + 1) > vehiclesPerStop) {
						return;
					}
						
					var row = jQuery("<tr></tr>");

					if((_ + 1) === vehiclesPerStop || (_ + 1) === distanceAways.length) {
						row.addClass("last");
					}
					
					// name cell
					var vehicleIdSpan = jQuery("<span></span>")
											.addClass("bus-id")
											.text(" Bus #" + OBA.Util.parseEntityId(distanceAway.tripStatus.vehicleId));
	
					jQuery('<td></td>')
						.text(headsign)
						.append(vehicleIdSpan)
						.appendTo(row);
				
					// distance cell
					jQuery('<td colspan="2"></td>')
						.addClass("distance")
						.text(OBA.Util.displayDistance(distanceAway.feet, distanceAway.stops, "stop", distanceAway.tripStatus))
						.appendTo(row);
						
					tableBody.append(row);				
				});
			}
		});
	}
	
	function showError(message) {
		hideError();
		
		var error = jQuery("<p></p>")
						.html(message);

		jQuery("#error").append(error);
		jQuery("#arrivals").empty();
	}
	
	function hideError() {
		jQuery("#error").children().remove();
	}
	
	function update() {
		var arrivalsDiv = jQuery("#arrivals");
		
		if(stopIdsToRequest === null) {
			return;
		}
		
		stopIdsToRequest.sort();
		
		jQuery.each(stopIdsToRequest, function(_, stopId) {
			if(stopId === null || stopId === "") {
				return;
			}

			var url = OBA.Config.stopUrl + "/" + stopId + ".json?callback=?";
			var params = {version: 2, key: OBA.Config.apiKey, minutesBefore: OBA.Config.arrivalsMinutesBefore, 
					minutesAfter: OBA.Config.arrivalsMinutesAfter};
			
			jQuery.jsonp({
				url: url,
				data: params,
				cache: false,
				timeout: (refreshInterval * 1000000),
				success: function(json, status) {	
					hideError();
					
					var stop, arrivals, refs = null;
					try {
						stop = json.data.references.stops[0];
						arrivals = json.data.entry.arrivalsAndDepartures;
						refs = json.data.references;
					} catch (typeError) {
						OBA.Util.log("invalid stop response from server");
						OBA.Util.log(json);
						return;
					}

					var applicableSituationIds = {};
					var routeToHeadsign = {};
					var routeToVehicleInfo = {};
					jQuery.each(arrivals, function(_, arrival) {
						var routeId = arrival.routeId;
						var arrivalStopId = arrival.stopId;
						var headsign = arrival.tripHeadsign;

						if (arrivalStopId !== stopId || routeId === null || headsign === null) {
							return;
						}

						if (! routeToVehicleInfo[routeId]) {
							routeToVehicleInfo[routeId] = [];
						}

						// most common headsign? FIXME
						routeToHeadsign[routeId] = headsign;

						// build map of situation IDs applicable to this stop
						jQuery.each(arrival.situationIds, function(_, situationId) {
							applicableSituationIds[situationId] = situationId;
						});			

						// hide non-realtime observations (they are used above to build a picture of what
						// stops at this stop usually).
						if(arrival.predicted === false || arrival.vehicleId === null || arrival.vehicleId === "") {
							return;
						}

						OBA.Util.log("A-D FOR STOP: VID=" + arrival.vehicleId);			

						// hide arrivals that just left the stop 
						if(arrival.distanceFromStop < 0) {
							OBA.Util.log("   --- HIDING BECAUSE OF DIST. FROM STOP (" + arrival.distanceFromStop + ")");
							return;
						}

						// if a vehicle is in progress, it has to be on the A-D's current trip to show up in bubble.
						// If a vehicle is in layover, bus should show if it's on A-D's current trip or the previous trip
						// /and/ over 50% complete in previous trip progress.
						if(arrival.tripStatus !== null) {
							var phase = ((typeof arrival.tripStatus.phase !== 'undefined' && arrival.tripStatus.phase !== '') 
									? arrival.tripStatus.phase : null);

							if(phase !== null
									&& (phase.toLowerCase() === 'layover_before' || phase.toLowerCase() === 'layover_during')) {	

								var distanceAlongTrip = arrival.tripStatus.distanceAlongTrip;
								var totalDistanceAlongTrip = arrival.tripStatus.totalDistanceAlongTrip;
								if(distanceAlongTrip !== null && totalDistanceAlongTrip !== null) {
									var ratio = distanceAlongTrip / totalDistanceAlongTrip;
									if(arrival.tripStatus.activeTripId !== arrival.tripId 
											&& ((arrival.blockTripSequence - 1) !== arrival.tripStatus.blockTripSequence && ratio > 0.50)) {

										OBA.Util.log("   --- HIDING LAYOVER VEHICLE: IS NOT ON PROPER TRIP (RATIO=" + ratio + ").");
										return;
									}
								}
							} else {
								if(arrival.tripStatus.activeTripId !== arrival.tripId) {
									OBA.Util.log("   --- HIDING NON-LAYOVER VEHICLE: IS NOT ON A-D'S TRIP.");
									return;						
								} 
							}
						}

						if(arrival.tripStatus === null || OBA.Config.vehicleFilterFunction("stop", arrival.tripStatus) === false) {
							OBA.Util.log("   --- HIDING BECAUSE OF FILTER FUNCTION");
							return;          
						}

						var meters = arrival.distanceFromStop;
						var feet = OBA.Util.metersToFeet(meters);
						var stops = arrival.numberOfStopsAway;

						var vehicleInfo = {stops: stops,
								feet: feet,
								tripStatus: arrival.tripStatus};

						OBA.Util.log("   +++ ADDING TO ARRIVAL LIST");
						routeToVehicleInfo[routeId].push(vehicleInfo);				
					}); // each arrival

					// build array of applicable situations
					var applicableSituations = [];
					jQuery.each(refs.situations, function(_, situation) {
						var situationId = situation.id;
						if(situationId in applicableSituationIds) {
							applicableSituations.push(situation.description.value);
						}
					});

					// update table for this stop ID
					var stopTable = jQuery("table.stop" + OBA.Util.parseEntityId(stopId));

					if(stopTable.length <= 0) {
						stopTable = getNewTableForStop(OBA.Util.parseEntityId(stopId), stop.name);
						arrivalsDiv.append(stopTable);
					}

					updateTableForStop(stopTable, applicableSituations, routeToHeadsign, routeToVehicleInfo);
				},
				error: function(xOptions, text) {
					try {
						xOptions.abort();
					} catch(e) {}

					showError("An error occured while updating arrival information&mdash;please check back later.");
				}
			}); // get JSON
		});

        setTimeout(update, refreshInterval * 1000);
	}
	
	return {
		initialize: function() {			
			setupUI();			
			update();
		}
	};
};

jQuery(document).ready(function() { OBA.Sign().initialize(); });
