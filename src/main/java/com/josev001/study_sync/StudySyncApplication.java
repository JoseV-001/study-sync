package com.josev001.study_sync;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;


@ConfigurationPropertiesScan
@SpringBootApplication
public class StudySyncApplication {

	public static void main(String[] args) {
		SpringApplication.run(StudySyncApplication.class, args);
	}

}
