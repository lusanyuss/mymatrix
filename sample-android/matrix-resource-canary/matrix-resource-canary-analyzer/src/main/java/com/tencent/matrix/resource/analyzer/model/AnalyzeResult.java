

package com.tencent.matrix.resource.analyzer.model;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;



public abstract class AnalyzeResult implements Serializable {
    public abstract void encodeToJSON(JSONObject jsonObject) throws JSONException;
}
