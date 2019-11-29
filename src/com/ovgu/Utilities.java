package com.ovgu;
import java.text.DateFormat;  
import java.text.SimpleDateFormat;  
import java.util.Date;  

public class Utilities {

	public static String LongToDate(long longTime) {

		Date d = new Date(longTime);  
		DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");  
		return  dateFormat.format(d);  

	}
}
