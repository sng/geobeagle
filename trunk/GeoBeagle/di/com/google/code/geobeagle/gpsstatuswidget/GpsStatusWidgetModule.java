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

package com.google.code.geobeagle.gpsstatuswidget;

import com.google.code.geobeagle.gpsstatuswidget.GpsStatusWidget.InflatedGpsStatusWidget;
import com.google.code.geobeagle.gpsstatuswidget.GpsStatusWidgetDelegate.GpsStatusWidgetDelegateFactory;
import com.google.code.geobeagle.gpsstatuswidget.GpsWidgetAndUpdater.GpsWidgetAndUpdaterFactory;
import com.google.code.geobeagle.gpsstatuswidget.MeterBars.MeterBarsFactory;
import com.google.code.geobeagle.gpsstatuswidget.MeterFader.MeterFaderFactory;
import com.google.code.geobeagle.gpsstatuswidget.TextLagUpdater.TextLagUpdaterFactory;
import com.google.code.geobeagle.gpsstatuswidget.UpdateGpsWidgetRunnable.UpdateGpsWidgetRunnableFactory;
import com.google.inject.Provides;
import com.google.inject.assistedinject.FactoryProvider;

import roboguice.config.AbstractAndroidModule;
import roboguice.inject.ContextScoped;

import android.content.Context;
import android.view.ViewGroup;

public class GpsStatusWidgetModule extends AbstractAndroidModule {

    @Override
    protected void configure() {
        bind(MeterBarsFactory.class).toProvider(
                FactoryProvider.newFactory(MeterBarsFactory.class, MeterBars.class));
        bind(TextLagUpdaterFactory.class).toProvider(
                FactoryProvider.newFactory(TextLagUpdaterFactory.class, TextLagUpdater.class));
        bind(UpdateGpsWidgetRunnableFactory.class).toProvider(
                FactoryProvider.newFactory(UpdateGpsWidgetRunnableFactory.class,
                        UpdateGpsWidgetRunnable.class));
        bind(MeterFaderFactory.class).toProvider(
                FactoryProvider.newFactory(MeterFaderFactory.class, MeterFader.class));
        bind(GpsStatusWidgetDelegateFactory.class).toProvider(
                FactoryProvider.newFactory(GpsStatusWidgetDelegateFactory.class,
                        GpsStatusWidgetDelegate.class));
        bind(InflatedGpsStatusWidget.class).in(ContextScoped.class);
        bind(GpsWidgetAndUpdaterFactory.class).toProvider(
                FactoryProvider.newFactory(GpsWidgetAndUpdaterFactory.class,
                        GpsWidgetAndUpdater.class));
    }

    @Provides
    GpsStatusWidget providesGpsStatusWidget(Context context,
            InflatedGpsStatusWidget inflatedGpsStatusWidget) {
        GpsStatusWidget gpsStatusWidget = new GpsStatusWidget(context);
        gpsStatusWidget.addView(inflatedGpsStatusWidget, ViewGroup.LayoutParams.FILL_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        return gpsStatusWidget;
    }


}
