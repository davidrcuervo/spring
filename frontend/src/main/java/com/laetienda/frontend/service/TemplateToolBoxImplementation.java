package com.laetienda.frontend.service;

import com.laetienda.lib.service.ToolBoxService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@Service("tt")
public class TemplateToolBoxImplementation implements TemplateToolBox {
    final static private Logger log = LoggerFactory.getLogger(TemplateToolBoxImplementation.class);

    final private Environment env;
    final private HttpServletRequest request;
    final private ToolBoxService tb;

    public TemplateToolBoxImplementation(
            Environment environment,
            HttpServletRequest httpServletRequest,
            ToolBoxService toolBoxService
    ) {
        this.env = environment;
        this.request = httpServletRequest;
        this.tb = toolBoxService;
    }

    @Override
    public String href(String index, Object... uriVariables) {
        log.debug("SERVICE_TEMPLATE_TOOL::href | $index: {}", index);
        String urlTemplate = env.getProperty(index, "#");

        return tb.setAddressParams(null, urlTemplate, uriVariables);
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

    @Override
    public List<String> getSegments(){
        log.debug("SERVITE_TEMPLATE::getSegments");

        return buildCurrentUri().getPathSegments().stream()
                .map(s -> s.replace(".html", ""))
                .toList();
    }

    public String getSegmentPath(List<String> segments, int index){
        log.debug("SERVICE_TEMPLATE::getSegmentPath | $index: {}", index);

        StringBuilder result = new StringBuilder();
        for(int c = 0; c <= index; c++ ){
            result.append("/").append(segments.get(c));
        }

        return result.append(".html").toString();
    }

    private UriComponents buildCurrentUri(){
        return UriComponentsBuilder
                .fromUriString(request.getRequestURL().toString())
                .query(request.getQueryString())
                .build();
    }
}
