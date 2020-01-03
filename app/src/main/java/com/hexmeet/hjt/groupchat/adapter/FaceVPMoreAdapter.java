package com.hexmeet.hjt.groupchat.adapter;

import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import androidx.viewpager.widget.PagerAdapter;

public class FaceVPMoreAdapter extends PagerAdapter {
    // 界面列表
    private List<View> views;

    public FaceVPMoreAdapter(List<View> views) {
        this.views = views;
    }

    @Override
    public void destroyItem(ViewGroup arg0, int arg1, Object arg2) {
        (arg0).removeView((View) (arg2));
    }

    @Override
    public int getCount() {
        return views.size();
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        (container).addView(views.get(position));
        return views.get(position);
    }

    // 判断是否由对象生成界
    @Override
    public boolean isViewFromObject(View arg0, Object arg1) {
        return (arg0 == arg1);
    }
}