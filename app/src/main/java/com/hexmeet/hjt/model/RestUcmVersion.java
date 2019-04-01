package com.hexmeet.hjt.model;

public class RestUcmVersion
{
   private String version;

   public String getVersion() {
      return version;
   }

   public void setVersion(String version) {
      this.version = version;
   }

   @Override
   public String toString()
   {
      return "RestUcmVersion [version=" + version + "]";
   }
   
}
