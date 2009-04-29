/*
 ** Licensed under the Apache License, Version 2.0 (the "License");
 ** you may not use this file except in compliance with the License.
 ** You may obtain a copy of the License at
 **
 **     http://www.apache.org/licenses/LICENSE-2.0
 **
 ** Unless required by applicable law or agreed to in writing, software
 ** distributed under the License is distributed on an "AS IS" BASIS,
 ** WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 ** See the License for the specific language governing permissions and
 ** limitations under the License.
 */

package com.google.code.geobeagle.ui.cachelist;

import com.google.code.geobeagle.R;
import com.google.code.geobeagle.data.Geocache;
import com.google.code.geobeagle.data.GeocacheFromMyLocationFactory;
import com.google.code.geobeagle.io.LocationSaver;
import com.google.code.geobeagle.ui.ErrorDisplayer;

class MenuActionMyLocation implements MenuAction {
    private final ErrorDisplayer mErrorDisplayer;
    private final GeocacheFromMyLocationFactory mGeocacheFromMyLocationFactory;
    private final GeocacheListPresenter mGeocacheListPresenter;
    private final LocationSaver mLocationSaver;

    public MenuActionMyLocation(LocationSaver locationSaver,
            GeocacheFromMyLocationFactory geocacheFromMyLocationFactory,
            GeocacheListPresenter geocacheListPresenter, ErrorDisplayer errorDisplayer) {
        mLocationSaver = locationSaver;
        mGeocacheFromMyLocationFactory = geocacheFromMyLocationFactory;
        mGeocacheListPresenter = geocacheListPresenter;
        mErrorDisplayer = errorDisplayer;
    }

    public void act() {
        Geocache myLocation = mGeocacheFromMyLocationFactory.create();
        if (myLocation == null) {
            mErrorDisplayer.displayError(R.string.current_location_null);
            return;
        }
        mLocationSaver.saveLocation(myLocation);
        mGeocacheListPresenter.onResume();
    }
}
