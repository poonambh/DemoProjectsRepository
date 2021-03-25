package com.psl.adms;

import javax.annotation.PreDestroy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.ComponentScan;

import com.psl.adms.commons.context.SystemUserContext;
import com.psl.adms.commons.context.TenantContext;

@EnableAutoConfiguration(exclude = { DataSourceAutoConfiguration.class })
@SpringBootApplication
@EnableEurekaClient
@ComponentScan("com.psl.adms")
public class WebApplication {

	public static void main(String[] args) {
		SpringApplication.run(WebApplication.class, args);
	}

	@PreDestroy
	public void onExit() {
		TenantContext.clear();
		SystemUserContext.clear();
	}

}
