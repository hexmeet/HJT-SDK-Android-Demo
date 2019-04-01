package com.hexmeet.hjt.me;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.hexmeet.hjt.BaseActivity;
import com.hexmeet.hjt.HjtApp;
import com.hexmeet.hjt.R;
import com.hexmeet.hjt.cache.SystemCache;
import com.hexmeet.hjt.call.PasswordDialog;
import com.hexmeet.hjt.event.AvatarUploadEvent;
import com.hexmeet.hjt.event.RenameEvent;
import com.hexmeet.hjt.login.Login;
import com.hexmeet.hjt.model.RestLoginResp;
import com.hexmeet.hjt.utils.Utils;
import com.hexmeet.hjt.widget.MenuItem;
import com.hexmeet.hjt.widget.PopupMenuBottom;
import org.apache.log4j.Logger;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class MeDetailActivity extends BaseActivity {
    public Logger LOG = Logger.getLogger(this.getClass());
    private ImageView avatar;
    private TextView username;
    private TextView displayName;
    private TextView department;
    private TextView company;
    private TextView cellphone;
    private TextView email;
    private PopupMenuBottom popuMenu;
    private File avatarTempCaptureFile;
    private static final int REQUEST_CODE_FROM_CAMERA = 110;
    private static final int REQUEST_CODE_FROM_GALLERY = 111;
    private static final int REQUEST_CODE_CLIP_DONE = 112;
    private static final int REQUEST_PERMISSION_CAMERA = 120;
    private static final int REQUEST_PERMISSION_GALLERY = 130;
    private String cropFilePath;

    public static void actionStart(Context context) {
        Intent intent = new Intent(context, MeDetailActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.me_details);
        if (savedInstanceState != null && savedInstanceState.getSerializable("filePath") != null) {
            avatarTempCaptureFile = new File((String) savedInstanceState.getSerializable("filePath"));
        }

        username = (TextView) findViewById(R.id.account);
        displayName = (TextView) findViewById(R.id.display_name);
        cellphone = (TextView) findViewById(R.id.phone);
        company = (TextView) findViewById(R.id.company);
        email = (TextView) findViewById(R.id.email);
        department = (TextView) findViewById(R.id.department);


        findViewById(R.id.me_name).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                showDialogInput();
            }
        });

        findViewById(R.id.back_btn).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        avatar = (ImageView) findViewById(R.id.avatar);
        findViewById(R.id.me_avatar).setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopuMenu();
            }
        });

        popuMenu = new PopupMenuBottom(this);
        popuMenu.setHint(getString(R.string.logout_commit));
        popuMenu.addItem(new MenuItem(this, getString(R.string.ok), Color.parseColor("#F57070"), 0));
        popuMenu.setItemOnClickListener(new PopupMenuBottom.OnItemOnClickListener() {
            @Override
            public void onItemClick(MenuItem item, int position) {
                if (position == 0) {
                    setResult(13);
                    HjtApp.getInstance().getAppService().logout();
                    LOG.info("android sdk : "+Build.VERSION.SDK_INT);
                    if(Build.VERSION.SDK_INT<=19){
                        Intent intent = new Intent(MeDetailActivity.this, Login.class);
                        startActivity(intent);
                    }
                    finish();
                }
            }
        });

        Button logout = (Button) findViewById(R.id.logout);
        logout.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                popuMenu.show(v);
            }
        });
        Utils.loadAvatar(avatar);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (SystemCache.getInstance().getLoginResponse() != null) {
            RestLoginResp loginResp = SystemCache.getInstance().getLoginResponse();
            displayName.setText(loginResp.getDisplayName());
            username.setText(loginResp.getUsername());
            cellphone.setText(TextUtils.isEmpty(loginResp.getCellphone()) ? loginResp.getTelephone() : loginResp.getCellphone());
            email.setText(loginResp.getEmail());
            company.setText(loginResp.getOrg());
            department.setText(SystemCache.getInstance().getDepartment());
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
        if(dialog != null) {
            dialog.dismiss();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putSerializable("filePath", Environment.getExternalStorageDirectory() + "/avatar_cap.jpg");
        super.onSaveInstanceState(outState);
    }

    private void showPopuMenu() {
        final AlertDialog dlg = new AlertDialog.Builder(this).create();
        dlg.show();

        Window window = dlg.getWindow();
        window.setContentView(R.layout.alertdialog);
        window.getDecorView().setBackgroundColor(Color.TRANSPARENT);

        TextView from_camera = (TextView) window.findViewById(R.id.tv_content1);
        TextView from_gallery = (TextView) window.findViewById(R.id.tv_content2);

        dlg.setCanceledOnTouchOutside(true);
        from_camera.setText(R.string.avatar_from_camera);
        from_gallery.setText(R.string.avatar_from_gallery);
        from_gallery.setTextColor(Color.parseColor("#232323"));

        from_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPermission(true);
                dlg.cancel();
            }
        });

        from_gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPermission(false);
                dlg.cancel();
            }
        });
    }

    private void checkPermission(boolean takeCamera) {
        int permission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permission != PackageManager.PERMISSION_GRANTED && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, takeCamera ? REQUEST_PERMISSION_CAMERA : REQUEST_PERMISSION_GALLERY);
        } else {
            if(takeCamera) {
                goToCameraCapture();
            } else {
                goToGallerySelector();
            }
        }
    }

    private void goToCameraCapture() {
        avatarTempCaptureFile = new File(Environment.getExternalStorageDirectory() + "/avatar_cap.jpg");
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, getFileUri(avatarTempCaptureFile));
        startActivityForResult(intent, REQUEST_CODE_FROM_CAMERA);
    }

    private void goToGallerySelector() {
        Intent intent = new Intent(Intent.ACTION_PICK, null);
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        startActivityForResult(intent, REQUEST_CODE_FROM_GALLERY);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSION_CAMERA:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    goToCameraCapture();
                } else {
                    Utils.showToast(MeDetailActivity.this, R.string.permission_warning);
                }
                break;
            case REQUEST_PERMISSION_GALLERY:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    goToGallerySelector();
                } else {
                    Utils.showToast(MeDetailActivity.this, R.string.permission_warning);
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CODE_FROM_CAMERA:
                    if (avatarTempCaptureFile != null && avatarTempCaptureFile.exists()) {
                        clipPhoto(getFileUri(avatarTempCaptureFile));
                    }
                    break;
                case REQUEST_CODE_FROM_GALLERY:
                    clipPhoto(data.getData());
                    break;
                case REQUEST_CODE_CLIP_DONE:
                    if (avatarTempCaptureFile != null && avatarTempCaptureFile.exists()) {
                        avatarTempCaptureFile.delete();
                    }
                    saveAvatar(data);
                    break;
                default:
                    break;
            }
        }
    }

    private Uri getFileUri(File file) {
        Uri uri;
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            uri = Uri.fromFile(file);
        } else {
            uri = FileProvider.getUriForFile(MeDetailActivity.this, "com.hexmeet.hjt.fileprovider", file);
        }
        return uri;
    }

    private void clipPhoto(Uri uri) {
        try {
            Uri destUri = getFileUri(getCropFile());
            Intent intent = new Intent("com.android.camera.action.CROP");
            intent.setDataAndType(uri, "image/*");

            grantWritePermission(intent, destUri);
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }

            intent.putExtra("crop", "true");
            intent.putExtra("aspectX", 1);
            intent.putExtra("aspectY", 1);
            intent.putExtra("outputX", 240);
            intent.putExtra("outputY", 240);
            intent.putExtra("return-data", false);
            intent.putExtra(MediaStore.EXTRA_OUTPUT,  destUri);
            intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.ordinal());
            intent.putExtra("noFaceDetection", true);//

            startActivityForResult(intent, REQUEST_CODE_CLIP_DONE);
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
            Utils.showToast(this, R.string.upload_failed);
        }
    }

    private void grantWritePermission(Intent intent, Uri uri) {
        List resInfoList = queryActivityByIntent(this, intent);
        if (resInfoList.size() == 0) {
            Utils.showToast(this, R.string.no_photo_editor);
            return;
        }
        for (Object aResInfoList : resInfoList) {
            ResolveInfo resolveInfo = (ResolveInfo) aResInfoList;
            String packageName = resolveInfo.activityInfo.packageName;
            grantUriPermission(packageName, uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        }
    }

    private static List<ResolveInfo> queryActivityByIntent(Activity activity, Intent intent){
        return activity.getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
    }

    private void saveAvatar(Intent picData) {
//        Bundle extras = picData.getExtras();
        if(cropFilePath != null) {
            File cropFile = new File(cropFilePath);
            if(cropFile.exists()) {
                HjtApp.getInstance().getAppService().uploadAvatar(cropFilePath);
            } else {
                onAvatarUploadEvent(new AvatarUploadEvent(false, "Crop file not exist"));
            }
        } else {
            onAvatarUploadEvent(new AvatarUploadEvent(false, "no Extras back from scale image"));
        }
    }

    private File getCropFile() throws IOException {
        File file = new File(Environment.getExternalStorageDirectory(), "/avatar_scale.jpg");
        if(file.exists()) {
            file.delete();
        }
        file.createNewFile();
        cropFilePath = file.getAbsolutePath();
        return file;
    }

    private PasswordDialog dialog;
    private String name;
    private void showDialogInput() {
        name = displayName.getText().toString();
        if(dialog == null) {
            dialog = new PasswordDialog.Builder(MeDetailActivity.this)
                    .setInputWatcher(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                        }

                        @Override
                        public void onTextChanged(CharSequence s, int start, int before, int count) {

                        }

                        @Override
                        public void afterTextChanged(Editable s) {
                            name = s.toString();
                        }
                    })
                    .setNegativeButton(getString(R.string.cancel), new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                        }
                    })
                    .setPositiveButton(getString(R.string.ok), new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(TextUtils.isEmpty(name)) {
                                Utils.showToast(MeDetailActivity.this, R.string.name_not_empty);
                            } else {
                                dialog.dismiss();
                                if(!name.equals(displayName.getText().toString())) {
                                    updateDisplayName(name);
                                }
                            }

                        }
                    }).setTitle(getString(R.string.update_name)).setInitValue(name, InputType.TYPE_CLASS_TEXT, getString(R.string.hint_input_new_name))
                    .createTwoButtonDialog();
        }
        dialog.show();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onAvatarUploadEvent(AvatarUploadEvent event) {
        if(event.isSuccess()) {
            Utils.showToast(this, R.string.upload_sucess);
            Utils.loadAvatar(avatar);
            if(!TextUtils.isEmpty(event.getAvatarFilePath())) {
                HjtApp.getInstance().getAppService().updateUserImage(event.getAvatarFilePath());
            }
        } else {
            Utils.showToast(this, R.string.upload_failed);
            LOG.error("avatar upload failed: ["+event.getMessage()+"]");
        }
    }

    private void updateDisplayName(final String newDisplayName) {
        HjtApp.getInstance().getAppService().userRename(newDisplayName);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRenameEvent(RenameEvent event) {
        if(event.isSuccess()) {
            Log.i("==setname",event.isSuccess()+"");
            Utils.showToast(this, R.string.rename_sucess);
            displayName.setText(SystemCache.getInstance().getLoginResponse().getDisplayName());
        } else {
            Utils.showToast(this, R.string.rename_failed);
            LOG.error("User rename failed: ["+event.getMessage()+"]");
        }
    }
}
