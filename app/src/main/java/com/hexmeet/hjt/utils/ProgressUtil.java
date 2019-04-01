package com.hexmeet.hjt.utils;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.os.Handler;

import com.hexmeet.hjt.widget.LoadingDialog;

public class ProgressUtil
{
   private static final String TAG = "progress";
   
   private Activity activity;
   private LoadingDialog progressDialog;
   private Handler progressHandler;
   private Runnable progressTask;
   private Handler timeoutHandler;
   private Runnable timeoutTask = null;
   private boolean isTaskScheduled = false;

   public ProgressUtil(Activity _activity, final int timeoutInMillis, final Runnable timeoutCallback, final String hint)
   {
      activity = _activity;
      progressHandler = new Handler();
      timeoutHandler = new Handler();
      progressTask = new Runnable()
      {
         @Override
         public void run()
         {
            if (progressDialog == null)
            {
               progressDialog = new LoadingDialog();
               progressDialog.setHint(hint);
            }

            timeoutTask = new Runnable()
            {
               @Override
               public void run()
               {
                  timeoutTask = null;
                  
                  if (timeoutCallback != null && !activity.isDestroyed())
                  {
                     activity.runOnUiThread(timeoutCallback);
                  }
                  
                  dismiss();
               }
            };
            timeoutHandler.postDelayed(timeoutTask, timeoutInMillis <= 1000 ? 1000 : timeoutInMillis);
            if (!activity.isDestroyed())
            {
               progressDialog.show(activity.getFragmentManager(), TAG);
            }
         }
      };
   }

   public synchronized void show()
   {
      if (!isTaskScheduled)
      {
         progressHandler.post(progressTask);
         isTaskScheduled = true;
      }
   }

   public synchronized void showDelayed(int delayInMillis)
   {
      if (!isTaskScheduled)
      {
         progressHandler.postDelayed(progressTask, delayInMillis < 100 ? 100 : delayInMillis);
         isTaskScheduled = true;
      }
   }

   public synchronized void dismiss()
   {
      if (isTaskScheduled)
      {
         progressHandler.removeCallbacks(progressTask);
         isTaskScheduled = false;
         if (timeoutTask != null)
         {
            timeoutHandler.removeCallbacks(timeoutTask);
         }
      }

      if (!activity.isDestroyed())
      {
         Fragment frag = activity.getFragmentManager().findFragmentByTag(TAG);
         if (frag != null)
         {
            ((DialogFragment) frag).dismissAllowingStateLoss();
         }
      }
   }
}
