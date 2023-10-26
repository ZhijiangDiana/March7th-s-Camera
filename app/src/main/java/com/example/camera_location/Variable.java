package com.example.camera_location;

import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.location.Location;
import utils.Pair;

public class Variable {
    private Variable(){}
    public static byte[] bRImage;
    public static String rImageName;

    public static Bitmap rImage;
    public static Bitmap dImage;
    public static boolean isAIRepaint;
    public static boolean enterFromList;
    public static SQLiteOpenHelper helper;
    public static boolean hasInitialized = false;
    public static Location location;
    public static Pair<String, String> address;
    public static String SDIP = "1.14.5.14";
    public static String SDPort = "1919";
}
