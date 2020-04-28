package com.hexmeet.hjt.me;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.andreabaccega.widget.FormEditText;
import com.hexmeet.hjt.BaseActivity;
import com.hexmeet.hjt.HjtApp;
import com.hexmeet.hjt.R;
import com.hexmeet.hjt.event.CallEvent;
import com.hexmeet.hjt.event.FeedbackEvent;
import com.hexmeet.hjt.event.LogPathEvent;
import com.hexmeet.hjt.me.SelectPicturesUtils.FullyGridLayoutManager;
import com.hexmeet.hjt.me.SelectPicturesUtils.GlideEngine;
import com.hexmeet.hjt.me.SelectPicturesUtils.GridImageAdapter;
import com.hexmeet.hjt.me.SelectPicturesUtils.OnItemClickListener;
import com.hexmeet.hjt.utils.NetworkUtil;
import com.hexmeet.hjt.utils.ProgressUtil;
import com.hexmeet.hjt.utils.Utils;
import com.luck.picture.lib.PictureSelector;
import com.luck.picture.lib.config.PictureConfig;
import com.luck.picture.lib.config.PictureMimeType;
import com.luck.picture.lib.decoration.GridSpacingItemDecoration;
import com.luck.picture.lib.entity.LocalMedia;
import com.luck.picture.lib.listener.OnResultCallbackListener;
import com.luck.picture.lib.permissions.PermissionChecker;
import com.luck.picture.lib.style.PictureParameterStyle;
import com.luck.picture.lib.style.PictureWindowAnimationStyle;
import com.luck.picture.lib.tools.PictureFileUtils;
import com.luck.picture.lib.tools.ScreenUtils;

