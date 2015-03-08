package com.smartdoctor.tracker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.sails.engine.Beacon;
import com.sails.engine.LocationRegion;
import com.sails.engine.SAILS;
import com.sails.engine.MarkerManager;
import com.sails.engine.PathRoutingManager;
import com.sails.engine.PinMarkerManager;
import com.sails.engine.SAILS.GeoNode;
import com.sails.engine.SAILSMapView;
import com.sails.engine.core.model.GeoPoint;
import com.sails.engine.overlay.ListOverlay;
import com.sails.engine.overlay.Marker;
import com.sails.engine.overlay.Overlay;
import com.sails.engine.overlay.TextListOverlay;
import com.sails.engine.overlay.TextOverlay;
import com.smartdoctor.R;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.app.ActionBar;
import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


public class TrackerActivity extends Activity {

    static SAILS mSails;
    static SAILSMapView mSailsMapView;
    ImageView zoomin;
    ImageView zoomout;
    ImageView lockcenter;
    Button endRouteButton;
  //  Button pinMarkerButton;
    TextView distanceView;
    TextView currentFloorDistanceView;
    TextView msgView;
    SlidingMenu menu;
    ActionBar actionBar;
    ExpandableListView expandableListView;
    ExpandableAdapter eAdapter;
    Vibrator mVibrator;
    Spinner floorList;
    ArrayAdapter<String> adapter;
    byte zoomSav = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracker);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        menu = new SlidingMenu(this);
        menu.setMode(SlidingMenu.LEFT);
        menu.setTouchModeAbove(SlidingMenu.TOUCHMODE_NONE);
        menu.setFadeDegree(0.0f);
        menu.attachToActivity(this, SlidingMenu.SLIDING_CONTENT);
        menu.setMenu(R.layout.expantablelist);
        mVibrator = (Vibrator) getSystemService(Service.VIBRATOR_SERVICE);
        actionBar = getActionBar();
       // actionBar.setHomeButtonEnabled(true);
        //actionBar.setDisplayHomeAsUpEnabled(true);
        //actionBar.setIcon(new ColorDrawable(getResources().getColor(android.R.color.transparent)));
        zoomin = (ImageView) findViewById(R.id.zoomin);
        zoomout = (ImageView) findViewById(R.id.zoomout);
        lockcenter = (ImageView) findViewById(R.id.lockcenter);
        endRouteButton = (Button) findViewById(R.id.stopRoute);
        // pinMarkerButton = (Button) findViewById(R.id.pinMarker);
        distanceView = (TextView) findViewById(R.id.distanceView);
        distanceView.setVisibility(View.INVISIBLE);
        currentFloorDistanceView = (TextView) findViewById(R.id.currentFloorDistanceView);
        currentFloorDistanceView.setVisibility(View.INVISIBLE);
        msgView = (TextView) findViewById(R.id.msgView);
        msgView.setVisibility(View.INVISIBLE);
 
        floorList = (Spinner) findViewById(R.id.spinner);

        zoomin.setOnClickListener(controlListener);
        zoomout.setOnClickListener(controlListener);
        lockcenter.setOnClickListener(controlListener);
        endRouteButton.setOnClickListener(controlListener);
        endRouteButton.setVisibility(View.INVISIBLE);
       // pinMarkerButton.setOnClickListener(controlListener);
       // pinMarkerButton.setVisibility(View.VISIBLE);
        expandableListView = (ExpandableListView) findViewById(R.id.expandableListView);
        expandableListView.setOnChildClickListener(childClickListener);

        LocationRegion.FONT_LANGUAGE = LocationRegion.NORMAL;
        
        //new a SAILS engine.
        mSails = new SAILS(this);
        //set location mode.
        // mSails.setMode(SAILS.BLE_GFP_IMU);
        mSails.setMode(SAILS.WIFI_GFP_IMU);
        //set floor number sort rule from descending to ascending.
        mSails.setReverseFloorList(true);
        //create location change call back.
        mSails.setOnLocationChangeEventListener(new SAILS.OnLocationChangeEventListener() {
            @Override
            public void OnLocationChange() {

                if (mSailsMapView.isCenterLock() && !mSailsMapView.isInLocationFloor() && !mSails.getFloor().equals("") && mSails.isLocationFix()) {
                    //set the map that currently location engine recognize.
                    mSailsMapView.getMapViewPosition().setZoomLevel((byte) 20);
                    mSailsMapView.loadCurrentLocationFloorMap();
                    Toast t = Toast.makeText(getBaseContext(), mSails.getFloorDescription(mSails.getFloor()), Toast.LENGTH_SHORT);
                    t.show();
                }
            }
        });

        mSails.setOnBLEPositionInitialzeCallback(10000,new SAILS.OnBLEPositionInitializeCallback() {
            @Override
            public void onStart() {

            }

            @Override
            public void onFixed() {

            }

            @Override
            public void onTimeOut() {
                if(!mSails.checkMode(SAILS.BLE_ADVERTISING))
                    mSails.stopLocatingEngine();
                new AlertDialog.Builder(TrackerActivity.this)
                        .setTitle("Positioning Timeout")
                        .setMessage("Put some time out message!")
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialoginterface, int i) {
                                mSailsMapView.setMode(SAILSMapView.GENERAL);
                            }
                        }).setPositiveButton("Retry", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        mSails.startLocatingEngine();
                    }
                }).show();
            }
        });

        mSails.setNoWalkAwayPushRepeatDuration(6000);
        mSails.setOnBTLEPushEventListener(new SAILS.OnBTLEPushEventListener() {
            @Override
            public void OnPush(final Beacon mB) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                    	
                        Toast.makeText(getApplication(),mB.push_name,Toast.LENGTH_SHORT).show();
                    }
                });

            }

            @Override
            public void OnNothingPush() {
                Log.e("Nothing Push","true");
            }
        });

        //new and insert a SAILS MapView from layout resource.
        mSailsMapView = new SAILSMapView(this);
        ((FrameLayout) findViewById(R.id.SAILSMap)).addView(mSailsMapView);
        //configure SAILS map after map preparation finish.
        mSailsMapView.post(new Runnable() {
            @Override
            public void run() {
                //please change token and building id to your own building project in cloud.
                mSails.loadCloudBuilding("be1e4839324e48cfb00943f0d5bec693", "54a626a8d98797a81400033a", new SAILS.OnFinishCallback() {
                    @Override
                    public void onSuccess(String response) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mapViewInitial();
                                routingInitial();
                                slidingMenuInitial();
                                Toast t = Toast.makeText(getBaseContext(), "test.", Toast.LENGTH_SHORT);
                                t.show();
         
                            }
                        });

                    }

                    @Override
                    public void onFailed(String response) {
                        Toast t = Toast.makeText(getBaseContext(), "Load cloud project fail, please check network connection.", Toast.LENGTH_SHORT);
                        t.show();
                    }
                });
            }
        });
    }

    void mapViewInitial() {
        //establish a connection of SAILS engine into SAILS MapView.
        mSailsMapView.setSAILSEngine(mSails);

        //set location pointer icon.
        mSailsMapView.setLocationMarker(R.drawable.circle, R.drawable.arrow, null, 35);

        //set location marker visible.
        mSailsMapView.setLocatorMarkerVisible(true);

        //load first floor map in package.
        mSailsMapView.loadFloorMap(mSails.getFloorNameList().get(0));
        //actionBar.setTitle("Map POIs");

        //Auto Adjust suitable map zoom level and position to best view position.
        mSailsMapView.autoSetMapZoomAndView();
       // LocationRegion locationRegion = new LocationRegion();
        
      
        //List<GeoNode> gn = new ArrayList<GeoNode>();
        //GeoNode g = new GeoNode(24.140175722030325, 120.68016191199679);
        //ocationRegion.setVertexList(gn);
        ///mSailsMapView.getMarkerManager().setLocationRegionMarker(locationRegion, Marker.boundCenterBottom(getResources().getDrawable(R.drawable.destination)));
      //  Marker marker = new Marker(new GeoPoint(24.14022053389309, 120.6803128659729),Marker.boundCenter(getResources().getDrawable(R.drawable.arrow)));
        Marker marker = new Marker(new GeoPoint(24.140245 , 120.680300),Marker.boundCenter(getResources().getDrawable(R.drawable.circle)));
        mSailsMapView.getRoutingManager().getPathPaint().setTextSize(25f);
        mSailsMapView.getRoutingManager().getPathPaint().setTextScaleX(4f);
        mSailsMapView.getRoutingManager().getPathStrokePaint().setStrokeWidth(0.0001f);
        

        TextOverlay textoverlay = new TextOverlay(new GeoPoint(24.140245 , 120.680300), "stethescope",   mSailsMapView.getRoutingManager().getPathPaint(),   mSailsMapView.getRoutingManager().getPathStrokePaint()); 
        TextListOverlay yourOverlay = new TextListOverlay(); 
        yourOverlay.getOverlayItems().add(textoverlay);
        mSailsMapView.getOverlays().add( yourOverlay);
        
       
        
        //lat='24.140175722030325' lon='120.68016191199679'

        //set location region click call back.
        mSailsMapView.setOnRegionClickListener(new SAILSMapView.OnRegionClickListener() {
            @Override
            public void onClick(List<LocationRegion> locationRegions) {
                LocationRegion lr = locationRegions.get(0);
                //begin to routing
                if (mSails.isLocationEngineStarted()) {
                    //set routing start point to current user location.
                    mSailsMapView.getRoutingManager().setStartRegion(PathRoutingManager.MY_LOCATION);

                    //set routing end point marker icon.
                    mSailsMapView.getRoutingManager().setTargetMakerDrawable(Marker.boundCenterBottom(getResources().getDrawable(R.drawable.destination)));

                    //set routing path's color.
                    mSailsMapView.getRoutingManager().getPathPaint().setColor(0xFF35b3e5);

                    endRouteButton.setVisibility(View.VISIBLE);
                    currentFloorDistanceView.setVisibility(View.VISIBLE);
                    msgView.setVisibility(View.VISIBLE);

                } else {
                    mSailsMapView.getRoutingManager().setTargetMakerDrawable(Marker.boundCenterBottom(getResources().getDrawable(R.drawable.map_destination)));
                    mSailsMapView.getRoutingManager().getPathPaint().setColor(0xFF85b038);
                    if (mSailsMapView.getRoutingManager().getStartRegion() != null)
                        endRouteButton.setVisibility(View.VISIBLE);
                }

                //set routing end point location.
                mSailsMapView.getRoutingManager().setTargetRegion(lr);
                

                //begin to route.
                if (mSailsMapView.getRoutingManager().enableHandler())
                    distanceView.setVisibility(View.VISIBLE);
            }
        });

        mSailsMapView.getPinMarkerManager().setOnPinMarkerClickCallback(new PinMarkerManager.OnPinMarkerClickCallback() {
            @Override
            public void OnClick(MarkerManager.LocationRegionMarker locationRegionMarker) {
                Toast.makeText(getApplication(), "(" + Double.toString(locationRegionMarker.locationRegion.getCenterLatitude()) + "," +
                        Double.toString(locationRegionMarker.locationRegion.getCenterLongitude()) + ")", Toast.LENGTH_SHORT).show();
            }
        });

        //set location region long click call back.
        mSailsMapView.setOnRegionLongClickListener(new SAILSMapView.OnRegionLongClickListener() {
            @Override
            public void onLongClick(List<LocationRegion> locationRegions) {
                if (mSails.isLocationEngineStarted())
                    return;

                mVibrator.vibrate(70);
                mSailsMapView.getMarkerManager().clear();
                mSailsMapView.getRoutingManager().setStartRegion(locationRegions.get(0));
                mSailsMapView.getMarkerManager().setLocationRegionMarker(locationRegions.get(0), Marker.boundCenter(getResources().getDrawable(R.drawable.start_point)));
            }
        });

        //design some action in floor change call back.
        mSailsMapView.setOnFloorChangedListener(new SAILSMapView.OnFloorChangedListener() {
            @Override
            public void onFloorChangedBefore(String floorName) {
                //get current map view zoom level.
                zoomSav = mSailsMapView.getMapViewPosition().getZoomLevel();
            }

            @Override
            public void onFloorChangedAfter(final String floorName) {
                Runnable r = new Runnable() {
                    @Override
                    public void run() {
                        //check is locating engine is start and current brows map is in the locating floor or not.
                        if (mSails.isLocationEngineStarted() && mSailsMapView.isInLocationFloor()) {
                            //change map view zoom level with animation.
                            mSailsMapView.setAnimationToZoom(zoomSav);
                        }
                    }
                };
                new Handler().postDelayed(r, 1000);

                int position = 0;
                for (String mS : mSails.getFloorNameList()) {
                    if (mS.equals(floorName))
                        break;
                    position++;
                }
                floorList.setSelection(position);
            }
        });

        //design some action in mode change call back.
        mSailsMapView.setOnModeChangedListener(new SAILSMapView.OnModeChangedListener() {
            @Override
            public void onModeChanged(int mode) {
                if (((mode & SAILSMapView.LOCATION_CENTER_LOCK) == SAILSMapView.LOCATION_CENTER_LOCK) && ((mode & SAILSMapView.FOLLOW_PHONE_HEADING) == SAILSMapView.FOLLOW_PHONE_HEADING)) {
                    lockcenter.setImageDrawable(getResources().getDrawable(R.drawable.center3));
                } else if ((mode & SAILSMapView.LOCATION_CENTER_LOCK) == SAILSMapView.LOCATION_CENTER_LOCK) {
                    lockcenter.setImageDrawable(getResources().getDrawable(R.drawable.center2));
                } else {
                    lockcenter.setImageDrawable(getResources().getDrawable(R.drawable.center1));
                }
            }
        });

        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, mSails.getFloorDescList());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        floorList.setAdapter(adapter);
        floorList.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!mSailsMapView.getCurrentBrowseFloorName().equals(mSails.getFloorNameList().get(position)))
                    mSailsMapView.loadFloorMap(mSails.getFloorNameList().get(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    void routingInitial() {
        mSailsMapView.getRoutingManager().setStartMakerDrawable(Marker.boundCenter(getResources().getDrawable(R.drawable.start_point)));
        mSailsMapView.getRoutingManager().setTargetMakerDrawable(Marker.boundCenterBottom(getResources().getDrawable(R.drawable.map_destination)));
        mSailsMapView.getRoutingManager().setOnRoutingUpdateListener(new PathRoutingManager.OnRoutingUpdateListener() {
            @Override
            public void onArrived(LocationRegion targetRegion) {
                Toast.makeText(getApplication(), "Arrive.", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onRouteSuccess() {
                List<GeoPoint> gplist = mSailsMapView.getRoutingManager().getCurrentFloorRoutingPathNodes();
                mSailsMapView.autoSetMapZoomAndView(gplist);
            }

            @Override
            public void onRouteFail() {
                Toast.makeText(getApplication(), "Route Fail.", Toast.LENGTH_SHORT).show();
                mSailsMapView.getRoutingManager().disableHandler();
            }

            @Override
            public void onPathDrawFinish() {
            }

            @Override
            public void onTotalDistanceRefresh(int distance) {
                distanceView.setText("Total Routing Distance: " + Integer.toString(distance) + " (m)");
            }

            @Override
            public void onReachNearestTransferDistanceRefresh(int distance, int nodeType) {
                switch (nodeType) {
                    case PathRoutingManager.SwitchFloorInfo.ELEVATOR:
                        currentFloorDistanceView.setText("To Nearest Elevator Distance: " + Integer.toString(distance) + " (m)");
                        break;
                    case PathRoutingManager.SwitchFloorInfo.ESCALATOR:
                        currentFloorDistanceView.setText("To Nearest Escalator Distance: " + Integer.toString(distance) + " (m)");
                        break;
                    case PathRoutingManager.SwitchFloorInfo.STAIR:
                        currentFloorDistanceView.setText("To Nearest Stair Distance: " + Integer.toString(distance) + " (m)");
                        break;
                    case PathRoutingManager.SwitchFloorInfo.DESTINATION:
                        currentFloorDistanceView.setText("To Destination Distance: " + Integer.toString(distance) + " (m)");
                        break;
                }
            }

            @Override
            public void onSwitchFloorInfoRefresh(List<PathRoutingManager.SwitchFloorInfo> infoList, int nearestIndex) {

                //set markers for every transfer location
                for (PathRoutingManager.SwitchFloorInfo mS : infoList) {
                    if (mS.direction != PathRoutingManager.SwitchFloorInfo.GO_TARGET)
                        mSailsMapView.getMarkerManager().setLocationRegionMarker(mS.fromBelongsRegion, Marker.boundCenter(getResources().getDrawable(R.drawable.transfer_point)));
                }

                //when location engine not turn,there is no current switch floor info.
                if (nearestIndex == -1)
                    return;

                PathRoutingManager.SwitchFloorInfo sf = infoList.get(nearestIndex);

                switch (sf.nodeType) {
                    case PathRoutingManager.SwitchFloorInfo.ELEVATOR:
                        if (sf.direction == PathRoutingManager.SwitchFloorInfo.UP)
                            msgView.setText("å°Žèˆªæ��ç¤º: \nè«‹æ�­é›»æ¢¯ä¸Šæ¨“è‡³" + mSails.getFloorDescription(sf.toFloorname));
                        else if (sf.direction == PathRoutingManager.SwitchFloorInfo.DOWN)
                            msgView.setText("å°Žèˆªæ��ç¤º: \nè«‹æ�­é›»æ¢¯ä¸‹æ¨“è‡³" + mSails.getFloorDescription(sf.toFloorname));
                        break;

                    case PathRoutingManager.SwitchFloorInfo.ESCALATOR:
                        if (sf.direction == PathRoutingManager.SwitchFloorInfo.UP)
                            msgView.setText("å°Žèˆªæ��ç¤º: \nè«‹æ�­æ‰‹æ‰¶æ¢¯ä¸Šæ¨“è‡³" + mSails.getFloorDescription(sf.toFloorname));
                        else if (sf.direction == PathRoutingManager.SwitchFloorInfo.DOWN)
                            msgView.setText("å°Žèˆªæ��ç¤º: \nè«‹æ�­æ‰‹æ‰¶æ¢¯ä¸‹æ¨“è‡³" + mSails.getFloorDescription(sf.toFloorname));
                        break;

                    case PathRoutingManager.SwitchFloorInfo.STAIR:
                        if (sf.direction == PathRoutingManager.SwitchFloorInfo.UP)
                            msgView.setText("å°Žèˆªæ��ç¤º: \nè«‹èµ°æ¨“æ¢¯ä¸Šæ¨“è‡³" + mSails.getFloorDescription(sf.toFloorname));
                        else if (sf.direction == PathRoutingManager.SwitchFloorInfo.DOWN)
                            msgView.setText("å°Žèˆªæ��ç¤º: \nè«‹èµ°æ¨“æ¢¯ä¸‹æ¨“è‡³" + mSails.getFloorDescription(sf.toFloorname));
                        break;

                    case PathRoutingManager.SwitchFloorInfo.DESTINATION:
                        msgView.setText("å°Žèˆªæ��ç¤º: \nå‰�å¾€" + sf.fromBelongsRegion.getName());
                        break;
                }
            }
        });
    }

    void slidingMenuInitial() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //1st stage groups
                List<Map<String, String>> groups = new ArrayList<Map<String, String>>();
                //2nd stage groups
                List<List<Map<String, LocationRegion>>> childs = new ArrayList<List<Map<String, LocationRegion>>>();
                for (String mS : mSails.getFloorNameList()) {
                    Map<String, String> group_item = new HashMap<String, String>();
                    group_item.put("group", mSails.getFloorDescription(mS));
                    groups.add(group_item);

                    List<Map<String, LocationRegion>> child_items = new ArrayList<Map<String, LocationRegion>>();
                    for (LocationRegion mlr : mSails.getLocationRegionList(mS)) {
                        if (mlr.getName() == null || mlr.getName().length() == 0)
                            continue;

                        Map<String, LocationRegion> childData = new HashMap<String, LocationRegion>();
                        childData.put("child", mlr);
                        child_items.add(childData);
                    }
                    childs.add(child_items);
                }
                eAdapter = new ExpandableAdapter(getBaseContext(), groups, childs);
                expandableListView.setAdapter(eAdapter);
            }
        });
    }

    ExpandableListView.OnChildClickListener childClickListener = new ExpandableListView.OnChildClickListener() {
        @Override
        public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {

            LocationRegion lr = eAdapter.childs.get(groupPosition).get(childPosition).get("child");

            if (!lr.getFloorName().equals(mSailsMapView.getCurrentBrowseFloorName())) {
                mSailsMapView.loadFloorMap(lr.getFloorName());
                mSailsMapView.getMapViewPosition().setZoomLevel((byte) 19);
                Toast.makeText(getBaseContext(), mSails.getFloorDescription(lr.getFloorName()), Toast.LENGTH_SHORT).show();
            }
            GeoPoint poi = new GeoPoint(lr.getCenterLatitude(), lr.getCenterLongitude());
            Log.e("Geo lat", String.valueOf(lr.getCenterLatitude()));
            Log.e("Geo long", String.valueOf(lr.getCenterLongitude()));
            mSailsMapView.setAnimationMoveMapTo(poi);
            menu.showContent();
            return false;
        }
    };

    View.OnClickListener controlListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v == zoomin) {
                //set map zoomin function.
                mSailsMapView.zoomIn();
            } else if (v == zoomout) {
                //set map zoomout function.
                mSailsMapView.zoomOut();
            } else if (v == lockcenter) {
                if (!mSails.isLocationFix() || !mSails.isLocationEngineStarted()) {
                    Toast t = Toast.makeText(getBaseContext(), "Location Not Found or Location Engine Turn Off.", Toast.LENGTH_SHORT);
                    t.show();
                    return;
                }
                if (!mSailsMapView.isCenterLock() && !mSailsMapView.isInLocationFloor()) {
                    //set the map that currently location engine recognize.
                    mSailsMapView.loadCurrentLocationFloorMap();

                    Toast t = Toast.makeText(getBaseContext(), "Go Back to Locating Floor First.", Toast.LENGTH_SHORT);
                    t.show();
                    return;
                }
                //set map mode.
                //FOLLOW_PHONE_HEADING: the map follows the phone's heading.
                //LOCATION_CENTER_LOCK: the map locks the current location in the center of map.
                //ALWAYS_LOCK_MAP: the map will keep the mode even user moves the map.
                if (mSailsMapView.isCenterLock()) {
                    if ((mSailsMapView.getMode() & SAILSMapView.FOLLOW_PHONE_HEADING) == SAILSMapView.FOLLOW_PHONE_HEADING)
                        //if map control mode is follow phone heading, then set mode to location center lock when button click.
                        mSailsMapView.setMode(mSailsMapView.getMode() & ~SAILSMapView.FOLLOW_PHONE_HEADING);
                    else
                        //if map control mode is location center lock, then set mode to follow phone heading when button click.
                        mSailsMapView.setMode(mSailsMapView.getMode() | SAILSMapView.FOLLOW_PHONE_HEADING);
                } else {
                    //if map control mode is none, then set mode to loction center lock when button click.
                    mSailsMapView.setMode(mSailsMapView.getMode() | SAILSMapView.LOCATION_CENTER_LOCK);
                }
            } else if (v == endRouteButton) {
                endRouteButton.setVisibility(View.INVISIBLE);
                distanceView.setVisibility(View.INVISIBLE);
                currentFloorDistanceView.setVisibility(View.INVISIBLE);
                msgView.setVisibility(View.INVISIBLE);
                //end route.
                mSailsMapView.getRoutingManager().disableHandler();
            }
           /* else if (v == pinMarkerButton) {
                Toast.makeText(getApplication(), "Please Touch Map and Set PinMarker.", Toast.LENGTH_SHORT).show();
                mSailsMapView.getPinMarkerManager().setOnPinMarkerGenerateCallback(Marker.boundCenterBottom(getResources().getDrawable(R.drawable.parking_target)), new PinMarkerManager.OnPinMarkerGenerateCallback() {
                    @Override
                    public void OnGenerate(MarkerManager.LocationRegionMarker locationRegionMarker) {
                        Toast.makeText(getApplication(), "One PinMarker Generated.", Toast.LENGTH_SHORT).show();
                    }
                });
            }*/
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                if (menu.isMenuShowing())
                    menu.showContent();
                else
                    menu.showMenu();


                //collapse all expandable groups.
                if (eAdapter != null) {
                    for (int i = 0; i < eAdapter.groups.size(); i++)
                        expandableListView.collapseGroup(i);
                }

                return true;

            case R.id.start_location_engine:
                if (!mSails.isLocationEngineStarted()) {
                    mSails.startLocatingEngine();
                    mSailsMapView.setLocatorMarkerVisible(true);
                    Toast.makeText(this, "Start Location Engine", Toast.LENGTH_SHORT).show();
                    mSailsMapView.setMode(SAILSMapView.LOCATION_CENTER_LOCK | SAILSMapView.FOLLOW_PHONE_HEADING);
                    lockcenter.setVisibility(View.VISIBLE);
                    endRouteButton.setVisibility(View.INVISIBLE);
                }

                return true;

            case R.id.stop_location_engine:
                if (mSails.isLocationEngineStarted()) {
                    mSails.stopLocatingEngine();
                    mSailsMapView.setLocatorMarkerVisible(false);
                    mSailsMapView.setMode(SAILSMapView.GENERAL);
                    mSailsMapView.getRoutingManager().disableHandler();
                  //  pinMarkerButton.setVisibility(View.VISIBLE);
                    endRouteButton.setVisibility(View.INVISIBLE);
                    distanceView.setVisibility(View.INVISIBLE);
                    currentFloorDistanceView.setVisibility(View.INVISIBLE);
                    msgView.setVisibility(View.INVISIBLE);
                    Toast.makeText(this, "Stop Location Engine", Toast.LENGTH_SHORT).show();
                }
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSailsMapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSailsMapView.onPause();
    }

    @Override
    public void onBackPressed() {
        finish();
        super.onBackPressed();
        ((FrameLayout) findViewById(R.id.SAILSMap)).removeAllViews();
    }
    
    

    class ExpandableAdapter extends BaseExpandableListAdapter {

        private Context context;
        List<Map<String, String>> groups;
        List<List<Map<String, LocationRegion>>> childs;

        public ExpandableAdapter(Context context, List<Map<String, String>> groups, List<List<Map<String, LocationRegion>>> childs) {
            this.context = context;
            this.groups = groups;
            this.childs = childs;
        }

        @Override
        public int getGroupCount() {
            return groups.size();
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            return childs.get(groupPosition).size();
        }

        @Override
        public Object getGroup(int groupPosition) {
            return groups.get(groupPosition);
        }

        @Override
        public Object getChild(int groupPosition, int childPosition) {
            return childs.get(groupPosition).get(childPosition);
        }

        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            LinearLayout linearLayout = (LinearLayout) layoutInflater.inflate(R.layout.group, null);
            String text = ((Map<String, String>) getGroup(groupPosition)).get("group");
            TextView tv = (TextView) linearLayout.findViewById(R.id.group_tv);
            tv.setText(text);
            linearLayout.setBackgroundColor(getResources().getColor(android.R.color.holo_blue_dark));
            tv.setTextColor(getResources().getColor(android.R.color.white));
            return linearLayout;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            LinearLayout linearLayout = (LinearLayout) layoutInflater.inflate(R.layout.child, null);
            LocationRegion lr = ((Map<String, LocationRegion>) getChild(groupPosition, childPosition)).get("child");
            TextView tv = (TextView) linearLayout.findViewById(R.id.child_tv);
            tv.setText(lr.getName());
            ImageView imageView = (ImageView) linearLayout.findViewById(R.id.child_iv);
            imageView.setImageResource(R.drawable.expand_item);
            return linearLayout;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }
    }
}
