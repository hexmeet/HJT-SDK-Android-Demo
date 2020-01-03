package com.hexmeet.hjt.chat;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.hexmeet.hjt.R;
import com.hexmeet.hjt.chat.adapter.ChatAdapter;
import com.jcodecraeer.xrecyclerview.XRecyclerView;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

public class ChatFrag extends Fragment {
    TextView mChatTitle;
    ImageView mCreateGroupChat;
    XRecyclerView mChatList;
    private View convertView;
    private List<String> mData;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        convertView = inflater.inflate(R.layout.chat, container, false);
        initView();
        initDate();
        return convertView;
    }

    private void initView() {
        mChatTitle = (TextView) convertView.findViewById(R.id.chat_title);
        mCreateGroupChat = (ImageView) convertView.findViewById(R.id.create_group_chat);
        mChatList = (XRecyclerView) convertView.findViewById(R.id.chat_list);
        //设置recyclerView 模式
        mChatList.setLayoutManager(new LinearLayoutManager(getActivity(),LinearLayoutManager.VERTICAL,false));
        //模拟数据
        mChatList.setAdapter(new ChatAdapter(createDataList(),getActivity()));

        //设置是否允许上拉刷新
        mChatList.setPullRefreshEnabled(false);
        //设置是否允许下拉加载
        mChatList.setLoadingMoreEnabled(false);

    }

    private void initDate() {
        //设置刷新的回调
        mChatList.setLoadingListener(new XRecyclerView.LoadingListener() {
            @Override
            public void onRefresh() {
                //刷新回调
            }

            @Override
            public void onLoadMore() {
                //下拉更新
            }
        });
        mCreateGroupChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(),ChatGroup.class);
                startActivity(intent);
            }
        });
    }

    protected List<String> createDataList() {
        mData = new ArrayList<>();
        for (int i=0;i<30;i++){
            mData.add("这是第"+i+"个View");
        }
        return mData;
    }
}
