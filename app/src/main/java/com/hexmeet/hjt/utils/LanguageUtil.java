package com.hexmeet.hjt.utils;

import android.content.Context;

import org.apache.log4j.Logger;

import java.util.Locale;

import androidx.fragment.app.FragmentActivity;

public class LanguageUtil extends FragmentActivity {
   private static Logger log = Logger.getLogger(LanguageUtil.class);

   public static boolean isEn(Context context){
      Locale locale = context.getResources().getConfiguration().locale;
      String language = locale.getLanguage();
      if (language.endsWith("en")) {
         log.warn("The current locale is English.");
         return true;
      }
      else {
         return false;
      }
   }
}
