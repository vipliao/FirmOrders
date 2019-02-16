package com.firm.order;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportResource;

@SpringBootApplication
//@ImportResource(locations={"classpath:application-quartz.xml"})
public class FirmOrdersBootApplication {

	public static void main(String[] args) {
		SpringApplication.run(FirmOrdersBootApplication.class, args);
	}

}

