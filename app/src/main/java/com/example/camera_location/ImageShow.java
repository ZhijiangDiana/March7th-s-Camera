package com.example.camera_location;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.icu.text.SimpleDateFormat;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import utils.Kattio;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.concurrent.TimeUnit;



public class ImageShow extends AppCompatActivity {
    public static ImageShow imageShow;
    public ImageShow(){
        imageShow = ImageShow.this;
    }
    public static AlertDialog.Builder waiting;
    private String TAG = "ImageShow";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_show);

        // 显示拍照图片
        ImageView imgShow = findViewById(R.id.imgShow);
        imgShow.setImageBitmap(Variable.rImage);
        Button ret = findViewById(R.id.retToCam);

        // 从图片列表点进来
        if(Variable.enterFromList) {
            Variable.enterFromList = false;
            return;
        }

        // 从主界面进来，处理AI绘画请求
        if(Variable.isAIRepaint){
            // sd重绘处理线程
            try {
                new Thread(new SDRepaint()).start();
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e(TAG, "JSON解析失败！");
            }

            waiting = new AlertDialog.Builder(imageShow);
            waiting.setView(R.layout.waiting_for_repaint)
                    .setCancelable(false)
                    .create()
                    .show();
        }
    }

    public void retToCam(View view) {
//        startActivity(new Intent(imageShow, CameraDemoActivity.class));
        finish();
    }


    public class SDRepaint implements Runnable {
        OkHttpClient okpClient;
        String ip = "http://192.168.192.121";
        String port = ":7860";
        String img2imgAPI = "/sdapi/v1/img2img";
        JSONObject json;
        String path = CameraDemoActivity.cameraDemoActivity.getFilesDir().getPath().toString();

        public SDRepaint() throws JSONException {
            okpClient = new OkHttpClient.Builder()
                    .readTimeout(114, TimeUnit.SECONDS)
                    .writeTimeout(514, TimeUnit.SECONDS)
                    .connectTimeout(1919, TimeUnit.SECONDS)
                    .build();

            json = new JSONObject();
            json.put("init_images", null);
            json.put("denoising_strength", 0.58);
            json.put("prompt", "(((one girl))), (rich in details), masterpiece, ((masterpiece)), breasts, looking_at_viewer, " +
                    "(short hair), multicolored_eyes, ((pink hair)), ((pink hair)), ((pink hair)), bangs, blue_eyes, (long locks:1.4), " +
                    "(ribbon earrings), collarbone, white_shirt, long_sleeves, choker, flower, blue camera, blue_skirt, " +
                    "blue_flower, critical angle, masterpiece, best quality, high quality, absurdres, shiny skin, " +
                    "colorful, dynamic pose, stunning art, best quality, hyper detailed, reflective hair, good lighting, " +
                    "ray tracing, depth of field, ultra-detailed, illustration, Amazing, fine detail, extremely detailed, " +
                    "beautiful detailed glow, intricate detail, highres, an extremely delicate and beautiful girl, " +
                    "beautiful detailed eyes, realistic, hdr, rounded eyes, detailed facial features, " +
                    "<lora:(Anime Person) march7th:0.8>");
            json.put("sampler_name", "DPM++ 2M Karras");
            json.put("batch_size", 1);
            json.put("steps", 40);
            json.put("cfg_scale", 10);
            json.put("width", 512);
            json.put("height", 768);
            json.put("negative_prompt", "red hair, horns, sketch by bad-artist, bad_prompt_version2, bad-hands-5, " +
                    "(worst quality, low quality:1.4), lowres, ((bad anatomy)), ((bad hands)), text, missing finger, " +
                    "extra digits, fewer digits, blurry, ((mutated hands and fingers)), (poorly drawn face), ((mutation)), " +
                    "((deformed face)), (ugly), ((bad proportions)), ((extra limbs)), extra face, (double head), " +
                    "(extra head), ((extra feet)), monster, logo, cropped, worst quality, low quality, normal quality, " +
                    "jpeg, humpbacked, long body, long neck, ((jpeg artifacts)), ((more than one person))" +
                    "((signature, watermark, username, blurry, artist name)), worst quality, low quality, extra digits");
            json.put("send_images", true);
            json.put("save_images", true);
            json.put("alwayson_scripts", null);
            JSONObject override_settings_OBJ = new JSONObject();
            override_settings_OBJ.put("sd_model_checkpoint", "(Anime Person) 9527_v10");
            json.put("override_settings", override_settings_OBJ);
        }

        Kattio ko;
        @Override
        public void run() {
            String rImgB64 = Base64.encodeToString(Variable.bRImage, Base64.NO_WRAP);
            try {
                ko = new Kattio(System.in, new FileOutputStream(path + "/imgDebug"));
                ko.print(rImgB64);
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            } finally {
                ko.close();
            }

            try {
                JSONArray init_images_ARR = new JSONArray();
                init_images_ARR.put(rImgB64);
                json.put("init_images", init_images_ARR);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                ko = new Kattio(System.in, new FileOutputStream(path + "/jsonDebug"));
                ko.print(json);
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            } finally {
                ko.close();
            }

            Request req = new Request.Builder()
                    .url(ip + port + img2imgAPI)
                    .post(RequestBody.create(MediaType.parse("application/json;charset=utf-8"),
                            String.valueOf(json)))
                    .build();
            Call call = okpClient.newCall(req);
            Response resp;
            try {
                resp = call.execute();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            String respB = null;
            try {
                respB = resp.body().string();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            try {
                ko = new Kattio(System.in, new FileOutputStream(path + "/respDebug"));
                ko.print(respB);
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            } finally {
                ko.close();
            }

            String dImageB64 = null;
            try {
                JSONObject respJSON = new JSONObject(respB);
                JSONArray imgARR = respJSON.getJSONArray("images");
                dImageB64 = (String) imgARR.get(0);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }

            byte[] bDImage = Base64.decode(dImageB64, Base64.NO_WRAP);
            Variable.dImage = BitmapFactory.decodeByteArray(bDImage, 0, bDImage.length);

            startActivity(new Intent(ImageShow.imageShow, RepaintShow.class));
        }
    }
}