package com.dataSonification.v2.sound;

import java.util.Arrays;

import com.dataSonification.v2.data.DataComponent;
import com.dataSonification.v2.util.Key;


class DefaultAnalyzer extends Analyzer {

    private static final Key[] local_writes = { Key.INCREMENT };
    
    DefaultAnalyzer() {
        super();
        writes.addAll(Arrays.asList(local_writes));
        defaults.put(Key.LAST_INCREMENT, 0);
    }

    boolean analyze(DataComponent e) {
        int last_increment = (Integer) cache.get(Key.LAST_INCREMENT);
        cache.put(Key.INCREMENT, last_increment+1);
        return true;
    }
    
}