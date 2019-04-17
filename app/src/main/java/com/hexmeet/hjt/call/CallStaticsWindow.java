package com.hexmeet.hjt.call;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.hexmeet.hjt.HjtApp;
import com.hexmeet.hjt.R;
import com.hexmeet.hjt.sdk.ChannelStatList;
import com.hexmeet.hjt.sdk.ChannelStatistics;

import java.util.List;


public class CallStaticsWindow {
    private Context context;
    private LinearLayout rootView;
    private Dialog dialog;
    private TextView title;
    private StatisticsAdapter adapter;

    @RequiresApi(api = Build.VERSION_CODES.FROYO)
    public CallStaticsWindow(Context context) {
        initDialog(context);
    }

    @RequiresApi(api = Build.VERSION_CODES.FROYO)
    private void initDialog(Context context) {
        this.context = context;
        rootView = (LinearLayout) LayoutInflater.from(context).inflate(R.layout.call_statics, null);
        rootView.findViewById(R.id.call_statistics_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        adapter = new StatisticsAdapter();
        ListView listView = (ListView) rootView.findViewById(R.id.call_statistics_list);
        listView.setAdapter(adapter);

        title = (TextView) rootView.findViewById(R.id.call_statistics_title);

        dialog = new Dialog(context, R.style.window_call);

        dialog.setContentView(rootView);
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                HjtApp.getInstance().getAppService().startMediaStaticsLoop();
            }
        });
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                HjtApp.getInstance().getAppService().stopMediaStaticsLoop();
            }
        });
    }

    public boolean isShowing() {
        return dialog != null && dialog.isShowing();
    }

    public void updateMediaStatistics(ChannelStatList channelStatList) {
        if (isShowing() && channelStatList != null) {
            if(channelStatList.signal_statistics != null) {
                title.setText(context.getString(R.string.call_statistics) + "("+getEncrytName(channelStatList.signal_statistics.encryption)+")");
            }
            adapter.update(channelStatList);
        }
    }

    private String getEncrytName(boolean encrypted) {
        String result = "";
        if (encrypted) {
            result = context.getResources().getString(R.string.encrypted_value_yes);
        } else  {
            result = context.getResources().getString(R.string.encrypted_value_no);
        }
        return result;
    }

    public void show() {
        if (!isShowing()) {
            dialog.show();
        }
    }

    public void dismiss() {
        if (isShowing()) {
            dialog.dismiss();
        }
    }

    private class StatisticsAdapter extends BaseAdapter {
        List<ChannelStatistics> list;
        @Override
        public int getCount() {
            return list == null ? 0 : list.size();
        }

        @Override
        public ChannelStatistics getItem(int position) {
            return list == null ? null : list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position * 100 + 100;
        }

        public void update(ChannelStatList channelStatList) {
            if(channelStatList != null && channelStatList.media_statistics != null) {
                list = channelStatList.media_statistics.getTotalList();
                notifyDataSetChanged();
            }
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.call_statics_item, null);
                holder.channel = (TextView) convertView.findViewById(R.id.channel);
                holder.protocol = (TextView) convertView.findViewById(R.id.protocol);
                holder.rate = (TextView) convertView.findViewById(R.id.rate);
                holder.rateUsed = (TextView) convertView.findViewById(R.id.rate_used);
                holder.resolution = (TextView) convertView.findViewById(R.id.resolution);
                holder.packetsLoss = (TextView) convertView.findViewById(R.id.packets_loss);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            if((position & 1) != 0) {
                convertView.setBackgroundColor(context.getResources().getColor(R.color.call_statistics_item_bg_1));
            } else {
                convertView.setBackgroundColor(context.getResources().getColor(R.color.call_statistics_item_bg_2));
            }

            ChannelStatistics stat = getItem(position);
            holder.channel.setText(getPipeName(stat.pipeName));
            holder.protocol.setText(stat.codec);
            //holder.rate.setText("" + stat.rtp_settingBitRate);
            if(stat.pipeName.equalsIgnoreCase("PS") || stat.pipeName.equalsIgnoreCase("PR")) {
                holder.rateUsed.setTextColor(getTextColor(stat));
            }
            holder.rateUsed.setText("" + stat.rtp_actualBitRate);
            if (stat.pipeName.equalsIgnoreCase("AS") || stat.pipeName.equalsIgnoreCase("AR")) {
                holder.resolution.setText("-");
            } else {
                holder.resolution.setText("" + translateResolution(stat.resolution) + "("+stat.frameRate+")");
            }
            holder.packetsLoss.setText("" + stat.packetLost + " (" + stat.packetLostRate + "%)");
            return convertView;
        }
    }

    private int getTextColor(ChannelStatistics statistics) {
        float rate = ((float) statistics.rtp_actualBitRate) / statistics.rtp_settingBitRate;
        Resources resources = context.getResources();
        if(rate <= 0.3f) {
            return resources.getColor(R.color.Red);
        } else if (rate <= 0.6f) {
            return resources.getColor(R.color.Yellow);
        } else {
            return resources.getColor(R.color.White);
        }
    }

    private String getPipeName(String name) {
        String result = name;
        if (name.equalsIgnoreCase("AS")) {
            result = context.getResources().getString(R.string.atx);
        } else if (name.equalsIgnoreCase("AR")) {
            result = context.getResources().getString(R.string.arx);
        } else if (name.equalsIgnoreCase("PS")) {
            result = context.getResources().getString(R.string.pvtx);
        } else if (name.equalsIgnoreCase("PR")) {
            result = context.getResources().getString(R.string.pvrx);
        } else if (name.equalsIgnoreCase("CS")) {
            result = context.getResources().getString(R.string.cvtx);
        } else if (name.equalsIgnoreCase("CR")) {
            result = context.getResources().getString(R.string.cvrx);
        }
        return result;
    }

    private String translateResolution(String resolution) {
        if(resolution == null) {
            return "-";
        }
        if (resolution.equalsIgnoreCase("320x180")) {
            return "180p";
        }
        if (resolution.equalsIgnoreCase("640x360")) {
            return "360p";
        }
        if (resolution.equalsIgnoreCase("1280x720")) {
            return "720p";
        }
        if (resolution.equalsIgnoreCase("1920x1080")) {
            return "1080p";
        }
        return resolution;
    }

    static class ViewHolder {
        public TextView channel;
        public TextView protocol;
        public TextView rate;
        public TextView rateUsed;
        public TextView resolution;
        public TextView packetsLoss;
    }

    public void clean() {
        dismiss();
        dialog = null;
        context = null;
    }

}
