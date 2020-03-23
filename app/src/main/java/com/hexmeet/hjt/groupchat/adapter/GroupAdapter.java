package com.hexmeet.hjt.groupchat.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.hexmeet.hjt.HjtApp;
import com.hexmeet.hjt.R;
import com.hexmeet.hjt.cache.SystemCache;
import com.hexmeet.hjt.chat.ImMsgBean;
import com.hexmeet.hjt.groupchat.utils.TimeUtil;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class GroupAdapter extends RecyclerView.Adapter{
    private Logger LOG = Logger.getLogger(this.getClass());
    public static final int GROUPVIEW_TYPE_LEFT_TEXT = 0;
    public static final int GROUPVIEW_TYPE_RIGHT_TEXT = 1;
    private Context mContext;

    private List<ImMsgBean> mData;

    public GroupAdapter(Context context) {
        this.mContext = context;
    }

    public void addData(ImMsgBean bean, boolean isNotifyDataSetChanged) {
        if (bean == null) {
            return;
        }
        if (mData == null) {
            mData = new ArrayList<>();
        }
        mData.add(bean);

        if (isNotifyDataSetChanged) {
            notifyDataSetChanged();
        }
    }

    public void clearList(){
        mData.clear();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = null;
        RecyclerView.ViewHolder holder = null;
        switch (viewType) {
            case GROUPVIEW_TYPE_LEFT_TEXT:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.listitem_cha_left_text, parent, false);
                holder = new LeftViewHolder(view);
                break;
            case GROUPVIEW_TYPE_RIGHT_TEXT:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.listitem_cha_right_text, parent, false);
                holder = new RightViewHolder(view);
                break;
        }
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ImMsgBean imMsgBean = mData.get(position);
        int itemViewType = getItemViewType(position);
        switch (itemViewType) {
            case GROUPVIEW_TYPE_LEFT_TEXT:
                fromMsgLeftLayout((LeftViewHolder) holder, imMsgBean, position);
                break;
            case GROUPVIEW_TYPE_RIGHT_TEXT:
                fromImgRightLayout((RightViewHolder) holder, imMsgBean, position);
                break;
        }
    }

    private void fromImgRightLayout(RightViewHolder holder, ImMsgBean imMsgBean, int position) {
        holder.mChatRightUsername.setText(HjtApp.getInstance().getAppService().getDisplayName());
      //  holder.mRightContext.setText(imMsgBean.getContent());
        holder.mRightContext.setText(imMsgBean.getContent());
        if(imMsgBean.getImageUrl()!=null){
            Glide.with(holder.mRightImg.getContext()).load(imMsgBean.getImageUrl()).apply(RequestOptions.bitmapTransform(new CircleCrop()))
                    .into(holder.mRightImg);
        }else {
            Glide.with(holder.mRightImg.getContext()).load(R.drawable.default_photo).apply(RequestOptions.bitmapTransform(new CircleCrop()))
                    .into(holder.mRightImg);
        }

        if (position != 0) {
            String showTime = TimeUtil.getTime(imMsgBean.getTime(), mData.get(position - 1)
                    .getTime());
            LOG.info(" showTime : "+showTime+",position -1 : "+mData.get(position - 1)
                    .getTime());
            if (showTime != null) {
                String timeShowString = TimeUtil.getTimeShowString(imMsgBean.getTime(), false);
                holder.mChatRightTime.setText(timeShowString);
                holder.mChatRightTime.setVisibility(View.VISIBLE);
            }else {
                holder.mChatRightTime.setVisibility(View.GONE);
            }
        }else {
            String timeShowString = TimeUtil.getTimeShowString(imMsgBean.getTime(), false);
            holder.mChatRightTime.setText(timeShowString);
            holder.mChatRightTime.setVisibility(View.VISIBLE);
        }


    }

    private void fromMsgLeftLayout(LeftViewHolder holder, ImMsgBean imMsgBean, int position) {
        holder.mLeftUsername.setText(imMsgBean.getName());
       // holder.mLeftContext.setText(imMsgBean.getContent());
        holder.mLeftContext.setText(imMsgBean.getContent());
        if(imMsgBean.getImageUrl()!=null){
            Glide.with(holder.mLeftImg.getContext()).load(imMsgBean.getImageUrl()).apply(RequestOptions.bitmapTransform(new CircleCrop()))
                    .into(holder.mLeftImg);
        }else {
            Glide.with(holder.mLeftImg.getContext()).load(R.drawable.default_photo).apply(RequestOptions.bitmapTransform(new CircleCrop()))
                    .into(holder.mLeftImg);
        }

        if (position != 0) {
            String showTime = TimeUtil.getTime(imMsgBean.getTime(), mData.get(position - 1)
                    .getTime());
            LOG.info(" showTime : "+showTime);
            if (showTime != null) {
                String timeShowString = TimeUtil.getTimeShowString(imMsgBean.getTime(), false);
                holder.mLeftTime.setText(timeShowString);
                holder.mLeftTime.setVisibility(View.VISIBLE);
            }else {
                holder.mLeftTime.setVisibility(View.GONE);
            }
        }else {
            String timeShowString = TimeUtil.getTimeShowString(imMsgBean.getTime(), false);
            holder.mLeftTime.setText(timeShowString);
            holder.mLeftTime.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return mData.get(position).getMsgType();
    }

    @Override
    public int getItemCount() {
        return mData == null ? 0 : mData.size();
    }



    class LeftViewHolder extends RecyclerView.ViewHolder {
        private TextView mLeftTime;
        private LinearLayout mLayoutLeft;
        private ImageView mLeftImg;
        private TextView mLeftUsername;
        private TextView mLeftContext;
        public LeftViewHolder(@NonNull View itemView) {
            super(itemView);


            mLeftTime = (TextView) itemView.findViewById(R.id.left_time);
            mLayoutLeft = (LinearLayout) itemView.findViewById(R.id.layout_left);
            mLeftImg = (ImageView) itemView.findViewById(R.id.left_img);
            mLeftUsername = (TextView) itemView.findViewById(R.id.left_username);
            mLeftContext = (TextView) itemView.findViewById(R.id.left_context);

        }
    }

    class RightViewHolder extends RecyclerView.ViewHolder {
        private TextView mChatRightTime;
        private ImageView mRightImg;
        private LinearLayout mLayoutContent;
        private TextView mChatRightUsername;
        private TextView mRightContext;
        public RightViewHolder(@NonNull View itemView) {
            super(itemView);
            mChatRightTime = (TextView)  itemView.findViewById(R.id.chat_right_time);
            mRightImg = (ImageView)  itemView.findViewById(R.id.right_img);
            mLayoutContent = (LinearLayout)  itemView.findViewById(R.id.layout_content);
            mChatRightUsername = (TextView)  itemView.findViewById(R.id.chat_right_username);
            mRightContext = (TextView) itemView. findViewById(R.id.right_context);

        }
    }

}
