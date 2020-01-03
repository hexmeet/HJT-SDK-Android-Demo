package com.hexmeet.hjt.chat;

import android.content.Context;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.hexmeet.hjt.R;
import com.sj.emoji.DefEmoticons;
import com.sj.emoji.EmojiBean;
import com.sj.emoji.EmojiDisplay;

import org.apache.log4j.Logger;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collections;

import sj.keyboard.adpater.EmoticonsAdapter;
import sj.keyboard.adpater.PageSetAdapter;
import sj.keyboard.data.EmoticonEntity;
import sj.keyboard.data.EmoticonPageEntity;
import sj.keyboard.data.EmoticonPageSetEntity;
import sj.keyboard.interfaces.EmoticonClickListener;
import sj.keyboard.interfaces.EmoticonDisplayListener;
import sj.keyboard.interfaces.PageViewInstantiateListener;
import sj.keyboard.utils.EmoticonsKeyboardUtils;
import sj.keyboard.utils.imageloader.ImageBase;
import sj.keyboard.widget.EmoticonPageView;
import sj.keyboard.widget.EmoticonsEditText;

public class SimpleCommonUtils {
    private static Logger LOG = Logger.getLogger(SimpleCommonUtils.class);

    public static void initEmoticonsEditText(EmoticonsEditText etContent) {
    etContent.addEmoticonFilter(new EmojiFilter());
    etContent.addEmoticonFilter(new XhsFilter());
}

        public static EmoticonClickListener getCommonEmoticonClickListener(final EditText editText) {
            return new EmoticonClickListener() {
                @Override
                public void onEmoticonClick(Object o, int actionType, boolean isDelBtn) {
                    if (isDelBtn) {
                        SimpleCommonUtils.delClick(editText);
                    } else {
                        if (o == null) {
                            return;
                        }
                        if (actionType == ChatContentActivity.EMOTICON_CLICK_TEXT) {
                            String content = null;
                            if (o instanceof EmojiBean) {
                                content = ((EmojiBean) o).emoji;
                            } else if (o instanceof EmoticonEntity) {
                                content = ((EmoticonEntity) o).getContent();
                            }

                            if (TextUtils.isEmpty(content)) {
                                return;
                            }
                            int index = editText.getSelectionStart();
                            Editable editable = editText.getText();
                            editable.insert(index, content);
                        }
                    }
                }
            };
        }

        public static PageSetAdapter sCommonPageSetAdapter;

        public static PageSetAdapter getCommonAdapter(Context context, EmoticonClickListener emoticonClickListener) {

            if(sCommonPageSetAdapter != null){
                return sCommonPageSetAdapter;
            }

            PageSetAdapter pageSetAdapter = new PageSetAdapter();

            addEmojiPageSetEntity(pageSetAdapter, context, emoticonClickListener);
            addTestPageSetEntity(pageSetAdapter, context);

            return pageSetAdapter;
        }

