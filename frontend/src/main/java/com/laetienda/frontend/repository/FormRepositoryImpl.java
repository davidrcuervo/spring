package com.laetienda.frontend.repository;

import com.laetienda.frontend.model.Form;
import com.laetienda.frontend.model.Input;
import com.laetienda.lib.annotation.HtmlForm;
import com.laetienda.lib.annotation.HtmlInput;
import com.laetienda.lib.interfaces.Forma;
import com.laetienda.model.user.Usuario;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

@Component
public class FormRepositoryImpl implements FormRepository {
    private static final Logger log = LoggerFactory.getLogger(FormRepositoryImpl.class);

    @Override
    public Form getForm(Forma forma) {

        Form result = new Form();

        if (forma instanceof Forma) {

            Annotation a = forma.getClass().getAnnotation(HtmlForm.class);
            List<Input> inputs = getHtmlInputs(forma);

            if(a instanceof HtmlForm && inputs != null){

                HtmlForm htmlForm = (HtmlForm)a;
                result.setAction(htmlForm.action());
                result.setName(htmlForm.name());
                result.setUrl(htmlForm.url());
                result.setMethod(htmlForm.method());
                result.setInputs(inputs);
                result.addButtonAttribute("text", "Submit");
                result.addButtonAttribute("class","btn btn-primary");

            }else{
                log.debug("{} is not instance of {}", a.getClass().getSimpleName(), HtmlForm.class.getSimpleName());
                result = null;
            }
        } else {
            log.debug("{} is not instance of {}", forma.getClass().getSimpleName(), HtmlForm.class.getSimpleName());
            result = null;
        }

        return result;
    }

    private List<Input> getHtmlInputs(Forma forma) {
        List<Input> result = new ArrayList<Input>();

        try {
            Field[] fields = forma.getClass().getDeclaredFields();
            log.debug("Amount of fields found on object. $object: {}, $fields: {}", forma.getClass().getCanonicalName(), fields.length);

            for (Field f : fields) {

                Annotation a = f.getAnnotation(HtmlInput.class);

                if (a instanceof HtmlInput) {
                    HtmlInput htmlInput = (HtmlInput) a;
                    f.setAccessible(true);
                    Object value = f.get(forma);

                    Input input = new Input();
                    input.setStyle_size(htmlInput.style_size());
                    input.setRequired(htmlInput.required());
                    input.setType(htmlInput.type());
                    input.setPlaceholder(htmlInput.placeholder());
                    input.setLabel(htmlInput.label());
                    input.setName(f.getName());
                    if(value instanceof String) input.setValue((String)value);
                    input.setId("id_" + forma.getClass().getSimpleName() + "_" + f.getName());
                    result.add(input);

                } else {
//                    log.debug("{} is not instance of HtmlInput.class. $object: {}", a.getClass().getSimpleName(), forma.getClass().getCanonicalName());
//                    result = null;
//                    break;
                }
            }
        }catch(Exception e){
            log.error("Exception while validating inputs. $exception: {}, $message: {}", e.getClass().getCanonicalName(), e.getMessage());
            log.debug("Exception while validating inputs.", e);
            result = null;
        }

        return result;
    }

    public static void main(String[] args){
        log.info("Testing {} class.", FormRepositoryImpl.class.getCanonicalName());

        FormRepository fr = new FormRepositoryImpl();
        Usuario usuario = new Usuario();

        Form result = fr.getForm(usuario);

        log.debug("$Method: {}", result.getMethod());
        log.debug("$Action: {}", result.getAction());
        log.debug("$Name: {}", result.getName());
        log.debug("$url: {}", result.getUrl());

        log.debug("===========");
        for(Input input : result.getInputs()){
            log.debug("$name: {}", input.getName());
            log.debug("$id: {}", input.getId());
            log.debug("$label: {}", input.getLabel());
            log.debug("$placeholder: {}", input.getPlaceholder());
            log.debug("$style_size: {}", input.getStyle_size());
            log.debug("$type: {}", input.getType());
            log.debug("$required: {}", input.getRequired());
            log.debug("$value: {}", input.getValue());
            log.debug("------------");
        }
    }
}
