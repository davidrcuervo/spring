package com.laetienda.frontend.service;

import com.laetienda.model.user.Usuario;
import org.springframework.web.client.RestTemplate;

public interface UserService {

    public Usuario find(String username);
    public Usuario post(Usuario user);
    public Usuario delete(Usuario user);
    public Usuario modify(Usuario user);
}
