package com.laetienda.utils.service.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.laetienda.lib.service.ToolBoxService;
import com.laetienda.model.schema.DbItem;
import com.laetienda.model.schema.ItemTypeA;
import com.laetienda.utils.lib.Attention;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static org.springframework.security.oauth2.client.web.client.RequestAttributeClientRegistrationIdResolver.clientRegistrationId;

@Component
public class ApiSchemaImplementation extends ApiRestClientImplementation implements ApiSchema{
    private final static Logger log = LoggerFactory.getLogger(ApiSchemaImplementation.class);

    @Value("${kc.client-registration-id.webapp}")
    private String webappClientId;

    @Value("${api.schema.create.uri}")
    private String createUri;

    @Value("${api.schema.find.uri}")
    private String findUri;

    @Value("${api.schema.isItemValid.uri}")
    private String isItemValidUri;

    @Value("${api.schema.findById.uri}")
    private String findByIdUri;

    @Value("${api.schema.findByQuery.uri}")
    private String findByQueryUri;

    @Value("${api.schema.findAll.uri}")
    private String findAllUri;

    @Value("${api.schema.deleteById.uri}")
    private String deleteByIdUri;

    @Value("${api.schema.update.uri}")
    private String updateUri;

    @Value("${api.schema.find.readers.uri}")
    private String findReadersUri;

    @Value("${api.schema.find.editors.uri}")
    private String findEditorsUri;

    private final ObjectMapper json;
    private final ToolBoxService tb;
    private final RestClient client;

    public ApiSchemaImplementation(
            RestClient restClient,
            ObjectMapper objectMapper,
            ToolBoxService toolBoxService
    ){
        super(restClient, toolBoxService);
        this.json = objectMapper;
        this.tb = toolBoxService;
        this.client = restClient;
    }

    @Override
    public <T extends DbItem> T create(Class<T> clazz, T item) throws HttpStatusCodeException {
        log.debug("SCHEMA_API::create. $clazz: {}", clazz.getName());
        return super.post(clazz, item, null, createUri, getClazzName(clazz));
    }

    public <T extends DbItem> T create(Class<T> clazz, T item, String token) throws HttpStatusCodeException {
        return super.post(clazz, item,
                a -> a.put("jwtToken", token),
                createUri, getClazzName(clazz));
    }

    @Override
    public <T extends DbItem> T find(Class<T> clazz, Map<String, String> body) throws HttpStatusCodeException {
        log.debug("SCHEMA_API::find. $clazz: {} | $address: {}", clazz.getName(), findUri);
        return super.post(clazz, body, null, findUri, getClazzName(clazz));
    }

    @Override
    public <T extends DbItem> T findByServiceId(Class<T> clazz, Map<String, String> body) throws HttpStatusCodeException {
        return super.post(
                clazz, body,
                clientRegistrationId(webappClientId),
                findUri, getClazzName(clazz));
    }

