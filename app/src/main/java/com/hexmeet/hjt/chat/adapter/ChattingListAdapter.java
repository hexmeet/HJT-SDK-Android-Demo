package com.hexmeet.hjt.chat.adapter;

import android.app.Activity;
import android.util.Log;
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
import com.hexmeet.hjt.chat.ImMsgBean;
import com.hexmeet.hjt.chat.SimpleCommonUtils;

import java.util.ArrayList;
import java.util.List;

public class ChattingListAdapter extends BaseAdapter {

    private final int VIEW_TYPE_COUNT = 2;
    public static final int VIEW_TYPE_LEFT_TEXT = 0;
  //  public final int VIEW_TYPE_LEFT_IMAGE = 4;

    public static final int VIEW_TYPE_RIGHT_TEXT = 1;
   // public final int VIEW_TYPE_RIGHT_IMAGE = 3;

    private Activity mActivity;
    private LayoutInflater mInflater;
    private List<ImMsgBean> mData;

    public ChattingListAdapter(Activity activity) {
        this.mActivity = activity;
        mInflater = LayoutInflater.from(activity);
    }

    public void addData(List<ImMsgBean> data) {
        if (data == null || data.size() == 0) {
            return;
        }
        if (mData == null) {
            mData = new ArrayList<>();
        }
        for (ImMsgBean bean : data) {
            addData(bean, false, false);
        }
        this.notifyDataSetChanged();
    }

    public void addData(ImMsgBean bean, boolean isNotifyDataSetChanged, boolean isFromHead) {
        if (bean == null) {
            return;
        }
        if (mData == null) {
            mData = new ArrayList<>();
        }

        if (bean.getMsgType() <= 0) {
            String content = bean.getContent();
            if (content != null) {
                if (content.indexOf("[img]") >= 0) {
                    //bean.setImage(content.replace("[img]", ""));
                   // bean.setMsgType(ImMsgBean.CHAT_MSGTYPE_IMG);
                } else {
                    //bean.setMsgType(ImMsgBean.CHAT_MSGTYPE_TEXT);
                }
            }
        }

        if (isFromHead) {
            mData.add(0, bean);
        } else {
            mData.add(bean);
        }

        if (isNotifyDataSetChanged) {
            this.notifyDataSetChanged();
        }
    }

    @Override
    public int getCount() {
        return mData == null ? 0 : mData.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public int getItemViewType(int position) {
        if (mData.get(position) == null) {
            return -1;
        }
        return mData.get(position).getMsgType();
    }

    @Override
    public int getViewTypeCount() {
        return VIEW_TYPE_COUNT;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ImMsgBean bean = mData.get(position);
        int type = getItemViewType(position);
        View holderView = null;
        switch (type) {
            case VIEW_TYPE_LEFT_TEXT:
                ViewHolderLeftText holder ;
                if (convertView == null) {
                    holder = new ViewHolderLeftText();
                    holderView = mInflater.inflate(R.layout.listitem_cha_left_text, null);
                    holderView.setFocusable(true);
                    holder.iv_avatar = (ImageView) holderView.findViewById(R.id.left_img);
                    holder.tv_content = (TextView) holderView.findViewById(R.id.left_context);
                    holderView.setTag(holder);
                    convertView = holderView;
                } else {
                    holder = (ViewHolderLeftText) convertView.getTag();
                }
                disPlayLeftTextView(position, convertView, holder, bean);
                break;
            case VIEW_TYPE_RIGHT_TEXT:
                ViewHolderRightText holderR;
                if (convertView == null) {
                    holderR = new ViewHolderRightText();
                    holderView = mInflater.inflate(R.layout.listitem_cha_right_text, null);
                    holderView.setFocusable(true);
                    holderR.iv_right_avatar = (ImageView) holderView.findViewById(R.id.right_img);
                    holderR.tv_right_content = (TextView) holderView.findViewById(R.id.right_context);
                    holderView.setTag(holderR);
                    convertView = holderView;
                } else {
                    holderR = (ViewHolderRightText) convertView.getTag();
                }
                disPlayRightTextView(position, convertView, holderR, bean);
               // disPlayLeftTextView(position, convertView, holder, bean);
                break;
            default:
                convertView = new View(mActivity);
                break;
        }
        return convertView;
    }

    public void disPlayLeftTextView(int position, View view, ViewHolderLeftText holder, ImMsgBean bean) {
        setContent(holder.tv_content, bean.getContent());
        holder.tv_content.setText(bean.getContent());
        Glide.with(holder.iv_avatar.getContext()).load(R.drawable.girl).apply(RequestOptions.bitmapTransform(new CircleCrop()))
                .into(holder.iv_avatar);
    }

    public void disPlayRightTextView(int position, View view, ViewHolderRightText holder, ImMsgBean bean) {
        Log.i("chattinglistadapter", "disPlayRightTextView: "+bean.getContent());
       setContent(holder.tv_right_content, bean.getContent());
        holder.tv_right_content.setText(bean.getContent());
       Glide.with(holder.iv_right_avatar.getContext()).load(R.drawable.girl).apply(RequestOptions.bitmapTransform(new CircleCrop()))
                .into(holder.iv_right_avatar);
    }

    public void setContent(TextView tv_content, String content) {
        SimpleCommonUtils.spannableEmoticonFilter(tv_content, content);
    }


    public final class ViewHolderLeftText {
        public ImageView iv_avatar;
        public TextView tv_content;
    }

    public final class ViewHolderRightText {
        public ImageView iv_right_avatar;
        public TextView tv_right_content;
    }
}