package com.example.camera_location;

import android.content.Intent;
import android.database.AbstractWindowedCursor;
import android.database.Cursor;
import android.database.CursorWindow;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import java.sql.Blob;
import java.util.ArrayList;
import java.util.List;

public class ImageList extends AppCompatActivity {
    public static ImageList imageList;
    public ImageList(){
        imageList = ImageList.this;
    }
    List<Bean> allImg;

    //第一步：定义对象
    ListView listView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_list);

        //第二步：绑定控件
        listView = findViewById(R.id.list_view);
        //第三步：准备数据
        allImg = new ArrayList<>(114514);
        SQLiteDatabase rdb = Variable.helper.getReadableDatabase();
        Cursor cursor = rdb.rawQuery("SELECT * FROM ImageDB", null);
        // Cursor窗口很大，你要忍一下
        CursorWindow cw = new CursorWindow("test", 16777216);
        AbstractWindowedCursor ac = (AbstractWindowedCursor) cursor;
        ac.setWindow(cw);
        while(cursor.moveToNext()){
            int id = cursor.getInt(cursor.getColumnIndexOrThrow("_id"));
            String imgName = cursor.getString(cursor.getColumnIndexOrThrow("name"));
            byte[] data = cursor.getBlob(cursor.getColumnIndexOrThrow("preView"));
            Bitmap preview = BitmapFactory.decodeByteArray(data, 0, data.length, new BitmapFactory.Options());
            allImg.add(new Bean(id, imgName, preview));
        }
        cursor.close();
        rdb.close();
        //第四步：设计每一个列表项的子布局，已设计
        //第五步：定义适配器 控件 -桥梁-数据
        ImageAdapter adapter=new ImageAdapter(imageList, R.layout.img_list_entity, allImg);
        listView.setAdapter(adapter);
        //第六部：设置事件监听
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Variable.enterFromList = true;
                Bean selImgEntity = allImg.get(position);
                int sel_id = selImgEntity.getId();
                SQLiteDatabase sel_rdb = Variable.helper.getReadableDatabase();
                Cursor sel_itm_cur = sel_rdb.rawQuery("SELECT * FROM ImageDB WHERE _id == " + sel_id, null);
                // Cursor窗口很大，你要忍一下
                CursorWindow cw = new CursorWindow("test", 16777216);
                AbstractWindowedCursor ac = (AbstractWindowedCursor) sel_itm_cur;
                ac.setWindow(cw);
                sel_itm_cur.moveToNext();
                byte[] sel_img_data = sel_itm_cur.getBlob(sel_itm_cur.getColumnIndexOrThrow("img"));
                sel_itm_cur.close();
                sel_rdb.close();
                Bitmap sel_img = BitmapFactory.decodeByteArray(
                        sel_img_data, 0, sel_img_data.length, new BitmapFactory.Options());
                Variable.rImage = sel_img;

                startActivity(new Intent(imageList, ImageShow.class));
            }
        });
    }

    public void retToCam(View view) {
//        startActivity(new Intent(imageList, CameraDemoActivity.class));
        finish();
    }
}