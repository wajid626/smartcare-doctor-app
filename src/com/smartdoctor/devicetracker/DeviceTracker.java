package com.smartdoctor.devicetracker;

import com.smartcare.smartdoctor.R;

import android.app.Activity;
import android.app.TabActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

@SuppressWarnings("deprecation")
public class DeviceTracker extends  Activity{

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_device_tracker);
	}
}
