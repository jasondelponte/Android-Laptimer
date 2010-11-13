package com.midlandroid.apps.android.laptimer.util;

import java.text.NumberFormat;
import java.util.List;

public final class TextUtil {
	public static String formatDateToString(final long time, final NumberFormat numFormat) {
    	int hours = (int) (time/3600000);
    	int minutes = (int) ((time-(hours*3600000))/60000);
    	int seconds = (int) ((time-(hours*3600000)-(minutes*60000))/1000);
    	int milSec = (int) ((time-(hours*3600000)-(minutes*60000)-(seconds*1000))/100);
    	
    	return new String(numFormat.format(hours)+":"+
    			numFormat.format(minutes)+":"+
    			numFormat.format(seconds)+"."+
    			Integer.valueOf(milSec));
    }
	
	public static String stringListToMultiLineString(List<String> array) {
		String outStr = "";
		for (String line : array) {
			outStr += line + "\n";
		}
		return outStr.substring(0, outStr.length()-1);
	}
}
