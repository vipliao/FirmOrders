package com.firm.order.utils;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class PropDescriptor {
   private Class<?> beanType;
   private Class<?> propType;
   private String name;
   private String baseName;

   public PropDescriptor(Class<?> beanType, Class<?> propType, String propName) {
      if(beanType == null) {
         throw new IllegalArgumentException("Bean Class can not be null!");
      } else if(propName == null) {
         throw new IllegalArgumentException("Bean Property name can not be null!");
      } else {
         this.propType = propType;
         this.beanType = beanType;
         this.name = propName;
         if(this.name.startsWith("m_") && this.name.length() > 2) {
            this.baseName = this.capitalize(this.name.substring(2));
         } else {
            this.baseName = this.capitalize(propName);
         }

      }
   }

   private String capitalize(String str) {
      return this.changeFirstCharacterCase(true, str);
   }

   private String uncapitalize(String str) {
      return this.changeFirstCharacterCase(false, str);
   }

   private String changeFirstCharacterCase(boolean capitalize, String str) {
      int strLen;
      if(str != null && (strLen = str.length()) != 0) {
         StringBuffer buf = new StringBuffer(strLen);
         if(capitalize) {
            buf.append(Character.toUpperCase(str.charAt(0)));
         } else {
            buf.append(Character.toLowerCase(str.charAt(0)));
         }

         buf.append(str.substring(1));
         return buf.toString();
      } else {
         return str;
      }
   }

   public synchronized Method getReadMethod(Class<?> currBean) {
      String readMethodName = null;
      if(this.propType != Boolean.TYPE && this.propType != null) {
         readMethodName = "get" + this.baseName;
      } else {
         readMethodName = "is" + this.baseName;
      }

      Class classStart = currBean;
      if(currBean == null) {
         classStart = this.beanType;
      }

      Method readMethod = this.findMemberMethod(classStart, readMethodName, 0, (Class[])null);
      if(readMethod == null && readMethodName.startsWith("is")) {
         readMethodName = "get" + this.baseName;
         readMethod = this.findMemberMethod(classStart, readMethodName, 0, (Class[])null);
      }

      if(readMethod != null) {
         int mf = readMethod.getModifiers();
         if(!Modifier.isPublic(mf)) {
            return null;
         }

         Class retType = readMethod.getReturnType();
         if(!this.propType.isAssignableFrom(retType)) {
            ;
         }
      }

      return readMethod;
   }

   public synchronized Method getWriteMethod(Class<?> currBean) {
      String writeMethodName = null;
      if(this.propType == null) {
         this.propType = this.findPropertyType(this.getReadMethod(currBean), (Method)null);
      }

      if(writeMethodName == null) {
         writeMethodName = "set" + this.baseName;
      }

      Class classStart = currBean;
      if(currBean == null) {
         classStart = this.beanType;
      }

      Method writeMethod = this.findMemberMethod(classStart, writeMethodName, 1, this.propType == null?null:new Class[]{this.propType});
      if(writeMethod != null) {
         int mf = writeMethod.getModifiers();
         if(!Modifier.isPublic(mf) || Modifier.isStatic(mf)) {
            writeMethod = null;
         }
      }

      return writeMethod;
   }

   private Class<?> findPropertyType(Method readMethod, Method writeMethod) {
      Class propertyType = null;
      if(readMethod != null) {
         propertyType = readMethod.getReturnType();
      }

      if(propertyType == null && writeMethod != null) {
         Class[] params = writeMethod.getParameterTypes();
         propertyType = params[0];
      }

      return propertyType;
   }

   private Method findMemberMethod(Class<?> beanClass, String mName, int num, Class[] args) {
      Method foundM = null;
      Method[] methods = beanClass.getDeclaredMethods();
      if(methods.length < 0) {
         return null;
      } else {
         Method[] arg6 = methods;
         int arg7 = methods.length;

         for(int arg8 = 0; arg8 < arg7; ++arg8) {
            Method method = arg6[arg8];
            if(method.getName().equalsIgnoreCase(mName)) {
               Class[] paramTypes = method.getParameterTypes();
               if(paramTypes.length == num) {
                  boolean match = true;

                  for(int i = 0; i < num; ++i) {
                     if(!args[i].isAssignableFrom(paramTypes[i])) {
                        match = false;
                        break;
                     }
                  }

                  if(match) {
                     foundM = method;
                     break;
                  }
               }
            }
         }

         if(foundM == null && beanClass.getSuperclass() != null) {
            foundM = this.findMemberMethod(beanClass.getSuperclass(), mName, num, args);
         }

         return foundM;
      }
   }

   public String getName() {
      return this.name;
   }
}