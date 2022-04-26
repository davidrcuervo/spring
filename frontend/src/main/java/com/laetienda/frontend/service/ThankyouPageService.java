package com.laetienda.frontend.service;

import com.laetienda.frontend.model.ThankyouPage;
import org.springframework.web.server.ResponseStatusException;

public interface ThankyouPageService {

    public ThankyouPage find(String key) throws ResponseStatusException;
    public ThankyouPage set(ThankyouPage entity);
}
