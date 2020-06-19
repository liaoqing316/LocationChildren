package com.example.locationchildren;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.MyLocationStyle;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener
{

    MapView mapView =null;
    AMap aMap = null;
    private SharedPreferences sp = null;
    private SharedPreferences.Editor editor;
    EditText et_tel;
    ImageView call;


    String[] permissions = new String[]{
            Manifest.permission.INTERNET,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.READ_PHONE_STATE};
    List<String> permissionList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (setPermission()) {
            mapView = findViewById(R.id.map);
            aMap = mapView.getMap();
            initLocation();
            et_tel = findViewById(R.id.et_tel);
            call = findViewById(R.id.iv_call);
            call.setOnClickListener(this);
            mapView.onCreate(savedInstanceState);
            gettel();
        }
    }
    boolean setPermission(){
        permissionList.clear();
        for(int i=0;i<permissions.length;i++){
            if(ContextCompat.checkSelfPermission(this,permissions[i])!= PackageManager.PERMISSION_GRANTED){
                permissionList.add(permissions[i]);
            }
        }
        if(!permissionList.isEmpty()){
            String[] permissions2 = permissionList.toArray(new String[permissionList.size()]);
            ActivityCompat.requestPermissions(this,permissions2,1);
            return false;
        }
        return true;
    }
    public void initLocation() {
            MyLocationStyle myLocationStyle;
            myLocationStyle = new MyLocationStyle();
            myLocationStyle.interval(2000);
            aMap.setMyLocationStyle(myLocationStyle);
            aMap.setMyLocationEnabled(true);
            aMap.moveCamera(CameraUpdateFactory.zoomTo(16));
            Intent intent = new Intent(this, sendService.class);
            bindService(intent, connection, BIND_AUTO_CREATE);
    }
    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    @Override
    protected void onDestroy() {//销毁地图
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    protected void onResume() {//重新绘制地图
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {//暂停地图的绘制
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {//保存地图当前状态
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.iv_call){
            String tel = et_tel.getText().toString().trim();
            int level = 0;
            for(int i=0;i<tel.length();i++) {
                if (Character.isDigit(tel.charAt(i))) {
                    level++;
                }
            }
            if(level==11){
                callPhone(tel);
            }else{
                Toast.makeText(this,"输入号码不对",Toast.LENGTH_SHORT).show();
            }
        }
    }
    void gettel(){
        sp = getSharedPreferences("data",MODE_PRIVATE);
        String data = sp.getString("tel","");
        et_tel.setText(data);
    }
    void callPhone(String tel){
        sp = getSharedPreferences("data",MODE_PRIVATE);
        editor = sp.edit();
        editor.putString("tel",tel);
        editor.apply();
        Intent dialIntent =  new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + tel));
        startActivity(dialIntent);
    }
}
