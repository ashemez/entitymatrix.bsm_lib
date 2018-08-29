package com.gizit.bsm.helpers;

import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Helper {

	public static String joinInts(int[] tokens, String delimiter) {
        if (tokens == null) return "";
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < tokens.length; i++) {
          if (i > 0 && delimiter != null) {
            result.append(delimiter);
          }
          result.append(String.valueOf(tokens[i]));
        }
        return result.toString();
    }
	
	public static String joinInts(Integer[] tokens, String delimiter) {
        if (tokens == null) return "";
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < tokens.length; i++) {
          if (i > 0 && delimiter != null) {
            result.append(delimiter);
          }
          result.append(String.valueOf(tokens[i]));
        }
        return result.toString();
    }
	
	public static String EscapeNullChar(String str) {
		str = str.replaceAll("[\\\\u0000]*", "");
		
		return str;
	}
	public static String EscapeJsonStr(String str) {
		str = str.replaceAll("\\\\", "\\\\\\\\");
		str = str.replaceAll("\"", "\\\\\"");
		str = str.replaceAll("\n", " ");
		str = str.replaceAll("\r", "");
		return str;
	}
	
	public static long CurrentTimeUTCMilliSecond() {
		return Calendar.getInstance().getTimeInMillis();
    }
	
	public static String CurrentTimeWindow() {
    	String err = "";
    	
		Calendar calendar = Calendar.getInstance();
		int day = calendar.get(Calendar.DAY_OF_MONTH);
		int month = calendar.get(Calendar.MONTH) + 1;
		int year = calendar.get(Calendar.YEAR);
		
		SimpleDateFormat sdf = new SimpleDateFormat("dd-M-yyyy hh:mm:ss");
		String dateInString = day + "-" + month + "-" + year + " 00:00:00";

		long de = 0;
		String destr = "";
		try {
			
			Date date = sdf.parse(dateInString);
			de = date.getTime();
			
			destr = String.valueOf(de);
			destr = destr.substring(0, destr.length() - 3);
			
		} catch (ParseException e) {
			err = e.getMessage();
		}

		if(!err.equals(""))
			return "0";
		
		return destr;
    }
	
	public static String CurrentMonthTimeWindow() {
    	String err = "";
    	
		Calendar calendar = Calendar.getInstance();
		int month = calendar.get(Calendar.MONTH) + 1;
		int year = calendar.get(Calendar.YEAR);
		
		SimpleDateFormat sdf = new SimpleDateFormat("dd-M-yyyy hh:mm:ss");
		String dateInString = "01" + "-" + month + "-" + year + " 00:00:00";

		long de = 0;
		String destr = "";
		try {
			
			Date date = sdf.parse(dateInString);
			de = date.getTime();
			
			destr = String.valueOf(de);
			destr = destr.substring(0, destr.length() - 3);
			
		} catch (ParseException e) {
			err = e.getMessage();
		}

		if(!err.equals(""))
			return "0";
		
		return destr;
    }
	
	public static int DayCountOfMonth(long timewindow) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(timewindow);
		return calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
	}
	
	public static String DoublePrecision2(double value) {
		DecimalFormat df = new DecimalFormat("###.##");
		return df.format(value).replaceAll(",", ".");
	}
	
	public static Double DoublePrecision2Double(double value) {
		DecimalFormat df = new DecimalFormat("###.##");
		String d = df.format(value).replaceAll(",", ".");
		return Double.parseDouble(d);
	}
	
	public static String UTCToDateStr(double utc) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis((long)(utc * 1000));
		
		int day = calendar.get(Calendar.DAY_OF_MONTH);
		int month = calendar.get(Calendar.MONTH) + 1;
		int year = calendar.get(Calendar.YEAR);
		int hour = calendar.get(Calendar.HOUR_OF_DAY);
		int min = calendar.get(Calendar.MINUTE);
		int sec = calendar.get(Calendar.SECOND);
		
		String monthStr = Integer.toString(month);
		if(month < 10)
			monthStr = "0" + monthStr;
		String dayStr = Integer.toString(day);
		if(day < 10)
			dayStr = "0" + dayStr;
		
		String hourStr = Integer.toString(hour);
		if(hour < 10)
			hourStr = "0" + hourStr;
		String minStr = Integer.toString(min);
		if(min < 10)
			minStr = "0" + minStr;

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/M/dd hh:mm:ss");
		String dateInString = year + "/" + monthStr + "/" + dayStr + " " + hourStr + ":" + minStr;
		
		return dateInString;
	}
	
	public static String UTCToDateStrWithSeconds(double utc) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis((long)(utc * 1000));
		
		int day = calendar.get(Calendar.DAY_OF_MONTH);
		int month = calendar.get(Calendar.MONTH) + 1;
		int year = calendar.get(Calendar.YEAR);
		int hour = calendar.get(Calendar.HOUR_OF_DAY);
		int min = calendar.get(Calendar.MINUTE);
		int sec = calendar.get(Calendar.SECOND);
		
		String monthStr = Integer.toString(month);
		if(month < 10)
			monthStr = "0" + monthStr;
		String dayStr = Integer.toString(day);
		if(day < 10)
			dayStr = "0" + dayStr;
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/M/dd hh:mm:ss");
		String dateInString = year + "/" + monthStr + "/" + dayStr + " " + hour + ":" + min + ":" + sec;
		
		return dateInString;
	}
	
	public static Double TodayStartingDate() {
		Calendar calendar = Calendar.getInstance();
		int year = calendar.get(Calendar.YEAR);
		int month = calendar.get(Calendar.MONTH);
		int day = calendar.get(Calendar.DAY_OF_MONTH);

		calendar.set(year, month, day, 0, 0, 0);
		
		return (double)((calendar.getTime().getTime() / 1000));
    }
	
	public static Double LastDayStartingDate() {
		Calendar calendar = Calendar.getInstance();
		int year = calendar.get(Calendar.YEAR);
		int month = calendar.get(Calendar.MONTH);
		int day = calendar.get(Calendar.DAY_OF_MONTH);

		calendar.set(year, month, day, 0, 0, 0);
		
		return (double)((calendar.getTime().getTime() / 1000) - 86400);
    }
	
	public static Double LastSevenDaysStartingDate() {
		Calendar calendar = Calendar.getInstance();
		int year = calendar.get(Calendar.YEAR);
		int month = calendar.get(Calendar.MONTH);
		int day = calendar.get(Calendar.DAY_OF_MONTH);

		calendar.set(year, month, day, 0, 0, 0);
		
		return (double)((calendar.getTime().getTime() / 1000) - (86400 * 6));
    }
	
	public static Double LastWeekStartingDate() {
		Date date = new Date();
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		int i = c.get(Calendar.DAY_OF_WEEK) - c.getFirstDayOfWeek();
		c.add(Calendar.DATE, -i - 6); // starting day is sunday so subtract 6 days
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		
		return (double)(c.getTime().getTime() / 1000); 
    }
	
	public static Double LastMonthStartingDate() {
		Calendar calendar = Calendar.getInstance();
		int year = calendar.get(Calendar.YEAR);
		int lastMonth = calendar.get(Calendar.MONTH) - 1;
		if(lastMonth == -1) {
			lastMonth = 11;
			year--;
		}
		calendar.set(year, lastMonth, 1, 0, 0, 0);
		
		return (double)(calendar.getTime().getTime() / 1000);
    }
	
	public static Double CurrentMonthStartingDate() {
		Calendar calendar = Calendar.getInstance();
		int year = calendar.get(Calendar.YEAR);
		int currentMonth = calendar.get(Calendar.MONTH);
		calendar.set(year, currentMonth, 1, 0, 0, 0);

		return (double)(calendar.getTime().getTime() / 1000);
    }
	
	private final static String ISO_ENCODING = "ISO-8859-1";
    private final static String UTF8_ENCODING = "UTF-8";
	public static String printSkippedBomString(final byte[] bytes) throws UnsupportedEncodingException {
        int length = bytes.length - 3;
        byte[] barray = new byte[length];
        System.arraycopy(bytes, 3, barray, 0, barray.length);
        return new String(barray, ISO_ENCODING);
    }

    public static String printUTF8String(final byte[] bytes) throws UnsupportedEncodingException {
        return new String(bytes, UTF8_ENCODING);
    }

    public static boolean isUTF8(byte[] bytes) {
        if ((bytes[0] & 0xFF) == 0xEF && 
            (bytes[1] & 0xFF) == 0xBB && 
            (bytes[2] & 0xFF) == 0xBF) {
            return true;
        }
        return false;
    }
    
    public static boolean isNumeric(String s) {  
        return s != null && s.matches("[-+]?\\d*\\.?\\d+");  
    }  
    
}
