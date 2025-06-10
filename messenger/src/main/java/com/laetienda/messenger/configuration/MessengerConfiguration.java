package com.laetienda.messenger.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
//import org.thymeleaf.spring5.SpringTemplateEngine;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.thymeleaf.templateresolver.ITemplateResolver;

import java.nio.charset.StandardCharsets;

//@Configuration
public class MessengerConfiguration {

//    @Bean
    public SpringTemplateEngine getSpringTemplateEngine(){
        SpringTemplateEngine springTemplateEngine = new SpringTemplateEngine();
        springTemplateEngine.addTemplateResolver(getEmailTemplateResolver());
        return springTemplateEngine;
    }

    private ClassLoaderTemplateResolver getEmailTemplateResolver() {
        ClassLoaderTemplateResolver emailTemplateResolver = new ClassLoaderTemplateResolver();
        emailTemplateResolver.setPrefix("/templates/");
        emailTemplateResolver.setSuffix(".html");
        emailTemplateResolver.setTemplateMode(TemplateMode.HTML);
        emailTemplateResolver.setCharacterEncoding(StandardCharsets.UTF_8.name());
        emailTemplateResolver.setCacheable(false);
        return emailTemplateResolver;
    }

}
