

package com.tencent.matrix.apk.model.output;

import com.tencent.matrix.apk.model.result.TaskHtmlResult;
import com.tencent.matrix.apk.model.result.TaskJsonResult;
import com.tencent.matrix.apk.model.result.TaskResultRegistry;

import java.util.HashMap;
import java.util.Map;



public class MMTaskResultRegistry extends TaskResultRegistry {

    @Override
    public Map<String, Class<? extends TaskHtmlResult>> getHtmlResult() {
        Map<String, Class<? extends TaskHtmlResult>> map = new HashMap<>();
        map.put("mm.html", MMTaskHtmlResult.class);
        return map;
    }

    @Override
    public Map<String, Class<? extends TaskJsonResult>> getJsonResult() {
        Map<String, Class<? extends TaskJsonResult>> map = new HashMap<>();
        map.put("mm.json", MMTaskJsonResult.class);
        return map;
    }
}
