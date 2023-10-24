package com.example.camera_location;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.AbstractWindowedCursor;
import android.database.Cursor;
import android.database.CursorWindow;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.*;
import android.hardware.camera2.*;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.icu.text.SimpleDateFormat;
import android.media.Image;
import android.media.ImageReader;
import android.net.Uri;
import android.os.*;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.util.Size;
import android.util.SparseArray;
import android.view.*;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.mysql.jdbc.PreparedStatement;
import utils.ViewUtils;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.*;

public class CameraDemoActivity extends AppCompatActivity {
    public CameraDemoActivity(){
        cameraDemoActivity = CameraDemoActivity.this;
    }
    public static CameraDemoActivity cameraDemoActivity;
    private TextureView textureView;
    private HandlerThread handlerThread;
    private Handler mCameraHandler;
    private CameraManager cameraManager;
    //最佳的预览尺寸
    private Size previewSize;
    //最佳的拍照尺寸
    private Size mCaptureSize;
    private String mCameraId;

    private CameraDevice cameraDevice;

    private CaptureRequest.Builder captureRequestBuilder;

    private CaptureRequest captureRequest;

    private CameraCaptureSession mCameraCaptureSession;

    private Button btn_photo;

    private ImageReader imageReader;

    private static final SparseArray<Integer> ORIENTATION = new SparseArray<>();

    CheckBox isAIRepaint;

    static {
        ORIENTATION.append(Surface.ROTATION_0, 90);
        ORIENTATION.append(Surface.ROTATION_90, 0);
        ORIENTATION.append(Surface.ROTATION_180, 270);
        ORIENTATION.append(Surface.ROTATION_270, 180);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_demo);
        if(!Variable.hasInitialized) {
            Variable.helper = ImgSQLiteOpenHelper.getInstance(cameraDemoActivity);
            ListInit();
            Variable.hasInitialized = true;
        }

