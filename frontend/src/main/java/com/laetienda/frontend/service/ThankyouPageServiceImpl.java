package com.laetienda.frontend.service;

import com.laetienda.frontend.model.ThankyouPage;
import com.laetienda.frontend.repository.ThankyouPageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpSession;

public class ThankyouPageServiceImpl implements ThankyouPageService{

    @Autowired
    private HttpSession session;
    private ThankyouPageRepository repository;

    public ThankyouPageServiceImpl(ThankyouPageRepository repo){
        repository = repo;
    }

    @Override
    public ThankyouPage find(String key) throws ResponseStatusException {
        ThankyouPage result = (ThankyouPage) session.getAttribute(key);

        if(result == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        return result;
    }

    @Override
    public ThankyouPage set(ThankyouPage entity) {
        session.setAttribute(entity.getKey(), entity);
        return entity;
    }
}
