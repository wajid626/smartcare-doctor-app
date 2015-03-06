package com.smartdoctor.utils;


import android.content.Context;
import android.content.SharedPreferences;

import com.smartdoctor.config.SmartDoctorConfig;

public class SmartDocUtils {
	
	public static SharedPreferences getSharedPreferences(Context context) {
		return context.getSharedPreferences(SmartDoctorConfig.SMARTCARE_PREF, 0);
	}
	
	public static String getLoginUserName(Context context) {
		return getSharedPreferences(context).getString(SmartDoctorConfig.PREF_LOGIN_NAME, null);
	}
	
}
