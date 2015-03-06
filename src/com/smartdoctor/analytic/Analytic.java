package com.smartdoctor.analytic;

import com.smartdoctor.R;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;

public class Analytic extends  Activity{

	private WebView analyticWebView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_analytic);

		analyticWebView = (WebView) findViewById(R.id.webView1);
		
		//enable Javascript
		analyticWebView.getSettings().setJavaScriptEnabled(true);
         
        //loads the WebView completely zoomed out
		analyticWebView.getSettings().setLoadWithOverviewMode(true);
		
		String myHtml = "<iframe src=\"http://smartcare-services.elasticbeanstalk.com\"  frameborder='0' width='800' height='600'/>";
		analyticWebView.loadData(myHtml, "text/html", null);
	}
}
