package com.smartdoctor.ehr;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.smartcare.smartdoctor.R;
import com.smartcare.smartdoctor.R.id;
import com.smartcare.smartdoctor.R.layout;
import com.gimbal.logging.GimbalLogConfig;
import com.gimbal.logging.GimbalLogLevel;
import com.gimbal.proximity.Proximity;
import com.gimbal.proximity.ProximityFactory;
import com.gimbal.proximity.ProximityListener;
import com.gimbal.proximity.ProximityOptions;
import com.gimbal.proximity.Visit;
import com.gimbal.proximity.VisitListener;
import com.gimbal.proximity.VisitManager;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.smartdoctor.utils.RestServiceUtil;


public class EhrActivity extends  Activity implements ProximityListener, VisitListener{

	private static final String PROXIMITY_APP_ID = "17d94fe658eb45a29ac432b10df82fa795a15a600a76d7e51db8d12cd188ae2f";
    private static final String PROXIMITY_APP_SECRET = "8b5a39624390e49e16d828d86e7d134a5360edec5d3eb68229199078d3261deb";
    
    private  VisitManager visitManager = null;
    
    private TableLayout table_layout;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		StrictMode.setThreadPolicy(policy);
		
		setContentView(R.layout.activity_ehr);

		initializeProximity();
		
		visitManager = ProximityFactory.getInstance().createVisitManager();
        visitManager.setVisitListener(this);
        ProximityOptions options = new ProximityOptions();
        options.setOption(ProximityOptions.VisitOptionSignalStrengthWindowKey, ProximityOptions.VisitOptionSignalStrengthWindowNone);
        visitManager.startWithOptions(options);
        
        startProximityService(); 
	}
	
	private void initializeProximity() {
    	GimbalLogConfig.setLogLevel(GimbalLogLevel.INFO);
        GimbalLogConfig.enableFileLogging(this.getApplicationContext());

        Proximity.initialize(this, PROXIMITY_APP_ID, PROXIMITY_APP_SECRET);
        Proximity.optimizeWithApplicationLifecycle(getApplication()); 
    }

    private void startProximityService() {
        Log.d(EhrActivity.class.getSimpleName(), "startSession");
        Proximity.startService(this);
    }

	@Override
	public void didArrive(Visit arg0) {		
	}

	@Override
	public void didDepart(Visit arg0) {
	}

	private String currentPatient = null;
	
	@Override
	public void receivedSighting(Visit visit, Date date, Integer rssi) {
		if ((rssi * -1) < 50)  {
			try {
				if (! visit.getTransmitter().getName().equals(currentPatient)){
					currentPatient = visit.getTransmitter().getName();
					//sighted = true;
					String patientDataJsonString = new RestServiceUtil().execute
							("PatientService/findPatientDataFromGimbalId?gimbalId=" + visit.getTransmitter().getName() ).get();
					
					String patientMedHistJsonString = new RestServiceUtil().execute
							("PatientService/findPatientMedicalHistory?patientName=" + visit.getTransmitter().getName() ).get();
					
					populatePatientDetailData(patientDataJsonString);
					populateMedicalHistoryTableData(patientMedHistJsonString);
				}
				
			} catch (Exception e) {
				e.printStackTrace();
			} 
		} 
	}

	@Override
	public void serviceStarted() {
		showToastMessage("Service Started.");
		
	}

	@Override
	public void startServiceFailed(int arg0, String arg1) {
		showToastMessage("StartServiceFailed " + arg1);
	}
	
	public void saveDiagnosis(View view) {
		String medHistory = ((EditText)findViewById(R.id.diagnosisNotes)).getText().toString();
		String patientName = ((TextView) findViewById(R.id.name)).getText().toString();
		
		if (null == medHistory || medHistory.isEmpty()) return;
		
		try {
			new RestServiceUtil().execute
			("PatientService/addPatientMedicalHistory?patientName=" + patientName + "&medHistory=" + medHistory).get();
			String patientMedHistJsonString = new RestServiceUtil().execute
					("PatientService/findPatientMedicalHistory?patientName=" + patientName).get();
			
			((EditText)findViewById(R.id.diagnosisNotes)).setText("");
			populateMedicalHistoryTableData(patientMedHistJsonString);
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}
	
	private void showToastMessage(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
            }
        });
    }
	
	/**
	 * 
	 * @param patientDataJsonString
	 * Reads PatientData JSON String and maps the attributes to teh UI element.
	 * 
	 */
	private void populatePatientDetailData(String patientDataJsonString) {
		TextView tvName =  (TextView)findViewById(R.id.name);
		TextView tvAge =  (TextView)findViewById(R.id.age);
		TextView tvSex =  (TextView)findViewById(R.id.sex);
		TextView tvAddress =  (TextView)findViewById(R.id.address);
		
		Gson gson = new Gson();
    	JsonArray objs = gson.fromJson(patientDataJsonString, JsonArray.class);
    	
    	for(JsonElement obj : objs) {
    		tvName.setText(((JsonObject)obj).get("PatientName").getAsString());
    		tvAge.setText(((JsonObject)obj).get("Age").getAsString());
    		tvSex.setText(((JsonObject)obj).get("Sex").getAsString());
    		tvAddress.setText(((JsonObject)obj).get("Address").getAsString());
    	}
	}
	
	private void populateMedicalHistoryTableData(String medHistoryString) {
		table_layout = (TableLayout)this.findViewById(R.id.medHistTable);
		
		//Clear any old data before re-populating
		table_layout.removeAllViews();
		
		Gson gson = new Gson();
    	JsonArray objs = gson.fromJson(medHistoryString, JsonArray.class);

    	TextView medHistoryCountTv = (TextView)findViewById(R.id.medHistoryCount);
    	medHistoryCountTv.setText("[" + objs.size() + "]");
    	
    	TableRow.LayoutParams tableRowParams = new TableRow.LayoutParams();
	    tableRowParams.setMargins(1, 1, 1, 1);
	    tableRowParams.weight = 1;

	    
	    for(JsonElement obj : objs) {
	    	
	    	TableRow row= new TableRow(this);
		    TableRow.LayoutParams lp = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT);
		    row.setLayoutParams(lp);
	        //row.setBackgroundColor(Color.BLACK);
	        tableRowParams.setMargins(1, 1, 1, 1);
		    tableRowParams.weight = 1;
			
			TextView tv = getTextView();
			tv.setText(((JsonObject)obj).get("MedicalHistory").getAsString()); 
			tv.setGravity(Gravity.LEFT);
			row.addView(tv, tableRowParams);
			
			tv = getTextView();
		    tv.setText(((JsonObject)obj).get("DateTime").getAsString()); 
		    tv.setGravity(Gravity.LEFT);
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