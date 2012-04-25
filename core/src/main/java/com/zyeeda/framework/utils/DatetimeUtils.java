/*
 * Copyright 2010 Zyeeda Co. Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * 		http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */
package com.zyeeda.framework.utils;

import java.text.ParseException;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Datetime utils.
 * 
 * @author		Rui Tang
 * @version		%I%, %G%
 * @since		1.0
 *
 */
public class DatetimeUtils {
	
	public static final String DEFAULT_DATE_FORMAT_PATTERN = "yyyy-MM-dd";
	public static final String DEFAULT_DATETIME_FORMAT_PATTERN = "yyyy-MM-dd HH:mm:ss";
	public static final String GMT_DATETIME_FORMAT_PATTERN = "yyyy-MM-dd'T'HH:mm:ssz";
	
	private static SimpleDateFormat getDefaultDateFormat() {
		return new SimpleDateFormat(DEFAULT_DATE_FORMAT_PATTERN);
	}
	
	private static SimpleDateFormat getDefaultDatetimeFormat() {
		return new SimpleDateFormat(DEFAULT_DATETIME_FORMAT_PATTERN);
	}
	
	private static SimpleDateFormat getGMTDatetimeFormat() {
		return new SimpleDateFormat(GMT_DATETIME_FORMAT_PATTERN);
	}

	public static Date parseDate(String str) throws ParseException {
		SimpleDateFormat sdf = getDefaultDateFormat();
		return sdf.parse(str);
	}
	
	public static Date parseDatetime(String str) throws ParseException {
		SimpleDateFormat sdf = getDefaultDatetimeFormat();
		return sdf.parse(str);
	}
	
	public static Date parseGMTDatetime(String str) throws ParseException {
		SimpleDateFormat sdf = getGMTDatetimeFormat();
		return sdf.parse(str);
	}
	
	public static String formatDate(Date date) {
		SimpleDateFormat sdf = getDefaultDateFormat();
		return sdf.format(date);
	}
	
	public static String formatDatetime(Date date) {
		SimpleDateFormat sdf = getDefaultDatetimeFormat();
		return sdf.format(date);
	}
	
	public static String formatGMTDatetime(Date date) {
		SimpleDateFormat sdf = getGMTDatetimeFormat();
		return sdf.format(date);
	}
	
	public static Date stringToDate(String date,String format){
		SimpleDateFormat dateFormat = new SimpleDateFormat(format);
		try {
			return dateFormat.parse(date);
		} catch (Exception e) {
		}
		return null;
	}
	
	  public static Date getCurenDatePussMonth(){
	    	Calendar c = Calendar.getInstance();
	    	int year = c.get(Calendar.YEAR);  
	        int month = c.get(Calendar.MONTH);  
	        int day = c.get(Calendar.DAY_OF_MONTH);
	        month = month + 2;
	        String dateStr = year + "-" + month + "-" + day;
	        System.out.println(dateStr);
	        Date date = DatetimeUtils.stringToDate(dateStr, DatetimeUtils.DEFAULT_DATE_FORMAT_PATTERN);
	    	return date;
	    }
}
