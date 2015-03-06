package com.smartcare.smartdoctor;

import com.smartcare.smartdoctor.R;
import com.smartdoctor.alert.Alerts;
import com.smartdoctor.analytic.Analytic;
import com.smartdoctor.devicetracker.DeviceTracker;
import com.smartdoctor.ehr.EhrActivity;
import com.smartdoctor.tricoder.Tricoder;

import android.app.Activity;
import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;

@SuppressWarnings("deprecation")
public class MainActivity extends  TabActivity{

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);        
        
        TabHost tabHost = getTabHost();
        
        // Tab for EHR
        TabSpec ehrspec = tabHost.newTabSpec("EHR");
        // setting Title and Icon for the Tab
        ehrspec.setIndicator("EHR", getResources().getDrawable(R.drawable.ehr_tab));
        Intent ehrIntent = new Intent(this, EhrActivity.class);
        ehrspec.setContent(ehrIntent);
         
        // Tab for Tricoder
        TabSpec tricoderspec = tabHost.newTabSpec("Tricoder");        
        tricoderspec.setIndicator("Tricoder", getResources().getDrawable(R.drawable.tricoder_tab));
        Intent tricoderIntent = new Intent(this, Tricoder.class);
        tricoderspec.setContent(tricoderIntent);
         
        // Tab for Tracker
        TabSpec trackerspec = tabHost.newTabSpec("Tracker");
        trackerspec.setIndicator("Tracker", getResources().getDrawable(R.drawable.device_tracking_tab));
        Intent trackerIntent = new Intent(this, DeviceTracker.class);
        trackerspec.setContent(tricoderIntent);
        
        // Tab for Analytic
        TabSpec analyticspec = tabHost.newTabSpec("Analytic");
        analyticspec.setIndicator("Analytic", getResources().getDrawable(R.drawable.analytic_tab));
        Intent analyticIntent = new Intent(this, Analytic.class);
        analyticspec.setContent(analyticIntent);
        
         
        // Tab for Alerts
        TabSpec alertsspec = tabHost.newTabSpec("Alerts");
        alertsspec.setIndicator("Alerts", getResources().getDrawable(R.drawable.alert_tab));
        Intent alertsIntent = new Intent(this, Alerts.class);
        alertsspec.setContent(alertsIntent);
        
        // Adding all TabSpec to TabHost
        tabHost.addTab(ehrspec); // Adding EHR tab
        tabHost.addTab(tricoderspec); // Adding Tricoder tab
        tabHost.addTab(trackerspec); // Adding Device Tracker tab
        tabHost.addTab(analyticspec); // Adding Analytic tab
        tabHost.addTab(alertsspec); // Adding Alerts tab
        
        tabHost.setCurrentTab(0);
        
    }
}
