package utils;

import android.annotation.TargetApi;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.util.Log;

import java.io.IOException;
import java.util.List;

import android.Manifest;
import android.content.pm.PackageManager;

/**
 * Created by Administrator on 2018/4/17.
 * 获取用户的地理位置
 */
public class GPSUtils {
    private static GPSUtils instance = null;
    private LocationManager locationManager = null;
    public static final int LOCATION_CODE = 1000;
    public static final int OPEN_GPS_CODE = 1001;
    private GPSUtils() {}

    public static GPSUtils getInstance() {
        if (instance == null)
            instance = new GPSUtils();
        return instance;
    }

    public Pair<String, String> getCountryCity(Context context) {
        Log.i("GPS: ", "getProvince");
        if(locationManager == null)
            locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);        // 默认Android GPS定位实例

        Location location = null;
        // 是否已经授权
        if (context.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            //判断GPS是否开启，没有开启，则开启
//            if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
//                //跳转到手机打开GPS页面
//                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
//                //设置完成后返回原来的界面
//                AppActivity.instance.startActivityForResult(intent,OPEN_GPS_CODE);
//            }
//
//            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);      // GPS芯片定位 需要开启GPS
//            location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);      // 利用网络定位 需要开启GPS
            location = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);      // 其他应用使用定位更新了定位信息 需要开启GPS
        }

        Pair<String, String> p = null;
        if(location != null) {
            Log.i("GPS: ", "获取位置信息成功");
            Log.i("GPS: ","经度：" + location.getLatitude());
            Log.i("GPS: ","纬度：" + location.getLongitude());

            // 获取地址信息
            p = getAddress(context, location.getLatitude(), location.getLongitude());
            Log.i("GPS: ","location：" + p.toString());
        } else
            Log.e("GPS: ", "获取位置信息失败，请检查是够开启GPS,是否授权");
        return p;
    }

    /*
     * 根据经度纬度 获取国家，省份
     * */
    public Pair<String, String> getAddress(Context context, double latitude, double longitude) {
        Pair<String, String> location = null;
        List<Address> addList = null;
        Geocoder ge = new Geocoder(context);
        try {
            addList = ge.getFromLocation(latitude, longitude, 1);
        } catch (IOException e) {
            e.printStackTrace();
            return location;
        }
        for (Address ad : addList)
            location = new Pair<>(ad.getCountryName(), ad.getLocality());
        return location;
    }
}

