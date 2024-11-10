package com.laetienda.schema.service;

import com.laetienda.model.schema.DbItem;
import com.laetienda.schema.repository.ItemRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ItemServiceImplementation implements ItemService{
    private final static Logger log = LoggerFactory.getLogger(ItemServiceImplementation.class);

    @Autowired private ItemRepository itemRepo;

    @Override
    public DbItem create(DbItem item) {
        return itemRepo.save(item);
    }
}
