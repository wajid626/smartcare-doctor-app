package com.smartdoctor.alert;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;


import com.smartdoctor.R;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.smartdoctor.utils.RestServiceUtil;
import com.smartdoctor.utils.SmartDocUtils;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup.LayoutParams;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class Alerts extends  Activity{

	TableLayout table_layout;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_alert);
		
		String alerts = null; 
		
		try {
			String userName = SmartDocUtils.getLoginUserName(this.getApplicationContext());
			
			alerts = new RestServiceUtil().execute("UserService/findMyAlerts?physicianName=" + userName).get();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		table_layout = (TableLayout)this.findViewById(R.id.alertTable);
		
		Gson gson = new Gson();
    	JsonArray objs = gson.fromJson(alerts, JsonArray.class);

		
    	TableRow.LayoutParams tableRowParams = new TableRow.LayoutParams();
	    tableRowParams.setMargins(1, 1, 1, 1);
	    tableRowParams.weight = 1;
	    
	    TableRow header= new TableRow(this);
	    TableRow.LayoutParams lpHeader = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT);
	    header.setLayoutParams(lpHeader);
        header.setBackgroundColor(Color.GRAY);
        //tableRowParams.setMargins(1, 1, 1, 1);
	    //tableRowParams.weight = 1;
	    
	    TextView tvHeader = getTextView();
	    tvHeader.setText("Patient Name");
	    tvHeader.setBackgroundColor(Color.GRAY);
	    header.addView(tvHeader, tableRowParams);
	    
	    tvHeader = getTextView();
	    tvHeader.setText("Description");
	    tvHeader.setBackgroundColor(Color.GRAY);
	    header.addView(tvHeader, tableRowParams);
	    
	    tvHeader = getTextView();
	    tvHeader.setText("Alert Date");
	    tvHeader.setBackgroundColor(Color.GRAY);
	    header.addView(tvHeader, tableRowParams);
	    
	    table_layout.addView(header);
	    
	    for(JsonElement obj : objs) {
	    	
	    	TableRow row= new TableRow(this);
		    TableRow.LayoutParams lp = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT);
		    row.setLayoutParams(lp);
	        row.setBackgroundColor(Color.BLACK);
	        tableRowParams.setMargins(1, 1, 1, 1);
		    tableRowParams.weight = 1;
			
			TextView tv = getTextView();
			tv.setText(((JsonObject)obj).get("PatientName").getAsString()); 
			row.addView(tv, tableRowParams);
			 
			tv = getTextView();
		    tv.setText(((JsonObject)obj).get("Message").getAsString()); 
			row.addView(tv, tableRowParams);
			
			tv = getTextView();
		    tv.setText(((JsonObject)obj).get("DateTime").getAsString()); 
			row.addView(tv, tableRowParams);
			
			table_layout.addView(row);
		 }
	}
	
	private TextView getTextView() {
		TextView tv = new TextView(this);
		tv.setGravity(Gravity.CENTER);
		tv.setBackgroundColor(Color.WHITE);
		
		return tv;
	}
}
