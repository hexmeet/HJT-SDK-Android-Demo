package com.hexmeet.hjt.model;

public class RestContactReq
{
   private int id;
   private String name;
   private Integer userId;
   private String email;
   private String telephone;
   private String cellphone;
   
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
   public Integer getUserId()
   {
      return userId;
   }
   public void setUserId(Integer userId)
   {
      this.userId = userId;
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
   
   @Override
   public String toString()
   {
      return "RestContactReq [id=" + id + ", name=" + name + ", userId=" + userId + ", email=" + email 
      + ", telephone=" + telephone + ", cellphone=" + cellphone + "]";
   }

}
