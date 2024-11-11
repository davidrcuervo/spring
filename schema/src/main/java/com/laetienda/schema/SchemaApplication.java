package com.laetienda.schema;

import com.laetienda.utils.service.api.SchemaApi;
import com.laetienda.utils.service.api.SchemaApiImplementation;
import com.laetienda.utils.service.api.UserApi;
import com.laetienda.utils.service.api.UserApiImplementation;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class SchemaApplication {

	public static void main(String[] args) {
		SpringApplication.run(SchemaApplication.class, args);
	}

}
