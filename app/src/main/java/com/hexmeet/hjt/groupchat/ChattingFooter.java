package com.hexmeet.hjt.groupchat;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.os.Environment;
import android.os.StatFs;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.hexmeet.hjt.R;
import com.hexmeet.hjt.event.ChatMore;
import com.hexmeet.hjt.groupchat.adapter.EmojiconGridViewLayout;
import com.hexmeet.hjt.groupchat.adapter.EmojisPagerAdapter;
import com.hexmeet.hjt.groupchat.adapter.FaceMoreAdapter;
import com.hexmeet.hjt.groupchat.adapter.FaceVPMoreAdapter;
import com.hexmeet.hjt.groupchat.utils.PrefUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import androidx.viewpager.widget.ViewPager;
import io.github.rockerhieu.emojicon.EmojiconEditText;
import io.github.rockerhieu.emojicon.EmojiconRecents;
import io.github.rockerhieu.emojicon.EmojiconRecentsManager;
import io.github.rockerhieu.emojicon.emoji.Emojicon;
import io.github.rockerhieu.emojicon.emoji.People;


public class ChattingFooter  extends LinearLayout implements View.OnClickListener, TextWatcher, ViewTreeObserver.OnGlobalLayoutListener,
        View.OnTouchListener, EmojiconRecents{
    private static final int CANCEL_DISTANCE = 60;
    private Context mContext;
    private View parentRoot, view;
    private EmojiconEditText etInput;
    private ImageView ivEmoji;
    private ViewPager vpExtra;
    private Button btnSend,ivExtra;
    private Activity mActivity;
    private int keyboardHeight;
    private int extraBarHeight;
    private LinearLayout.LayoutParams extraLp;
    private LayoutInflater mInflater;
    private OnChattingFooterListener mChattingFooterListener;
    private View extraView;

    private Container mContainer;


    private int columns = 7;
    private int rows =20;

    private Button ivMore;
    private FaceVPMoreAdapter vpMoreAdapter;
    public EmojiconGridViewLayout.OnEmojiconClickedListener onEmojiconClickedListener;
    private EmojisPagerAdapter mEmojisAdapter;

    public ChattingFooter(Context context) {
        super(context);
        initView(context);
    }

    public void setOnChattingFooterListener(OnChattingFooterListener onChattingFooterListener) {
        this.mChattingFooterListener = onChattingFooterListener;
    }

    public ChattingFooter(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public void setActivity(Activity activity, View root) {
        mActivity = activity;
        parentRoot = activity.getWindow().getDecorView();
        parentRoot.getViewTreeObserver().addOnGlobalLayoutListener(this);
    }

    public void isShowMore(boolean type){
        if(type){
            ivExtra.setVisibility(VISIBLE);
            ivMore.setVisibility(GONE);
        }else {
            ivExtra.setVisibility(GONE);
            ivMore.setVisibility(VISIBLE);
        }

    }

    private void initView(Context context) {
        mContext = context;
        checkDeviceHasNavigationBar(context);
        setFocusable(true);
        keyboardHeight = PrefUtils.getKeyboardHeight(context);
        setOrientation(VERTICAL);
        view = LayoutInflater.from(mContext).inflate(R.layout.chatting_footer, this, true);
        extraView = view.findViewById(R.id.extra_view);
        initEmoji();
        initMore();
        etInput = (EmojiconEditText) view.findViewById(R.id.et_input);
        ivEmoji = (ImageView) view.findViewById(R.id.iv_emoji);
        ivExtra = (Button) view.findViewById(R.id.iv_extra);
        ivMore = (Button)findViewById(R.id.iv_more);
        btnSend = (Button) view.findViewById(R.id.btn_send);

        ivMore.setOnClickListener(this);
        etInput.setOnClickListener(this);
        ivEmoji.setOnClickListener(this);
        btnSend.setOnClickListener(this);
        etInput.addTextChangedListener(this);
        ivEmoji.setSelected(false);
        ivMore.setSelected(false);

        extraLp = (LayoutParams) extraView.getLayoutParams();
        extraLp.height = keyboardHeight < 200 ? 831 : keyboardHeight;
        extraLp.weight = ViewGroup.LayoutParams.MATCH_PARENT;
        extraView.setLayoutParams(extraLp);
        etInput.setOnKeyBackListener(new EmojiconEditText.OnKeyBackListener() {
            @Override
            public boolean onKeyBack() {
                if (isShowExtra()) {
                    hideExtra();
                    hideSoftInputFromWindow(etInput);
                    ivEmoji.setSelected(false);
                    return true;
                }
                return false;
            }
        });
        etInput.requestFocus();
        etInput.setCursorVisible(false);
    }

    private EmojiconRecentsManager mRecentsManager;


    public void setOnEmojiconClickedListener(EmojiconGridViewLayout.OnEmojiconClickedListener listener) {
        this.onEmojiconClickedListener = listener;
    }
    private void initEmoji() {
        vpExtra = (ViewPager) view.findViewById(R.id.vp_extra);
        EmojiconRecents recents = this;
        EmojiconGridViewLayout emojiconGridViewLayout = new EmojiconGridViewLayout(mContext, People.DATA, recents, this, false);
        setOnEmojiconClickedListener(new EmojiconGridViewLayout.OnEmojiconClickedListener() {
            @Override
            public void onEmojiconClicked(Emojicon emojicon) {
                if (emojicon == null) {
                    return;
                }

                int start = etInput.getSelectionStart();
                int end = etInput.getSelectionEnd();
                if (start < 0) {
                    etInput.append(emojicon.getEmoji());
                } else {
                    etInput.getText()
                            .replace(Math.min(start, end),
                                    Math.max(start, end),
                                    emojicon.getEmoji(),
                                    0,
                                    emojicon.getEmoji()
                                            .length());
                }
            }
        });

        mEmojisAdapter = new EmojisPagerAdapter(Arrays.asList(
                emojiconGridViewLayout
        ));

        mRecentsManager = EmojiconRecentsManager.getInstance(view.getContext());
        int page = mRecentsManager.getRecentPage();
        if (page == 0 && mRecentsManager.size() == 0) {
            page = 1;
        }

        if (page != 0) {
            vpExtra.setCurrentItem(page, false);
        }

        vpExtra.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            int oldPosition = 0;

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
               // pointIndicator.playBy(oldPosition, position);
                oldPosition = position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

    }

    private void initMore() {
        ArrayList<View> viewPagerItems = new ArrayList<>();
        viewPagerItems.clear();
        viewPagerItems.add(getMoreItem());
        vpMoreAdapter = new FaceVPMoreAdapter(viewPagerItems);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.et_input:
                etInput.setCursorVisible(true);
                showSoftInputFromWindow(etInput);
                setEmojiImag(false);
                if (null != mChattingFooterListener) {
                    mChattingFooterListener.OnInEditMode();
                }
                break;
            case R.id.iv_emoji:
                ivEmoji.setSelected(!ivEmoji.isSelected());
                setEmojiImag(ivEmoji.isSelected());
                if (ivEmoji.isSelected()) {
                    extraView.setVisibility(VISIBLE);
                    vpExtra.setAdapter(mEmojisAdapter);
                    hideSoftInputFromWindow(etInput);
                } else {
                    showSoftInputFromWindow(etInput);
                }
                setInputMode();
                if (null != mChattingFooterListener) {
                    mChattingFooterListener.OnInEditMode();
                }
                break;
            case R.id.iv_more:
                ivMore.setSelected(!ivMore.isSelected());
                if(ivMore.isSelected()){
                    hideSoftInputFromWindow(etInput);
                    etInput.setVisibility(VISIBLE);
                    extraView.setVisibility(VISIBLE);
                    vpExtra.setAdapter(vpMoreAdapter);
                }else {
                    showSoftInputFromWindow(etInput);
                }

                setEmojiImag(false);
                setInputMode();
               if (null != mChattingFooterListener) {
                   mChattingFooterListener.OnInEditMode();
               }
                break;
            case R.id.btn_send:
                if (null != mChattingFooterListener) {
                    mChattingFooterListener.OnSendTextMessageRequest(etInput.getText());
                    etInput.setText("");
                }
                break;
            default:
                break;
        }
    }

    private void setEmojiImag(boolean selected) {
        if(selected){
            ivEmoji.setImageResource(R.drawable.chat_softkeyboard);
        }else {
            ivEmoji.setImageResource(R.drawable.chat_emoji);
        }

    }

    private void setInputMode() {
        if (null != mActivity) {
            if (extraView.getVisibility() == VISIBLE) {
                mActivity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);

            } else {
                mActivity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
            }
        }
    }

    private void showSendOrExtra() {
        if (TextUtils.isEmpty(etInput.getText())) {
            ivExtra.setVisibility(VISIBLE);
            btnSend.setVisibility(GONE);
        } else {
            ivExtra.setVisibility(GONE);
            btnSend.setVisibility(VISIBLE);
        }
    }

    private void showSendOrMore() {
        if (TextUtils.isEmpty(etInput.getText())) {
            ivMore.setVisibility(VISIBLE);
            btnSend.setVisibility(GONE);
        } else {
            ivMore.setVisibility(GONE);
            btnSend.setVisibility(VISIBLE);
        }
    }



    public void hideSoftInputFromWindow(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) view
                .getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputMethodManager == null) {
            return;
        }
        inputMethodManager.hideSoftInputFromWindow(view.getApplicationWindowToken(), 0);
    }

    public void showSoftInputFromWindow(View view) {
        Log.d("tag", "keyboardHeight" + keyboardHeight);
        if (extraView.getVisibility() == VISIBLE && keyboardHeight > 200) {
            mActivity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
        } else {
            extraView.setVisibility(GONE);
            mActivity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        }
        InputMethodManager inputMethodManager = (InputMethodManager) view
                .getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputMethodManager == null) {
            return;
        }
        inputMethodManager.showSoftInput(view, InputMethodManager.SHOW_FORCED);

    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        //showSendOrExtra();
        showSendOrMore();
    }

    public static boolean checkDeviceHasNavigationBar(Context activity) {
        int id = activity.getResources().getIdentifier("config_showNavigationBar", "bool", "android");
        return (id > 0 && activity.getResources().getBoolean(id));
    }

    @Override
    public void onGlobalLayout() {
        Rect rootRect = new Rect();
        parentRoot.getWindowVisibleDisplayFrame(rootRect);
        int screenHeight = parentRoot.getRootView()
                .getHeight();
        int heightDifference = screenHeight
                - (rootRect.bottom - rootRect.top);

        if (heightDifference < 350 && heightDifference > 20) {
            extraBarHeight = heightDifference;
        }
        if (heightDifference > 350 && extraBarHeight > 20) {
            keyboardHeight = heightDifference - extraBarHeight;
            extraLp.height = keyboardHeight;
            PrefUtils.setKeyboardHeight(mContext, keyboardHeight);

        }
    }

    public boolean isShowExtra() {
        return extraView.getVisibility() == VISIBLE;
    }

    public void hideExtra() {
        extraView.setVisibility(GONE);
    }

    public long getAvailableSize() {

        File path = Environment.getExternalStorageDirectory(); //取得sdcard文件路径
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long availableBlocks = stat.getAvailableBlocks();
        return (availableBlocks * blockSize) / 1024 / 1024;//  MIB单位
    }

    long currentTimeMillis = 0;

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (getAvailableSize() < 10) {
            Log.d("tag", "sdcard no memory ");
            return false;
        }

        long time = System.currentTimeMillis() - currentTimeMillis;
        if (time <= 300) {
            Log.d("tag", "nvalid click");
            currentTimeMillis = System.currentTimeMillis();
            return false;
        }

        if (!isExistExternalStore()) {
            return false;

        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                break;
            case MotionEvent.ACTION_MOVE:
                break;

        }

        return false;
    }

    /**
     * 是否有外存卡
     *
     * @return
     */
    public static boolean isExistExternalStore() {
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            return true;
        } else {
            return false;
        }
    }

    private View getMoreItem() {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(mContext.LAYOUT_INFLATER_SERVICE);
        View inflate = inflater.inflate(R.layout.layout_more_grid, null);
        GridView gridview = (GridView) inflate.findViewById(R.id.chart_more_gv);
        final List<ChatMore> list = new ArrayList<>();
        list.add(new ChatMore(R.drawable.chat_audiocall, mContext.getString(R.string.chat_audio_call)));
        list.add(new ChatMore(R.drawable.chat_videocall, mContext.getString(R.string.chat_video_call)));

        FaceMoreAdapter adapter = new FaceMoreAdapter(list,mContext);
        gridview.setAdapter(adapter);
        gridview.setNumColumns(3);
        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                String name = list.get(position).getName();
                if(name!=null && name.equals(mContext.getString(R.string.chat_audio_call))){
                    if (null != mChattingFooterListener) {
                        mChattingFooterListener.onP2pCall(false);
                    }
                }else {
                    if (null != mChattingFooterListener) {
                        mChattingFooterListener.onP2pCall(true);
                    }
                }

            }
        });
        return gridview;
    }

    @Override
    public void addRecentEmoji(Context context, Emojicon emojicon) {

    }


    public interface OnChattingFooterListener {

       // void OnVoiceRcdInitReuqest();

      //  void OnVoiceRcdStartRequest();

        /**
         * Called when the voce record button nomal and cancel send voice.
         */
      //  void OnVoiceRcdCancelRequest();

        /**
         * Called when the voce record button nomal and send voice.
         */
      //  void OnVoiceRcdStopRequest();

        void OnSendTextMessageRequest(CharSequence text);

       // void OnUpdateTextOutBoxRequest(CharSequence text);

        //void OnSendCustomEmojiRequest(int emojiid, String emojiName);

        //void OnEmojiDelRequest();

        void OnInEditMode();

        void onPause();

        void onResume();

        void release();

        void onP2pCall(boolean isVideoCall);
    }


}

