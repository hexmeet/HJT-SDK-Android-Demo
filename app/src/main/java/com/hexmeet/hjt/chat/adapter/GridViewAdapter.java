package com.hexmeet.hjt.chat.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.hexmeet.hjt.R;

import java.util.List;

public class GridViewAdapter  extends BaseAdapter {
    private Context context;
    private List<String> data;
    LayoutInflater layoutInflater;

    public GridViewAdapter(Context context, List<String> data) {
        this.context = context;
        this.data = data;
        layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return data.size()+1;//注意此处
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @SuppressLint("ViewHolder")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolderText holderText;
        if(convertView==null){
           holderText = new ViewHolderText();
           convertView = layoutInflater.inflate(R.layout.grid_item, null);
           holderText.iv_avatar= convertView.findViewById(R.id.detail_grid_img);
           holderText.tv_content = convertView.findViewById(R.id.detail_grid_name);
           convertView.setTag(holderText);
        }else {
            holderText = (ViewHolderText)convertView.getTag();
        }

        if (position < data.size()) {
            Glide.with(holderText.iv_avatar.getContext()).load(R.drawable.girl).apply(RequestOptions.bitmapTransform(new CircleCrop()))
                    .into(holderText.iv_avatar);
            holderText.tv_content.setText(data.get(position));
        }else{
            holderText.iv_avatar.setImageResource(R.drawable.user_add);//最后一个显示加号图片
        }




        return convertView;
    }


    public  class ViewHolderText {
        public ImageView iv_avatar;
        public TextView tv_content;
    }
}
