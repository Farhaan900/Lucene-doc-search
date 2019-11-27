package lucene;
import java.text.DateFormat;  
import java.text.SimpleDateFormat;  
import java.util.Date;  
import java.util.Calendar;  

public class Utilities {
	
	public static String LongToDate(long longTime) {
		Date d = new Date(longTime * 1000);  
        DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");  
        return  dateFormat.format(d);  
        
	}

}
