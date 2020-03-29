package br.com.siecola.gae_exemplo1;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;

@SpringBootApplication
@EnableResourceServer
public class GaeExemplo1Application {

	public static void main(String[] args) {
		SpringApplication.run(GaeExemplo1Application.class, args);
	}

}
