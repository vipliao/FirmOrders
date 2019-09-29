package com.firm.order.config.context;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.stereotype.Component;

/**
 * Properties specific to Interface.
 * <p>
 * Properties are configured in the application.yml file.
 */

//@Configuration(value="applicationProperties")
@ConfigurationProperties(
        prefix = "application",
        ignoreUnknownFields = false
)
@Data
public class ApplicationProperties {
  /*  public ApplicationProperties(){

    }*/

    //private static ApplicationProperties applicationProperties;

    private String serverPrivatekey;
    private String clientPublickey;

   /* public String getServerPrivatekey() {
        return serverPrivatekey;
    }

    public void setServerPrivatekey(String serverPrivatekey) {
        this.serverPrivatekey = serverPrivatekey;
    }

    public String getClientPublickey() {
        return clientPublickey;
    }

    public void setClientPublickey(String clientPublickey) {
        this.clientPublickey = clientPublickey;
    }*/

   /*public static ApplicationProperties initConfig() {
        if (applicationProperties == null) {
            applicationProperties = SpringContextHolder.getBean("applicationProperties", ApplicationProperties.class);
        }
        return applicationProperties;
    }*/


}