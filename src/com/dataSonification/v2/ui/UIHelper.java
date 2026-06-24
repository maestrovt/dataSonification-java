package com.dataSonification.v2.ui;

import com.dataSonification.v2.Config;
import com.dataSonification.v2.ID;
import com.dataSonification.v2.data.DataInfo;

public interface UIHelper {
    public void help(ID id, DataInfo info);
    public void setConfig(Config config);
}