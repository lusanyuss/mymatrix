

package com.tencent.matrix.apk.model.result;

import java.util.Map;



public abstract class TaskResultRegistry {

    public abstract Map<String, Class<? extends TaskHtmlResult>> getHtmlResult();

    public abstract Map<String, Class<? extends TaskJsonResult>> getJsonResult();

}
