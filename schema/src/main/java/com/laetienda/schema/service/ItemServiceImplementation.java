package com.laetienda.schema.service;

import com.laetienda.lib.exception.NotValidCustomException;
import com.laetienda.model.schema.DbItem;
import com.laetienda.schema.repository.ItemRepository;
import com.laetienda.utils.service.api.UserApi;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import java.security.Principal;

@Service
public class ItemServiceImplementation implements ItemService{
    private final static Logger log = LoggerFactory.getLogger(ItemServiceImplementation.class);

    @Autowired private ItemRepository itemRepo;
    @Autowired private HttpServletRequest request;
    @Autowired private UserApi userApi;

    @Value("${admuser.username}")
    private String admUser;

    @Value("${admuser.password}")
    private String admUserPassword;

    @Override
    public DbItem create(DbItem item) throws NotValidCustomException {
        String username = request.getUserPrincipal().getName();
        log.debug("ITEM_SERVICE::create $username: {}", username);
        readersAndEditorsExists(item);
        item.setOwner(username);
        item.addEditor(username);
        return itemRepo.save(item);
    }

    private void readersAndEditorsExists(DbItem item) throws NotValidCustomException {

        try {
            ((UserApi)userApi.setCredentials(admUser, admUserPassword)).startSession();
            item.getEditors().forEach((editor) -> {
                userApi.findByUsername(editor);
            });

            item.getReaders().forEach((reader) -> {
                userApi.findByUsername(reader);
            });

        }catch(HttpClientErrorException ex){
            log.debug("ITEM_SERVICE::verifyReadersAndEditors $error: {}", ex.getMessage());
            throw new NotValidCustomException(ex.getMessage(), ex.getStatusCode(), "item");

        }finally{
            userApi.endSession();
        }
    }

    private Boolean canEdit(DbItem item){
        String username = request.getUserPrincipal().getName();
        return item.getEditors().contains(username);
    }

    private Boolean canRead(DbItem item){
        String username = request.getUserPrincipal().getName();
        return item.getReaders().contains(username);
    }
}
