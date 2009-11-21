package com.google.code.geobeagle.database;

import com.google.code.geobeagle.Geocache;
import com.google.code.geobeagle.GeocacheList;
import com.google.code.geobeagle.GeocacheListPrecomputed;
import com.google.code.geobeagle.activity.main.GeoUtils;
import com.google.code.geobeagle.database.DistanceAndBearing.IDistanceAndBearingProvider;

import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.WeakHashMap;

/** Wraps another CachesProvider to make it sorted. Geocaches closer to 
 * the provided center come first in the getCaches list. 
 * Until setCenter has been called, the list will not be sorted. */
public class CachesProviderSorted implements ICachesProviderCenter,
IDistanceAndBearingProvider {

    private final ICachesProvider mCachesProvider;
    private boolean mHasChanged = true;
    private double mLatitude;
    private double mLongitude;
    /** Value is null if the list needs to be re-sorted */
    private GeocacheList mSortedList = null;
    private DistanceComparator mDistanceComparator;
    private boolean isInitialized = false;

    private class DistanceComparator implements Comparator<Geocache> {
        public int compare(Geocache geocache1, Geocache geocache2) {
            final float d1 = getDistanceAndBearing(geocache1).getDistance();
            final float d2 = getDistanceAndBearing(geocache2).getDistance();
            if (d1 < d2)
                return -1;
            if (d1 > d2)
                return 1;
            return 0;
        }
    }
    
    public CachesProviderSorted(ICachesProvider cachesProvider) {
        mCachesProvider = cachesProvider;
        mDistanceComparator = new DistanceComparator();
        isInitialized = false;
    }

    private WeakHashMap<Geocache, DistanceAndBearing> mDistances =
        new WeakHashMap<Geocache, DistanceAndBearing>();
    
    public DistanceAndBearing getDistanceAndBearing(Geocache cache) {
        DistanceAndBearing d = mDistances.get(cache);
        if (d == null) {
            float distance = cache.getDistanceTo(mLatitude, mLongitude);
            float bearing = (float)GeoUtils.bearing(mLatitude, mLongitude,
                    cache.getLatitude(), cache.getLongitude());
            d = new DistanceAndBearing(cache, distance, bearing);
            mDistances.put(cache, d);
        }
        return d;
    }
    
    /** Updates mSortedList to a sorted version of the current underlying cache list*/
    private void updateSortedList() {
        if (mSortedList != null && !mCachesProvider.hasChanged())
            //No need to update
            return;

        final GeocacheList unsortedList = mCachesProvider.getCaches();
        ArrayList<Geocache> sortedList = new ArrayList<Geocache>(unsortedList);

        Collections.sort(sortedList, mDistanceComparator);
        mSortedList = new GeocacheListPrecomputed(sortedList);
    }

    @Override
    public GeocacheList getCaches() {
        if (!isInitialized) {
            return mCachesProvider.getCaches();
        }

        updateSortedList();
        return mSortedList;
    }

    @Override
    public int getCount() {
        return mCachesProvider.getCount();
    }

    @Override
    public boolean hasChanged() {
        return mHasChanged || mCachesProvider.hasChanged();
    }

    @Override
    public void resetChanged() {
        mHasChanged = false;
        if (mCachesProvider.hasChanged())
            mSortedList = null;
        mCachesProvider.resetChanged();
    }

    @Override
    public void setCenter(double latitude, double longitude) {
        //TODO: Not good enough to compare doubles with '=='?
        if (isInitialized && latitude == mLatitude && longitude == mLongitude)
            return;
        //This only sets what position the caches are sorted against,
        //not which caches are selected for sorting!
        mLatitude = latitude;
        mLongitude = longitude;
        mHasChanged = true;
        isInitialized = true;
        mSortedList = null;
        mDistances.clear();
    }

    public double getFurthestCacheDistance() {
        if (!isInitialized) {
            Log.e("GeoBeagle", "getFurthestCacheDistance called before setCenter");
            return 0;
        }
        if (mSortedList == null || mCachesProvider.hasChanged()) {
            updateSortedList();
        }
        if (mSortedList.size() == 0)
            return 0;
        return mSortedList.get(mSortedList.size()-1).getDistanceTo(mLatitude, mLongitude);
    }
}
