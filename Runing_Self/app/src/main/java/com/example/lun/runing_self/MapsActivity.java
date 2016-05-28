package com.example.lun.runing_self;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Criteria;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.text.SimpleDateFormat;
import java.util.ArrayList;


public class MapsActivity extends AppCompatActivity
        implements LocationListener,View.OnClickListener {

    Button start, pause;
    TextView time;
    private long startTime = 0L;

    private Handler customHandler = new Handler();

    long timeInMilliseconds = 0L;
    long timeSwapBuff = 0L;
    long updatedTime = 0L;
    boolean stopTimer = false;



    static final int MIN_TIME = 5000;// 位置更新條件：5000 毫秒
    static final float MIN_DIST = 5; // 位置更新條件：5 公尺
    LocationManager mgr;        // 定位總管
    TextView txv;
    GoogleMap map;  //操控地圖的物件
    LatLng currPoint; // 儲存目前的位置


    private final String TAG = "=== Map Demo ==>";
    /**墾丁*/
    final LatLng PULI = new LatLng(23.968757, 120.951486);
    /**日月潭*/
    final LatLng NCNU = new LatLng(23.950351, 120.930439);

    /** Map */
    private TextView txtOutput;
    private Marker markerMe;

    /** 記錄軌跡 */
    private ArrayList<LatLng> traceOfMe;

    /** GPS */
    private String provider;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        txv = (TextView) findViewById(R.id.txv);
        mgr = (LocationManager) getSystemService(LOCATION_SERVICE);

        setUpMapIfNeeded();


        start = (Button) findViewById(R.id.startButton);
        pause = (Button) findViewById(R.id.pauseButton);
        time = (TextView) findViewById(R.id.timerValue);
        start.setOnClickListener(this);
        pause.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {
            case R.id.startButton:
                startTime = SystemClock.uptimeMillis();
                customHandler.postDelayed(updateTimerThread, 0);
                break;

            case R.id.pauseButton:
                timeSwapBuff += timeInMilliseconds;
                customHandler.removeCallbacks(updateTimerThread);
                break;
        }
    }

    private Runnable updateTimerThread = new Runnable() {

        public void run() {
            timeInMilliseconds = SystemClock.uptimeMillis() - startTime;

            updatedTime = timeSwapBuff + timeInMilliseconds;

            int secs = (int) (updatedTime / 1000);
            int mins = secs / 60;
            secs = secs % 60;
            int milliseconds = (int) (updatedTime % 1000);
            String localtime = "" + String.format("%02d", mins) + ":" + String.format("%02d", secs)
                    + ":" + String.format("%03d", milliseconds);
            time.setText(localtime);

            if (!stopTimer)
                customHandler.postDelayed(this, 0);
        }

    };


    @Override
    protected void onStart() {
        super.onStart();

        initView();
        if (initLocationProvider()) {
            whereAmI();
        } else {
            txtOutput.setText("請開啟定位！");
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();


        // 取得最佳的定位提供者
        String best = mgr.getBestProvider(new Criteria(), true);
        if (best != null) { // 取得快取的最後位置,如果有的話
            txv.setText("取得定位資訊中...");
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            mgr.requestLocationUpdates(best, MIN_TIME, MIN_DIST, this);    // 註冊位置事件監聽器
        } else { // 無提供者, 顯示提示訊息
            txv.setText("請確認已開啟定位功能!");
        }
    }

    private void initView() {
        txtOutput = (TextView) findViewById(R.id.txtOutput);
    }

    private void setUpMapIfNeeded() {

        if (map == null) {
            map = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();                     //取得 Google Map 物件
            if (map != null) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                map.setMyLocationEnabled(true);    // 顯示『我的位置』圖示按鈕
                map.setMapType(GoogleMap.MAP_TYPE_NORMAL);    // 設定地圖為普通街道模式
                map.moveCamera(CameraUpdateFactory.zoomTo(18));    // 將地圖縮放級數改為 18
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mgr.removeUpdates(this);    // 停止監聽位置事件
    }

    @Override
    public void onLocationChanged(Location location) { // 位置變更事件
        if(location != null) { // 如果可以取得座標
            txv.setText(String.format("緯度 %.4f, 經度 %.4f (%s 定位 )",
                    location.getLatitude(),  // 目前緯度
                    location.getLongitude(), // 目前經度
                    location.getProvider()));// 定位方式

            // 將地圖中心點移到目前的經緯度
            currPoint = new LatLng(location.getLatitude(), location.getLongitude());
        }
        else { // 無法取得座標
            txv.setText("暫時無法取得定位資訊...");
        }
    }

    @Override
    public void onProviderDisabled(String provider) { }
    @Override
    public void onProviderEnabled(String provider) { }
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) { }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) { // 依照選項的 id 來處理
            case R.id.satellite:
                item.setChecked(!item.isChecked()); // 切換功能表項目的打勾狀態
                if(item.isChecked())               // 設定是否顯示衛星圖
                    map.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                else
                    map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                break;
            case R.id.traffic:
                item.setChecked(!item.isChecked()); // 切換功能表項目的打勾狀態
                map.setTrafficEnabled(item.isChecked()); // 設定是否顯示交通圖
                break;
            case R.id.setGPS:
                Intent i = new Intent( // 利用 Intent 啟動系統的定位服務設定
                        Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(i);
                break;
            case R.id.about:
                new AlertDialog.Builder(this) // 用交談窗顯示程式版本與版權聲明
                        .setTitle("關於 我的地圖")
                        .setMessage("作者：\n劉兆倫")
                        .setPositiveButton("關閉", null)
                        .show();
                break;
            case R.id.finish:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }



    /************************************************
     *
     * 						GPS部份
     *
     ***********************************************/
    /**
     * GPS初始化，取得可用的位置提供器
     * @return
     */
    private boolean initLocationProvider() {
        mgr = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        //1.選擇最佳提供器
//		Criteria criteria = new Criteria();
//		criteria.setAccuracy(Criteria.ACCURACY_FINE);
//		criteria.setAltitudeRequired(false);
//		criteria.setBearingRequired(false);
//		criteria.setCostAllowed(true);
//		criteria.setPowerRequirement(Criteria.POWER_LOW);
//
//		provider = locationMgr.getBestProvider(criteria, true);
//
//		if (provider != null) {
//			return true;
//		}


        //2.選擇使用GPS提供器
        if (mgr.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            provider = LocationManager.GPS_PROVIDER;
            return true;
        }


        //3.選擇使用網路提供器
//		if (locationMgr.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
//			provider = LocationManager.NETWORK_PROVIDER;
//			return true;
//		}

        return false;
    }

    /**
     * 執行"我"在哪裡
     * 1.建立位置改變偵聽器
     * 2.預先顯示上次的已知位置
     */
    private void whereAmI() {
//		String provider = LocationManager.GPS_PROVIDER;

        //取得上次已知的位置
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Location location = mgr.getLastKnownLocation(provider);
        updateWithNewLocation(location);

        //GPS Listener
        mgr.addGpsStatusListener(gpsListener);


        //Location Listener
        long minTime = 5000;//ms
        float minDist = 5.0f;//meter
        mgr.requestLocationUpdates(provider, minTime, minDist, locationListener);
    }

    /**
     * 顯示"我"在哪裡
     * @param lat
     * @param lng
     */
    private void showMarkerMe(double lat, double lng){
        if (markerMe != null) {
            markerMe.remove();
        }

        MarkerOptions markerOpt = new MarkerOptions();
        markerOpt.position(new LatLng(lat, lng));
        markerOpt.title("我在這裡");
        markerMe = map.addMarker(markerOpt);

        Toast.makeText(this, "lat:" + lat + ",lng:" + lng, Toast.LENGTH_SHORT).show();
    }

    private void cameraFocusOnMe(double lat, double lng){
        CameraPosition camPosition = new CameraPosition.Builder()
                .target(new LatLng(lat, lng))
                .zoom(16)
                .build();

        map.animateCamera(CameraUpdateFactory.newCameraPosition(camPosition));
    }

    private void trackToMe(double lat, double lng){
        if (traceOfMe == null) {
            traceOfMe = new ArrayList<LatLng>();
        }
        traceOfMe.add(new LatLng(lat, lng));

        PolylineOptions polylineOpt = new PolylineOptions();
        for (LatLng latlng : traceOfMe) {
            polylineOpt.add(latlng);
        }

        polylineOpt.color(Color.RED);

        Polyline line = map.addPolyline(polylineOpt);
        line.setWidth(10);
    }

    /**
     * 更新並顯示新位置
     * @param location
     */
    private void updateWithNewLocation(Location location) {
        String where = "";
        if (location != null) {
            //經度
            double lng = location.getLongitude();
            //緯度
            double lat = location.getLatitude();
            //速度
            float speed = location.getSpeed();
            //時間
            long time = location.getTime();
            String timeString = getTimeString(time);

            where = "\n速度: " + speed +
                    "\n時間: " + timeString +
                    "\nProvider: " + provider;

            //標記"我"
            showMarkerMe(lat, lng);
            cameraFocusOnMe(lat, lng);
            trackToMe(lat, lng);

            //移動攝影機跟著"我"
//			CameraPosition cameraPosition = new CameraPosition.Builder()
//		    .target(new LatLng(lat, lng))      		// Sets the center of the map to ZINTUN
//		    .zoom(13)                   // Sets the zoom
//		    .bearing(90)                // Sets the orientation of the camera to east
//		    .tilt(30)                   // Sets the tilt of the camera to 30 degrees
//		    .build();                   // Creates a CameraPosition from the builder
//			mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

//			CameraPosition camPosition = new CameraPosition.Builder()
//											.target(new LatLng(lat, lng))
//											.zoom(16)
//											.build();
//
//			mMap.animateCamera(CameraUpdateFactory.newCameraPosition(camPosition));

        }else{
            where = "No location found.";
        }

        //位置改變顯示
        txtOutput.setText(where);
    }


    GpsStatus.Listener gpsListener = new GpsStatus.Listener() {

        @Override
        public void onGpsStatusChanged(int event) {
            switch (event) {
                case GpsStatus.GPS_EVENT_STARTED:
                    Log.d(TAG, "GPS_EVENT_STARTED");
                    Toast.makeText(MapsActivity.this, "GPS_EVENT_STARTED", Toast.LENGTH_SHORT).show();
                    break;

                case GpsStatus.GPS_EVENT_STOPPED:
                    Log.d(TAG, "GPS_EVENT_STOPPED");
                    Toast.makeText(MapsActivity.this, "GPS_EVENT_STOPPED", Toast.LENGTH_SHORT).show();
                    break;

                case GpsStatus.GPS_EVENT_FIRST_FIX:
                    Log.d(TAG, "GPS_EVENT_FIRST_FIX");
                    Toast.makeText(MapsActivity.this, "GPS_EVENT_FIRST_FIX", Toast.LENGTH_SHORT).show();
                    break;

                case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
                    Log.d(TAG, "GPS_EVENT_SATELLITE_STATUS");
                    break;
            }
        }
    };


    LocationListener locationListener = new LocationListener(){

        @Override
        public void onLocationChanged(Location location) {
            updateWithNewLocation(location);
        }

        @Override
        public void onProviderDisabled(String provider) {
            updateWithNewLocation(null);
        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            switch (status) {
                case LocationProvider.OUT_OF_SERVICE:
                    Log.v(TAG, "Status Changed: Out of Service");
                    Toast.makeText(MapsActivity.this, "Status Changed: Out of Service",
                            Toast.LENGTH_SHORT).show();
                    break;
                case LocationProvider.TEMPORARILY_UNAVAILABLE:
                    Log.v(TAG, "Status Changed: Temporarily Unavailable");
                    Toast.makeText(MapsActivity.this, "Status Changed: Temporarily Unavailable",
                            Toast.LENGTH_SHORT).show();
                    break;
                case LocationProvider.AVAILABLE:
                    Log.v(TAG, "Status Changed: Available");
                    Toast.makeText(MapsActivity.this, "Status Changed: Available",
                            Toast.LENGTH_SHORT).show();
                    break;
            }
        }

    };

    private String getTimeString(long timeInMilliseconds){
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return format.format(timeInMilliseconds);
    }


//	private boolean checkGooglePlayServices(){
//		int result = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
//		switch (result) {
//			case ConnectionResult.SUCCESS:
//				Log.d(TAG, "SUCCESS");
//				return true;
//
//			case ConnectionResult.SERVICE_INVALID:
//				Log.d(TAG, "SERVICE_INVALID");
//				GooglePlayServicesUtil.getErrorDialog(ConnectionResult.SERVICE_INVALID, this, 0).show();
//				break;
//
//			case ConnectionResult.SERVICE_MISSING:
//				Log.d(TAG, "SERVICE_MISSING");
//				GooglePlayServicesUtil.getErrorDialog(ConnectionResult.SERVICE_MISSING, this, 0).show();
//				break;
//
//			case ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED:
//				Log.d(TAG, "SERVICE_VERSION_UPDATE_REQUIRED");
//				GooglePlayServicesUtil.getErrorDialog(ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED, this, 0).show();
//				break;
//
//			case ConnectionResult.SERVICE_DISABLED:
//				Log.d(TAG, "SERVICE_DISABLED");
//				GooglePlayServicesUtil.getErrorDialog(ConnectionResult.SERVICE_DISABLED, this, 0).show();
//				break;
//		}
//
//		return false;
//	}

}