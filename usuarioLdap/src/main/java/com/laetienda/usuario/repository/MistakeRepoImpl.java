package com.laetienda.usuario.repository;

import com.laetienda.lib.model.Mistake;

import java.util.*;

public class MistakeRepoImpl implements MistakeRepository{

    private Mistake mistake;
    private Map<String, List<String>> mistakes;

    public MistakeRepoImpl (){
        mistake = new Mistake();
        mistakes = new HashMap<String, List<String>>();
        mistake.setErrors(mistakes);
        mistake.setStatus(500);
        mistake.setTimestamp(new Date());
    }

    public MistakeRepoImpl(int status){
        mistake = new Mistake();
        mistakes = new HashMap<String, List<String>>();
        mistake.setErrors(mistakes);
        mistake.setStatus(status);
        mistake.setTimestamp(new Date());
    }

    @Override
    public void addMistake(String name, String message) {
        List<String> errors = mistakes.get(name);

        if(errors == null) {
            errors = new ArrayList<String>();
            mistakes.put(name, errors);
        }
        errors.add(message);
    }

    @Override
    public Mistake get(){
        return mistake;
    }
}
