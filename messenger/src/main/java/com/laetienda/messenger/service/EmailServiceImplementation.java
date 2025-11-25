package com.laetienda.messenger.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.laetienda.lib.exception.NotValidCustomException;
import com.laetienda.model.messager.EmailMessage;

import com.laetienda.utils.service.api.ApiUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.server.ServerErrorException;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import java.nio.charset.StandardCharsets;

@Service
public class EmailServiceImplementation implements EmailService{
    final private static Logger log = LoggerFactory.getLogger(EmailServiceImplementation.class);

    @Autowired private JavaMailSender emailSender;
    @Autowired private SpringTemplateEngine templateEngine;
    @Autowired private ObjectMapper json;
    @Autowired private ApiUser apiUser;

    @Value("${spring.mail.username}")
    private String senderAddress;

    @Override
    public void send(EmailMessage message) throws HttpStatusCodeException {
        log.debug("MAIL_SERVICE::send.");

        try{
            MimeMessage  mimeMessage = emailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED, StandardCharsets.UTF_8.name());
            Context context = new Context();
            buildItem(message, context);
            context.setVariable("message", message);
            context.setVariable("user", apiUser);
            helper.setFrom(senderAddress);

            for(String to : message.getTo()){
                log.trace("MAIL_SERVICE::send. $to: {}", to);
                helper.setTo(to);
            }

            helper.setSubject(message.getSubject());
            String html = templateEngine.process(message.getView(), context);
            helper.setText(html, true);

            log.trace("MAIL_SERVICE::send. $subject: {} | $body: {}", message.getSubject(), html);
            emailSender.send(mimeMessage);

        }catch(MessagingException e){
            String errorMessage = String.format("Failed to send email. $error: %s", e.getMessage());
            log.error("MAIL_SERVICE::send. {}", errorMessage);
            log.debug(errorMessage, e);
            throw new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, errorMessage);
        }
    }

    @Override
    public void test() throws HttpStatusCodeException {
        log.debug("MAIL_SERVICE::test.");

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("myself@la-etienda.com");
            message.setTo("davidrcuervo@gmail.com");
            message.setSubject("Java Mail test message");
            message.setText("Body of Java Mail test message");
            emailSender.send(message);

        }catch(MailException e){
            log.warn("MAIL_SERVICE::test. $error: {}", e.getMessage());
            log.trace(e.getMessage(), e);
            throw new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    private void buildItem(EmailMessage message, Context context) throws HttpStatusCodeException{
        log.trace("EMAIL_SERVICE::buildItem. $item: {}", message.getJsonItem());

        if(message.getJsonItem() != null && message.getClazzName() != null
                && !message.getClazzName().isBlank() && !message.getJsonItem().isBlank()){

            try {
                Class<?> clazz = Class.forName(message.getClazzName());
                log.trace("EMAIL_SERVICE::buildItem $clazzName: {}", message.getClazzName());
                context.setVariable("item", json.readValue(message.getJsonItem(), clazz));

            } catch (ClassNotFoundException | JsonProcessingException e) {
                log.error("EMAIL_SERVICE::buildItem. $error: {}", e.getMessage());
                throw new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
            }
        }
    }
}
