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

import com.google.code.geobeagle.CombinedLocationListener;
import com.google.code.geobeagle.CombinedLocationManager;
import com.google.code.geobeagle.GeoBeagle;
import com.google.code.geobeagle.LocationControlBuffered;
import com.google.code.geobeagle.LocationControlDi;
import com.google.code.geobeagle.R;
import com.google.code.geobeagle.ResourceProvider;
import com.google.code.geobeagle.LocationControlBuffered.GpsDisabledLocation;
import com.google.code.geobeagle.data.CacheListData;
import com.google.code.geobeagle.data.DistanceFormatter;
import com.google.code.geobeagle.data.GeocacheFactory;
import com.google.code.geobeagle.data.GeocacheFromMyLocationFactory;
import com.google.code.geobeagle.data.GeocacheVectorFactory;
import com.google.code.geobeagle.data.GeocacheVectors;
import com.google.code.geobeagle.data.IGeocacheVector;
import com.google.code.geobeagle.io.CacheWriter;
import com.google.code.geobeagle.io.Database;
import com.google.code.geobeagle.io.DatabaseDI;
import com.google.code.geobeagle.io.GeocachesSql;
import com.google.code.geobeagle.io.GpxImporter;
import com.google.code.geobeagle.io.GpxImporterDI;
import com.google.code.geobeagle.io.LocationSaver;
import com.google.code.geobeagle.io.CacheReader.WhereFactoryAllCaches;
import com.google.code.geobeagle.io.CacheReader.WhereFactoryNearestCaches;
import com.google.code.geobeagle.io.DatabaseDI.SQLiteWrapper;
import com.google.code.geobeagle.io.GpxToCacheDI.XmlPullParserWrapper;
import com.google.code.geobeagle.ui.ErrorDisplayer;
import com.google.code.geobeagle.ui.GpsStatusWidget;
import com.google.code.geobeagle.ui.Misc;
import com.google.code.geobeagle.ui.UpdateGpsWidgetRunnableDI;
import com.google.code.geobeagle.ui.GpsStatusWidget.MeterFormatter;
import com.google.code.geobeagle.ui.GpsStatusWidget.MeterView;
import com.google.code.geobeagle.ui.GpsStatusWidget.UpdateGpsWidgetRunnable;
import com.google.code.geobeagle.ui.cachelist.GeocacheListPresenter.BaseAdapterLocationListener;
import com.google.code.geobeagle.ui.cachelist.MenuActionRefresh.ActionAndTolerance;
import com.google.code.geobeagle.ui.cachelist.MenuActionRefresh.AdapterCachesSorter;
import com.google.code.geobeagle.ui.cachelist.MenuActionRefresh.DistanceUpdater;
import com.google.code.geobeagle.ui.cachelist.MenuActionRefresh.SqlCacheLoader;
import com.google.code.geobeagle.ui.cachelist.MenuActionRefresh.Timing;
import com.google.code.geobeagle.ui.cachelist.MenuActionRefresh.TitleUpdater;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class CacheListDelegateDI {
    public static CacheListDelegate create(ListActivity listActivity, LayoutInflater layoutInflater) {
        final ErrorDisplayer errorDisplayer = new ErrorDisplayer(listActivity);
        final Database database = DatabaseDI.create(listActivity);
        final LocationManager locationManager = (LocationManager)listActivity
                .getSystemService(Context.LOCATION_SERVICE);
        final CombinedLocationManager combinedLocationManager = new CombinedLocationManager(
                locationManager);
        final LocationControlBuffered locationControlBuffered = LocationControlDi
                .create(locationManager);
        final GeocacheFactory geocacheFactory = new GeocacheFactory();
        final GeocacheFromMyLocationFactory geocacheFromMyLocationFactory = new GeocacheFromMyLocationFactory(
                geocacheFactory, locationControlBuffered);
        final SQLiteWrapper sqliteWrapper = new SQLiteWrapper(null);
        final GeocachesSql geocachesSql = DatabaseDI.create(sqliteWrapper);
        final CacheWriter cacheWriter = DatabaseDI.createCacheWriter(sqliteWrapper);
        final DistanceFormatter distanceFormatter = new DistanceFormatter();
        final LocationSaver locationSaver = new LocationSaver(cacheWriter);
        final GeocacheVectorFactory geocacheVectorFactory = new GeocacheVectorFactory(
                distanceFormatter);
        final ArrayList<IGeocacheVector> geocacheVectorsList = new ArrayList<IGeocacheVector>(10);
        final GeocacheVectors geocacheVectors = new GeocacheVectors(geocacheVectorFactory,
                geocacheVectorsList);
        final CacheListData cacheListData = new CacheListData(geocacheVectors);
        final XmlPullParserWrapper xmlPullParserWrapper = new XmlPullParserWrapper();
        final GpxImporter gpxImporter = GpxImporterDI.create(database, sqliteWrapper, listActivity,
                xmlPullParserWrapper, errorDisplayer);
        final View gpsWidgetView = layoutInflater.inflate(R.layout.gps_widget, null);

        final GeocacheSummaryRowInflater geocacheSummaryRowInflater = new GeocacheSummaryRowInflater(
                layoutInflater, geocacheVectors);

        final GeocacheListAdapter geocacheListAdapter = new GeocacheListAdapter(geocacheVectors,
                geocacheSummaryRowInflater);
        final MeterFormatter meterFormatter = new MeterFormatter();
        final MeterView meterView = new MeterView(getTextView(gpsWidgetView, R.id.location_viewer),
                meterFormatter);
        final Misc.Time time = new Misc.Time();

        final GpsStatusWidget gpsStatusWidget = new GpsStatusWidget(new ResourceProvider(
                listActivity), meterView, getTextView(gpsWidgetView, R.id.provider), getTextView(
                gpsWidgetView, R.id.lag), getTextView(gpsWidgetView, R.id.accuracy), getTextView(
                gpsWidgetView, R.id.status), time, new Location(""), combinedLocationManager);
        final CombinedLocationListener gpsStatusWidgetLocationListener = new CombinedLocationListener(
                locationControlBuffered, gpsStatusWidget);
        final UpdateGpsWidgetRunnable updateGpsWidgetRunnable = UpdateGpsWidgetRunnableDI
                .create(gpsStatusWidget);

        final WhereFactoryAllCaches whereFactoryAllCaches = new WhereFactoryAllCaches();
        final WhereFactoryNearestCaches whereFactoryNearestCaches = new WhereFactoryNearestCaches();
        final FilterNearestCaches filterNearestCaches = new FilterNearestCaches(
                whereFactoryAllCaches, whereFactoryNearestCaches);
        final ListTitleFormatter listTitleFormatter = new ListTitleFormatter();

        final Timing timing = new Timing();
        final TitleUpdater titleUpdater = new TitleUpdater(geocachesSql, listActivity,
                filterNearestCaches, cacheListData, listTitleFormatter, timing);
        final SqlCacheLoader sqlCacheLoader = new SqlCacheLoader(geocachesSql, filterNearestCaches,
                cacheListData, locationControlBuffered, titleUpdater, timing);
        final AdapterCachesSorter adapterCachesSorter = new AdapterCachesSorter(cacheListData,
                timing, locationControlBuffered);
        final GpsDisabledLocation gpsDisabledLocation = new GpsDisabledLocation();
        final DistanceUpdater distanceUpdater = new DistanceUpdater(geocacheListAdapter);
        final ActionAndTolerance[] actionAndTolerances = new ActionAndTolerance[] {
                new ActionAndTolerance(sqlCacheLoader, 500, gpsDisabledLocation),
                new ActionAndTolerance(adapterCachesSorter, 6, gpsDisabledLocation),
                new ActionAndTolerance(distanceUpdater, 1, gpsDisabledLocation)
        };
        final MenuActionRefresh menuActionRefresh = new MenuActionRefresh(locationControlBuffered,
                timing, actionAndTolerances);
        final MenuActionMyLocation menuActionMyLocation = new MenuActionMyLocation(locationSaver,
                geocacheFromMyLocationFactory, menuActionRefresh, errorDisplayer);

        final BaseAdapterLocationListener baseAdapterLocationListener = new BaseAdapterLocationListener(
                menuActionRefresh);
        final GeocacheListPresenter geocacheListPresenter = new GeocacheListPresenter(
                combinedLocationManager, locationControlBuffered, gpsStatusWidgetLocationListener,
                gpsWidgetView, updateGpsWidgetRunnable, geocacheVectors,
                baseAdapterLocationListener, listActivity, geocacheListAdapter, errorDisplayer,
                sqliteWrapper, database);
        final MenuActionToggleFilter menuActionToggleFilter = new MenuActionToggleFilter(
                filterNearestCaches, menuActionRefresh);
        final MenuActionSyncGpx menuActionSyncGpx = new MenuActionSyncGpx(gpxImporter,
                menuActionRefresh);
        final MenuActions menuActions = new MenuActions(menuActionSyncGpx, menuActionMyLocation,
                menuActionToggleFilter, menuActionRefresh);

        final ContextAction contextActions[] = CacheListDelegateDI.createContextActions(
                listActivity, database, sqliteWrapper, cacheListData, cacheWriter, geocacheVectors,
                errorDisplayer, geocacheListAdapter, titleUpdater);

        final GeocacheListController geocacheListController = new GeocacheListController(
                listActivity, menuActions, contextActions, sqliteWrapper, database, gpxImporter,
                menuActionRefresh, filterNearestCaches, errorDisplayer);
        return new CacheListDelegate(geocacheListController, geocacheListPresenter);
    }

    public static ContextAction[] createContextActions(ListActivity parent, Database database,
            SQLiteWrapper sqliteWrapper, CacheListData cacheListData, CacheWriter cacheWriter,
            GeocacheVectors geocacheVectors, ErrorDisplayer errorDisplayer,
            BaseAdapter geocacheListAdapter, TitleUpdater titleUpdater) {
        final Intent intent = new Intent(parent, GeoBeagle.class);
        final ContextActionView contextActionView = new ContextActionView(geocacheVectors, parent,
                intent);
        final ContextActionDelete contextActionDelete = new ContextActionDelete(
                geocacheListAdapter, cacheWriter, geocacheVectors, titleUpdater);
        return new ContextAction[] {
                contextActionDelete, contextActionView
        };
    }

    private static TextView getTextView(View gpsWidgetView, int id) {
        return (TextView)gpsWidgetView.findViewById(id);
    }
}
