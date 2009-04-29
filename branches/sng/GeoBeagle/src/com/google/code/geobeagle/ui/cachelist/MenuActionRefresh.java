
package com.google.code.geobeagle.ui.cachelist;

import com.google.code.geobeagle.LocationControlBuffered;
import com.google.code.geobeagle.R;
import com.google.code.geobeagle.data.CacheListData;
import com.google.code.geobeagle.data.Geocache;
import com.google.code.geobeagle.io.GeocachesSql;

import android.app.ListActivity;
import android.os.Handler;
import android.widget.Toast;

import java.util.ArrayList;

public class MenuActionRefresh implements MenuAction {

    static class SortRunnable implements Runnable {
        private final MenuActionRefresh mMenuActionRefresh;

        SortRunnable(MenuActionRefresh menuActionRefresh) {
            mMenuActionRefresh = menuActionRefresh;
        }

        public void run() {
            mMenuActionRefresh.sort();
        }
    }

    private final GeocachesSql mGeocachesSql;
    private final LocationControlBuffered mLocationControlBuffered;
    private final CacheListData mCacheListData;
    private final GeocacheListAdapter mGeocacheListAdapter;
    private final ListActivity mListActivity;
    private final Handler mHandler;

    public MenuActionRefresh(ListActivity listActivity, Handler handler,
            LocationControlBuffered locationControlBuffered, GeocachesSql geocachesSql,
            CacheListData cacheListData, GeocacheListAdapter geocacheListAdapter) {
        mGeocachesSql = geocachesSql;
        mCacheListData = cacheListData;
        mGeocacheListAdapter = geocacheListAdapter;
        mLocationControlBuffered = locationControlBuffered;
        mListActivity = listActivity;
        mHandler = handler;
    }

    public void act() {
        Toast.makeText(mListActivity, R.string.sorting, Toast.LENGTH_SHORT).show();
        mHandler.postDelayed(new MenuActionRefresh.SortRunnable(this), 200);
    }

    void sort() {
        mGeocachesSql.loadNearestCaches(mLocationControlBuffered);
        ArrayList<Geocache> geocaches = mGeocachesSql.getGeocaches();
        mCacheListData.add(geocaches, mLocationControlBuffered);
        mListActivity.setListAdapter(mGeocacheListAdapter);
        mListActivity.setTitle(mListActivity.getString(R.string.cache_list_title, geocaches.size(),
                mGeocachesSql.getCount()));
    }
}
