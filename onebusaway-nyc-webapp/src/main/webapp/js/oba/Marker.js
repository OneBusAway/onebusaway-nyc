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

OBA.Marker = function(entityId, latlng, map, popup, options) {
	var markerOptions = {
		position: new google.maps.LatLng(latlng[0], latlng[1])
	};

	var marker = new google.maps.Marker(jQuery.extend(markerOptions, options || {}));
	
	return {
		getMap: function() {
			return marker.getMap();
		},

		setMap: function(map) {
			// marker cannot be removed from map if it is attached to the current infoWindow.
			if(map === null) {
				if(OBA.popupMarker !== null && OBA.popupMarker.getId() === entityId) {
					return;
				}
			}

			marker.setMap(map);
		},

		addMarker: function() {
			marker.setMap(map);
		},

		removeMarker: function() {
			marker.setMap(null);
		},

		updateOrientation: function(_orientation) {
			var orientation = 'undefined';

			if(typeof _orientation !== 'undefined' && _orientation !== 'NaN') {
				orientation = Math.floor(_orientation / 5) * 5;
			}

			var icon = new google.maps.MarkerImage(OBA.Config.vehicleIconFilePrefix + '-' + orientation + '.' + OBA.Config.vehicleIconFileType,
					new google.maps.Size(OBA.Config.vehicleIconSize, OBA.Config.vehicleIconSize),
					new google.maps.Point(0,0),
					new google.maps.Point(OBA.Config.vehicleIconCenter, OBA.Config.vehicleIconCenter));

			marker.setIcon(icon);
		},

		updatePosition: function(latlng) {
			marker.setPosition(latlng);
		},

		getPosition: function() {
			return marker.getPosition();
		},

		getRawMarker: function() {
			return marker;
		},
		
		getPopup: function() {
			return popup;
		},
		
		refreshPopup: function() {
			popup.refresh();
		},
		
		isDisplayed: function() {
			return marker.getMap() !== null;
		},

		getType: function() {
			return options['type'];
		},
		
		getId: function() {
			return entityId;
		}
	};
};

OBA.StopMarker = function(stopId, latlng, direction, map, opts) {
	opts = jQuery.extend(opts || {}, {zIndex: 100, type: 'stop'});

	if(typeof opts.icon === 'undefined') {
		var iconPath = OBA.Config.stopIconFilePrefix + '.' + OBA.Config.stopIconFileType;

		if(direction !== null) {
			iconPath = OBA.Config.stopIconFilePrefix + '-' + direction + '.' + OBA.Config.stopIconFileType;
		}
	
		var icon = new google.maps.MarkerImage(iconPath,
					new google.maps.Size(OBA.Config.stopIconSize, OBA.Config.stopIconSize),
					new google.maps.Point(0,0),
					new google.maps.Point(OBA.Config.stopIconCenter, OBA.Config.stopIconCenter));

		opts.icon = icon;
	}
	
	var popup = OBA.StopPopup(stopId, map);
	var marker = OBA.Marker(stopId, latlng, map, popup, opts);
	var showPopup = function() { 
		OBA.Config.analyticsFunction("Stop Marker Click", OBA.Util.parseEntityId(stopId));
		popup.show(marker); 
	};
	
	marker.showPopup = showPopup;
	google.maps.event.addListener(marker.getRawMarker(), "click", showPopup);

	return marker;
};

OBA.VehicleMarker = function(vehicleId, latlng, orientation, map, opts) {
	opts = jQuery.extend(opts || {}, {zIndex: 200, type: 'vehicle'});

	if(typeof opts.icon === 'undefined') {
		var icon = new google.maps.MarkerImage(OBA.Config.vehicleIconFilePrefix + '-unknown.' + OBA.Config.vehicleIconFileType,
						new google.maps.Size(OBA.Config.vehicleIconSize, OBA.Config.vehicleIconSize),
						new google.maps.Point(0,0),
						new google.maps.Point(OBA.Config.vehicleIconCenter, OBA.Config.vehicleIconCenter));

		opts.icon = icon;
	}
	
	var popup = OBA.VehiclePopup(vehicleId, map);
	var marker = OBA.Marker(vehicleId, latlng, map, popup, opts);
	var showPopup = function() { 
		OBA.Config.analyticsFunction("Vehicle Marker Click", OBA.Util.parseEntityId(vehicleId));
		popup.show(marker); 
	};

	marker.showPopup = showPopup;
	google.maps.event.addListener(marker.getRawMarker(), "click", showPopup);

	if(typeof orientation !== 'undefined') {
		marker.updateOrientation(orientation);
	}

	return marker;
};
