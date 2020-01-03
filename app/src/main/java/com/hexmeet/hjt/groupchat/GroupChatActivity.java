package com.hexmeet.hjt.groupchat;

import android.content.Context;
import android.content.Intent;
import android.icu.util.Calendar;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.hexmeet.hjt.AppCons;
import com.hexmeet.hjt.BaseActivity;
import com.hexmeet.hjt.CallState;
import com.hexmeet.hjt.HjtApp;
import com.hexmeet.hjt.R;
import com.hexmeet.hjt.cache.EmMessageCache;
import com.hexmeet.hjt.cache.SystemCache;
import com.hexmeet.hjt.cache.SystemStateAdapter;
import com.hexmeet.hjt.chat.ImMsgBean;
import com.hexmeet.hjt.event.CallEvent;
import com.hexmeet.hjt.event.EmMessageBody;
import com.hexmeet.hjt.event.GroupMemberInfo;
import com.hexmeet.hjt.groupchat.adapter.GroupAdapter;
import com.hexmeet.hjt.groupchat.utils.StatusBarCompat;
import com.hexmeet.hjt.groupchat.utils.TimeUtil;
import com.hexmeet.hjt.model.IMGroupContactInfo;

import org.apache.log4j.Logger;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import em.common.EMEngine;


public class GroupChatActivity extends BaseActivity implements ChattingFooter.OnChattingFooterListener{
    private Logger LOG = Logger.getLogger(this.getClass());
    private ChattingFooter mChattingFooter;
    private View mRoot;
    private RecyclerView rlvMessage;
    private GroupAdapter messageAdapter;
    private GestureDetector detector;

    private GestureDetector.SimpleOnGestureListener simpleOnGestureListener = new GestureDetector.SimpleOnGestureListener() {
        @Override
        public boolean onDown(MotionEvent e) {
            if (mChattingFooter.isShowExtra()) {
                mChattingFooter.hideExtra();
            }
            hideSoftKeyboard();
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if (mChattingFooter.isShowExtra()) {
                mChattingFooter.hideExtra();
            }
            hideSoftKeyboard();
            return super.onFling(e1, e2, velocityX, velocityY);
        }
    };
    private TextView number;
    private EMEngine.UserInfo isSelf;
    private String userId;
    private ImageView mChatBack;


