package com.hexmeet.hjt.groupchat.adapter;

import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

public class EmojisPagerAdapter extends PagerAdapter {
    private List<EmojiconGridViewLayout> views;


    public EmojisPagerAdapter(List<EmojiconGridViewLayout> views) {
        super();
        this.views = views;
    }

    @Override
    public int getCount() {
        return views.size();
    }


    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View v = views.get(position).rootView;
        ((ViewPager) container).addView(v, 0);
        return v;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object view) {
        ((ViewPager) container).removeView((View) view);
    }

    @Override
    public boolean isViewFromObject(View view, Object key) {
        return key == view;
    }
}