import org.apache.log4j.Logger;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FeedbackActivity extends BaseActivity implements View.OnClickListener {

    private Logger LOG = Logger.getLogger(FeedbackActivity.class);
    private FormEditText mobile;
    private Button submit;
    ProgressUtil  progressUtil;

    public static void actionStart(Context context) {
        Intent intent = new Intent(context, FeedbackActivity.class);
        context.startActivity(intent);
    }

    private ImageView mFeedbackBtn;
    private TextView mTextCount;
    private EditText mEtContent;
    private RecyclerView mRecycler;
    private static final int MAX_COUNT = 200;
    private PictureWindowAnimationStyle mWindowAnimationStyle;
    private GridImageAdapter mAdapter;
    private int maxSelectNum = 5;
    private PictureParameterStyle mPictureParameterStyle;
    private TextView mTextPhoto;
    List<String> filePath = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
        if (savedInstanceState == null) {
            clearCache();
        }
        setContentView(R.layout.activity_feedback);
        mFeedbackBtn = (ImageView) findViewById(R.id.feedback_btn);
        mTextCount = (TextView) findViewById(R.id.text_count);
        mEtContent = (EditText) findViewById(R.id.et_content);
        mRecycler = (RecyclerView) findViewById(R.id.recycler);
        mTextPhoto = (TextView)findViewById(R.id.photo_text);
        submit = (Button)findViewById(R.id.feedback_submit);
        mobile = (FormEditText) findViewById(R.id.mobile);
        mobile.setTextSize(HjtApp.isCnVersion() ? 15 : 11);

        mEtContent.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                mTextCount.setText((MAX_COUNT - s.length()+"/"+MAX_COUNT));
            }
        });

        mobile.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                mTextCount.setText((MAX_COUNT - s.length()+"/"+MAX_COUNT));
            }
        });

        mFeedbackBtn.setOnClickListener(this);
        submit.setOnClickListener(this);

        initPhoto();
    }

    private void initPhoto() {
        FullyGridLayoutManager manager = new FullyGridLayoutManager(this,
                4, GridLayoutManager.VERTICAL, false);
        mRecycler.setLayoutManager(manager);

        mRecycler.addItemDecoration(new GridSpacingItemDecoration(4,
                ScreenUtils.dip2px(this, 8), false));
        mAdapter = new GridImageAdapter(getContext(), onAddPicClickListener);
        getStyle();
        //相册上弹动画
        mWindowAnimationStyle = new PictureWindowAnimationStyle();
        mWindowAnimationStyle.ofAllAnimation(R.anim.picture_anim_up_in, R.anim.picture_anim_down_out);
        mAdapter.setSelectMax(maxSelectNum);
        mRecycler.setAdapter(mAdapter);

        mAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(int position, View v) {
                List<LocalMedia> selectList = mAdapter.getData();
                if (selectList.size() > 0) {
                    LocalMedia media = selectList.get(position);
                    String mimeType = media.getMimeType();
                    int mediaType = PictureMimeType.getMimeType(mimeType);
                    if(mediaType==1){
                        PictureSelector.create(FeedbackActivity.this)
                                .themeStyle(R.style.picture_default_style) // xml设置主题
                                .setPictureStyle(mPictureParameterStyle)// 动态自定义相册主题
                                .setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)// 设置相册Activity方向，不设置默认使用系统
                                .loadImageEngine(GlideEngine.createGlideEngine())// 外部传入图片加载引擎，必传项
                                .openExternalPreview(position, selectList);
                    }

                }
            }
        });
    }

    private GridImageAdapter.onAddPicClickListener onAddPicClickListener = new GridImageAdapter.onAddPicClickListener() {
        @Override
        public void onAddPicClick() {
                PictureSelector.create(FeedbackActivity.this)
                        .openGallery(PictureMimeType.ofImage())// 全部.PictureMimeType.ofAll()、图片.ofImage()、视频.ofVideo()、音频.ofAudio()
                        .loadImageEngine(GlideEngine.createGlideEngine())// 外部传入图片加载引擎，必传项
                        .theme(R.style.picture_style)// 主题样式设置 具体参考 values/styles   用法：R.style.picture.white.style v2.3.3后 建议使用setPictureStyle()动态方式
                        .isWeChatStyle(true)// 是否开启微信图片选择风格
                        .isUseCustomCamera(false)// 是否使用自定义相机
                        .setLanguage(-1)// 设置语言，默认中文
                        .setPictureStyle(mPictureParameterStyle)// 动态自定义相册主题
                        .setPictureWindowAnimationStyle(mWindowAnimationStyle)// 自定义相册启动退出动画
                        .isWithVideoImage(true)// 图片和视频是否可以同选,只在ofAll模式下有效
                        .maxSelectNum(5)// 最大图片选择数量
                        .minSelectNum(1)// 最小选择数量
                        .imageSpanCount(4)// 每行显示个数
                        .isReturnEmpty(false)// 未选择数据时点击按钮是否可以返回
                        .setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)// 设置相册Activity方向，不设置默认使用系统
                        .selectionMode(true ? PictureConfig.MULTIPLE : PictureConfig.SINGLE)// 多选 or 单选
                        .isSingleDirectReturn(true)// 单选模式下是否直接返回，PictureConfig.SINGLE模式下有效
                        .previewImage(true)// 是否可预览图片
                        .isCamera(true)// 是否显示拍照按钮
                        .isZoomAnim(true)// 图片列表点击 缩放效果 默认true
                        .compress(true)// 是否压缩
                        .synOrAsy(true)//同步true或异步false 压缩 默认同步
                        .hideBottomControls(false)// 是否显示uCrop工具栏，默认不显示
                        .selectionMedia(mAdapter.getData())// 是否传入已选图片
                        .cutOutQuality(90)// 裁剪输出质量 默认100
                        .minimumCompressSize(100)// 小于100kb的图片不压缩

                        .forResult(new OnResultCallbackListener() {
                            @Override
                            public void onResult(List<LocalMedia> result) {
                                LOG.info("localmedia : "+result.size());
                                for (LocalMedia media : result) {
                                    if(media.getAndroidQToPath()!= null){
                                        filePath.add(media.getAndroidQToPath());
                                    }else {
                                        filePath.add(media.getCompressPath());
                                    }
                                }
                                if(result.size()>0){
                                    mTextPhoto.setVisibility(View.GONE);
                                }
                                mAdapter.setList(result);
                                mAdapter.notifyDataSetChanged();
                            }

                        });


        }
    };

    private void getStyle()  {
        mPictureParameterStyle = new PictureParameterStyle();
        mPictureParameterStyle.isChangeStatusBarFontColor = false; // 是否改变状态栏字体颜色(黑白切换)
        mPictureParameterStyle.isOpenCompletedNumStyle = false;// 是否开启右下角已完成(0/9)风格
        mPictureParameterStyle.isOpenCheckNumStyle = true; // 是否开启相册数字选择风格
        mPictureParameterStyle.pictureStatusBarColor = Color.parseColor("#393a3e"); // 状态栏背景色
        mPictureParameterStyle.pictureTitleBarBackgroundColor = Color.parseColor("#393a3e"); // 相册列表标题栏背景色
        mPictureParameterStyle.pictureContainerBackgroundColor = ContextCompat.getColor(getContext(), R.color.title_bar); // 相册父容器背景色
        mPictureParameterStyle.pictureTitleUpResId = R.drawable.picture_icon_wechat_up;// 相册列表标题栏右侧上拉箭头
        mPictureParameterStyle.pictureTitleDownResId = R.drawable.picture_icon_wechat_down;// 相册列表标题栏右侧下拉箭头
        mPictureParameterStyle.pictureLeftBackIcon = R.drawable.picture_icon_close;// 相册返回箭头
        mPictureParameterStyle.pictureTitleTextColor = ContextCompat.getColor(getContext(), R.color.picture_color_white); // 标题栏字体颜色
        mPictureParameterStyle.pictureRightDefaultTextColor = ContextCompat.getColor(getContext(), R.color.picture_color_53575e);// 相册右侧按钮字体默认颜色
        mPictureParameterStyle.pictureRightSelectedTextColor = ContextCompat.getColor(getContext(), R.color.picture_color_white);// 相册右侧按可点击字体颜色
        mPictureParameterStyle.pictureUnCompleteBackgroundStyle = R.drawable.picture_send_button_two;// 相册右侧按钮背景样式
        mPictureParameterStyle.pictureCompleteBackgroundStyle = R.drawable.picture_send_button;// 相册右侧按钮可点击背景样式
        mPictureParameterStyle.pictureCheckedStyle = R.drawable.picture_wechat_num_selector; // 相册列表勾选图片样式
        mPictureParameterStyle.pictureWeChatTitleBackgroundStyle = R.drawable.picture_album_bg; // 相册标题背景样式
        mPictureParameterStyle.pictureWeChatChooseStyle = R.drawable.picture_wechat_select_cb; // 预览右下角样式
        mPictureParameterStyle.pictureWeChatLeftBackStyle = R.drawable.picture_icon_back;  // 相册返回箭头
        mPictureParameterStyle.pictureBottomBgColor = ContextCompat.getColor(getContext(), R.color.picture_color_grey); // 相册列表底部背景色
        mPictureParameterStyle.picturePreviewTextColor = ContextCompat.getColor(getContext(), R.color.picture_color_white);// 相册列表底下预览文字色值(预览按钮可点击时的色值)
        mPictureParameterStyle.pictureUnPreviewTextColor = ContextCompat.getColor(getContext(), R.color.picture_color_9b); // 相册列表底下不可预览文字色值(预览按钮不可点击时的色值)
        mPictureParameterStyle.pictureCompleteTextColor = ContextCompat.getColor(getContext(), R.color.picture_color_white);// 相册列表已完成色值
        mPictureParameterStyle.pictureUnCompleteTextColor = ContextCompat.getColor(getContext(), R.color.picture_color_53575e); // 相册列表未完成色值
        mPictureParameterStyle.picturePreviewBottomBgColor = ContextCompat.getColor(getContext(), R.color.picture_color_half_grey); // 预览界面底部背景色
        mPictureParameterStyle.pictureExternalPreviewDeleteStyle = R.drawable.picture_icon_delete;// 外部预览界面删除按钮样式
        mPictureParameterStyle.pictureExternalPreviewGonePreviewDelete = true;// 外部预览界面是否显示删除按钮
        mPictureParameterStyle.pictureNavBarColor = Color.parseColor("#393a3e");// 设置NavBar Color SDK Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP有效
    }

    public Context getContext() {
        return this;
    }

    private void clearCache() {
        // 清空图片缓存，包括裁剪、压缩后的图片 注意:必须要在上传完成后调用 必须要获取权限
        if (PermissionChecker.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            PictureFileUtils.deleteAllCacheDirFile(getContext());
        } else {
            PermissionChecker.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PictureConfig.APPLY_STORAGE_PERMISSIONS_CODE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case PictureConfig.CHOOSE_REQUEST:
                    List<LocalMedia> selectList = PictureSelector.obtainMultipleResult(data);
                    mAdapter.setList(selectList);
                    mAdapter.notifyDataSetChanged();
                    break;
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PictureConfig.APPLY_STORAGE_PERMISSIONS_CODE:
                for (int i = 0; i < grantResults.length; i++) {
                    if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                        PictureFileUtils.deleteCacheDirFile(getContext(), PictureMimeType.ofImage());
                    } else {
                       /* Toast.makeText(MainActivity.this,
                                getString(R.string.picture_jurisdiction), Toast.LENGTH_SHORT).show();*/
                    }
                }
                break;
        }
    }

    @Override
    public void onClick(View v) {
        if(v.getId()==R.id.feedback_btn){
            onBackPressed();
        }else if(v.getId()==R.id.feedback_submit){
            boolean isNetworkOk = NetworkUtil.isNetConnected(this);
            if(!isNetworkOk){
                Utils.showToast(this, R.string.network_unconnected);
            }else {
                setLoading();
                logFile();
            }
        }
    }
    private void setLoading() {
        progressUtil = new ProgressUtil(this, 60000, new Runnable() {
            @Override
            public void run() {
                Utils.showToast(HjtApp.getInstance().getContext(), R.string.feedback_fail);
                progressUtil.dismiss();
            }
        }, getString(R.string.feedback_process));
        progressUtil.show();
    }

    private void logFile() {
        // 附件
        File file1 = new File(Environment.getExternalStorageDirectory().toString() + "/crash"
                + "/hjt_crash.log");
        File file2 = new File(Environment.getExternalStorageDirectory().toString() + "/crash"
                + "/hjt_crash.log1");
        File uilog = new File(Environment.getExternalStorageDirectory().toString() + "/crash"
                + "/hjt_app.log");
        // add sdk logs
        String sdkPath = HjtApp.getInstance().getAppService().obtainLogPath();
        File fileSDK = new File(sdkPath);
        String emSdkLog = HjtApp.getInstance().getAppService().getEmSdkLog();
        File fileEmSDK = new File(emSdkLog);

        if (file1.exists()) {
            filePath.add(String.valueOf(file1));
        }
        if (file2.exists()) {
            filePath.add(String.valueOf(file2));
        }
        if (uilog.exists()) {
            filePath.add(String.valueOf(uilog));
        }
        if (fileSDK.exists()) {
            // create sdk log file if not existed.
            File crashdir = new File(Environment.getExternalStorageDirectory().toString() + "/crash");
            if (!crashdir.exists()) {
                crashdir.mkdirs();
            }
            // move to external directory
            File fileSDKTemp = new File(Environment.getExternalStorageDirectory().toString() + "/crash"
                    + "/hjt_sdk.gz");
            if (Utils.copyFile(fileSDK, fileSDKTemp)) {
                LOG.info("re_diagnosis onClick, move " + fileSDK.getPath() + " to " + fileSDKTemp.getPath()
                        + " succeed.");
                filePath.add(String.valueOf(fileSDKTemp));
            } else {
                LOG.warn("re_diagnosis onClick, move " + fileSDK.getPath() + " to " + fileSDKTemp.getPath()
                        + " failed.");
            }
        }

        if (fileEmSDK.exists()) {
            File crashdir = new File(Environment.getExternalStorageDirectory().toString() + "/crash");
            if (!crashdir.exists()) {
                crashdir.mkdirs();
            }

            File fileEmSDKTemp = new File(Environment.getExternalStorageDirectory().toString() + "/crash"
                    + "/hjt_emsdk.gz");
            if (Utils.copyFile(fileEmSDK, fileEmSDKTemp)) {
                LOG.info("re_diagnosis onClick, move " + fileSDK.getPath() + " to " + fileEmSDKTemp.getPath()
                        + " succeed.");
                filePath.add(String.valueOf(fileEmSDKTemp));
            } else {
                LOG.warn("re_diagnosis onClick, move " + fileSDK.getPath() + " to " + fileEmSDKTemp.getPath()
                        + " failed.");
            }
        }

        for (int i = 0; i< filePath.size(); i++){
            LOG.info("FILEPAHT :" + filePath.get(i));
        }

        HjtApp.getInstance().getAppService().feedbackFiles(filePath, mEtContent.getText().toString(),mobile.getText().toString());
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onFeedbackEvent(FeedbackEvent event) {
        if(event!=null && event.getNumber()==100){
            Utils.showToast(HjtApp.getInstance().getContext(), R.string.feedback_successful);
            progressUtil.dismiss();
            onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }
}
