package com.laetienda.webapp_test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class WebappTestApplication implements CommandLineRunner, ExitCodeGenerator {
	final private static Logger log = LoggerFactory.getLogger(WebappTestApplication.class);

	private int exitCode;

	public static void main(String[] args) {
		SpringApplication.exit(
				SpringApplication.run(WebappTestApplication.class, args));
	}

	@Override
	public void run(String[] args){
		log.trace("Hello World!");
		this.exitCode = 404;
	}

	@Override
	public int getExitCode(){
		log.debug("MAIN::getExitCode. $exitCode: {}", this.exitCode);
		return this.exitCode;
	}

}
