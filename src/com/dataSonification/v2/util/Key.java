package com.dataSonification.v2.util;


/**
 * Key is a typesafe enumeration representing keys in Config
 * objects.  
 * @author Kimo Johnson
 *
 */
public enum Key {
	
	/**
	 * Used in Core.
	 */
	LOG_LEVEL (Integer.class),
	CORE_CONFIG_NAME (String.class),
	UI (Class.class),
	D_ID (Integer.class),
	S_ID (Integer.class),
	DB_ID (Integer.class),
	DB_NAME (String.class),
	DB_DRIVER (String.class),
	DB_USER (String.class),
	DB_PASSWORD (String.class),
    INST_DB_NAME (String.class),
    INST_DB_DRIVER (String.class),
    INST_DB_USER (String.class),
    INST_DB_PASSWORD (String.class),
	DATA_SOURCES (String.class),
	DATA_SOURCE (Class.class),
	DATA_ENTITIES (String.class),
	SAMPLE_DIR (String.class),
	USE_MIDI (Boolean.class),
	USE_JSYN (Boolean.class),
	UI_HELPER (Class.class),
	INST_ID (Integer.class),
	INST_TYPE (Class.class),
    VERSION (String.class),
    VERSION_VERBOSE (String.class),
    UPTIME (String.class),
    ACTIVE_DBS (String.class),
	/**
	 * Used in Sonification.
	 */
	ENABLED (Boolean.class),
	DESCRIPTION (String.class),
	ANALYZER (Class.class),
	ARRANGER (Class.class),
	TRAINER (Class.class),
	PROGRAM (Integer.class),
	CHANNEL (Integer.class),
	INSTRUMENT (Class.class),
	PAN (Double.class),
	TICKER (String.class),
	AMPLITUDE (Double.class),
	/**
	 * Used in MovementAnalyzer.
	 */
	SIGNIFICANT_MOVE (Double.class),
	REF1_FIELD (Double.class, Source.DATA),
	CURRENT_FIELD (Double.class, Source.DATA),
	LAST_INCREMENT (Integer.class, Source.COMPONENT),
	INCREMENT (Integer.class, Source.COMPONENT),
	/**
	 * Used in ThreeNoteArranger.
	 */
	BASE_PITCH (Integer.class),
	DURATION (Double.class),
	LOUDNESS (Double.class),
	HOLD (Double.class),
	TEMPO (Double.class),
	MAX_RANGE (Integer.class),
	/**
	 * Used in TrillAnalyzer.
	 */
	TOP_FIELD (Double.class, Source.DATA),
	BOTTOM_FIELD (Double.class, Source.DATA),
	BOTTOM_SIZE_FIELD (Integer.class, Source.DATA),
	TOP_SIZE_FIELD (Integer.class, Source.DATA),
	LAST_TOP_SIZE (Integer.class, Source.COMPONENT),
	LAST_BOTTOM_SIZE (Integer.class, Source.COMPONENT),
	THRESHOLD (Integer.class),
	THRESHOLD_CHANGE (Integer.class),
	LAST_BOTTOM (Double.class, Source.COMPONENT),
	LAST_TOP (Double.class, Source.COMPONENT),
	TOP_TRILL_LENGTH (Integer.class, Source.COMPONENT),
	BOTTOM_TRILL_LENGTH (Integer.class, Source.COMPONENT),
	/**
	 * Used in TargetAnalyzer
	 */
	TARGET_FIELD (Double.class, Source.DATA),
	/**
	 * Used in V1SocketDataSource.
	 */
	ADDRESS (String.class),
	PORT (Integer.class),
	TIMEOUT (Integer.class),
	/**
	 * Used in SampleInstrument
	 */
	NAME (String.class),
	START_PITCH (Integer.class),
	STEP (Integer.class),
	END_PITCH (Integer.class),
	FILE_BASE (String.class),
	FILE_TYPE (String.class),
	REMAPPER (Class.class),
    
    ACTIVE (Boolean.class),
    INST_NAME(String.class),
	/**
	 * Used in FourNoteArranger
	 */
	TARGET_DISTANCE (Integer.class),
	/**
	 * Used in TargetAnalyzer
	 * Distance from User-Defined Target
	 * In Units of Significant Movement
	 */
	TARGET_INCREMENT (Integer.class, Source.COMPONENT),
    /**
     * Used in SliderAnalyzer
     */
	REF2_FIELD (Double.class, Source.DATA),
	SPAN (Integer.class, Source.COMPONENT),
	LAST_SPAN (Integer.class, Source.COMPONENT),
    /**
     * Used in UnboundedSliderArranger
     */
	CLOSE_INTERVAL (Integer.class),
    /**
     * Used in AdaptiveMovementTrainer
     */
	STRONG_THRESH (Double.class),
	OUT_THRESH (Double.class),
	FIELD1_VALUE (Double.class, Source.COMPONENT),
	FIELD2_VALUE (Double.class, Source.COMPONENT),
	FIELD3_VALUE (Double.class, Source.COMPONENT),
    /**
     * Used in VoiceInstrument
     */
	TICKER_DIR (String.class),
	EVENT_DIR (String.class),
    /**
     * Used in VoiceUIHelper
     */
	VOICE_DELAY (Double.class),
    /**
     * Used in AdaptiveUnboundedTrainer
     */
	AVERAGE1 (String.class, Source.COMPONENT),
	AVERAGE2 (String.class, Source.COMPONENT),
    /**
     * Used in to check status
     */
	RUNNING (Boolean.class),
	/**
	 * Used in SizeAnalyzer
	 */
	SIZE_FIELD (Integer.class, Source.DATA),
	TRILL_LENGTH (Integer.class, Source.COMPONENT),
	/**
	 * Used for error condition (in SAXParser)
	 */
    BAD_KEY (Object.class, Source.DATA);
	
	public enum Source { CONFIG, DATA, COMPONENT }
	
	/**
	 * The type
	 */
	private final Class type;

	private final Source source;
	/**
	 * Constructs a new config key.
	 * @param type the class
	 */
	Key(Class type) {
		this(type, Source.CONFIG);
	}
	
	/**
	 * Constructs a new key with the specified source.
	 * @param type
	 * @param source
	 */
	Key(Class type, Source source) {
		this.source = source;
		this.type = type;
	}
	
	public Class getType() {
	    return type;
	}
	
	public Source getSource() {
		return source;
	}
	
}