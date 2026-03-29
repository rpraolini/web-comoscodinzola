package it.asso.core.common;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import org.slf4j.Logger;import org.slf4j.LoggerFactory;

public class Utils {
	
	private static Logger logger = LoggerFactory.getLogger(Utils.class);

	public static String stringToDateISO(String stringToConvert) throws ParseException {
		
		if(stringToConvert == null || stringToConvert.isEmpty()) {
			return null;
		}else {
			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
			Date date = dateFormat.parse(stringToConvert);
			DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
			String dateStr = formatter.format(date);	
			return dateStr;
		}
		
	}
	
	public static String isOtherThenPercentage(String arg) {
		
		if (Def.NUM_MENO_UNO.equalsIgnoreCase(arg)) {
			arg = Def.STR_PERCENTAGE;
		}else if (Def.NUM_MENO_UNO.equalsIgnoreCase(arg)) {
			arg = Def.STR_PERCENTAGE;
		}else if(arg == null) {
			arg = Def.STR_PERCENTAGE;
		}else if("".equals(arg)){
			arg = Def.STR_PERCENTAGE;
		}
		return arg;
	}
	
	public static boolean isNullOrBlank(String arg) {
			if(arg == null) {
				return true;
			}else if("".equals(arg)){
				return true;
			}
			return false;
		}
	
	public static String isBlankThenNull(String arg) {
		if("".equals(arg)){
			return null;
		}
		return arg;
	}
	
	public static String getActualDateFormatted() {
		Date date = new Date();
	    String strDateFormat = Def.DATA_FORMAT_DATA_HOUR;
	    DateFormat dateFormat = new SimpleDateFormat(strDateFormat);
	    String formattedDate= dateFormat.format(date);
	    return formattedDate;
	}

	public static Date convertStringToDate(String source) {
		SimpleDateFormat sdf = new SimpleDateFormat(Def.DATA_FORMAT_SIMPLE);
		try {
			return sdf.parse(source);
		} catch (ParseException e) {
			return null;
		}
	}
	
	public static LocalDate convertStringToLocalDate(String source) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(Def.DATA_FORMAT_SIMPLE);
		return LocalDate.parse(source, formatter);
	}
	
	public static Long getLong(String arg) {
		Long l = 0L;
		try {
			if(arg == null) {
				l = 0L;
			}else {
				l = Long.parseLong(arg);
			}
			
		}catch(NumberFormatException ex){
			logger.error(ex.getMessage());
		}
		return l;
	}

	public static Double getDouble(String arg) {
		Double d = 0D ;
		try {
			if(arg == null) {
				d = 0D;
			}else {
				d = Double.parseDouble(arg);
			}
			
		}catch(NumberFormatException ex){
			logger.error(ex.getMessage());
		}
		return d;
	}
	
}