    @Override
    public <T extends DbItem> long isItemValid(Class<T> clazz, Long itemId) throws HttpStatusCodeException {
        log.debug("SCHEMA_API::isItemValid. $itemId: {} | $clazz: {} | $address: {}", itemId, clazz.getName(), isItemValidUri);

        String result = super.get(null, isItemValidUri, itemId, getClazzName(clazz));

        try{
            return Long.parseLong(result);
        }catch(NumberFormatException e){
            log.error(
                    "API_SCHEMA::isItemValid. {}",
                    Attention.PARSE_LONG_EXCEPTION.getError(e.getMessage())
            );
            throw new HttpServerErrorException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    Attention.PARSE_LONG_EXCEPTION.getMessage(e.getMessage())
            );
        }
    }

    @Override
    public <T extends DbItem> T findById(Class<T> clazz, Long id) throws HttpStatusCodeException {
        log.debug("SCHEMA_API::findById. $id: {} | $clazz: {} | $address: {}", id, clazz.getName(), findByIdUri);
        return super.get(clazz, null, findByIdUri, id, getClazzName(clazz));
    }

    @Override
    public <T extends DbItem> List<T> findAll(Class<T> clazz, Map<String, String> params) throws HttpStatusCodeException {
        log.debug("API_SCHEMA::findAll | $clazz: {} | $address: {}", clazz.getName(), findAllUri);
        String address = tb.setAddressParams(params, findAllUri, getClazzName(clazz));
        return super.getList(clazz, null, address, null);
    }

    @Override
    public <T extends DbItem> List<T> findAllWithToken(Class<T> clazz, Map<String, String> params, String token) throws HttpStatusCodeException {
        log.debug("API_SCHEMA::findAllWithToken | $clazz: {} | $address: {}", clazz.getName(), findAllUri );
        return super.getList(
                clazz,
                a->a.put("jwtToken", token),
                findAllUri, params, getClazzName(clazz)
        );
    }

    @Override
    public <T extends DbItem> List<T> findByQuery(Class<T> clazz, Map<String, String> body) throws HttpStatusCodeException {
        log.debug("API_SCHEMA::findByQuery. $clazz: {} | $address: {}", clazz.getName(), findByQueryUri);

        try {
            String result = super.post(
                    json.writeValueAsString(body),
                    null,
                    findByQueryUri, getClazzName(clazz)
            );

            return json.readValue(
                    result,
                    json.getTypeFactory().constructCollectionType(List.class, clazz)
            );
        } catch (JsonProcessingException e) {
            log.warn("API_SCHEMA::findByQuery. $error: {} ", e.getMessage());
            throw new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @Override
    public <T extends DbItem> List<T> findByQueryByClientRegistrationId(Class<T> clazz, Map<String, String> body) throws HttpStatusCodeException {
        log.debug("API_SCHEMA::findByQueryByClientRegistrationId. $clazz: {} | $address: {}", clazz.getName(), findByQueryUri);

        try {
            String result = super.post(
                    json.writeValueAsString(body),
                    clientRegistrationId(webappClientId),
                    findByQueryUri, getClazzName(clazz)
            );

            return json.readValue(result,
                    json.getTypeFactory().constructCollectionType(List.class, clazz)
            );
        } catch (JsonProcessingException e) {
            log.warn("API_SCHEMA::findByQueryByClientRegistrationId. $error: {} ", e.getMessage());
            throw new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());

        }
    }

    @Override
    public <T extends DbItem> void delete(Class<T> clazz, Map<String, String> body) throws HttpClientErrorException {
    }

    @Override
    public <T extends DbItem> void deleteById(Class<T> clazz, Long id) throws HttpStatusCodeException {
        log.debug("SCHEMA_API::deleteById. $idStr: {} | $clazz: {} | $address: {}", id, clazz.getName(), deleteByIdUri);

        super.delete(
                null,
                deleteByIdUri,
                id, getClazzName(clazz));
    }

    @Override
    public <T extends DbItem> void deleteById(Class<T> clazz, Long id, String token) throws HttpStatusCodeException {

        super.delete(
                a -> a.put("jwtToken", token),
                deleteByIdUri,
                id,
                getClazzName(clazz));
    }

    @Override
    public <T extends DbItem> T update(Class<T> clazz, DbItem item) throws HttpStatusCodeException {
        log.debug("SCHEMA_API::update. $clazz: {} | $address: {}", clazz.getName(), updateUri);
        return super.put(clazz, clazz.cast(item), null, updateUri, getClazzName(clazz));
    }

    @Override
    public <T extends DbItem> String getClazzName(Class<T> clazz){
        return Base64.getUrlEncoder().encodeToString(clazz.getName().getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public <T extends DbItem> List<String> getReaders(Class<T> clazz, Long id, String token) throws HttpStatusCodeException {
        log.debug("API_SCHEMA::getReaders | $address: {}", findReadersUri);

        return this.getStringList(
                clazz,
                a->a.put("jwtToken", token),
                null,
                findReadersUri, id
        );
    }

    @Override
    public <T extends DbItem> List<String> getEditors(Class<T> clazz, Long id, String token) throws HttpStatusCodeException {
        log.debug("API_SCHEMA::getEditors | $address: {}", findEditorsUri);
        return this.getStringList(
                clazz,
                a -> a.put("jwtToken", token),
                null,
                findEditorsUri, id
        );
    }

    private <T extends DbItem> List<String> getStringList(
            Class<T> clazz,
            Consumer<Map<String, Object>> attributes,
            Map<String, String> params,
            String address, Object... uriVariables
    )throws HttpStatusCodeException{
        String clazzName = getClazzName(clazz);

        if(params == null) params = new HashMap<>();
        params.put("clazzNameEncoded", clazzName);

        String uri = tb.setAddressParams(params, address, uriVariables);
        return client.get().uri(uri)
                .accept(MediaType.APPLICATION_JSON)
                .attributes(attributes == null ? a -> {} : attributes)
                .retrieve()
                .toEntity(new ParameterizedTypeReference<List<String>>(){})
                .getBody();
    }
}
