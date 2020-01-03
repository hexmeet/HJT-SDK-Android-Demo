package com.hexmeet.hjt.groupchat.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.hexmeet.hjt.R;
import com.hexmeet.hjt.event.ChatMore;

import java.util.List;

public class FaceMoreAdapter extends BaseAdapter {
    private List<ChatMore> list;
    private Context mContext;

    public FaceMoreAdapter(List<ChatMore> list, Context mContext) {
        super();
        this.list = list;
        this.mContext = mContext;
    }
    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        FaceMoreAdapter.ViewHolder holder;
        if (convertView == null) {
            holder = new FaceMoreAdapter.ViewHolder();
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_more, null);
            holder.iv = (ImageView) convertView.findViewById(R.id.iv_icon);
            holder.name = (TextView) convertView.findViewById(R.id.tv_name);
            convertView.setTag(holder);
        } else {
            holder = (FaceMoreAdapter.ViewHolder) convertView.getTag();
        }
        holder.iv.setImageResource(list.get(position).getImg());

         holder.name.setText(list.get(position).getName());
        return convertView;
    }

    class ViewHolder {
        ImageView iv;
        TextView name;
    }
}
