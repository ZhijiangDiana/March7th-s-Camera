package com.example.camera_location;

import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;

import java.util.List;

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
}
