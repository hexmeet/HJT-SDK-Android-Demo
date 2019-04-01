package com.hexmeet.hjt.model;

import java.io.Serializable;

public class RestAppVersionInfo implements Serializable
{
   private static final long serialVersionUID = 4706573860924934965L;
   private String packageName;
   private long publishTime;
   private String downloadUrl;
   private boolean urgentUpgrade;
   private String description;
   
   public String getPackageName()
   {
      return packageName;
   }
   public void setPackageName(String packageName)
   {
      this.packageName = packageName;
   }
   public long getPublishTime()
   {
      return publishTime;
   }
   public void setPublishTime(long publishTime)
   {
      this.publishTime = publishTime;
   }
   public String getDownloadUrl()
   {
      return downloadUrl;
   }
   public void setDownloadUrl(String downloadUrl)
   {
      this.downloadUrl = downloadUrl;
   }
   public boolean isUrgentUpgrade()
   {
      return urgentUpgrade;
   }
   public void setUrgentUpgrade(boolean urgentUpgrade)
   {
      this.urgentUpgrade = urgentUpgrade;
   }
   public String getDescription()
   {
      return description;
   }
   public void setDescription(String description)
   {
      this.description = description;
   }
   @Override
   public String toString()
   {
      return "RestAppVersionInfo [packageName=" + packageName + ", publishTime=" + publishTime
            + ", downloadUrl=" + downloadUrl + ", urgentUpgrade=" + urgentUpgrade + ", description="
            + description + "]";
   }
}
