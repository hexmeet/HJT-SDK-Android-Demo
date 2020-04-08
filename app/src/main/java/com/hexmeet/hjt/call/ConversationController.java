package com.hexmeet.hjt.call;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.hexmeet.hjt.AppSettings;
import com.hexmeet.hjt.HjtApp;
import com.hexmeet.hjt.R;
import com.hexmeet.hjt.cache.EmMessageCache;
import com.hexmeet.hjt.cache.SystemCache;
import com.hexmeet.hjt.model.FeatureSupport;
import com.hexmeet.hjt.utils.ResourceUtils;
import com.hexmeet.hjt.utils.ScreenUtil;
import com.hexmeet.hjt.utils.Utils;

import org.apache.log4j.Logger;

public class ConversationController implements View.OnClickListener{
    private Logger LOG = Logger.getLogger(Conversation.class);
    private View rootView;
    private ViewGroup cameraSwitchBtn, hangUpBtn, micMuteBtn, localVideoBtn, manageMeetingBtn, layoutModeBtn, moreBtn;
    private ViewGroup titleBar, bottomBar;
    private ImageView signalLevel;
    private Chronometer time;
    private TextView roomText,peopleNumber;
    private boolean barShowing = true;
    private IController iController;
    private int screenWidthWithoutNavigationBar;
    private LinearLayout videoSwitchBtn;
    private ViewGroup moreDetail;
    private final LinearLayout layoutChatBtn;
    private final TextView handUp;
    private final ViewGroup shareBtn;
    private final TextView updateUserName;
    private final ImageView encryptedImg;

    public interface IController{
        void updateCellsAsLayoutModeChanged();
        void showMediaStatistics();
        void onVideoSwitchClick(boolean isVideo);
        void showLocalCamera(boolean show);
        void showConferenceManager();
        void updateMarginTopForMessageOverlay(int margin);
        void updateCellLocalMuteState(boolean isMute);
        void switchVoiceMode(boolean isVoiceMode);
        void showChat();
        void changeUserName();
        void onClickShareScreen();
        void onNoChangeLayout();
        void onHangUp();
    }

    public ConversationController(View rootView, final IController iController, int width) {
        this.rootView = rootView;
        this.iController = iController;
        screenWidthWithoutNavigationBar = width;

        titleBar = (ViewGroup) rootView.findViewById(R.id.title_bar);
        bottomBar = (ViewGroup) rootView.findViewById(R.id.bottom_bar);

        cameraSwitchBtn = (ViewGroup) rootView.findViewById(R.id.toolbar_switch_camera);
        hangUpBtn = (ViewGroup) rootView.findViewById(R.id.toolbar_hangup);
        micMuteBtn = (ViewGroup) rootView.findViewById(R.id.toolbar_local_mute);
        localVideoBtn = (ViewGroup) rootView.findViewById(R.id.toolbar_local_camera);
        manageMeetingBtn = (ViewGroup) rootView.findViewById(R.id.toolbar_conference);
        layoutModeBtn = (ViewGroup) rootView.findViewById(R.id.toolbar_layout_mode);
        moreBtn = (ViewGroup) rootView.findViewById(R.id.toolbar_more);
        moreDetail = (ViewGroup) rootView.findViewById(R.id.more_detail);
        layoutChatBtn = (LinearLayout) rootView.findViewById(R.id.toolbar_layout_chat);
        handUp = (TextView) rootView.findViewById(R.id.hand_up);
        updateUserName = (TextView) rootView.findViewById(R.id.update_user_name);
        shareBtn = (ViewGroup) rootView.findViewById(R.id.toolbar_layout_share);
        encryptedImg = (ImageView) rootView.findViewById(R.id.icon_encrypted_btn);

        adjustBottomButtons();
        adjustHangUp();
        setLayoutMode(AppSettings.getInstance().isSpeakerMode());
        if(HjtApp.getInstance().getAppService()!=null && SystemCache.getInstance().isLayoutModeEnable()) {
            HjtApp.getInstance().getAppService().setLayoutMode(false ? 2 : 1);
        }

        cameraSwitchBtn.setOnClickListener(this);
        hangUpBtn.setOnClickListener(this);
        micMuteBtn.setOnClickListener(this);
        localVideoBtn.setOnClickListener(this);
        manageMeetingBtn.setOnClickListener(this);
        layoutModeBtn.setOnClickListener(this);
        layoutChatBtn.setOnClickListener(this);
        moreBtn.setOnClickListener(this);
        shareBtn.setOnClickListener(this);
        updateUserName.setOnClickListener(this);

        moreDetail.getChildAt(0).setOnClickListener(this);
        moreDetail.getChildAt(1).setOnClickListener(this);
        moreDetail.getChildAt(2).setOnClickListener(this);
        //moreDetail.getChildAt(3).setOnClickListener(this);
        updateHandUpMenu(SystemCache.getInstance().isRemoteMuted());
        moreBtn.setOnClickListener(this);

        signalLevel = (ImageView) rootView.findViewById(R.id.call_statistics_btn);
        time = (Chronometer) rootView.findViewById(R.id.timer_chronometer);
        roomText = (TextView) rootView.findViewById(R.id.room_number);
        peopleNumber=(TextView)rootView.findViewById(R.id.people_number);

        videoSwitchBtn = (LinearLayout) rootView.findViewById(R.id.video_content_switch);

        videoSwitchBtn.setOnClickListener(this);

        MultiClick callStatisticsClick = new MultiClick(4) {
            @Override
            public void onFinalClick() {
                iController.showMediaStatistics();
            }
        };
        signalLevel.setOnClickListener(callStatisticsClick);

        showLocalCamera(SystemCache.getInstance().isUserShowLocalCamera());
        //是否支持会话、音频模式
        FeatureSupport featureSupport = SystemCache.getInstance().getFeatureSupport();
        if(featureSupport!=null){
            layoutChatBtn.setVisibility(featureSupport.isChatInConference() &&  EmMessageCache.getInstance().isIMAddress() ? View.VISIBLE : View.GONE);
            ((TextView)moreDetail.getChildAt(2)).setVisibility(featureSupport.isSwitchingToAudioConference() ? View.VISIBLE : View.GONE);
            //((TextView)moreDetail.getChildAt(3)).setVisibility(featureSupport.isSitenameIsChangeable() ? View.VISIBLE : View.GONE);
            updateUserName.setVisibility(featureSupport.isSitenameIsChangeable() ? View.VISIBLE : View.GONE);
        }
    }

