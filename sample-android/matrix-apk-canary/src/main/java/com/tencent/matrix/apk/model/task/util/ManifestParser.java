

package com.tencent.matrix.apk.model.task.util;

import brut.androlib.AndrolibException;
import brut.androlib.res.decoder.AXmlResourceParser;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.tencent.matrix.javalib.util.Util;
import org.xmlpull.v1.XmlPullParser;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Stack;



public class ManifestParser {

    private static final String ROOTTAG = "manifest";
    private final AXmlResourceParser resourceParser;
    private File manifestFile;
    private boolean isParseStarted;
    private final Stack<JsonObject> jsonStack = new Stack<>();
    private JsonObject result;

    public ManifestParser(String path) {
        manifestFile = new File(path);
        resourceParser = ApkResourceDecoder.createAXmlParser();
    }

    public ManifestParser(File manifestFile) {
        if (manifestFile != null) {
            this.manifestFile = manifestFile;
        }
        resourceParser = ApkResourceDecoder.createAXmlParser();
    }

    public ManifestParser(File manifestFile, File arscFile) throws IOException, AndrolibException {
        if (manifestFile != null) {
            this.manifestFile = manifestFile;
        }
        resourceParser = ApkResourceDecoder.createAXmlParser(arscFile);
    }

    public JsonObject parse() throws Exception {

        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream(manifestFile);
            try {
                resourceParser.open(inputStream);
                int token = resourceParser.nextToken();

                while (token != XmlPullParser.END_DOCUMENT) {
                    token = resourceParser.next();
                    if (token == XmlPullParser.START_TAG) {
                        handleStartElement();
                    } else if (token == XmlPullParser.TEXT) {
                        handleElementContent();
                    } else if (token == XmlPullParser.END_TAG) {
                        handleEndElement();
                    }
                }
            } finally {
                resourceParser.close();
                if (inputStream != null) {
                    inputStream.close();
                }
            }
        } catch (Exception e) {
            throw e;
        }


        return result;
    }


    private void handleStartElement() {
        final String name = resourceParser.getName();

        if (name.equals(ROOTTAG)) {
            isParseStarted = true;
        }
        if (isParseStarted) {
            JsonObject jsonObject = new JsonObject();
            for (int i = 0; i < resourceParser.getAttributeCount(); i++) {
                if (!Util.isNullOrNil(resourceParser.getAttributePrefix(i))) {
                    jsonObject.addProperty(resourceParser.getAttributePrefix(i) + ":" + resourceParser.getAttributeName(i), resourceParser.getAttributeValue(i));
                } else {
                    jsonObject.addProperty(resourceParser.getAttributeName(i), resourceParser.getAttributeValue(i));
                }
            }
            jsonStack.push(jsonObject);
        }
    }

    private void handleElementContent() {
        //do nothing
    }

    private void handleEndElement() {
        final String name = resourceParser.getName();
        JsonObject jsonObject = jsonStack.pop();

        if (jsonStack.isEmpty()) {                                      //root element
            result = jsonObject;
        } else {
            JsonObject preObject = jsonStack.peek();
            JsonArray jsonArray = null;
            if (preObject.has(name)) {
                jsonArray = preObject.getAsJsonArray(name);
                jsonArray.add(jsonObject);
            } else {
                jsonArray = new JsonArray();
                jsonArray.add(jsonObject);
                preObject.add(name, jsonArray);
            }
        }
    }


}
