package com.laetienda.company.controller;

import com.laetienda.company.service.FriendService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("${api.company.folder}") //api/v0/company
public class FriendController {
    private final static Logger log = LoggerFactory.getLogger(FriendController.class);

    @Autowired private FriendService service;
}