    public float getTopBarHeight() {
        return titleBar.getHeight();
    }

    public void updateHandUpMenu(boolean remoteMute) {
        handUp.setVisibility(remoteMute ? View.VISIBLE : View.GONE );
       if(!SystemCache.getInstance().isUserVideoMode() && remoteMute && moreBtn.getVisibility()==View.GONE){
            moreBtn.setVisibility(View.VISIBLE);
        }else if(!SystemCache.getInstance().isUserVideoMode() && !remoteMute && updateUserName.getVisibility()==View.GONE && moreBtn.getVisibility()==View.VISIBLE){
            moreBtn.setVisibility(View.GONE);
        }
    }

    public void setRoomNum(String num) {
        roomText.setText(num);
    }

    public void startTime(long startTime) {
        time.setBase(startTime);
        time.start();
    }

    public void setNumber(String num) {
        LOG.info("peopleNumber controller : "+num);
        peopleNumber.setText(num);
    }
    private void adjustHangUp() {
        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) hangUpBtn.getLayoutParams();
        hangUpBtn.setLayoutParams(lp);
    }

    public void setChatShow() {
        LOG.info("setChatShow");
        layoutChatBtn.setVisibility(SystemCache.getInstance().getFeatureSupport().isChatInConference()? View.VISIBLE : View.GONE);
    }

    public void setEncryptedImg(){
        LOG.info("setEncryptedImg()");
        encryptedImg.setVisibility(View.VISIBLE);
        HjtApp.getInstance().getAppService().stopMediaStaticsLoop();
    }


    public void muteMic(boolean mute) {
        LOG.info("muteMic icon: "+mute);
        micMuteBtn.getChildAt(0).setSelected(mute);
        TextView title = (TextView) micMuteBtn.getChildAt(1);
        title.setTextColor(mute ? rootView.getResources().getColor(R.color.text_color):rootView.getResources().getColor(R.color.White));
        title.setText(mute ? R.string.unmute :  R.string.mute);
        iController.updateCellLocalMuteState(mute);
    }

    @SuppressLint("ResourceAsColor")
    public void muteVideo(boolean mute) {
        LOG.info("muteVideo : "+mute);
        localVideoBtn.getChildAt(0).setSelected(mute);
        TextView title = (TextView) localVideoBtn.getChildAt(1);
        title.setTextColor(mute ? rootView.getResources().getColor(R.color.text_color):rootView.getResources().getColor(R.color.White));
        title.setText(mute ? R.string.enable_video : R.string.stop_video);
    }

    public void toggleLayoutMode() {
        LOG.info("toggleLayoutMode isSpeaker : "+AppSettings.getInstance().isSpeakerMode());
        setLayoutMode(!AppSettings.getInstance().isSpeakerMode());
        HjtApp.getInstance().getAppService().setLayoutMode(!AppSettings.getInstance().isSpeakerMode() ? 2 : 1);
    }

    public void setLayoutMode(boolean isSpeaker) {
        LOG.info("isSpeaker : "+isSpeaker);
        layoutModeBtn.getChildAt(0).setSelected(isSpeaker);
        iController.updateCellsAsLayoutModeChanged();
    }

    public void shareScreen(boolean share) {
        LOG.info("shareScreen icon: "+share);
        shareBtn.getChildAt(0).setSelected(share);
        TextView title = (TextView) shareBtn.getChildAt(1);
        title.setText(share ? R.string.stop_share :  R.string.share);
    }

    private void alertLayoutModeDisable() {
        iController.onNoChangeLayout();
    }

    public void updateAsAudioCall() {
        localVideoBtn.setVisibility(View.GONE);
        layoutModeBtn.setVisibility(View.GONE);
        cameraSwitchBtn.setVisibility(View.GONE);
    }

    public void updateSignalLevel(float level) {
        if (level < 2) {
            signalLevel.setImageResource(R.drawable.image_signal1);
        } else if (level >= 2 && level < 3) {
            signalLevel.setImageResource(R.drawable.image_signal2);
        } else if (level >= 3 && level < 4) {
            signalLevel.setImageResource(R.drawable.image_signal3);
        } else if (level >= 4 && level < 5) {
            signalLevel.setImageResource(R.drawable.image_signal4);
        } else if (level >= 5) {
            signalLevel.setImageResource(R.drawable.image_signal5);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.toolbar_hangup:
                LOG.info("End call as click HangUp");
                iController.onHangUp();
                break;
            case R.id.hand_up:
                if(SystemCache.getInstance().isRemoteMuted()) {
                    Toast.makeText(v.getContext(), R.string.request_speak_is_sent, Toast.LENGTH_SHORT).show();
                    HjtApp.getInstance().getAppService().requestHandUp();
                } else {
                    Toast.makeText(v.getContext(), R.string.allowed_speak, Toast.LENGTH_SHORT).show();
                }
                moreDetail.setVisibility(View.GONE);
                break;
            case R.id.toolbar_more:
                moreDetail.setVisibility(moreDetail.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
                break;
            case R.id.toolbar_switch_camera:
                HjtApp.getInstance().getAppService().switchCamera();
                break;
            case R.id.toolbar_local_mute:
              boolean mute = !((ViewGroup)v).getChildAt(0).isSelected();
                LOG.info("mute onClick : "+mute+",micEnabled : "+HjtApp.getInstance().getAppService().micEnabled());
                if(mute ^ !HjtApp.getInstance().getAppService().micEnabled()){
                    HjtApp.getInstance().getAppService().muteMic(mute);
                }
                muteMic(!HjtApp.getInstance().getAppService().micEnabled());
                break;
            case R.id.toolbar_layout_mode:
                if(SystemCache.getInstance().isLayoutModeEnable()) {
                    toggleLayoutMode();
                } else {
                    alertLayoutModeDisable();
                }
                break;
            case R.id.toolbar_conference:
                iController.showConferenceManager();
                break;
            case R.id.toolbar_local_camera:
                boolean muteVideo = !((ViewGroup)v).getChildAt(0).isSelected();
                SystemCache.getInstance().setUserMuteVideo(muteVideo);
                muteVideo(muteVideo);
                HjtApp.getInstance().getAppService().enableVideo(!muteVideo);
                break;

            case R.id.video_content_switch:
                boolean isContent = v.isSelected();
                if(isContent) {
                    onVideoShow();
                    iController.onVideoSwitchClick(true);
                } else {
                    onContentShow();
                    iController.onVideoSwitchClick(false);
                }
                break;
            case R.id.switch_localview:
                showLocalCamera(!SystemCache.getInstance().isUserShowLocalCamera());
                moreDetail.setVisibility(View.GONE);
                break;
            case R.id.switch_vadio_mode:
                showVideoMode(!SystemCache.getInstance().isUserVideoMode());
                moreDetail.setVisibility(View.GONE);
                break;
            case R.id.toolbar_layout_chat:
                iController.showChat();
                break;
            case R.id.update_user_name:
                iController.changeUserName();
                moreDetail.setVisibility(View.GONE);
                break;
            case R.id.toolbar_layout_share:
                iController.onClickShareScreen();
                break;
            default:
                break;
        }
    }

    public void showVideoMode(boolean isVideoMode) {//切换  语音  flase /视频  true
        LOG.info("showVideoMode : "+isVideoMode);
        ((TextView)moreDetail.getChildAt(2)).setVisibility(isVideoMode ?View.VISIBLE : View.GONE);
        iController.switchVoiceMode(isVideoMode);
        iController.showLocalCamera(isVideoMode);
        ((TextView)moreDetail.getChildAt(0)).setVisibility(isVideoMode ? View.VISIBLE : View.GONE);
        //隐藏本地视频
        localVideoBtn.setVisibility(isVideoMode ? View.VISIBLE : View.GONE);
        //隐藏视图模式
        layoutModeBtn.setVisibility(isVideoMode ? View.VISIBLE : View.GONE);
        //是否隐藏转换摄像头
        cameraSwitchBtn.setVisibility(isVideoMode ? View.VISIBLE :View.GONE);
        //视频模式下 判断是否启用本地视频
        if(SystemCache.getInstance().isUserMuteVideo() &&  localVideoBtn.getVisibility()==View.VISIBLE){
            HjtApp.getInstance().getAppService().enableVideo(false);
        }
        //判断申请发言是否开启，如果没开启隐藏 ，开启则显示
        if(!isVideoMode && handUp.getVisibility()==View.GONE && updateUserName.getVisibility()==View.GONE){
            moreBtn.setVisibility(View.GONE);
        }else {
            moreBtn.setVisibility(View.VISIBLE);
        }

        if(!isVideoMode){//语音模式 隐藏共享屏幕功能
            if(SystemCache.getInstance().isSharedScreen()){
                iController.onClickShareScreen();
            }
            shareBtn.setVisibility(View.GONE );
        }

    }

    private void showLocalCamera(boolean show) {
        LOG.info("showLocalCamera controller : "+show);
        SystemCache.getInstance().setUserShowLocalCamera(show);
        ((TextView)moreDetail.getChildAt(0)).setText(show ? R.string.close_local_view : R.string.open_local_view);
        iController.showLocalCamera(show);
    }

    private DecelerateInterpolator interpolator = new DecelerateInterpolator();
    public void showBar() {
        if(!barShowing) {
            titleBar.setVisibility(View.VISIBLE);
            bottomBar.setVisibility(View.VISIBLE);
            barShowing = true;
            if(videoSwitchBtn.getVisibility() == View.VISIBLE) {
                adjustSwitcherLayout(false);
            }
            iController.updateMarginTopForMessageOverlay(titleBar.getHeight());
            handler.sendEmptyMessageDelayed(0, 10000);
        }
    }

    private void adjustSwitcherLayout(boolean isBottom) {
        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) videoSwitchBtn.getLayoutParams();
        if(isBottom) {
            lp.bottomMargin = ScreenUtil.dp_to_px(10);
        } else {
            lp.bottomMargin = (ScreenUtil.dp_to_px(10) + bottomBar.getHeight());
        }
        videoSwitchBtn.setLayoutParams(lp);
    }

    public void hideBar() {
        if(moreDetail.getVisibility() == View.VISIBLE) {
            moreDetail.setVisibility(View.GONE);
        }

        if(barShowing) {
            handler.removeMessages(0);
            titleBar.setVisibility(View.INVISIBLE);
            bottomBar.setVisibility(View.INVISIBLE);
            barShowing = false;
            if(videoSwitchBtn.getVisibility() == View.VISIBLE) {
                adjustSwitcherLayout(true);
            }
            iController.updateMarginTopForMessageOverlay(0);
        }
    }

    public void toggleBar() {
        if(barShowing) {
            if(moreDetail.getVisibility() == View.VISIBLE) {
                moreDetail.setVisibility(View.GONE);
            } else {
                hideBar();
            }
        } else {
            showBar();
        }
    }

    public void adjustBottomButtons() {
        int btnCount = bottomBar.getChildCount();
        int width = ResourceUtils.originScreenWidth / btnCount;
        for (int x = 0; x < btnCount; x ++) {
            View child = bottomBar.getChildAt(x);
            if (child.getVisibility() == View.VISIBLE) {
                LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) child.getLayoutParams();
                lp.width = width;
                child.setLayoutParams(lp);
            }
        }

        RelativeLayout.LayoutParams rp = (RelativeLayout.LayoutParams) moreDetail.getLayoutParams();
        rp.width = width;
        moreDetail.setLayoutParams(rp);
    }

    public void showSwitchAsContent(boolean withContent) {
        if(withContent) {
            videoSwitchBtn.setVisibility(View.VISIBLE);
            ((TextView)videoSwitchBtn.getChildAt(0)).setText(R.string.content);
            onContentShow();
            if(barShowing) {
                adjustSwitcherLayout(false);
            } else {
                adjustSwitcherLayout(true);
            }
        } else {
            videoSwitchBtn.setVisibility(View.INVISIBLE);
        }
    }

    public void onContentShow() {
        videoSwitchBtn.setSelected(true);
        videoSwitchBtn.getChildAt(0).setSelected(true);
        videoSwitchBtn.getChildAt(1).setSelected(false);
    }

    public void onVideoShow() {
        videoSwitchBtn.setSelected(false);
        videoSwitchBtn.getChildAt(0).setSelected(false);
        videoSwitchBtn.getChildAt(1).setSelected(true);
    }

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if(msg.what == 0) {
                hideBar();
            }
        }
    };

    public void clean() {
        time.stop();
        handler.removeCallbacksAndMessages(null);
    }

}
