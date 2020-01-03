package com.hexmeet.hjt.chat.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.hexmeet.hjt.R;
import com.hexmeet.hjt.chat.ChatContentActivity;
import com.hexmeet.hjt.groupchat.GroupChatActivity;

import java.util.List;

import androidx.recyclerview.widget.RecyclerView;

public class ChatAdapter extends  RecyclerView.Adapter{

    private List<String> mData;
    private Context context;

    public ChatAdapter(List<String> data,Context context) {
        this.mData = data;
        this.context=context;
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View inflate = LayoutInflater.from(context).inflate(R.layout.chat_adapter, null);
        return new InitViewHolder(inflate);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        final InitViewHolder holder =(InitViewHolder) viewHolder;
        holder.mUserName.setText(mData.get(position));
        Glide.with(context).load(R.drawable.girl).apply(RequestOptions.bitmapTransform(new CircleCrop()))
                .into(holder.mChatAvatar);


        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context,holder.mUserName.getText(),Toast.LENGTH_LONG).show();
                Intent intent = new Intent(context, GroupChatActivity.class);
                intent.putExtra("username",holder.mUserName.getText());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mData !=null ? mData.size() : 0;
    }


    static  class InitViewHolder extends  RecyclerView.ViewHolder{
        private ImageView mChatAvatar;
        private TextView mUserName;
        private TextView mUserContext;
        private TextView mUserTime;
        private TextView mMsgNumber;
        private ImageView mNotDisturb;
        private TextView mNotDisturbMsgRemind;

        public InitViewHolder(View itemView) {
            super(itemView);
            mChatAvatar = (ImageView) itemView.findViewById(R.id.chat_avatar);
            mUserName = (TextView) itemView.findViewById(R.id.user_name);
            mUserContext = (TextView) itemView.findViewById(R.id.user_context);
            mUserTime = (TextView) itemView.findViewById(R.id.user_time);
            mMsgNumber = (TextView) itemView.findViewById(R.id.msg_number);
            mNotDisturb = (ImageView) itemView.findViewById(R.id.not_disturb);
            mNotDisturbMsgRemind = (TextView) itemView.findViewById(R.id.not_disturb_msg_remind);

        }
    }
}
