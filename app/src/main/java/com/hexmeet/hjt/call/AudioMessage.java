package com.hexmeet.hjt.call;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.hexmeet.hjt.R;

public class AudioMessage  extends LinearLayout {

    private ButtonOnClickListener listener;

    public interface ButtonOnClickListener {
        void onClickListener();
    }

    public AudioMessage(Context context) {
        super(context);
        initView(context);
    }

    private void initView(Context context) {
        View inflate = LayoutInflater.from(context).inflate(R.layout.audio_layout, this, true);
        Button audioBtn = inflate.findViewById(R.id.audio_mode_btn);
        audioBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onClickListener();
            }
        });
    }

    public void setListener(ButtonOnClickListener listener) {
        this.listener = listener;
    }

}
