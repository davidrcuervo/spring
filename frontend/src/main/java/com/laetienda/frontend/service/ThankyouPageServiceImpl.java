package com.laetienda.frontend.service;

import com.laetienda.frontend.model.ThankyouPage;
import com.laetienda.frontend.repository.ThankyouPageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpSession;

public class ThankyouPageServiceImpl implements ThankyouPageService{
    final private static Logger log = LoggerFactory.getLogger(ThankyouPageServiceImpl.class);

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
        log.debug("Adding thankyou page key to session attribures. $key: {}", entity.getKey());
        session.setAttribute(entity.getKey(), entity);
        return entity;
    }

    @Override
    public ThankyouPage get(String key) {
        ThankyouPage result = (ThankyouPage) session.getAttribute(key);

        if(result == null){
//            log.debug("There is no thankyou page token in session attributes. $key: {}", key);
        }else{
//            log.debug("Getting thankyou page attribute. $key: {}", result.getKey());
            session.removeAttribute(key);
        }

        return result;
    }
}