        textureView = findViewById(R.id.textureView);
        btn_photo = findViewById(R.id.btn_photo);
        btn_photo.setOnClickListener(OnClick);
        isAIRepaint = findViewById(R.id.isAIRepaint);
        isAIRepaint.setChecked(Variable.isAIRepaint);
    }

    private final View.OnClickListener OnClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // 更新checked状态
            Variable.isAIRepaint = isAIRepaint.isChecked();

            //获取摄像头的请求
            try {
                CaptureRequest.Builder cameraDeviceCaptureRequest = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
                cameraDeviceCaptureRequest.addTarget(imageReader.getSurface());
                //获取摄像头的方向
                int rotation = getWindowManager().getDefaultDisplay().getRotation();
                CameraCaptureSession.CaptureCallback mCaptureCallback = new CameraCaptureSession.CaptureCallback() {
                    @Override
                    public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
                        super.onCaptureCompleted(session, request, result);
                        Toast.makeText(CameraDemoActivity.this, "拍照结束！", Toast.LENGTH_SHORT).show();
                        unLockFocus();
                    }
                };
                //设置拍照方向
                cameraDeviceCaptureRequest.set(CaptureRequest.JPEG_ORIENTATION, (Integer) ORIENTATION.get(rotation));
                mCameraCaptureSession.stopRepeating();
                mCameraCaptureSession.capture(cameraDeviceCaptureRequest.build(), mCaptureCallback, mCameraHandler);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
            //获取图像的缓冲区
            //获取文件的存储权限及操作
        }
    };

    private void unLockFocus() {
        captureRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_CANCEL);
        try {
            mCameraCaptureSession.setRepeatingRequest(captureRequestBuilder.build(), null, mCameraHandler);

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        startCameraThread();
        if (!textureView.isAvailable()) {
            textureView.setSurfaceTextureListener(mTextureListener);
        } else {
            startPreview();
        }
    }

    TextureView.SurfaceTextureListener mTextureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surface, int width, int height) {
            //SurfaceTexture组件可用的时候,设置相机参数，并打开摄像头
            //设置摄像头参数
            setUpCamera(width, height);
            //打开摄像头
            openCamera();
        }

        @Override
        public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surface, int width, int height) {
            //尺寸发生变化的时候
        }

        @Override
        public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surface) {
            //组件被销毁的时候
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surface) {
            //组件更新的时候
        }
    };

    private void setUpCamera(int width, int height) {
        cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        //拿到摄像头的id
        try {
            for (String cameraId : cameraManager.getCameraIdList()) {
                //得到摄像头的参数
                CameraCharacteristics cameraCharacteristics = cameraManager.getCameraCharacteristics(cameraId);
                Integer facing = cameraCharacteristics.get(CameraCharacteristics.LENS_FACING);
                if (facing != null && facing == CameraCharacteristics.LENS_FACING_FRONT) {
                    continue;
                }
                StreamConfigurationMap map = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                if (map != null) { //找到摄像头能够输出的，最符合我们当前屏幕能显示的最小分辨率
//                    previewSize = getOptimalSize(map.getOutputSizes(SurfaceTexture.class), width, height);
                    Size[] allOutputSizes = map.getOutputSizes(ImageFormat.JPEG);
                    mCaptureSize = Collections.max(Arrays.asList(allOutputSizes), new Comparator<Size>() {
                        @Override
                        public int compare(Size o1, Size o2) {
                            return Long.signum((long) o1.getWidth() * o1.getHeight() - (long) o2.getWidth() * o2.getHeight());
                        }
                    });
                    previewSize = getBestSize(mCaptureSize);
                }
                //建立ImageReader准备存储照片
                setUpImageReader();
                mCameraId = cameraId;
                break;
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void setUpImageReader() {
        imageReader = ImageReader.newInstance(mCaptureSize.getWidth(), mCaptureSize.getHeight(), ImageFormat.JPEG, 2);
        imageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
            @Override
            public void onImageAvailable(ImageReader reader) {
                mCameraHandler.post(new ImageSaver(reader.acquireNextImage()));
            }

        }, mCameraHandler);
    }

    public void getList(View view) {
        startActivity(new Intent(cameraDemoActivity, ImageList.class));
    }

    //存储图片的过程
    private class ImageSaver implements Runnable {
        private Image image;

        public ImageSaver(Image image) {
            this.image = image;
        }

        @Override
        public void run() {
            ByteBuffer byteBuffer = image.getPlanes()[0].getBuffer();
            byte[] data = new byte[byteBuffer.remaining()];
            byteBuffer.get(data);
//            String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + "/CameraV2";
//            Log.e("CameraDemo", path);
//            File file = new File(path);
//            //判断当前的文件目录是否存在，如果不存在就创建这个文件目录
//            if (!file.exists()) {
//                file.mkdir();
//            }
            String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            String fileName = "IMG_" + timeStamp + ".jpg";

            //获取要保存的图片的位图
            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length, null);
            Variable.bRImage = data;
            Variable.rImage = bitmap;
            Variable.rImageName = fileName;
            startActivity(new Intent(CameraDemoActivity.this, ImageShow.class));

            //MediaStore 相当于管理媒体资源的一个管理器，类似于一个数据库，对媒体资源的一个索引(包括图片 音频 视频)，在里面都有索引
            if (MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, fileName, "") == null) {
                Toast.makeText(CameraDemoActivity.this, "保存失败！", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(CameraDemoActivity.this, "保存成功！", Toast.LENGTH_SHORT).show();
            }

            // 生成图片的缩略图
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            float scaleWidth = ((float) ViewUtils.dp2px(cameraDemoActivity, 90)) / width;
            float scaleHeight = ((float) ViewUtils.dp2px(cameraDemoActivity, 120)) / height;
            Matrix matrix = new Matrix();
            matrix.postScale(scaleWidth, scaleHeight);
            Bitmap thumbImg = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix,true);
            // 生成缩略图byte[]
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            thumbImg.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] thumbData = baos.toByteArray();
            // 将图片写入数据库
            SQLiteDatabase wdb = Variable.helper.getWritableDatabase();
            ContentValues cv = new ContentValues();
            cv.put("name", fileName);
            cv.put("img", data);
            cv.put("preview", thumbData);
