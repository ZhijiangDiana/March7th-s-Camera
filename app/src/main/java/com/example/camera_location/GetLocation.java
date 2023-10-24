package com.example.camera_location;

import android.location.Location;
import utils.GPSUtils;

public class GetLocation implements Runnable{

    @Override
    public void run() {
        GPSUtils gpsIns = GPSUtils.getInstance();
        while (true) {
            Location location = gpsIns.getLatLon(CameraDemoActivity.cameraDemoActivity);
            Variable.location = location;
            Variable.address = gpsIns.getAddress(CameraDemoActivity.cameraDemoActivity, location.getLatitude(), location.getLongitude());
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
