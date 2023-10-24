package com.example.camera_location;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

//用于将上下文、listview 子项布局的 id 和数据都传递过来
public class ImageAdapter extends ArrayAdapter<Bean> {
    public ImageAdapter(Context context, int resource, List<Bean> objects) {
        super(context, resource, objects);
    }

    //每个子项被滚动到屏幕内的时候会被调用
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Bean imgEntity = getItem(position);
        //为每一个子项加载设定的布局
        View view = LayoutInflater.from(getContext()).inflate(R.layout.img_list_entity, parent, false);
        //分别获取 image view 和 textview 的实例
        ImageView thumb = view.findViewById(R.id.imgPreview);
        TextView imgName = view.findViewById(R.id.imgName);
        // 设置要显示的图片和文字
        thumb.setImageBitmap(imgEntity.getPreviewImg());
        imgName.setText(imgEntity.getName());
        return view;
    }
}