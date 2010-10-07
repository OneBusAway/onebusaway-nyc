/*
 * Copyright 2010, OpenPlans Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.onebusaway.nyc.stif;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.onebusaway.gtfs.impl.HibernateGtfsRelationalDaoImpl;
import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs.services.HibernateOperation;
import org.onebusaway.nyc.stif.model.ServiceCode;
import org.onebusaway.nyc.stif.model.TripRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Create a mapping from Destination Sign Code (DSC) to GTFS Trip objects using
 * data in STIF, MTA's internal format.
 */
public class StifTripLoaderSupport {

  private static final Logger _log = LoggerFactory.getLogger(StifTripLoaderSupport.class);

  private GtfsMutableRelationalDao gtfsDao;

  private Map<TripIdentifier, List<Trip>> tripsByIdentifier;

  private Map<String, String> stopIdsByLocation = new HashMap<String, String>();

  private int _totalTripCount;

  public void setGtfsDao(GtfsMutableRelationalDao dao) {
    this.gtfsDao = dao;
  }

  public static ServiceCode scheduleIdForGtfsDayCode(char dayCode) {
    switch (dayCode) {
      case 'A':
        return ServiceCode.SATURDAY;
      case 'B':
        return ServiceCode.WEEKDAY_SCHOOL_CLOSED;
      case 'E':
      case 'C':
        return ServiceCode.WEEKDAY_SCHOOL_OPEN;
      case 'D':
        return ServiceCode.SUNDAY;
      default:
        return null;
    }
  }

  public int getTotalTripCount() {
    return _totalTripCount;
  }

  public void putStopIdForLocation(String location, String stopId) {
    stopIdsByLocation.put(location, stopId);
  }

  public TripIdentifier getIdentifierForTripRecord(TripRecord tripRecord) {
    String routeName = tripRecord.getSignCodeRoute();
    if (routeName == null || routeName.trim().length() == 0)
      routeName = tripRecord.getRoute();
    String startStop = getStopIdForLocation(tripRecord.getOriginLocation());
    int startTime = tripRecord.getOriginTime();
    if (startTime < 0) {
      // skip a day ahead for previous-day trips.
      startTime += 24 * 60 * 60;
    }
    return new TripIdentifier(routeName, startTime, startStop);
  }

  public List<Trip> getTripsForIdentifier(TripIdentifier id) {

    /**
     * Lazy initialization
     */
    if (tripsByIdentifier == null) {

      tripsByIdentifier = new HashMap<TripIdentifier, List<Trip>>();

      Collection<Trip> allTrips = gtfsDao.getAllTrips();
      _totalTripCount = allTrips.size();
      int index = 0;

      for (Trip trip : allTrips) {

        if (index % 500 == 0)
          _log.info("trip=" + index + " / " + allTrips.size());
        index++;

        TripIdentifier tripIdentifier = getTripAsIdentifier(trip);

        List<Trip> trips = tripsByIdentifier.get(tripIdentifier);
        if (trips == null) {
          trips = new ArrayList<Trip>();
          tripsByIdentifier.put(tripIdentifier, trips);
        }
        trips.add(trip);
      }
    }

    return tripsByIdentifier.get(id);
  }

  /****
   * Private Methods
   ****/

  private String getStopIdForLocation(String originLocation) {
    return stopIdsByLocation.get(originLocation);
  }

  private TripIdentifier getTripAsIdentifier(final Trip trip) {

    String routeName = trip.getRoute().getId().getId();
    int startTime = -1;
    String startStop;

    /**
     * This is WAY faster if we are working in hibernate, in that we don't need
     * to look up EVERY StopTime for a Trip, along with the Stop objects for
     * those StopTimes. Instead, we just grab the first departure time.
     */
    if (gtfsDao instanceof HibernateGtfsRelationalDaoImpl) {
      HibernateGtfsRelationalDaoImpl dao = (HibernateGtfsRelationalDaoImpl) gtfsDao;
      List<?> rows = (List<?>) dao.execute(new HibernateOperation() {
        @Override
        public Object doInHibernate(Session session) throws HibernateException,
            SQLException {
          Query query = session.createQuery("SELECT st.departureTime, st.stop.id.id FROM StopTime st WHERE st.trip = :trip AND st.departureTime >= 0 ORDER BY st.departureTime ASC LIMIT 1");
          query.setParameter("trip", trip);
          return query.list();
        }
      });
      Object[] values = (Object[]) rows.get(0);
      startTime = ((Integer) values[0]);
      startStop = (String) values[1];
    } else {
      StopTime stopTime = gtfsDao.getStopTimesForTrip(trip).get(0);
      startTime = stopTime.getDepartureTime();
      startStop = stopTime.getStop().getId().getId();
    }
    return new TripIdentifier(routeName, startTime, startStop);
  }

}
