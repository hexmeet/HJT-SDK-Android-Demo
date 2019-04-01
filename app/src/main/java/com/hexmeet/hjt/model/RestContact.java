package com.hexmeet.hjt.model;

import java.io.Serializable;

public class RestContact implements Serializable
{
   private static final long serialVersionUID = 4706573860924934965L;
   private int id;
   private String name;
   private int ownerId;
   private String email;
   private String telephone;
   private String cellphone;
   private int userId;
   private long lastModifiedTime;
   private String userName;
   private String orgName;
   private String h323ConfNumber;
   private String sipConfNumber;
   private String pstn;
   private String callNumber;
   private String imageURL;
   private String status;
   
   public int getId()
   {
      return id;
   }
   public void setId(int id)
   {
      this.id = id;
   }
   public String getName()
   {
      return name;
   }
   public void setName(String name)
   {
      this.name = name;
   }
   
   public int getUserId()
   {
      return userId;
   }
   public void setUserId(int userId)
   {
      this.userId = userId;
   }
   public long getLastModifiedTime()
   {
      return lastModifiedTime;
   }
   public void setLastModifiedTime(long lastModifiedTime)
   {
      this.lastModifiedTime = lastModifiedTime;
   }
   public String getEmail()
   {
      return email;
   }
   public void setEmail(String email)
   {
      this.email = email;
   }
   public String getTelephone()
   {
      return telephone;
   }
   public void setTelephone(String telephone)
   {
      this.telephone = telephone;
   }
   public String getCellphone()
   {
      return cellphone;
   }
   public void setCellphone(String cellphone)
   {
      this.cellphone = cellphone;
   }
   public String getH323ConfNumber()
   {
      return h323ConfNumber;
   }
   public void setH323ConfNumber(String confNumber)
   {
      h323ConfNumber = confNumber;
   }
   public String getPstn()
   {
      return pstn;
   }
   public void setPstn(String pstn)
   {
      this.pstn = pstn;
   }
   public String getOrgName()
   {
      return orgName;
   }
   public void setOrgName(String orgName)
   {
      this.orgName = orgName;
   }
   public int getOwnerId()
   {
      return ownerId;
   }
   public void setOwnerId(int ownerId)
   {
      this.ownerId = ownerId;
   }
   public String getUserName()
   {
      return userName;
   }
   public void setUserName(String userName)
   {
      this.userName = userName;
   }
   public String getSipConfNumber()
   {
      return sipConfNumber;
   }
   public void setSipConfNumber(String sipConfNumber)
   {
      this.sipConfNumber = sipConfNumber;
   }
   public String getCallNumber()
   {
      return callNumber;
   }
   public void setCallNumber(String callNumber)
   {
      this.callNumber = callNumber;
   }
   public String getImageURL()
   {
      return imageURL;
   }
   public void setImageURL(String imageURL)
   {
      this.imageURL = imageURL;
   }
   public String getStatus()
   {
      return status;
   }
   public void setStatus(String status)
   {
      this.status = status;
   }
   @Override
   public String toString()
   {
      return "RestContact [id=" + id + ", name=" + name + ", ownerId=" + ownerId + ", email=" + email
            + ", telephone=" + telephone + ", cellphone=" + cellphone + ", userId=" + userId
            + ", lastModifiedTime=" + lastModifiedTime + ", userName=" + userName + ", orgName=" + orgName
            + ", h323ConfNumber=" + h323ConfNumber + ", sipConfNumber=" + sipConfNumber + ", pstn=" + pstn
            + ", callNumber=" + callNumber + ", imageURL=" + imageURL + ", status=" + status + "]";
   }


}
