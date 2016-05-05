/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.bpm.core;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonWriter;
import com.haulmont.bpm.core.jsonconverter.CubaBpmnJsonConverter;
import com.haulmont.bpm.core.jsonconverter.CustomServiceTaskJsonConverter;
import com.haulmont.bpm.entity.StencilSet;
import com.haulmont.bpm.exception.BpmException;
import com.haulmont.cuba.core.EntityManager;
import com.haulmont.cuba.core.Persistence;
import com.haulmont.cuba.core.Transaction;
import com.haulmont.cuba.core.TypedQuery;
import com.haulmont.cuba.core.global.Metadata;
import com.haulmont.cuba.core.global.Resources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.io.IOException;
import java.io.StringWriter;

@Component(StencilSetManager.NAME)
public class StencilSetManagerBean implements StencilSetManager {

    private static final Logger log = LoggerFactory.getLogger(StencilSetManagerBean.class);

    protected String DEFAULT_STENCIL_SET_NAME = "default";

    @Inject
    protected Persistence persistence;

    @Inject
    protected Metadata metadata;

    @Inject
    protected Resources resources;

    protected String basicStencilSet;

    @Override
    public String getStencilSet() {
        return getStencilSet(DEFAULT_STENCIL_SET_NAME);
    }

    protected String getStencilSet(String name) {
        String basicStencilSet = getBasicStencilSet();
        StencilSet customStencilSet = findCustomStencilSetByName(name);
        if (customStencilSet != null)
            try {
                return mergeStencilSets(basicStencilSet, customStencilSet.getJsonData());
            } catch (IOException e) {
                throw new BpmException("Error while evaluation stencil set", e);
            }
        else {
            log.debug("Custom stencil set {} not found. Return the basic one", name);
            return basicStencilSet;
        }
    }

    @Override
    public void setStencilSet(String jsonData) {
        setStencilSet(DEFAULT_STENCIL_SET_NAME, jsonData);
    }

    protected void setStencilSet(String name, String jsonData) {
        try (Transaction tx = persistence.getTransaction()) {
            EntityManager em = persistence.getEntityManager();
            StencilSet stencilSet = findCustomStencilSetByName(name);
            if (stencilSet == null) {
                stencilSet = metadata.create(StencilSet.class);
                stencilSet.setName(name);
                stencilSet.setJsonData(jsonData);
                em.persist(stencilSet);
            } else {
                stencilSet.setJsonData(jsonData);
                em.merge(stencilSet);
            }
            tx.commit();
        }
    }

    @Override
    public void resetStencilSet() {
        try (Transaction tx = persistence.getTransaction()) {
            EntityManager em = persistence.getEntityManager();
            em.createQuery("delete from bpm$StencilSet s where s.name = :name")
                    .setParameter("name", DEFAULT_STENCIL_SET_NAME)
                    .executeUpdate();
            tx.commit();
        }
    }

    @Nullable
    protected StencilSet findCustomStencilSetByName(String name) {
        try (Transaction tx = persistence.getTransaction()) {
            EntityManager em = persistence.getEntityManager();
            TypedQuery<StencilSet> query = em.createQuery("select s from bpm$StencilSet s where s.name = :name", StencilSet.class)
                    .setParameter("name", name);
            StencilSet stencilSet = query.getFirstResult();
            tx.commit();
            return stencilSet;
        }
    }

    protected String getBasicStencilSet() {
        if (basicStencilSet == null) {
            basicStencilSet = resources.getResourceAsString("stencilset.json");
            if (basicStencilSet == null) {
                throw new BpmException("Basic stencil set not found in the classpath");
            }
        }
        return basicStencilSet;
    }

    protected String mergeStencilSets(String basicStencilSetJson, String customStencilSetJson) throws IOException {
        JsonParser basicParser = new JsonParser();
        JsonElement basicRootElement = basicParser.parse(basicStencilSetJson);

        JsonParser customParser = new JsonParser();
        JsonElement customRootElement = customParser.parse(customStencilSetJson);

        StringWriter out = new StringWriter();
        JsonWriter dstJsonWriter = new JsonWriter(out);
        dstJsonWriter.beginObject();

        JsonObject srcRootJsonObject = basicRootElement.getAsJsonObject();
        dstJsonWriter.name("title");
        dstJsonWriter.value(srcRootJsonObject.get("title").getAsString());
        dstJsonWriter.name("namespace");
        dstJsonWriter.value(srcRootJsonObject.get("namespace").getAsString());
        dstJsonWriter.name("description");
        dstJsonWriter.value(srcRootJsonObject.get("description").getAsString());

        JsonArray basicPropertyPackages = basicRootElement.getAsJsonObject().getAsJsonArray("propertyPackages");
        JsonArray customPropertyPackages = customRootElement.getAsJsonObject().getAsJsonArray("propertyPackages");
        basicPropertyPackages.addAll(customPropertyPackages);

        dstJsonWriter.name("propertyPackages");
        dstJsonWriter.jsonValue(basicPropertyPackages.toString());

        JsonArray basicStencils = basicRootElement.getAsJsonObject().getAsJsonArray("stencils");
        JsonArray customStencils = customRootElement.getAsJsonObject().getAsJsonArray("stencils");
        basicStencils.addAll(customStencils);

        dstJsonWriter.name("stencils");
        dstJsonWriter.jsonValue(basicStencils.toString());

        JsonObject basicRulesObject = basicRootElement.getAsJsonObject().get("rules").getAsJsonObject();

        dstJsonWriter.name("rules");
        dstJsonWriter.jsonValue(basicRulesObject.toString());

        dstJsonWriter.endObject();
        dstJsonWriter.close();

        return out.toString();
    }

    @Override
    public void registerServiceTaskStencilBpmnJsonConverter(String stencilId) {
        CubaBpmnJsonConverter.addConvertersToBpmnMapItem(stencilId, CustomServiceTaskJsonConverter.class);
    }
}
