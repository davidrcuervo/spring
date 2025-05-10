package com.laetienda.frontend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

@Service("utils")
public class FrontendUtilsServiceImplementation implements FrontendUtilsService {
    final static private Logger log = LoggerFactory.getLogger(FrontendUtilsServiceImplementation.class);

    @Autowired Environment env;

    @Override
    public String href(String index) {
        log.debug("FRONTEND_SERVICE::getApi. $index: {}", index);
        return env.getProperty(index, "#");
    }
}
