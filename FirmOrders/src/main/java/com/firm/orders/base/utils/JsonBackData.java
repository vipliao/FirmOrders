package com.firm.orders.base.utils;

public class JsonBackData {
   private boolean success;
   private String backMsg;
   private Object backData;

   public JsonBackData() {
      this.success = true;
   }

   public boolean isSuccess() {
      return this.success;
   }

   public void setSuccess(boolean success) {
      this.success = success;
   }

   public String getBackMsg() {
      return this.backMsg;
   }

   public void setBackMsg(String backMsg) {
      this.backMsg = backMsg;
   }

   public Object getBackData() {
      return this.backData;
   }

   public void setBackData(Object backData) {
      this.backData = backData;
   }

   private JsonBackData(boolean success, String backMsg, Object backData) {
      this.success = success;
      this.backMsg = backMsg;
      this.backData = backData;
   }

   public static JsonBackData success() {
      return new JsonBackData(true, (String)null, (Object)null);
   }

   public static JsonBackData success(String msg) {
      return new JsonBackData(true, msg, (Object)null);
   }

   public static JsonBackData successRtnObject(Object data) {
      return new JsonBackData(true, (String)null, data);
   }

   public static JsonBackData fail() {
      return new JsonBackData(false, (String)null, (Object)null);
   }

   public static JsonBackData fail(String msg) {
      return new JsonBackData(false, msg, (Object)null);
   }

   public static JsonBackData failRtnObject(Object data) {
      return new JsonBackData(false, (String)null, data);
   }
}