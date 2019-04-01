package com.hexmeet.hjt.model;

public class RestFeedback
{
   private int id;
   private int userId;
   private String userName;
   private String appType;
   private String appVersion;
   private String content;
   private long createTime;

   public int getId()
   {
      return id;
   }

   public void setId(int id)
   {
      this.id = id;
   }

   public int getUserId()
   {
      return userId;
   }

   public void setUserId(int userId)
   {
      this.userId = userId;
   }

   public String getUserName()
   {
      return userName;
   }

   public void setUserName(String userName)
   {
      this.userName = userName;
   }

   public String getAppType()
   {
      return appType;
   }

   public void setAppType(String appType)
   {
      this.appType = appType;
   }

   public String getAppVersion()
   {
      return appVersion;
   }

   public void setAppVersion(String appVersion)
   {
      this.appVersion = appVersion;
   }

   public String getContent()
   {
      return content;
   }

   public void setContent(String content)
   {
      this.content = content;
   }

   public long getCreateTime()
   {
      return createTime;
   }

   public void setCreateTime(long createTime)
   {
      this.createTime = createTime;
   }
}
