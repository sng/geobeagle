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

package com.google.code.geobeagle.xmlimport;

import com.google.code.geobeagle.ErrorDisplayer;
import com.google.code.geobeagle.R;
import com.google.code.geobeagle.bcaching.BCachingAnnotations.BCachingUserName;
import com.google.code.geobeagle.cachedetails.FileDataVersionChecker;
import com.google.code.geobeagle.cachedetails.FileDataVersionWriter;
import com.google.code.geobeagle.database.DbFrontend;
import com.google.code.geobeagle.xmlimport.EventHelperDI.EventHelperFactory;
import com.google.code.geobeagle.xmlimport.XmlimportAnnotations.ImportDirectory;
import com.google.code.geobeagle.xmlimport.gpx.GpxAndZipFiles;
import com.google.code.geobeagle.xmlimport.gpx.IGpxReader;
import com.google.code.geobeagle.xmlimport.gpx.GpxAndZipFiles.GpxFilesAndZipFilesIter;
import com.google.inject.Provider;

import org.xmlpull.v1.XmlPullParserException;

import java.io.FileNotFoundException;
import java.io.IOException;

public class ImportThreadDelegate {

    public static class ImportThreadHelper {
        private final EventHandlers mEventHandlers;
        private final EventHelperFactory mEventHelperFactory;
        private final GpxLoader mGpxLoader;
        private boolean mHasFiles;
        private final MessageHandlerInterface mMessageHandler;
        private final OldCacheFilesCleaner mOldCacheFilesCleaner;
        private final Provider<String> mBCachingUserNameProvider;
        private final Provider<String> mImportFolderProvider;

        public ImportThreadHelper(GpxLoader gpxLoader, MessageHandlerInterface messageHandler,
                EventHelperFactory eventHelperFactory, EventHandlers eventHandlers,
                OldCacheFilesCleaner oldCacheFilesCleaner,
                @BCachingUserName Provider<String> bcachingUserNameProvider,
                @ImportDirectory Provider<String> importFolderProvider) {
            mGpxLoader = gpxLoader;
            mMessageHandler = messageHandler;
            mEventHelperFactory = eventHelperFactory;
            mEventHandlers = eventHandlers;
            mHasFiles = false;
            mOldCacheFilesCleaner = oldCacheFilesCleaner;
            mBCachingUserNameProvider = bcachingUserNameProvider;
            mImportFolderProvider = importFolderProvider;
        }

        public void cleanup() {
            mMessageHandler.loadComplete();
        }

        public void end() throws ImportException {
            mGpxLoader.end();
            if (!mHasFiles && mBCachingUserNameProvider.get().length() == 0)
                throw new ImportException(R.string.error_no_gpx_files, mImportFolderProvider.get());
        }

        public boolean processFile(IGpxReader gpxReader) throws XmlPullParserException, IOException {
            String filename = gpxReader.getFilename();

            mHasFiles = true;
            mGpxLoader.open(filename, gpxReader.open());
            int len = filename.length();
            String extension = filename.substring(Math.max(0, len - 3), len).toLowerCase();
            return mGpxLoader.load(mEventHelperFactory.create(mEventHandlers.get(extension)));
        }

        public void start() {
            mOldCacheFilesCleaner.clean();
            mGpxLoader.start();
        }

        public void startBCachingImport() {
            mMessageHandler.startBCachingImport();
        }
    }

    private final ErrorDisplayer mErrorDisplayer;
    private final GpxAndZipFiles mGpxAndZipFiles;
    private final ImportThreadHelper mImportThreadHelper;
    private final FileDataVersionWriter mFileDataVersionWriter;
    private final FileDataVersionChecker mFileDataVersionChecker;
    private final DbFrontend mDbFrontend;

    public ImportThreadDelegate(GpxAndZipFiles gpxAndZipFiles,
            ImportThreadHelper importThreadHelper, ErrorDisplayer errorDisplayer,
            FileDataVersionWriter fileDataVersionWriter,
            FileDataVersionChecker fileDataVersionChecker, DbFrontend dbFrontend) {
        mGpxAndZipFiles = gpxAndZipFiles;
        mImportThreadHelper = importThreadHelper;
        mErrorDisplayer = errorDisplayer;
        mFileDataVersionWriter = fileDataVersionWriter;
        mFileDataVersionChecker = fileDataVersionChecker;
        mDbFrontend = dbFrontend;
    }

    public void run() {
        try {
            tryRun();
        } catch (final FileNotFoundException e) {
            mErrorDisplayer.displayError(R.string.error_opening_file, e.getMessage());
            return;
        } catch (IOException e) {
            mErrorDisplayer.displayError(R.string.error_reading_file, e.getMessage());
            return;
        } catch (XmlPullParserException e) {
            mErrorDisplayer.displayError(R.string.error_parsing_file, e.getMessage());
            return;
        } catch (ImportException e) {
            mErrorDisplayer.displayError(e.getError(), e.getPath());
            return;
        } catch (CancelException e) {
            return;
        } finally {
            mImportThreadHelper.cleanup();
        }
        mImportThreadHelper.startBCachingImport();
    }

    protected void tryRun() throws IOException, XmlPullParserException, ImportException,
            CancelException {
        if (mFileDataVersionChecker.needsUpdating()) {
            mDbFrontend.forceUpdate();
        }
        GpxFilesAndZipFilesIter gpxFilesAndZipFilesIter = mGpxAndZipFiles.iterator();

        mImportThreadHelper.start();
        while (gpxFilesAndZipFilesIter.hasNext()) {
            if (!mImportThreadHelper.processFile(gpxFilesAndZipFilesIter.next())) {
                throw new CancelException();
            }
        }
        mFileDataVersionWriter.writeVersion();
        mImportThreadHelper.end();
    }
}
