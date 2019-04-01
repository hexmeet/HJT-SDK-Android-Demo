package com.hexmeet.hjt.utils;

import android.graphics.Bitmap;


public class MyBitmapEntity {
   int x;
   int y;
   int width;
   int height;
   Bitmap bitmap;
   
   public MyBitmapEntity() {}
   
   public MyBitmapEntity(Bitmap bitmap, int x, int y, int w, int h) {
      this.bitmap = bitmap;
      this.x = x;
      this.y = y;
      this.width = w;
      this.height = h;
   }

   public float getX()
   {
      return x;
   }

   public void setX(int x)
   {
      this.x = x;
   }

   public int getY()
   {
      return y;
   }

   public void setY(int y)
   {
      this.y = y;
   }

   public int getWidth()
   {
      return width;
   }

   public void setWidth(int width)
   {
      this.width = width;
   }

   public int getHeight()
   {
      return height;
   }

   public void setHeight(int height)
   {
      this.height = height;
   }

   public Bitmap getBitmap()
   {
      return bitmap;
   }

   public void setBitmap(Bitmap bitmap)
   {
      this.bitmap = bitmap;
   }
   
}
