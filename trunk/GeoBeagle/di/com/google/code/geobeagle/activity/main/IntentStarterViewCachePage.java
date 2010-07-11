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

package com.google.code.geobeagle.activity.main;

import com.google.code.geobeagle.ErrorDisplayer;
import com.google.code.geobeagle.activity.main.intents.GeocacheToCachePage;
import com.google.code.geobeagle.activity.main.intents.IntentFactory;
import com.google.code.geobeagle.activity.main.intents.IntentStarterViewUri;
import com.google.inject.Inject;

class IntentStarterViewCachePage extends IntentStarterViewUri {
    @Inject
    public IntentStarterViewCachePage(GeoBeagle geoBeagle, IntentFactory intentFactory,
            GeocacheToCachePage geocacheToCachePage, ErrorDisplayer errorDisplayer) {
        super(geoBeagle, intentFactory, geocacheToCachePage, errorDisplayer);
    }
}