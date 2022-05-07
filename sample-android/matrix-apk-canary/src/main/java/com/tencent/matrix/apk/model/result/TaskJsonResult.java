

package com.tencent.matrix.apk.model.result;


import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.tencent.matrix.apk.model.task.TaskFactory;
import com.tencent.matrix.javalib.util.Util;

import javax.xml.parsers.ParserConfigurationException;

/**
 * 
 */

public class TaskJsonResult extends TaskResult {

    protected final JsonObject jsonObject;

    protected final JsonObject config;

    public TaskJsonResult(int taskType, JsonObject config) throws ParserConfigurationException {
        super(taskType);
        this.config = config;
        jsonObject = new JsonObject();
        jsonObject.addProperty("taskType", taskType);
        jsonObject.addProperty("taskDescription", TaskFactory.TaskDescription.get(taskType));
    }

    public void add(String name, String value) {
        if (!Util.isNullOrNil(name)) {
            jsonObject.addProperty(name, value);
        }
    }

    public void add(String name, boolean value) {
        if (!Util.isNullOrNil(name)) {
            jsonObject.addProperty(name, value);
        }
    }

    public void add(String name, Number value) {
        if (!Util.isNullOrNil(name)) {
            jsonObject.addProperty(name, value);
        }
    }

    public void add(String name, JsonElement jsonElement) {
        if (!Util.isNullOrNil(name)) {
            jsonObject.add(name, jsonElement);
        }
    }

    @Override
    public void setStartTime(long startTime) {
        super.setStartTime(startTime);
        jsonObject.addProperty("start-time", this.startTime);
    }

    @Override
    public void setEndTime(long endTime) {
        super.setEndTime(endTime);
        jsonObject.addProperty("end-time", this.endTime);
    }

    public void format(JsonObject jsonObject) {
        //do nothing
    }

    @Override
    public String toString() {
        return jsonObject.toString();
    }

    @Override
    public JsonObject getResult() {
        return jsonObject;
    }
}
