package com.laetienda.frontend.lib;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;

public class QuickTests {
    private final static Logger log = LoggerFactory.getLogger(QuickTests.class);

    public void stringReplace(){
        String text = "delete.html?username={username}";
        log.info("{}", text);
        text = text.replaceFirst("\\{username\\}", "admuser");
        log.info("{}", text);
    }
}
