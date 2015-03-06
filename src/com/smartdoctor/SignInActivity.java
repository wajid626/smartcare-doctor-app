package com.smartdoctor;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;

import com.smartdoctor.R;
import com.smartdoctor.config.SmartDoctorConfig;
import com.smartdoctor.utils.RestServiceUtil;
import com.smartdoctor.utils.SmartDocUtils;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class SignInActivity  extends Activity{
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
	}
	
	public void login(View view) {
		
		String userName = ((EditText)findViewById(R.id.txtUsername)).getText().toString();
		String password = ((EditText)findViewById(R.id.txtPassword)).getText().toString();
		if ( !userName.equalsIgnoreCase(password)) {
			((TextView)findViewById(R.id.error)).setVisibility(0);
			return;
		}
		
		//Save logged-in user that can be retrieved throughout the application
		Editor editor = SmartDocUtils.getSharedPreferences(this.getApplicationContext()).edit();
		editor.putString(SmartDoctorConfig.PREF_LOGIN_NAME, userName).commit();
		
		Intent mainIntent = new Intent(getApplicationContext(), MainActivity.class);
		this.startActivity(mainIntent);
	}
}