//            wdb.execSQL("INSERT INTO ImageDB(name, img, preview) VALUES ('"+Variable.rImageName+"', '"+data+"', '"+thumbData+"')");
            wdb.insert("ImageDB", null, cv);
            wdb.close();

            // 写入List缓存
            ImageList.allImg.add(new Bean(ImageList.allImg.size()+1, fileName, thumbImg));

            //关闭image
            image.close();
        }
    }

    //得到最佳的预览尺寸
    private Size getBestSize(Size cameraSize){
        WindowManager windowManager = getWindow().getWindowManager();
        Point point = new Point();
        windowManager.getDefaultDisplay().getRealSize(point);
        // 仅使用竖屏
        Size screenSize = new Size(Math.min(point.x, point.y), Math.max(point.x, point.y));
        Size camSizePor = new Size(Math.min(cameraSize.getHeight(), cameraSize.getWidth()),
                Math.max(cameraSize.getHeight(), cameraSize.getWidth()));
        Size bestSize = null;
        bestSize = new Size(screenSize.getWidth(), camSizePor.getHeight()*screenSize.getWidth()/camSizePor.getWidth());


        return bestSize;
    }
//    private Size getOptimalSize(Size[] outputSizes, int width, int height) {
//        ArrayList<Size> arrayList = new ArrayList<>();
//        for (Size option : outputSizes) {
//            if (width > height) { //横屏
//                if (option.getWidth() > width && option.getHeight() > height) {
//                    arrayList.add(option);
//                }
//            } else { //竖屏
//                if (option.getWidth() > height && option.getHeight() > width) {
//                    arrayList.add(option);
//                }
//            }
//        }
//        if (arrayList.size() > 1) {
//            return Collections.min(arrayList, new Comparator<Size>() {
//                @Override
//                public int compare(Size o1, Size o2) {
//                    return Long.signum((long) o1.getWidth() * o1.getHeight() - (long) o2.getWidth() * o2.getHeight());
//                }
//            });
//
//        }
//        return outputSizes[0];
//    }

    private void openCamera() {
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this,
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.CAMERA,
                            android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    1);
            return;
        }
        try {
            cameraManager.openCamera(mCameraId, mStateCallback, mCameraHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) { //摄像头打开
            cameraDevice = camera;
            startPreview();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) { //摄像头关闭
            cameraDevice.close();
            cameraDevice = null;
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {//摄像头出现错误
            cameraDevice.close();
            cameraDevice = null;
        }
    };

    //开始预览
    private void startPreview() {
        //建立图像缓冲区
        SurfaceTexture surfaceTexture = textureView.getSurfaceTexture();
        surfaceTexture.setDefaultBufferSize(previewSize.getWidth(), previewSize.getHeight());

        //得到界面的显示对象
        Surface surface = new Surface(surfaceTexture);
        try {
            captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            captureRequestBuilder.addTarget(surface);
            //建立通道(CaptureRequest和CaptureSession会话)
            cameraDevice.createCaptureSession(Arrays.asList(surface, imageReader.getSurface()), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    captureRequest = captureRequestBuilder.build();
                    mCameraCaptureSession = session;
                    try {
                        mCameraCaptureSession.setRepeatingRequest(captureRequest, null, mCameraHandler);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {

                }
            }, mCameraHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    //开启摄像头线程
    private void startCameraThread() {
        handlerThread = new HandlerThread("myHandlerThread");
        handlerThread.start();
        mCameraHandler = new Handler(handlerThread.getLooper());
    }

    private void ListInit() {
        ImageList.allImg = new ArrayList<>(114514);
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
            ImageList.allImg.add(new Bean(id, imgName, preview));
        }
        cursor.close();
        rdb.close();
    }
}

