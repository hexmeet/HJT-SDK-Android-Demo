package com.hexmeet.hjt.utils;

import android.content.Context;

import org.apache.log4j.Logger;

import java.util.Locale;

public class LanguageUtil extends android.support.v4.app.FragmentActivity {
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
