package com.firm.order;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.firm.order.config.jasypt.FirmEncryptablePropertyResolver;
import com.firm.order.config.jasypt.FrimEncryptablePropertyDetector;
import com.ulisesbocchio.jasyptspringboot.EncryptablePropertyDetector;
import com.ulisesbocchio.jasyptspringboot.EncryptablePropertyResolver;

@SpringBootApplication
//@ImportResource(locations={"classpath:application-quartz.xml"})
public class FirmOrdersBootApplication {
	@Bean
    public EncryptablePropertyDetector encryptablePropertyDetector() {
        return new FrimEncryptablePropertyDetector();
    }
	@Bean
	public EncryptablePropertyResolver encryptablePropertyResolver() {
		return new FirmEncryptablePropertyResolver();
	}
	public static void main(String[] args) {
		SpringApplication.run(FirmOrdersBootApplication.class, args);
	}

}

