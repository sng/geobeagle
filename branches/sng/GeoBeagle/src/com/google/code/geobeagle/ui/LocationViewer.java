
package com.google.code.geobeagle.ui;

import com.google.code.geobeagle.R;
import com.google.code.geobeagle.Util;

import android.location.Location;
import android.location.LocationProvider;

/**
 * @author sng Displays the current location as well as the GPS status.
 */
public class LocationViewer {
    private final MockableContext mContext;
    private final MockableTextView mCoordinates;
    private final MockableTextView mLastUpdateTime;
    private final MockableTextView mStatus;

    public LocationViewer(MockableContext context, MockableTextView coordinates,
            MockableTextView lastUpdateTime, MockableTextView status) {
        this.mContext = context;
        this.mCoordinates = coordinates;
        this.mLastUpdateTime = lastUpdateTime;
        this.mStatus = status;
        this.mCoordinates.setText(R.string.getting_location_from_gps);
    }

    public void setDisabled() {
        mStatus.setText("DISABLED");
    }

    public void setEnabled() {
        mStatus.setText("ENABLED");
    }

    public void setLocation(Location location) {
        setLocation(location, location.getTime());
    }

    public void setLocation(Location location, long time) {
        mCoordinates.setText(location.getProvider() + ": "
                + Util.formatDegreesAsDecimalDegreesString(location.getLatitude()) + " "
                + Util.formatDegreesAsDecimalDegreesString(location.getLongitude()) + "  �"
                + location.getAccuracy() + "m");
        mLastUpdateTime.setText(Util.formatTime(time));
    }

    public void setStatus(String provider, int status) {
        switch (status) {
            case LocationProvider.OUT_OF_SERVICE:
                mStatus.setText(provider + " status: "
                        + mContext.getString(R.string.out_of_service));
                break;
            case LocationProvider.AVAILABLE:
                mStatus.setText(provider + " status: " + mContext.getString(R.string.available));
                break;
            case LocationProvider.TEMPORARILY_UNAVAILABLE:
                mStatus.setText(provider + " status: "
                        + mContext.getString(R.string.temporarily_unavailable));
                break;
        }
    }
}
