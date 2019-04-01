package com.hexmeet.hjt.model;

import java.io.Serializable;

public class OfflineMessage implements Serializable
{
   private int receiverUserId;
   private long launchTime;
   private boolean videoCall;
   
   public int getReceiverUserId()
   {
      return receiverUserId;
   }
   public void setReceiverUserId(int receiverUserId)
   {
      this.receiverUserId = receiverUserId;
   }
   public long getLaunchTime()
   {
      return launchTime;
   }
   public void setLaunchTime(long launchTime)
   {
      this.launchTime = launchTime;
   }
   public boolean isVideoCall()
   {
      return videoCall;
   }
   public void setVideoCall(boolean videoCall)
   {
      this.videoCall = videoCall;
   }
   @Override
   public String toString()
   {
      return "OfflineMessage [receiverUserId=" + receiverUserId + ", launchTime=" + launchTime + ", videoCall="
            + videoCall + "]";
   }
   
}
