package columbia.cellular.Utils;

import android.annotation.SuppressLint;

@SuppressLint("DefaultLocale")
public class CommonUtils {
	public static final int SIZE_KB = 1 << 10;
	public static final int SIZE_MB = 1 << 20;
	public static final int SIZE_GB = 1 << 30;
	
	public static String normalizeFileSize(long sizeInBytes){
		double divisor;
		String unit;
		if(sizeInBytes >= SIZE_GB){
			divisor = SIZE_GB;
			unit = "GB";
		}else if(sizeInBytes >= SIZE_MB){
			divisor = SIZE_MB;
			unit = "MB";
		}else if (sizeInBytes >= SIZE_KB){
			divisor = SIZE_KB;
			unit = "KB";
		}else{
			divisor = 1;
			unit = "B";
		}
		
		return String.format("%.2f%s", sizeInBytes/divisor, unit);
	}
}
