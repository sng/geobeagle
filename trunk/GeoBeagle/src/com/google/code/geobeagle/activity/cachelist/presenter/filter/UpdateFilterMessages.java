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

package com.google.code.geobeagle.activity.cachelist.presenter.filter;

public enum UpdateFilterMessages {

    DISMISS_APPLY_FILTER_PROGRESS {
        @Override
        void handleMessage(UpdateFilterMediator updateFilterMediator, int arg1) {
            updateFilterMediator.dismissApplyFilterProgress();
        }
    },
    DISMISS_CLEAR_FILTER_PROGRESS {
        @Override
        void handleMessage(UpdateFilterMediator updateFilterMediator, int arg1) {
            updateFilterMediator.dismissClearFilterProgress();
        }
    },
    INCREMENT_APPLY_FILTER_PROGRESS {
        @Override
        void handleMessage(UpdateFilterMediator updateFilterMediator, int arg1) {
            updateFilterMediator.incrementApplyFilterProgress();
        }
    },
    SHOW_APPLY_FILTER_PROGRESS {
        @Override
        void handleMessage(UpdateFilterMediator updateFilterMediator, int arg1) {
            updateFilterMediator.showApplyFilterProgress(arg1);
        }
    },
    SHOW_HIDING_ARCHIVED_CACHES_PROGRESS {
        @Override
        void handleMessage(UpdateFilterMediator updateFilterMediator, int arg1) {
            updateFilterMediator.showHidingArchivedCachesProgress();
        }
    },
    DISMISS_HIDING_ARCHIVED_CACHES_PROGRESS {
        @Override
        void handleMessage(UpdateFilterMediator updateFilterMediator, int arg1) {
            updateFilterMediator.dismissHidingArchivedCachesProgress();
        }
    },
    END_FILTERING {
        @Override
        void handleMessage(UpdateFilterMediator updateFilterMediator, int arg1) {
            updateFilterMediator.endFiltering();
        }
    },
    SHOW_HIDING_WAYPOINTS_PROGRESS {
        @Override
        void handleMessage(UpdateFilterMediator updateFilterMediator, int arg1) {
            updateFilterMediator.showHidingWaypointsProgress();
        }
    },
    DISMISS_HIDING_WAYPOINTS_PROGRESS {
        @Override
        void handleMessage(UpdateFilterMediator updateFilterMediator, int arg1) {
            updateFilterMediator.dismissHidingWaypointsProgress();
        }
    };

    public static UpdateFilterMessages fromOrd(int i) {
        return UpdateFilterMessages.values()[i];
    }

    abstract void handleMessage(UpdateFilterMediator updateFilterMediator, int arg1);
}