    public static void actionStart(Context context) {
        Intent intent = new Intent(context, GroupChatActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
        LOG.info("onCreate()");
        setContentView(R.layout.activity_group_chat);
        initView();
        isSelf = HjtApp.getInstance().getAppService().getImUserInfo();
        userId = String.valueOf(SystemCache.getInstance().getLoginResponse().getUserId());
        initImMessageBody();
    }

    private void initImMessageBody() {
        List<EmMessageBody> messageBody = EmMessageCache.getInstance().getMessageBody();
        if(messageBody!=null){
            for (int i=0;i < messageBody.size();i ++){
                addNewsAdapter(messageBody.get(i));
            }
        }
    }

    private void initView() {
        mRoot = findViewById(R.id.ll_root);
        number = (TextView) findViewById(R.id.group_number);
        mChatBack = (ImageView) findViewById(R.id.chat_back_btn);
        mChattingFooter = (ChattingFooter) findViewById(R.id.chatting_footer);
        mChattingFooter.isShowMore(false);
        detector = new GestureDetector(this, simpleOnGestureListener);
        mChattingFooter.setActivity(this, mRoot);
        rlvMessage = (RecyclerView) findViewById(R.id.rlv_message);
        rlvMessage.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        rlvMessage.setLayoutManager(llm);
        messageAdapter = new GroupAdapter(this);
        rlvMessage.setAdapter(messageAdapter);
        rlvMessage.setOverScrollMode(View.OVER_SCROLL_NEVER);
        mChattingFooter.setOnChattingFooterListener(this);
        rlvMessage.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                detector.onTouchEvent(event);
                return false;
            }
        });
        number.setText(getString(R.string.group_chat)+"("+SystemCache.getInstance().getParticipant()+")");

        mChatBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideSoftKeyboard();
                onBackPressed();
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void OnSendTextMessageRequest(final CharSequence text) {
        EmMessageBody body = new EmMessageBody();
        body.setGroupId(isSelf.userid);
        body.setContent(text.toString());
        body.setTime(TimeUtil.currentTime(Calendar.getInstance().getTimeInMillis()));
        body.setFrom(isSelf.userid);
        //发送到适配器
        addNewsAdapter(body);
        //保存当前信息
        EmMessageCache.getInstance().addMessageBody(body);
        //发送到sdk
        HjtApp.getInstance().getAppService().sendMessage(text.toString());
        //刷新
        showLastMessage();
    }



    @Override
    public void OnInEditMode() {
        showLastMessage();
    }

    @Override
    protected void onStart() {
        LOG.info("onStart()");
        super.onStart();
        hideSoftKeyboard();
        EmMessageCache.getInstance().registerSystemCallBack(callback);
    }

    @Override
    public void onPause() {
        LOG.info("onPause()");
        super.onPause();
        hideSoftKeyboard();
        EmMessageCache.getInstance().unRegisterSystemCallBack(callback);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void release() {
    }

    @Override
    public void onP2pCall(boolean isVideoCall) {
        LOG.info("onP2pCall : "+isVideoCall);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
    /**
     * hide inputMethod
     */
    public boolean hideSoftKeyboard() {
        boolean b = false;
        InputMethodManager inputMethodManager = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputMethodManager != null) {
            View localView = this.getCurrentFocus();
            if (localView != null && localView.getWindowToken() != null) {
                IBinder windowToken = localView.getWindowToken();
                b = inputMethodManager.hideSoftInputFromWindow(windowToken, 0);

            }
        }
        return b;
    }

    private void showLastMessage() {
        rlvMessage.postDelayed(new Runnable() {
            @Override
            public void run() {
                rlvMessage.scrollToPosition( messageAdapter.getItemCount()-1);
            }
        }, 100);
    }

    SystemStateAdapter callback = new SystemStateAdapter() {
        @Override
        public void onMessageReciveData(final EmMessageBody messageBody) {
            LOG.info("new message data");
            if(messageBody!=null){
                addNewsAdapter(messageBody);
            }

        }

        @Override
        public void onGroupAmount(String num) {
            LOG.info("GroupAmount : "+num);
            number.setText(getString(R.string.group_chat)+"("+num+")");
        }

        @Override
        public void onGroupMemberInfo() {
            LOG.info("onGroupMemberInfo() : ");
            messageAdapter.clearList();
            initImMessageBody();
        }
    };
    //获取新消息
    private void addNewsAdapter(EmMessageBody messageBody) {
        List<IMGroupContactInfo> contactInfo = EmMessageCache.getInstance().getContactInfo();
        for (int i=0;i < contactInfo.size();i ++){
            if(messageBody.getFrom().equals(contactInfo.get(i).getEmUserId())){
                if(userId.equals(contactInfo.get(i).getEvUserId())){
                    LOG.info("userId : "+userId+";evuserid : "+contactInfo.get(i).getEvUserId());
                    receriveMsgTextMe(messageBody, contactInfo.get(i).getDisplayName(),contactInfo.get(i).getImageUrl());
                }else {
                    receriveMsgText(messageBody,contactInfo.get(i).getDisplayName(),contactInfo.get(i).getImageUrl());
                }
            }
        }
    }

    //接收消息（right）
    private void receriveMsgTextMe(EmMessageBody body, String userName,String imagUrl) {
        ImMsgBean   bean = new ImMsgBean();
        bean.setTime(TimeUtil.getDateTime(body.time));
        bean.setContent(body.content);
        bean.setName(userName);
        bean.setImageUrl(imagUrl);
        bean.setMsgType(GroupAdapter.GROUPVIEW_TYPE_RIGHT_TEXT);
        messageAdapter.addData(bean, true);
        showLastMessage();
    }

    //接收消息（left）
    private void receriveMsgText(EmMessageBody body,String name,String imagUrl) {
        ImMsgBean    bean = new ImMsgBean();
        bean.setTime(TimeUtil.getDateTime(body.time));
        bean.setContent(body.content);
        bean.setName(name);
        bean.setImageUrl(imagUrl);
        bean.setMsgType(GroupAdapter.GROUPVIEW_TYPE_LEFT_TEXT);
        messageAdapter.addData(bean, true);
        showLastMessage();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onCallStateEvent(CallEvent event) {
        if (event.getCallState() == CallState.IDLE) {
            finish();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
