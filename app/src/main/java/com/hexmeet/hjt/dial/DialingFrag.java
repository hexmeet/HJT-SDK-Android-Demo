package com.hexmeet.hjt.dial;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hexmeet.hjt.HexMeet;
import com.hexmeet.hjt.HjtApp;
import com.hexmeet.hjt.R;
import com.hexmeet.hjt.cache.SystemCache;
import com.hexmeet.hjt.utils.NetworkUtil;
import com.hexmeet.hjt.utils.Utils;
import com.hexmeet.hjt.widget.NumberKeyboard;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DialingFrag extends Fragment {
    private Logger LOG = Logger.getLogger(this.getClass());
    private NumberKeyboard numberKeyboard;
    private ViewGroup closeCameraView, closeMicView;
    private LinearLayout recentContainer;
    private View recentBtn;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.dialing, container, false);

        root.findViewById(R.id.dial_btn).setOnClickListener(clickListener);
        recentContainer = (LinearLayout) root.findViewById(R.id.recent_list_container);

        closeCameraView = (ViewGroup) root.findViewById(R.id.close_camera);
        closeCameraView.setOnClickListener(clickListener);
        closeMicView = (ViewGroup) root.findViewById(R.id.close_mic);
        closeMicView.setOnClickListener(clickListener);
        closeCameraView.getChildAt(1).setSelected(SystemCache.getInstance().isUserMuteVideo());
        closeMicView.getChildAt(1).setSelected(SystemCache.getInstance().isUserMuteMic());
        recentBtn = root.findViewById(R.id.btn_recent);
        recentBtn.setOnClickListener(clickListener);

        numberKeyboard = new NumberKeyboard((TextView) root.findViewById(R.id.call_number), root.findViewById(R.id.number_keyboard)
                , new NumberKeyboard.NumberKeyboardListener() {
            @Override
            public void onKeyClick() {
                checkAndCloseRecentView();
            }
        });
        return root;
    }

    private View.OnClickListener clickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.dial_btn:
                    String number = numberKeyboard.getNumber();
                    if(validate(number)) {
                        SystemCache.getInstance().setUserMuteVideo(closeCameraView.getChildAt(1).isSelected());
                        //SystemCache.getInstance().setUserMuteMic(closeMicView.getChildAt(1).isSelected());
                        String sipNumberWithoutPassword = number;
                        String password = "";
                        if (number.contains("*")) {
                            String[] strs = number.split("\\*");
                            sipNumberWithoutPassword = strs[0];
                            password = strs[1];
                        }
                        HjtApp.getInstance().getAppService().muteMic(closeMicView.getChildAt(1).isSelected());
                        ((HexMeet)getActivity()).dialOut(sipNumberWithoutPassword, password);
                    }
                    break;
                case R.id.close_camera:
                    boolean cameraClose = closeCameraView.getChildAt(1).isSelected();
                    closeCameraView.getChildAt(1).setSelected(!cameraClose);
                    break;
                case R.id.close_mic:
                    boolean micClose = closeMicView.getChildAt(1).isSelected();
                    closeMicView.getChildAt(1).setSelected(!micClose);
                    break;
                case R.id.btn_recent:
                    boolean open = v.isSelected();
                    showRecentView(!open);
                    v.setSelected(!open);
                    break;
            }
        }
    };

    private void checkAndCloseRecentView() {
        boolean open = recentBtn.isSelected();
        if(open) {
            recentBtn.setSelected(false);
        }

        if(recentContainer.getVisibility() == View.VISIBLE) {
            recentContainer.setVisibility(View.GONE);
        }
    }

    private void showEmptyRecent() {
        TextView emptyView = new TextView(getContext());
        emptyView.setText(R.string.empty_recent);
        emptyView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        emptyView.setPadding(0, 40, 0, 40);
        emptyView.setGravity(Gravity.CENTER);
        emptyView.setTextColor(getResources().getColor(R.color.font_color_AB313131));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        recentContainer.addView(emptyView, lp);
    }

    private void showRecentView(boolean show) {
        recentContainer.setVisibility(show ? View.VISIBLE : View.GONE);
        if(show) {
            recentContainer.removeAllViews();
            LinkedList<String> recents = RecentPreference.getInstance().getRecentList();
            if(recents.isEmpty()) {
                showEmptyRecent();
            } else {
                LayoutInflater inflater = LayoutInflater.from(getContext());
                for (int i = 0; i < recents.size(); i++) {
                    String item = recents.get(i);
                    ViewGroup child = (ViewGroup) inflater.inflate(R.layout.recent_item, recentContainer, false);
                    child.getChildAt(0).setTag(item);
                    child.getChildAt(0).setOnClickListener(recent_click);
                    ((TextView)child.getChildAt(0)).setText(item);

                    child.getChildAt(1).setTag(item);
                    child.getChildAt(1).setOnClickListener(recent_del_click);

                    if(i == (recents.size() -1)){
                        child.getChildAt(2).setVisibility(View.GONE);
                    }
                    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    recentContainer.addView(child, lp);
                }
            }
        }
    }

    private View.OnClickListener recent_click = new OnClickListener() {
        @Override
        public void onClick(View v) {
            checkAndCloseRecentView();
            String number = (String) v.getTag();
            numberKeyboard.setNumberFromRecent(number);
        }
    };
    private View.OnClickListener recent_del_click = new OnClickListener() {
        @Override
        public void onClick(View v) {
            recentContainer.removeView((View) v.getParent());
            String number = (String) v.getTag();
            RecentPreference.getInstance().updateRecent(number, false);
            if(recentContainer.getChildCount() == 0) {
                showEmptyRecent();
            }
        }
    };

    private boolean validate(String number) {
        if (StringUtils.isEmpty(number)) {
            LOG.warn("dialing number is empty!");
            Utils.showToast(getContext(), R.string.input_call_number);
            return false;
        }
        Pattern p = Pattern.compile("[0-9]+(\\*[0-9]+)?");
        Matcher m = p.matcher(number);
        if (!m.matches()) {
            LOG.warn("dialing number: " + number + " NOT match format: " + R.string.format);
            Utils.showToast(getContext(), R.string.format);
            return false;
        }

        if (!NetworkUtil.isCloudReachable(getContext())) return false;

        return true;
    }
}
