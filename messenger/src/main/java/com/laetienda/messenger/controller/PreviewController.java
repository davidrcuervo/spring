package com.laetienda.messenger.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
@RequestMapping("/api/v0/email/preview")
public class PreviewController {
    final private static Logger log = LoggerFactory.getLogger(PreviewController.class);

    @GetMapping("/{templateName}")
    public String getTemplate(@PathVariable String templateName, @RequestParam Map<String, String> variables){
        log.trace("Running email preview");
        String template = variables.containsKey("template") ? variables.get("template") : "default";
        String result = String.format("%s/%s", template, templateName);
        log.trace("$path: {}", result);
        return result;
    }
}
