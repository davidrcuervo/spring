package com.laetienda.schema.repository;

import com.laetienda.model.schema.DbItem;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemRepository extends CrudRepository<DbItem, Long> {
    List<DbItem> findByOwner(String username);
    List<DbItem> findByEditors(String editorUserId);
    List<DbItem> findByReaders(String readerUserId);
}
