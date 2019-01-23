/*
 * Copyright (c) 2008-2019 Haulmont.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
import com.haulmont.cuba.core.app.FileStorageAPI;
import com.haulmont.cuba.core.entity.FileDescriptor;
import com.haulmont.cuba.core.global.Metadata;
import com.haulmont.cuba.core.global.Resources;
import com.haulmont.cuba.core.global.TimeSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

@Component(StencilSetManager.NAME)
public class StencilSetManagerBean implements StencilSetManager {

    protected static final Logger log = LoggerFactory.getLogger(StencilSetManagerBean.class);

    protected static final String STENCILSET_JSON_FILE_NAME = "com/haulmont/bpm/stencilset.json";

    protected static final String DEFAULT_STENCIL_SET_NAME = "default";

    protected static final int UUID_LENGTH = 36;

    @Inject
    protected Persistence persistence;

    @Inject
    protected Metadata metadata;

    @Inject
    protected Resources resources;

    @Inject
    protected FileStorageAPI fileStorageAPI;

    @Inject
    protected TimeSource timeSource;

    protected String basicStencilSet;

    protected final int BUFFER = 2048;

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
            basicStencilSet = resources.getResourceAsString(STENCILSET_JSON_FILE_NAME);
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

    @Override
    public byte[] exportStencilSet(String stencilsJson, List<FileDescriptor> iconFiles) {
        ByteArrayOutputStream dest = new ByteArrayOutputStream();
        ZipOutputStream out = new ZipOutputStream(dest);
        ZipEntry stencilsetZipEntry = new ZipEntry(STENCILSET_JSON_FILE_NAME);
        try {
            out.putNextEntry(stencilsetZipEntry);
            out.write(stencilsJson.getBytes(StandardCharsets.UTF_8));

            for (FileDescriptor iconFile : iconFiles) {
                ZipEntry iconZipEntry = new ZipEntry(iconFile.getId() + "-" + iconFile.getName());
                out.putNextEntry(iconZipEntry);
                byte[] iconBytes = fileStorageAPI.loadFile(iconFile);
                out.write(iconBytes);
            }
            out.close();
            dest.close();
        } catch (Exception e) {
            throw new BpmException("Error on export stencils", e);
        }
        return dest.toByteArray();
    }

    @Override
    public void importStencilSet(byte[] zipBytes) {
        ByteArrayInputStream origin = new ByteArrayInputStream(zipBytes);
        ZipInputStream zis = new ZipInputStream(origin);
        ZipEntry entry;
        try {
            while ((entry = zis.getNextEntry()) != null) {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                int count;
                byte data[] = new byte[BUFFER];
                while ((count = zis.read(data, 0, BUFFER)) != -1) {
                    bos.write(data, 0, count);
                }
                bos.close();
                byte[] bytes = bos.toByteArray();
                if (STENCILSET_JSON_FILE_NAME.equals(entry.getName())) {
                    setStencilSet(new String(bytes, StandardCharsets.UTF_8));
                } else {
                    FileDescriptor fd;
                    String fileIdStr = entry.getName().substring(0, UUID_LENGTH);
                    UUID fileId = UUID.fromString(fileIdStr);
                    try (Transaction tx = persistence.getTransaction()) {
                        EntityManager em = persistence.getEntityManager();
                        fd = em.find(FileDescriptor.class, fileId);

                        if (fd == null) {
                            fd = metadata.create(FileDescriptor.class);
                            fd.setId(fileId);
                            fd.setCreateDate(timeSource.currentTimestamp());
                            em.persist(fd);
                        }

                        String fileName = entry.getName().substring(UUID_LENGTH + 1);
                        fd.setName(fileName);
                        fd.setExtension(fileName.substring(fileName.lastIndexOf(".") + 1));
                        fd.setSize((long) bytes.length);

                        if (fileStorageAPI.fileExists(fd)) {
                            fileStorageAPI.removeFile(fd);
                        }
                        fileStorageAPI.saveFile(fd, bytes);

                        tx.commit();
                    }

                }
            }
        } catch (Exception e) {
            throw new BpmException("Error on import stencils", e);
        }
    }
}
