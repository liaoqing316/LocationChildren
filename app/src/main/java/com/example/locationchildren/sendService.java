package com.example.locationchildren;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;

import java.io.OutputStream;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;

public class sendService extends Service implements AMapLocationListener {
    AMapLocationClient aMapLocationClient = null;
    AMapLocationClientOption aMapLocationClientOption = null;

    //socket通信
    private final String host = "127.0.0.1";
    private final int post = 8000;
    private Socket socket;
    private OutputStream outputStream;


    double longitude = 0;
    double latitude = 0;
    Date date = null;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        initLocation();
        connect();
        return null;
    }

    public void initLocation(){
        aMapLocationClient = new AMapLocationClient(getApplicationContext());
        aMapLocationClient.setLocationListener(this);
        aMapLocationClientOption = new AMapLocationClientOption();
        aMapLocationClientOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        aMapLocationClientOption.setInterval(180000);
        aMapLocationClientOption.setNeedAddress(false);
        aMapLocationClientOption.setMockEnable(false);
        aMapLocationClient.setLocationOption(aMapLocationClientOption);
        aMapLocationClient.startLocation();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        disSocket();
        if (aMapLocationClient != null) {
            aMapLocationClient.stopLocation();
            aMapLocationClient.onDestroy();
        }
    }

    public void connect(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true){
                    try {
                        socket = new Socket(host,post);
                        socket.setSoTimeout(3000);
                        outputStream = socket.getOutputStream();
                        break;
                    }catch (Exception e){
                        e.printStackTrace();
                    }finally {
                        disSocket();
                    }
                }
            }
        }).start();
    }

    private void disSocket(){
        //如果不为空，则断开socket
        if(socket !=null){
            try {
                outputStream.close();
                socket.close();
                socket = null;
            }catch (Exception e){
                e.printStackTrace();
            }
        }

    }

    public void sendLocation(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String str = "longitude:"+longitude+"\n";
                    outputStream.write(str.getBytes());
                    str = "latitude:"+latitude+"\n";
                    outputStream.write(str.getBytes());
                    str = "time:"+date+"\n";
                    outputStream.write(str.getBytes());
                    Log.i("SendData:", "Success");
                }catch (Exception e){
                    e.printStackTrace();
                    Log.i("SendData:", "Fail");
                    connect();
                }
            }
        }).start();
    }

    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {
        if (aMapLocation.getErrorCode() == 0){
            longitude = aMapLocation.getLongitude();
            latitude = aMapLocation.getLatitude();
            @SuppressLint("SimpleDateFormat") SimpleDateFormat df
                    = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            date = new Date(aMapLocation.getTime());
            df.format(date);
            sendLocation();
        }
    }
}
