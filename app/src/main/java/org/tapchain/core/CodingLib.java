package org.tapchain.core;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class CodingLib {
	public static Object decode(String objStr, String type) {
		Object rtn = null;
		if(type == null)
			return rtn;
		if(type.equals("String")) {
			rtn = objStr;
		}else if(type.equals("Integer")) {
			rtn = Integer.valueOf(objStr);
		}else if(type.equals("Float")) {
			rtn = Float.valueOf(objStr);
		}else if(type.equals("Calendar")){
			Calendar c = Calendar.getInstance();
			SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
			try {
				c.setTime(sdf.parse(objStr));
			} catch (ParseException e) {
				e.printStackTrace();
			}
			rtn = c;
		}else {
			rtn = "test";
		}
		return rtn;
	}

	public static String encode(Object obj) {
		if(obj instanceof String) 
			return (String) obj;
		else if(obj instanceof Integer)
			return Integer.toString((Integer) obj);
		else if(obj instanceof Float)
			return Float.toString((Float)obj);
		else if(obj instanceof Calendar) {
			Calendar cal = (Calendar)obj;
			String s = String.format("%02d:%02d", cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE));
			return s;
		}
		return obj.toString();
	}

	public static String talk(Object... objects) {
		StringBuilder stringBuilder = new StringBuilder();
		for(Object obj: objects) {
			String code = encode(obj);
			if (obj instanceof Calendar && ((Calendar) obj).get(Calendar.MINUTE) == 0)
				code = String.format("%d o'clock", ((Calendar) obj).get(Calendar.HOUR_OF_DAY));
			stringBuilder.append(code).append(" ");
		}
		return stringBuilder.toString();
	}
}
