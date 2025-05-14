package com.laetienda.webapp_test.module;

public interface SchemaModule {
    void setPort(int port);

    void cycle();
    void login();
    void createBadEditor();
    void addReader();
    void addEditor();
    void removeReader();
}
