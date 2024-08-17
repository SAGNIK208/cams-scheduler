package io.gupshup.cams_scheduler;

import io.gupshup.cams_scheduler.config.ZooKeeperConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class CamsSchedulerApplication {

	public static void main(String[] args) {
		SpringApplication.run(CamsSchedulerApplication.class, args);
	}

	@Value("${zookeeper.connection.string}")
	private String zkConnectionString;

	@Bean
	public ZooKeeperConfig zooKeeperService() throws Exception {
		return ZooKeeperConfig.getInstance(zkConnectionString);
	}
}
