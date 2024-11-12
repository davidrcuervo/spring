package com.laetienda.schema.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.laetienda.lib.exception.NotValidCustomException;
import com.laetienda.model.schema.DbItem;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Repository;

@Repository
public class SchemaRepositoryImplementation implements SchemaRepository{
    private final static Logger log = LoggerFactory.getLogger(SchemaRepositoryImplementation.class);

    @Autowired
    private ObjectMapper jsonMapper;

    @PersistenceContext
    private EntityManager em;

    @Override
    @Transactional
    public DbItem create(Class<?> clazz, DbItem item) throws NotValidCustomException {
        log.debug("SCHEMA_REPO::create $clazzName: {}", clazz.getName());
        try{
            em.persist(clazz.cast(item));
            return item;
        }catch(Exception ex){
            log.error(ex.getMessage());
            log.trace(ex.getMessage(), ex);
            throw new NotValidCustomException(ex.getMessage(), HttpStatus.BAD_REQUEST, "item");
        }
    }
}
