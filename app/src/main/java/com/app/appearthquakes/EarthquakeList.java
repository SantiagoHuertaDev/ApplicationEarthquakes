package com.app.appearthquakes;

import java.util.List;

public class EarthquakeList {
    private List<EarthquakeFeature> earthquakes;

    public List<EarthquakeFeature> getEarthquakes() {
        return earthquakes;
    }

    public void setEarthquakes(List<EarthquakeFeature> earthquakes) {
        this.earthquakes = earthquakes;
    }
}
