package com.hexmeet.hjt.chat;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.Toast;

import com.hexmeet.hjt.BaseActivity;
import com.hexmeet.hjt.R;
import com.hexmeet.hjt.chat.adapter.GridViewAdapter;

import java.util.ArrayList;
import java.util.List;

public class ChatDetailActivity extends BaseActivity {
    private ImageView mDetailBackBtn;
    private ExpandableGridView mGvAddUser;
    private Switch mMessageDisturbanceFreeSwitch;
    private RelativeLayout mSettingChatBg,mEmptyChat,group_mg;
    private List<String> mData;
    private GridViewAdapter gridViewAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_detail);
        initView();

        initData();
    }

    private void initView() {
        mDetailBackBtn = (ImageView) findViewById(R.id.detail_back_btn);
        mGvAddUser = (ExpandableGridView) findViewById(R.id.gv_add_user);
        mMessageDisturbanceFreeSwitch = (Switch) findViewById(R.id.message_disturbance_free_switch);
        mSettingChatBg = (RelativeLayout) findViewById(R.id.setting_chat_bg);
        mEmptyChat = (RelativeLayout) findViewById(R.id.empty_chat);
        group_mg = (RelativeLayout) findViewById(R.id.group_mg);
    }

    protected List<String> dataList() {
        mData = new ArrayList<>();
        for (int i=0;i<15;i++){
            mData.add("我是"+i);
        }
        return mData;
    }

    private void initData() {
        gridViewAdapter = new GridViewAdapter(this,  dataList());
        mGvAddUser.setAdapter(gridViewAdapter);
        mGvAddUser.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //点击添加图片
                if(position==parent.getChildCount()-1){
                    Toast.makeText(ChatDetailActivity.this, "您点击了添加", Toast.LENGTH_SHORT).show();

                }
            }
        });

        group_mg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ChatDetailActivity.this,NewGroupOwners.class);
                startActivity(intent);
            }
        });
    }
}
