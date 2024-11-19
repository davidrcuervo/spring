package com.laetienda.schema.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.laetienda.lib.exception.NotValidCustomException;
import com.laetienda.model.schema.DbItem;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Repository;

import java.util.Map;

@Repository
public class SchemaRepositoryImplementation implements SchemaRepository{
    private final static Logger log = LoggerFactory.getLogger(SchemaRepositoryImplementation.class);

    @Autowired
    private ObjectMapper jsonMapper;

    @PersistenceContext
    private EntityManager em;

    @Override
    @Transactional
    public <T> T create(Class<T> clazz, DbItem item) throws NotValidCustomException {
        log.debug("SCHEMA_REPO::create $clazzName: {}", clazz.getName());
        try{
            em.persist(item);
            return clazz.cast(item);
        }catch(Exception ex){
            log.error(ex.getMessage());
            log.trace(ex.getMessage(), ex);
            throw new NotValidCustomException(ex.getMessage(), HttpStatus.BAD_REQUEST, "item");
        }
    }

    @Override
    @Transactional
    public <T> T update(Class<T> clazz, DbItem item) throws NotValidCustomException {
        log.debug("SCHEMA_REPO::update $clazzName: {}", clazz.getName());
        T temp = clazz.cast(item);
        temp = em.merge(temp);
        em.persist(temp);
        return temp;
    }

    @Override
    public <T> T find(Class<T> clazz, Map<String, String> body) {
        log.debug("SCHEMA_REPO::find $clazzName: {}", clazz.getName());
        Map.Entry<String, String> entry = body.entrySet().stream().findFirst().get();
        String key = entry.getKey();
        String value = entry.getValue();

        String query = String.format("SELECT t FROM %s t WHERE t.%s = :value", clazz.getName(), key);
        log.debug("SCHEMA_REPO::find $query: {}", query);
        TypedQuery<T> jpaQuery = em.createQuery(query, clazz);
        jpaQuery.setParameter("value", value);

        try{
            return jpaQuery.getSingleResult();
        }catch(NoResultException ex){
            return null;
        }
    }

    @Override
    @Transactional
    public <T> void delete(Class<T> clazz, T item) {
        log.debug("SCHEMA_REPO::delete $clazzName: {}", clazz.getName());
        em.remove(item);
    }

    @Override
    public <T> T findById(Long id, Class<T> clazz) throws NotValidCustomException {
        log.debug("SCHEMA_REPO::findById $clazz: {}", clazz.getName());
        String query = String.format("SELECT t FROM %s t WHERE t.id = :id", clazz.getName());
        log.debug("SCHEMA_REPO::findById. $query: {}", query);

        TypedQuery<T> jpaQuery = em.createQuery(query, clazz);
        jpaQuery.setParameter("id", id);

        try {
            return jpaQuery.getSingleResult();
        }catch(NoResultException ex){
            log.debug("SCHEMA_REPO::findById. $error: {}", ex.getMessage());
            return null;
        }
    }
}
