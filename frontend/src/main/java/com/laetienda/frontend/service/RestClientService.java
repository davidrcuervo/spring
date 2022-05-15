package com.laetienda.frontend.service;

import com.laetienda.model.user.Usuario;
import org.springframework.stereotype.Service;

@Service
public interface RestClientService {

    public <T> T post(String apiurl, Object data, Class<T> clazz);
    public <T> T find(String apiurl, String id, Class<T> clazz);
    public <T> T findall(String apiurl, Class<T> clazz);
    public <T> T delete(String apiurl, Object data, Class<T> clazz);
    public <T> T modify(String apiurl, Object data, Class<T> clazz);

}
