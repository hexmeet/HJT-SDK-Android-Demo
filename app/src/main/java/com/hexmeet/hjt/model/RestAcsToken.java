package com.hexmeet.hjt.model;

public class RestAcsToken
{
   private String acsJsonWebToken;
   private String internalEdgeIp;
   private String externalEdgeIp;

   public String getAcsJsonWebToken()
   {
      return acsJsonWebToken;
   }

   public void setAcsJsonWebToken(String acsJsonWebToken)
   {
      this.acsJsonWebToken = acsJsonWebToken;
   }

   public String getInternalEdgeIp()
   {
      return internalEdgeIp;
   }

   public void setInternalEdgeIp(String internalEdgeIp)
   {
      this.internalEdgeIp = internalEdgeIp;
   }

   public String getExternalEdgeIp()
   {
      return externalEdgeIp;
   }

   public void setExternalEdgeIp(String externalEdgeIp)
   {
      this.externalEdgeIp = externalEdgeIp;
   }

}
