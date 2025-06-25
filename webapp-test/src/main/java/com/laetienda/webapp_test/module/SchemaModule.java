package com.laetienda.webapp_test.module;

import com.laetienda.lib.exception.NotValidCustomException;

public interface SchemaModule {
    void setPort(int port);

    void cycle() throws NotValidCustomException;
    void login();
    void createBadEditor();
    void addReader() throws NotValidCustomException;
    void addEditor() throws NotValidCustomException;
    void removeReader() throws NotValidCustomException;
}
