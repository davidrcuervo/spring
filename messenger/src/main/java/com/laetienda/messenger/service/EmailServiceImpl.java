package com.laetienda.messenger.service;

import com.laetienda.lib.exception.NotValidCustomException;
import com.laetienda.model.messager.EmailMessage;

import jakarta.validation.Validator;
import org.apache.logging.log4j.message.SimpleMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerErrorException;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import java.nio.charset.StandardCharsets;

@Service
public class EmailServiceImpl implements EmailService{
    final private static Logger log = LoggerFactory.getLogger(EmailServiceImpl.class);

    @Autowired
    private JavaMailSender emailSender;

    @Autowired
    private SpringTemplateEngine templateEngine;

//    @Autowired
//    private Validator validator;

    @Value("${spring.mail.username}")
    private String senderAddress;

    @Override
    public void sendMessage(EmailMessage message) throws NotValidCustomException {

        try{
            MimeMessage  mimeMessage = emailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED, StandardCharsets.UTF_8.name());
            Context context = new Context();
            context.setVariable("message", message);
            helper.setFrom(senderAddress);

            for(String to : message.getTo()){
                log.trace("email address to: {}", to);
                helper.setTo(to);
            }

//            helper.setTo(Arrays.toString(message.getTo().toArray()));
            helper.setSubject(message.getSubject());
            String html = templateEngine.process(message.getView(), context);
            helper.setText(html, true);

            log.trace("Sending email: {} with html body: {}", message, html);
            emailSender.send(mimeMessage);

        }catch(MessagingException e){
            String errorMessage = String.format("Failed to send email. $error: %s", e.getMessage());
            log.error(errorMessage);
            log.debug(e.getMessage(), e);
            throw new ServerErrorException(errorMessage, e);
        }
    }

    @Override
    public void testMailer() {
        log.trace("Sending test message");
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("myself@la-etienda.com");
        message.setTo("davidrcuervo@gmail.com");
        message.setSubject("Java Mail test message");
        message.setText("Body of Java Mail test message");
        emailSender.send(message);
    }
}
