package com.laetienda.schema.repository;

import com.laetienda.model.schema.DbItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface ItemRepository extends JpaRepository<DbItem, Long> {
    List<DbItem> findByOwner(String username);
    List<DbItem> findByEditors(String editorUserId);
    List<DbItem> findByReaders(String readerUserId);

    @Query(value = """
SELECT DISTINCT u.userId FROM (
    SELECT i.owner AS userId FROM ITEM i WHERE i.id = :itemId
    UNION
    SELECT e.editor AS userId FROM ITEM_EDITOR e WHERE e.ITEM_ID = :itemId
    UNION
    SELECT dg.owner AS userId FROM ITEM_EDITOR_GROUP ieg
        JOIN DB_GROUP dg ON ieg.EDITOR_GROUP_ID = dg.ID
        WHERE ieg.item_id = :itemId
    UNION
    SELECT gm.members AS userId FROM ITEM_EDITOR_GROUP ieg
        JOIN ITEM_GROUP_MEMBER gm ON ieg.EDITOR_GROUP_ID = gm.ITEM_GROUP_ID
        WHERE ieg.ITEM_ID = :itemId
) u
""", nativeQuery = true)
    Set<String> getEditors(@Param("itemId") long itemId);

    @Query(value = """
SELECT DISTINCT u.userId FROM (
    SELECT r.reader AS userId FROM ITEM_READER r WHERE r.ITEM_ID = :itemId
    UNION
    SELECT dg.owner AS userId FROM ITEM_READER_GROUP irg
        JOIN DB_GROUP dg ON irg.READER_GROUP_ID = dg.ID
        WHERE irg.item_id = :itemId
    UNION
    SELECT gm.members AS userId FROM ITEM_READER_GROUP irg
        JOIN ITEM_GROUP_MEMBER gm ON irg.READER_GROUP_ID = gm.ITEM_GROUP_ID
        WHERE irg.ITEM_ID = :itemId
) u
""", nativeQuery = true)
    Set<String> getReaders(@Param("itemId") long itemId);
}
