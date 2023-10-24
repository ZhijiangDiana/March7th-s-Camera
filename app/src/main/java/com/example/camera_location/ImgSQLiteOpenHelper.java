package com.example.camera_location;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import androidx.annotation.Nullable;

public class ImgSQLiteOpenHelper extends SQLiteOpenHelper {
    private static SQLiteOpenHelper Instance;
    public static synchronized SQLiteOpenHelper getInstance(Context context){
        if(Instance == null)
            Instance = new ImgSQLiteOpenHelper(context, "ImgDB.db", null, 1);
        return Instance;
    }

    private ImgSQLiteOpenHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE ImageDB(_id integer primary key autoincrement not null, " +
                "name text not null, " +
                "img blob not null, " +
                "preView blob not null)");

//            db.execSQL("INSERT INTO AddExercise(firstNum, secondNum, ans) VALUES ('"+firstNum+"' ,'"+secondNum+"','"+ans+"')");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
