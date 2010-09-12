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

package com.google.code.geobeagle.cachedetails;

import com.google.inject.Inject;

import android.content.Intent;
import android.webkit.WebView;

class DetailsWebView {
    private final CacheDetailsLoader cacheDetailsLoader;

    @Inject
    DetailsWebView(CacheDetailsLoader cacheDetailsLoader) {
        this.cacheDetailsLoader = cacheDetailsLoader;
    }

    String loadDetails(WebView webView, Intent intent) {
        webView.getSettings().setJavaScriptEnabled(true);
        String sourceName = intent.getStringExtra(DetailsActivity.INTENT_EXTRA_GEOCACHE_SOURCE);
        String id = intent.getStringExtra(DetailsActivity.INTENT_EXTRA_GEOCACHE_ID);
        String details = cacheDetailsLoader.load(sourceName, id);
        webView.loadDataWithBaseURL(null, details, "text/html", "utf-8", null);
        return id + ": " + intent.getStringExtra(DetailsActivity.INTENT_EXTRA_GEOCACHE_NAME);
    }
}