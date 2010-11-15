package com.midlandroid.apps.android.laptimer.util;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

public final class TextUtil {
	
	/**
	 * Using the provided date object the function will 
	 * create a string formated to provided date.
	 * @param date
	 * @return
	 */
	public static String formatDateToString(final long date) {
		Calendar cal = Calendar.getInstance();
		TimeZone tz = cal.getTimeZone();
		
		DateFormat dfm = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		dfm.setTimeZone(tz);
		
		return dfm.format(date);
	}
	
	/**
	 * Converts a long time to a number format
	 * @param time
	 * @param numFormat
	 * @return
	 */
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
	
	/**
	 * Converts a array of strings into a multi line string.
	 * @param array
	 * @return
	 */
	public static String stringListToMultiLineString(List<String> array) {
		String outStr = new String("");
		for (String line : array) {
			outStr += line + "\n";
		}
		
		if (outStr.length() > 0)
			outStr = outStr.substring(0, outStr.length()-1);
		
		return outStr;
	}
	
	/**
	 * Builds a multi line array 
	 * @param array
	 * @return
	 */
	public static String stringListToMultiLineStringReversed(List<String> array) {
		List<String> reversed = new ArrayList<String>();
		// Reverse the list
		for (String line : array) {
			reversed.add(0, line);
		}
		
		return stringListToMultiLineString(reversed);
	}
}
