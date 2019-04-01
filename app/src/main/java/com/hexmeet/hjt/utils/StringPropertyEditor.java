package com.hexmeet.hjt.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.hexmeet.hjt.BaseActivity;
import com.hexmeet.hjt.R;

import org.apache.commons.lang3.StringUtils;

public class StringPropertyEditor extends BaseActivity
{
   private TextView propertyName;
   private EditText propertyValue;

   public static void actionStart(Fragment context, String propertyName, String initValue, int requestCode,
         boolean number)
   {
      Intent intent = new Intent(context.getActivity(), StringPropertyEditor.class);
      intent.putExtra("propertyName", propertyName);
      intent.putExtra("initValue", initValue);
      intent.putExtra("notNull", false);
      intent.putExtra("number", number);
      context.startActivityForResult(intent, requestCode);
   }

   @Override
   protected void onCreate(Bundle savedInstanceState)
   {
      super.onCreate(savedInstanceState);

      requestWindowFeature(Window.FEATURE_NO_TITLE);
      ScreenUtil.initStatusBar(this);
      setContentView(R.layout.string_property_editor);

      final boolean isnumber = getIntent().getBooleanExtra("number", false);
      String propName = getIntent().getStringExtra("propertyName");
      if (propName.equalsIgnoreCase("remark") || propName.equalsIgnoreCase("name"))
      {
         propName = " " + propName;
      }
      propertyName = (TextView) findViewById(R.id.property_name);
      propertyName.setText(getString(R.string.update) + propName);

      findViewById(R.id.back).setOnClickListener(new OnClickListener()
      {
         @Override
         public void onClick(View v)
         {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
            
            finish();
         }
      });

      propertyValue = (EditText) findViewById(R.id.property_value);
      String initValue = getIntent().getStringExtra("initValue");
      if (isnumber)
      {
         propertyValue.setInputType(InputType.TYPE_CLASS_NUMBER);
         // propertyValue.setFilters(new InputFilter[] { new InputFilter.LengthFilter(12) });
      }

      if (initValue == null || initValue.equals(""))
      {
         propertyValue.setHint(getString(R.string.please_enter) + propName);
      }
      else
      {
         propertyValue.setText(initValue);
      }
      propertyValue.setSelection(propertyValue.getText().length());

      findViewById(R.id.right_btn).setOnClickListener(new OnClickListener()
      {
         @Override
         public void onClick(View v)
         {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
            
            final String val = propertyValue.getText().toString().trim();

            if (isnumber && StringUtils.isNotEmpty(val))
            {
               if (val.length() > 12)
               {
                  AlertDialog.Builder builder2 = new AlertDialog.Builder(StringPropertyEditor.this)
                        .setMessage(R.string.conference_password_restrictions).setPositiveButton(R.string.ok,
                              null);
                  AlertDialog dialog = builder2.show();
                  TextView messageView = (TextView) dialog.findViewById(android.R.id.message);
                  messageView.setGravity(Gravity.CENTER);
                  return;
               }
            }
            
            Intent intent = new Intent();
            intent.putExtra("propertyValue", val);
            setResult(RESULT_OK, intent);
            finish();
         }
      });
   }
}
