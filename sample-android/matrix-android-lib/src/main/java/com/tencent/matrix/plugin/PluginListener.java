

package com.tencent.matrix.plugin;

import com.tencent.matrix.report.Issue;



public interface PluginListener {
    void onInit(Plugin plugin);

    void onStart(Plugin plugin);

    void onStop(Plugin plugin);

    void onDestroy(Plugin plugin);

    void onReportIssue(Issue issue);
}
