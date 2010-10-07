package org.onebusaway.nyc.vehicle_tracking.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.nyc.stif.StifTripLoader;
import org.onebusaway.nyc.vehicle_tracking.model.DestinationSignCodeRecord;
import org.onebusaway.nyc.vehicle_tracking.services.VehicleTrackingDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Load STIF data, including the mapping between destination sign codes and trip
 * ids, into the database
 * 
 * @author bdferris
 * 
 */
public class StifTask implements Runnable {

  private Logger _log = LoggerFactory.getLogger(StifTask.class);

  private GtfsMutableRelationalDao _gtfsMutableRelationalDao;

  private VehicleTrackingDao _vehicleTrackingDao;

  private List<File> _stifPaths = new ArrayList<File>();

  private Set<String> _notInServiceDscs = new HashSet<String>();

  private File _notInServiceDscPath;

  @Autowired
  public void setGtfsMutableRelationalDao(
      GtfsMutableRelationalDao gtfsMutableRelationalDao) {
    _gtfsMutableRelationalDao = gtfsMutableRelationalDao;
  }

  @Autowired
  public void setVehicleTrackingDao(VehicleTrackingDao vehicleTrackingDao) {
    _vehicleTrackingDao = vehicleTrackingDao;
  }

  /**
   * The path of the directory containing STIF files to process
   */
  public void setStifPath(File path) {
    _stifPaths.add(path);
  }

  public void setStifPaths(List<File> paths) {
    _stifPaths.addAll(paths);
  }

  public void setNotInServiceDsc(String notInServiceDsc) {
    _notInServiceDscs.add(notInServiceDsc);
  }

  public void setNotInServiceDscs(List<String> notInServiceDscs) {
    _notInServiceDscs.addAll(notInServiceDscs);
  }

  public void setNotInServiceDscPath(File notInServiceDscPath) {
    _notInServiceDscPath = notInServiceDscPath;
  }

  public void run() {

    StifTripLoader loader = new StifTripLoader();
    loader.setGtfsDao(_gtfsMutableRelationalDao);

    for (File path : _stifPaths)
      loadStif(path, loader);

    Map<String, List<AgencyAndId>> tripMapping = loader.getTripMapping();

    Set<String> inServiceDscs = new HashSet<String>();

    for (Map.Entry<String, List<AgencyAndId>> entry : tripMapping.entrySet()) {
      String destinationSignCode = entry.getKey();
      List<AgencyAndId> tripIds = entry.getValue();
      for (AgencyAndId tripId : tripIds) {
        DestinationSignCodeRecord record = new DestinationSignCodeRecord();
        record.setDestinationSignCode(destinationSignCode);
        record.setTripId(tripId);
        _vehicleTrackingDao.saveOrUpdateDestinationSignCodeRecord(record);
      }
    }

    int withoutMatch = loader.getTripsWithoutMatchCount();
    int total = loader.getTripsCount();

    _log.info("stif trips without match: " + withoutMatch + " / " + total);

    readNotInServiceDscs();

    for (String notInServiceDsc : _notInServiceDscs) {

      if (inServiceDscs.contains(notInServiceDsc))
        _log.warn("overlap between in-service and not-in-service dscs: "
            + notInServiceDsc);

      DestinationSignCodeRecord record = new DestinationSignCodeRecord();
      record.setDestinationSignCode(notInServiceDsc);
      record.setTripId(null);
      _vehicleTrackingDao.saveOrUpdateDestinationSignCodeRecord(record);
    }

  }

  public void loadStif(File path, StifTripLoader loader) {

    // Exclude files and directories like .svn
    if (path.getName().startsWith("."))
      return;

    if (path.isDirectory()) {
      for (String filename : path.list()) {
        File contained = new File(path, filename);
        loadStif(contained, loader);
      }
    } else {
      loader.run(path);
    }
  }

  private void readNotInServiceDscs() {
    if (_notInServiceDscPath != null) {
      try {
        BufferedReader reader = new BufferedReader(new FileReader(
            _notInServiceDscPath));
        String line = null;
        while ((line = reader.readLine()) != null)
          _notInServiceDscs.add(line);
      } catch (IOException ex) {
        throw new IllegalStateException("unable to read nonInServiceDscPath: "
            + _notInServiceDscPath);
      }
    }
  }
}
