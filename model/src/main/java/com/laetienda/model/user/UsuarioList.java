package com.laetienda.model.user;

import java.util.HashMap;
import java.util.Map;

public class UsuarioList {

    private Map<String, Usuario> users;

    public UsuarioList(){
        users = new HashMap<>();
    }

    public UsuarioList(Map<String, Usuario> users) {
        this.users = users;
    }

    public Map<String, Usuario> getUsers() {
        return users;
    }

    public void setUsers(Map<String, Usuario> users) {
        this.users = users;
    }

    public UsuarioList addUser(Usuario user){
        if(users == null){
            users = new HashMap<>();
        }

        if(!users.containsKey(user.getUsername())){
            users.put(user.getUsername(), user);
        }

        return this;
    }
}
