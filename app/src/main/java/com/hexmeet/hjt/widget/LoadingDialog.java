package com.hexmeet.hjt.widget;

import android.app.Dialog;
import android.app.DialogFragment;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.hexmeet.hjt.R;

public class LoadingDialog extends DialogFragment
{
   private AnimationDrawable animation;
   private String hint = "Loading";

   public void setHint(String hint)
   {
      this.hint = hint;
   }

   @Override
   public Dialog onCreateDialog(Bundle savedInstanceState)
   {
      LayoutInflater inflater = getActivity().getLayoutInflater();
      View view = inflater.inflate(R.layout.dialog_loading, null);

      ((TextView) view.findViewById(R.id.hint)).setText(this.hint);

      ImageView imageView = (ImageView) view.findViewById(R.id.anim);
      animation = (AnimationDrawable) imageView.getBackground();

      Dialog dialog = new Dialog(getActivity(), R.style.dialog);
      dialog.setContentView(view);
      WindowManager.LayoutParams lp = dialog.getWindow().getAttributes();
      lp.dimAmount = 0.1f;
      dialog.getWindow().setAttributes(lp);
      dialog.setCanceledOnTouchOutside(false);
      
      animation.start();

      return dialog;
   }
}
