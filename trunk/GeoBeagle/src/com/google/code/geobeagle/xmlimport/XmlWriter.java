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

import com.google.code.geobeagle.cachedetails.FilePathStrategy;
import com.google.inject.Inject;

import java.io.IOException;
import java.util.HashMap;

class XmlWriter implements EventHandler {
    private final FilePathStrategy filePathStrategy;
    private String filename;
    private final TagWriter tagWriter;
    private Tag tagWpt;
    private String time;

    @Inject
    public XmlWriter(FilePathStrategy filePathStrategy, TagWriter tagWriter) {
        this.filePathStrategy = filePathStrategy;
        this.tagWriter = tagWriter;
    }

    public void endTag(String name, String previousFullPath) throws IOException {
        if (!previousFullPath.startsWith("/gpx/wpt"))
            return;

        if (tagWriter.isOpen())
            tagWriter.endTag(name);

        if (previousFullPath.equals(EventHandlerGpx.XPATH_WPT)) {
            tagWriter.endTag("gpx");
            tagWriter.close();
        }
    }

    @Override
    public void open(String filename) {
        this.filename = filename;
    }

    @Override
    public void startTag(String name, String fullPath, XmlPullParserWrapper xmlPullParser)
            throws IOException {
        if (!fullPath.startsWith("/gpx/wpt"))
            return;

        HashMap<String, String> attributes = new HashMap<String, String>();

        int attributeCount = xmlPullParser.getAttributeCount();
        for (int i = 0; i < attributeCount; i++) {
            attributes.put(xmlPullParser.getAttributeName(i), xmlPullParser.getAttributeValue(i));
        }
        Tag tag = new Tag(name, attributes);

        if (fullPath.equals("/gpx/wpt")) {
            tagWpt = tag;
        }  else if (tagWriter.isOpen()) {
            tagWriter.startTag(tag);
        }
    }

    @Override
    public boolean text(String fullPath, String text, XmlPullParserWrapper xmlPullParser)
            throws IOException {
        if (!fullPath.startsWith("/gpx/wpt"))
            return true;

        if (text.trim().length() == 0)
            return true;

        if (fullPath.equals("/gpx/wpt/time")) {
            time = text;
        } else if (fullPath.equals(EventHandlerGpx.XPATH_WPTNAME)) {
            tagWriter.open(filePathStrategy.getPath(filename, text, "gpx"));
            HashMap<String, String> emptyHashMap = new HashMap<String, String>();
            tagWriter.startTag(new Tag("gpx", emptyHashMap));
            tagWriter.startTag(tagWpt);
            if (time != null) {
                tagWriter.startTag(new Tag("time", emptyHashMap));
                tagWriter.text(time);
                tagWriter.endTag("time");
            }
            tagWriter.startTag(new Tag("name", emptyHashMap));
        }

        if (tagWriter.isOpen())
            tagWriter.text(text);
        return true;
    }
}