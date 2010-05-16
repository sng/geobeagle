
package com.google.code.geobeagle.bcaching;

import com.google.code.geobeagle.ErrorDisplayer;
import com.google.code.geobeagle.bcaching.BCachingAnnotations.DetailsReaderAnnotation;
import com.google.code.geobeagle.bcaching.communication.BCachingException;
import com.google.code.geobeagle.bcaching.communication.BCachingListImportHelper.BufferedReaderFactory;
import com.google.code.geobeagle.xmlimport.CachePersisterFacade;
import com.google.code.geobeagle.xmlimport.EventHelper;
import com.google.code.geobeagle.xmlimport.FileAlreadyLoadedChecker;
import com.google.code.geobeagle.xmlimport.GpxLoader;
import com.google.code.geobeagle.xmlimport.GpxToCache;
import com.google.code.geobeagle.xmlimport.GpxToCache.Aborter;
import com.google.code.geobeagle.xmlimport.GpxToCacheDI.XmlPullParserWrapper;
import com.google.code.geobeagle.xmlimport.XmlimportAnnotations.GpxAnnotation;
import com.google.inject.Inject;

import org.xmlpull.v1.XmlPullParserException;

import android.os.PowerManager.WakeLock;

import java.io.BufferedReader;
import java.util.Hashtable;

public class DetailsReaderImport {
    private final Hashtable<String, String> params;
    private final BufferedReaderFactory bufferedReaderFactory;
    private final GpxLoader gpxLoader;
    private EventHelper eventHelper;

    @Inject
    DetailsReaderImport(@DetailsReaderAnnotation Hashtable<String, String> params,
            BufferedReaderFactory bufferedReaderFactory, ErrorDisplayer errorDisplayer,
            WakeLock wakeLock, CachePersisterFacade cachePersisterFacade,
            @GpxAnnotation EventHelper eventHelper, GpxToCache gpxToCache) {
        this.params = params;
        this.bufferedReaderFactory = bufferedReaderFactory;
        this.gpxLoader = new GpxLoader(cachePersisterFacade, errorDisplayer, gpxToCache, wakeLock);
        this.eventHelper = eventHelper;
    }

    public void getCacheDetails(String csvIds, int updatedCaches) throws BCachingException {
        params.put("ids", csvIds);

        try {
            BufferedReader bufferedReader = bufferedReaderFactory.create(params);
            gpxLoader.open("BCaching.com." + updatedCaches, bufferedReader);
        } catch (XmlPullParserException e) {
            throw new BCachingException("Error parsing data from baching.com: "
                    + e.getLocalizedMessage());
        }
        gpxLoader.load(eventHelper);
    }
}
