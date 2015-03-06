package com.smartdoctor.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import com.smartdoctor.config.SmartDoctorConfig;

import android.os.AsyncTask;




public class RestServiceUtil extends AsyncTask<String, Void, String> {

	@Override
	protected String doInBackground(String... params) {
		try{
			HttpClient client = new DefaultHttpClient();
	        HttpGet request = new HttpGet(SmartDoctorConfig.RESTFUL_URL + params[0]);
	        
	    	HttpResponse response = client.execute(request);
	        BufferedReader rd = new BufferedReader (new InputStreamReader(response.getEntity().getContent()));
	        return rd.readLine();
		} catch ( Exception e) {
			e.printStackTrace();
	    }
		
        return new JSONObject().toString();
	}
}