package com.dataSonification.v2.util;


public class Converter {
    /**
     * Converts an object from one type to another.
     * 
     * @param obj
     *            the object to be converted
     * @param type
     *            the target type
     * @return the converted object
     */
    public static Object convert(Object obj, Class type) {
        /*
         * Only handling a few cases
         */
        Class objClass = obj.getClass();

        //Log.println(Subsystem.DATA, null, "Converter: convertObject: mapping " + objClass + " to "
        //        + type + ".", Log.P_VERBOSE);
        Object converted = null;
        if (obj instanceof Double) {
            double d = ((Double) obj).doubleValue();
            if (type == Integer.class) {
                converted = Integer.valueOf((int) Math.round(d));
            } else if (type == Boolean.class) {
                converted = Boolean.valueOf(Math.abs(d) > 0.01);
            }

        } else if (obj instanceof String) {

            String s = (String) obj;
			Log.println(Subsystem.CORE, ReturnCode.NO_CODE, "Parsing String: " + s);
			
            if (type == Class.class) {
                String class_name = TableToClass.map(s);

				try {
					Log.println(Subsystem.CORE, ReturnCode.NO_CODE, "Parsing Class: " + class_name);
					converted = Class.forName(class_name);
				} catch (ClassNotFoundException e) {
					converted = null;
				}
            } else if (type == Boolean.class) {
                converted = Boolean.valueOf((String)obj);
            } else if (type == Integer.class) {
                converted = Integer.valueOf((String)obj);
            } else if (type == Double.class) {
                converted = Double.valueOf((String)obj);
            }

        } else if (obj instanceof Integer) {

            int i = ((Integer) obj).intValue();
            if (type == Boolean.class) {
                converted = Boolean.valueOf(i != 0);
            }
            if(type == Double.class) {
            	converted = Double.valueOf(i);
            }

        } else {
            throw new IllegalArgumentException(
                    "Config.convertObject does not handle " + objClass + " to "
                            + type + " conversions.");
        }

        return converted;
    }
}