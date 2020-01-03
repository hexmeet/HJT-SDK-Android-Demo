package com.hexmeet.hjt.chat;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.hexmeet.hjt.BaseActivity;
import com.hexmeet.hjt.R;
import com.hexmeet.hjt.chat.adapter.ChattingListAdapter;
import com.hexmeet.hjt.groupchat.ChattingFooter;
import com.hexmeet.hjt.groupchat.adapter.GroupAdapter;
import com.hexmeet.hjt.groupchat.utils.StatusBarCompat;
import com.hexmeet.hjt.utils.StateUtil;
import com.sj.emoji.EmojiBean;

import org.apache.log4j.Logger;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import sj.keyboard.data.EmoticonEntity;
import sj.keyboard.interfaces.EmoticonClickListener;
import sj.keyboard.utils.EmoticonsKeyboardUtils;
import sj.keyboard.widget.EmoticonsEditText;
import sj.keyboard.widget.FuncLayout;

public class ChatContentActivity extends BaseActivity implements View.OnClickListener , ChattingFooter.OnChattingFooterListener{
    private Logger LOG = Logger.getLogger(this.getClass());
    private LinearLayout mEkBar;
    private TextView mName;
    private ImageView mChatBackBtn,mMore;
    private RecyclerView mRlvMessage;
    private ChattingFooter mChatFooter;
    private View mChatContentView;
    private GroupAdapter messageAdapter;
    private GestureDetector gestureDetector;
    public static int EMOTICON_CLICK_TEXT = 1;
    public static int EMOTICON_CLICK_BIGIMAGE = 2;
    public static void actionStart(Context context) {
        Intent intent = new Intent(context, ChatContentActivity.class);
        context.startActivity(intent);
    }

    private GestureDetector.SimpleOnGestureListener simpleOnGestureListener = new GestureDetector.SimpleOnGestureListener() {
        @Override
        public boolean onDown(MotionEvent e) {
            if (mChatFooter.isShowExtra()) {
                mChatFooter.hideExtra();
            }
            hideSoftKeyboard();
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if (mChatFooter.isShowExtra()) {
                mChatFooter.hideExtra();
            }
            hideSoftKeyboard();
            return super.onFling(e1, e2, velocityX, velocityY);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LOG.info("onCreate()");
        setContentView(R.layout.activity_chat_content);
       /* Bundle bundle = getIntent().getExtras();
        username = bundle.getString("username");
        imageUrl = bundle.getString("imageUrl");
        userId = bundle.getString("userId");*/
        initView();
        initDate();
    }

    private void initView() {
        mEkBar = (LinearLayout) findViewById(R.id.ekBar);
        mName = (TextView) findViewById(R.id.name);
        mChatBackBtn = (ImageView) findViewById(R.id.chat_back_btn);
        mMore = (ImageView) findViewById(R.id.more);
        mChatContentView =  findViewById(R.id.chat_content_view);
        mRlvMessage = (RecyclerView) findViewById(R.id.rlv_message);
        mChatFooter = (ChattingFooter) findViewById(R.id.chat_footer);

        gestureDetector = new GestureDetector(this, simpleOnGestureListener);
        mChatFooter.setActivity(this, mChatContentView);
        mChatFooter.setOnChattingFooterListener(this);
        mRlvMessage.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        mRlvMessage.setLayoutManager(llm);
        messageAdapter = new GroupAdapter(this);
        mRlvMessage.setAdapter(messageAdapter);
        mRlvMessage.setOverScrollMode(View.OVER_SCROLL_NEVER);
        mRlvMessage.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                gestureDetector.onTouchEvent(event);
                return false;
            }
        });
    }

    private void initDate() {
        mChatBackBtn.setOnClickListener(this);
        mMore.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.chat_back_btn:
                onBackPressed();
                break;
            case R.id.more:
                Intent intent = new Intent(this,ChatDetailActivity.class);
                startActivity(intent);
                break;

        }
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



    @Override
    public void OnSendTextMessageRequest(CharSequence text) {

    }


    @Override
    public void OnInEditMode() {

    }

    @Override
    public void onPause() {
        super.onPause();
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

    }
}
