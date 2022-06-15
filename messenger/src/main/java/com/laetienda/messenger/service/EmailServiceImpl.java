package com.laetienda.messenger.service;

import com.laetienda.model.messager.EmailMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerErrorException;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

@Service
public class EmailServiceImpl implements EmailService{
    final private static Logger log = LoggerFactory.getLogger(EmailServiceImpl.class);

    @Autowired
    private JavaMailSender emailSender;

    @Autowired
    private SpringTemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String senderAddress;

    @Override
    public void sendMessage(EmailMessage message) throws ResponseStatusException {
        try{
            MimeMessage  mimeMessage = emailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED, StandardCharsets.UTF_8.name());
            Context context = new Context();
            context.setVariables(message.getVariables());
            helper.setFrom(senderAddress);
            helper.setTo(Arrays.toString(message.getTo().toArray()));
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
}