        /**
         * 插入emoji表情集
         *
         * @param pageSetAdapter
         * @param context
         * @param emoticonClickListener
         */
        public static void addEmojiPageSetEntity(PageSetAdapter pageSetAdapter, Context context, final EmoticonClickListener emoticonClickListener) {
            ArrayList<EmojiBean> emojiArray = new ArrayList<>();
            LOG.info("emojiArray : "+emojiArray.size());
            Collections.addAll(emojiArray, DefEmoticons.sEmojiArray);
            EmoticonPageSetEntity emojiPageSetEntity
                    = new EmoticonPageSetEntity.Builder()
                    .setLine(3)
                    .setRow(7)
                    .setEmoticonList(emojiArray)
                    .setIPageViewInstantiateItem(getDefaultEmoticonPageViewInstantiateItem(new EmoticonDisplayListener<Object>() {
                        @Override
                        public void onBindView(int position, ViewGroup parent, EmoticonsAdapter.ViewHolder viewHolder, Object object, final boolean isDelBtn) {
                            final EmojiBean emojiBean = (EmojiBean) object;
                            if (emojiBean == null && !isDelBtn) {
                                return;
                            }

                            viewHolder.ly_root.setBackgroundResource(com.keyboard.view.R.drawable.bg_emoticon);

                            if (isDelBtn) {
                                viewHolder.iv_emoticon.setImageResource(R.drawable.chat_del_emoji);
                            } else {
                                viewHolder.iv_emoticon.setImageResource(emojiBean.icon);
                            }

                            viewHolder.rootView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if (emoticonClickListener != null) {
                                        emoticonClickListener.onEmoticonClick(emojiBean, ChatContentActivity.EMOTICON_CLICK_TEXT, isDelBtn);
                                    }
                                }
                            });
                        }
                    }))
                    .setShowDelBtn(EmoticonPageEntity.DelBtnStatus.LAST)
                    .setIconUri(ImageBase.Scheme.DRAWABLE.toUri("icon_emoji"))
                    .build();
            pageSetAdapter.add(emojiPageSetEntity);
        }

        /**
         * 测试页集
         *
         * @param pageSetAdapter
         * @param context
         */
        public static void addTestPageSetEntity(PageSetAdapter pageSetAdapter, Context context) {
           /* PageSetEntity pageSetEntity = new PageSetEntity.Builder()
                    .addPageEntity(new PageEntity(new SimpleAppsGridView(context)))
                    .setIconUri(R.drawable.chat_del_emoji)
                    .setShowIndicator(false)
                    .build();
            pageSetAdapter.add(pageSetEntity);*/
        }


        @SuppressWarnings("unchecked")
        public static Object newInstance(Class _Class, Object... args) throws Exception {
            return newInstance(_Class, 0, args);
        }

        @SuppressWarnings("unchecked")
        public static Object newInstance(Class _Class, int constructorIndex, Object... args) throws Exception {
            Constructor cons = _Class.getConstructors()[constructorIndex];
            return cons.newInstance(args);
        }

        public static void delClick(EditText editText) {
            int action = KeyEvent.ACTION_DOWN;
            int code = KeyEvent.KEYCODE_DEL;
            KeyEvent event = new KeyEvent(action, code);
            editText.onKeyDown(KeyEvent.KEYCODE_DEL, event);
        }

        public static PageViewInstantiateListener<EmoticonPageEntity> getDefaultEmoticonPageViewInstantiateItem(final EmoticonDisplayListener<Object> emoticonDisplayListener) {
            return getEmoticonPageViewInstantiateItem(EmoticonsAdapter.class, null, emoticonDisplayListener);
        }

        public static PageViewInstantiateListener<EmoticonPageEntity> getEmoticonPageViewInstantiateItem(final Class _class, final EmoticonClickListener onEmoticonClickListener, final EmoticonDisplayListener<Object> emoticonDisplayListener) {
            return new PageViewInstantiateListener<EmoticonPageEntity>() {
                @Override
                public View instantiateItem(ViewGroup container, int position, EmoticonPageEntity pageEntity) {
                    if (pageEntity.getRootView() == null) {
                        EmoticonPageView pageView = new EmoticonPageView(container.getContext());
                        pageView.setNumColumns(pageEntity.getRow());
                        pageEntity.setRootView(pageView);
                        try {
                            EmoticonsAdapter adapter = (EmoticonsAdapter) newInstance(_class, container.getContext(), pageEntity, onEmoticonClickListener);
                            if (emoticonDisplayListener != null) {
                                adapter.setOnDisPlayListener(emoticonDisplayListener);
                            }
                            pageView.getEmoticonsGridView().setAdapter(adapter);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    return pageEntity.getRootView();
                }
            };
        }

        public static void spannableEmoticonFilter(TextView tv_content, String content) {
            SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(content);

            Spannable spannable = EmojiDisplay.spannableFilter(tv_content.getContext(),
                    spannableStringBuilder,
                    content,
                    EmoticonsKeyboardUtils.getFontHeight(tv_content));

            spannable = XhsFilter.spannableFilter(tv_content.getContext(),
                    spannable,
                    content,
                    EmoticonsKeyboardUtils.getFontHeight(tv_content),
                    null);
            tv_content.setText(spannable);
        }
}

