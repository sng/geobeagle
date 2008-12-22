
package com.google.code.geobeagle;

import android.location.Location;

public interface LocationViewer {

    void setLocation(Location location, long time);

    void setLocation(Location location);

    void setStatus(int status);
}