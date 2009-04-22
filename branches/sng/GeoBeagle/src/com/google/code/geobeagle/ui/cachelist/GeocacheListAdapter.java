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

import com.google.code.geobeagle.data.GeocacheVectors;
import com.google.code.geobeagle.ui.cachelist.row.RowInflaterStrategy;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class GeocacheListAdapter extends BaseAdapter {

    private final GeocacheVectors mGeocacheVectors;
    private final RowInflaterStrategy mRowInflaterStrategy;

    public GeocacheListAdapter(GeocacheVectors geocacheVectors, RowInflaterStrategy rowInflaterStrategy) {
        mGeocacheVectors = geocacheVectors;
        mRowInflaterStrategy = rowInflaterStrategy;
    }

    public int getCount() {
        return mGeocacheVectors.size();
    }

    public Object getItem(int position) {
        return position;
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        return mRowInflaterStrategy.getView(position, convertView);
    }
}
