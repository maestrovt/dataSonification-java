package com.dataSonification.v2.util;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class TableToClass {
    
    /**
     * A mapping from table names to class names.
     */
    private static Map<String,String> table_class;

    /**
     * Initialize the table name to class name mapping
     */
    static {
        // Initialize the table to class mapping
        try
        {
            Map<String,String> m = PackageInspector.getTableClassMap();
            // make a defensive copy
            table_class = new HashMap<String,String>(m);
            
            Log.println(Subsystem.DATA, null, "DBManager: dynamically found " + table_class.size() + " classes.", Log.P_VERBOSE);
        }
        catch (Exception e)
        {
            // If we fail to dynamically load class names, use this predefined table
            table_class = new HashMap<String,String>();
            table_class.put("sonification","com.dataSonification.v2.sound.Sonification");
            table_class.put("v1socketdatasource","com.dataSonification.v2.data.V1SocketDataSource");
            table_class.put("threenotearranger","com.dataSonification.v2.sound.ThreeNoteArranger");
            table_class.put("movementanalyzer","com.dataSonification.v2.sound.MovementAnalyzer");
            table_class.put("javamidiinstrument","com.dataSonification.v2.sound.JavaMidiInstrument");
            table_class.put("sampleinstrument","com.dataSonification.v2.sound.SampleInstrument");
            table_class.put("fournotearranger","com.dataSonification.v2.sound.FourNoteArranger");
            table_class.put("boundedsliderarranger","com.dataSonification.v2.sound.BoundedSliderArranger");
            table_class.put("boundedslideranalyzer","com.dataSonification.v2.sound.BoundedSliderAnalyzer");
            table_class.put("simplesliderarranger","com.dataSonification.v2.sound.SimpleSliderArranger");
            table_class.put("colortickertrainer", "com.dataSonification.v2.sound.ColorTickerTrainer");
            table_class.put("unboundedsliderarranger", "com.dataSonification.v2.sound.UnboundedSliderArranger");
            table_class.put("slideranalyzer", "com.dataSonification.v2.sound.SliderAnalyzer");
            table_class.put("targetanalyzer","com.dataSonification.v2.sound.TargetAnalyzer");
            table_class.put("adaptivemovementtrainer", "com.dataSonification.v2.sound.AdaptiveMovementTrainer");
            table_class.put("adaptiveboundedtrainer", "com.dataSonification.v2.sound.AdaptiveBoundedTrainer");
            table_class.put("adaptiveunboundedtrainer", "com.dataSonification.v2.sound.AdaptiveUnboundedTrainer");
            table_class.put("voiceinstrument", "com.dataSonification.v2.sound.VoiceInstrument");
            table_class.put("voiceuihelper", "com.dataSonification.v2.ui.VoiceUIHelper");
            table_class.put("v2socketdatasource", "com.dataSonification.v2.data.V2SocketDataSource");
            table_class.put("twonotearranger", "com.dataSonification.v2.sound.TwoNoteArranger");
        }
    }
    
    /**
     * Maps table names to class names from this project.
     * @param table the table name
     * @return the complete class name for the class associated with the specified table
     */
    public static String map(String table)
    {
        if (null == table)
        {
            Log.println(Subsystem.DATA, ReturnCode.DATA_TABLE_ERROR, "DBManager: requested table is not valid: " + table, Log.P_ERROR);
            return null;
        }
        String cls = table_class.get(table.toLowerCase());
        if (cls != null)
        {
            return cls;
        }
        
        // If the class was not found, look up table in the set of values
        Collection<String> values = table_class.values();
        if (values.contains(table))
        {
            return table;
        }
        return null;
        
    }
    
}