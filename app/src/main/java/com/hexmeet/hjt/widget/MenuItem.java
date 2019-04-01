package com.hexmeet.hjt.widget;

import android.content.Context;
import android.graphics.drawable.Drawable;

public class MenuItem {
    public Drawable mDrawable;
    public CharSequence mText;
    public int mColor;

    public MenuItem(Drawable drawable, CharSequence text) {
        this.mDrawable = drawable;
        this.mText = text;
        this.mColor = -1;
    }

    public MenuItem(Context context, CharSequence text) {
        this.mText = text;
        this.mDrawable = null;
        this.mColor = -1;
    }

    public MenuItem(Context context, CharSequence text, int color, int k) {
        this.mText = text;
        this.mDrawable = null;
        this.mColor = color;
    }

    public MenuItem(Context context, int textId, int drawableId) {
        this.mText = context.getResources().getText(textId);
        this.mDrawable = context.getResources().getDrawable(drawableId);
        this.mColor = -1;
    }

    public MenuItem(Context context, CharSequence text, int drawableId) {
        this.mText = text;
        this.mDrawable = context.getResources().getDrawable(drawableId);
        this.mColor = -1;
    }
}
