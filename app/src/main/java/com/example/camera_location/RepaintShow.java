package com.example.camera_location;

import android.content.Intent;
import android.icu.text.SimpleDateFormat;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import java.util.Date;

public class RepaintShow extends AppCompatActivity {
    public static RepaintShow repaintShow;
    public RepaintShow(){
        repaintShow = RepaintShow.this;
    }

    ImageView imgShow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_repaint_show);

        imgShow = findViewById(R.id.repaintShow);
        imgShow.setImageBitmap(Variable.dImage);



    }

    public void retToCam(View view) {
        startActivity(new Intent(repaintShow, CameraDemoActivity.class));
    }

    boolean isR = false;
    public void rdSwitch(View view) {
        isR = !isR;
        if(isR)
            imgShow.setImageBitmap(Variable.rImage);
        else
            imgShow.setImageBitmap(Variable.dImage);
    }

    public void saveImg(View view) {
        String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        String fileName = "IMG_Repaint_" + timeStamp + ".jpg";

        //MediaStore 相当于管理媒体资源的一个管理器，类似于一个数据库，对媒体资源的一个索引(包括图片 音频 视频)，在里面都有索引
        if (MediaStore.Images.Media.insertImage(getContentResolver(), Variable.dImage, fileName, "") == null) {
            Toast.makeText(RepaintShow.repaintShow, "保存失败！", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(RepaintShow.repaintShow, "保存成功！", Toast.LENGTH_SHORT).show();
        }
    }

    public void rerepaint(View view) {
        startActivity(new Intent(repaintShow, ImageShow.class));
    }
}