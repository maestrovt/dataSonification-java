package com.dataSonification.v2.sound;

import java.util.Arrays;

import com.dataSonification.v2.Const;
import com.dataSonification.v2.data.DataComponent;
import com.dataSonification.v2.util.Key;

/**
 * Trill Analyzer
 *
 * Parameters:
 *
 * Significant move, threshold size, threshold change.
 *
 * Data:
 *
 * Top field, top field size, bottom field, bottom field size.
 *
 * Description:
 *
 * The analyzer will examine the Data Event (consisting of the four data items) to
 * determine if either top field size or bottom field size (or both) exceed the
 * threshold size set by the user. It either (or both) of those conditions are
 * met, then the analyzer should call for the sonification of that Data Event.
 * If either condition is met, one sonification will be enabled. If both are
 * met, there will be two sonifications.
 *
 * If the analyzer determines that either the top or bottom field, and its
 * corresponding field size, have been previously sonified, then it is the same
 * event. The sonification will not be repeated.
 */
class TrillAnalyzer extends Analyzer
{
    
    /**
     * The data keys that this analyzer reads.
     */
    private static final Key[] local_reads = { Key.TOP_FIELD, Key.BOTTOM_FIELD, Key.BOTTOM_SIZE_FIELD,
            Key.TOP_SIZE_FIELD, Key.SIGNIFICANT_MOVE, Key.THRESHOLD, Key.THRESHOLD_CHANGE,
            Key.LAST_BOTTOM, Key.LAST_TOP };
            
            /**
             * The keys that this analyzer writes.
             */
            private static final Key[] local_writes = { Key.LAST_TOP, Key.LAST_BOTTOM, Key.INCREMENT,
                    Key.TOP_TRILL_LENGTH, Key.BOTTOM_TRILL_LENGTH };
                    
                    
//	 package-private access
                    TrillAnalyzer()
                    {
                        super();
                        reads.addAll(Arrays.asList(local_reads));
                        writes.addAll(Arrays.asList(local_writes));
                        
                        defaults.put(Key.LAST_BOTTOM, 0.0);
                        defaults.put(Key.LAST_TOP, 0.0);
                        defaults.put(Key.LAST_BOTTOM_SIZE, 0);
                        defaults.put(Key.LAST_TOP_SIZE, 0);
                    }
                    
                    
//	 package-private access
                    boolean analyze(DataComponent data)
                    {
                        
                        double currentTop = (Double) data.getField(Key.TOP_FIELD);
                        double currentBottom = (Double) data.getField(Key.BOTTOM_FIELD);
                        
                        int bottomSize = (Integer) data.getField(Key.BOTTOM_SIZE_FIELD);
                        int topSize = (Integer) data.getField(Key.TOP_SIZE_FIELD);
                        
                        
                        double lastBottom = (Double) cache.get(Key.LAST_BOTTOM);
                        double lastTop = (Double) cache.get(Key.LAST_TOP);
                        int lastBottomSize = (Integer) cache.get(Key.LAST_BOTTOM_SIZE);
                        int lastTopSize = (Integer) cache.get(Key.LAST_TOP_SIZE);
                        double sig_move = (Double) cache.get(Key.SIGNIFICANT_MOVE);
                        int threshold = (Integer) cache.get(Key.THRESHOLD);
                        int thresholdChange = (Integer) cache.get(Key.THRESHOLD_CHANGE);
                        
                        // Does top sonify?
                        int topTrillLength = 0;
                        boolean topSame = true;
                        if (topSize >= threshold)
                        {
                            // Is this data essentially the same as the last data received?
                            topSame = (topSize == lastTopSize) && (Math.abs(currentTop - lastTop) < Const.MIN_PRICE_DIFF);
                            
                            topTrillLength = topSame ? 0 : ((topSize - threshold) / thresholdChange) + 1;
                            
                        }
                        
                        // Does bottom sonify?
                        int bottomTrillLength = 0;
                        boolean bottomSame = true;
                        if (bottomSize >= threshold)
                        {
                            // Is this data essentially the same as the last data received?
                            bottomSame = (bottomSize == lastBottomSize) && (Math.abs(currentBottom - lastBottom) < Const.MIN_PRICE_DIFF);
                            
                            // set trill length to 0 if we are not sonifying due to same data
                            bottomTrillLength = bottomSame ? 0 : ((bottomSize - threshold) / thresholdChange) + 1;
                            
                        }
                        
                        double dist = (currentTop - currentBottom) / sig_move;
                        int increment = (int) Math.floor(dist);
                        
                        
                        // adjust increment if it is numerically close to the next increment
                        if ((increment+1 - dist) < Const.SIG_MOVE_TOL)
                        {
                            increment += 1;
                        }
                        
                        boolean to_sonify = !topSame || !bottomSame;
                        
                        // These values are not used in the Arranger so they can be written back now
                        cache.put(Key.LAST_TOP, currentTop);
                        cache.put(Key.LAST_TOP_SIZE, topSize);
                        cache.put(Key.LAST_BOTTOM, currentBottom);
                        cache.put(Key.LAST_BOTTOM_SIZE, bottomSize);
                        
                        cache.put(Key.TOP_TRILL_LENGTH, topTrillLength);
                        cache.put(Key.BOTTOM_TRILL_LENGTH, bottomTrillLength);
                        cache.put(Key.INCREMENT, increment);
                        
                        return to_sonify;
                    }
}