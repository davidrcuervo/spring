package com.laetienda.frontend.service;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

@Service("tt")
public class TemplateToolBoxImplementation implements TemplateToolBox {
    final static private Logger log = LoggerFactory.getLogger(TemplateToolBoxImplementation.class);

    final private Environment env;
    final private HttpServletRequest request;

    public TemplateToolBoxImplementation(
            Environment environment,
            HttpServletRequest httpServletRequest
    ) {
        this.env = environment;
        this.request = httpServletRequest;
    }

    @Override
    public String href(String index) {
        log.debug("SERVICE_TEMPLATE_TOOL::href | $index: {}", index);
        return env.getProperty(index, "#");
    }

    @Override
    public String getUri(){
        return buildCurrentUri().toUriString();
    }

    @Override
    public String getPath(){
        String result = buildCurrentUri().getPath();
        log.debug("SERVICE_TEMPLATE_TOOL::getPath | $path: {}", result);
        return result;
    }

    @Override
    public boolean isActive(String link){
        log.debug("SERVICE_TEMPLATE::isValid | $path: {}", link);
        return getPath().equals(link);
    }

    private UriComponents buildCurrentUri(){
        return UriComponentsBuilder
                .fromUriString(request.getRequestURL().toString())
                .query(request.getQueryString())
                .build();
    }
}
