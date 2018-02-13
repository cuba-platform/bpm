/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.bpm.gui.app;

import com.haulmont.bali.util.Dom4j;
import com.haulmont.bpm.config.BpmConfig;
import com.haulmont.bpm.form.ProcFormDefinition;
import com.haulmont.bpm.form.ProcFormParam;
import com.haulmont.cuba.core.global.Configuration;
import com.haulmont.cuba.core.global.Messages;
import com.haulmont.cuba.core.global.Resources;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.text.StrTokenizer;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Component
public class ProcessFormRepository {

    private static final Logger log = LoggerFactory.getLogger(ProcessFormRepository.class);

    @Inject
    protected Resources resources;

    @Inject
    protected Messages messages;

    @Inject
    protected Configuration configuration;

    protected List<ProcFormDefinition> forms = new ArrayList<>();

    protected boolean initialized = false;

    protected ReadWriteLock lock = new ReentrantReadWriteLock();

    public List<ProcFormDefinition> getForms() {
        lock.readLock().lock();
        try {
            checkInitialized();
            return forms;
        } finally {
            lock.readLock().unlock();
        }
    }

    protected void checkInitialized() {
        if (!initialized) {
            lock.readLock().unlock();
            lock.writeLock().lock();
            try {
                if (!initialized) {
                    init();
                    initialized = true;
                }
            } finally {
                lock.readLock().lock();
                lock.writeLock().unlock();
            }
        }
    }

    protected void init() {
        String configName = configuration.getConfig(BpmConfig.class).getFormsConfig();
        StrTokenizer tokenizer = new StrTokenizer(configName);
        for (String location : tokenizer.getTokenArray()) {
            Resource resource = resources.getResource(location);
            if (resource.exists()) {
                InputStream stream = null;
                try {
                    stream = resource.getInputStream();
                    loadConfig(Dom4j.readDocument(stream).getRootElement());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                } finally {
                    IOUtils.closeQuietly(stream);
                }
            } else {
                log.warn("Resource {} not found, ignore it", location);
            }
        }
    }

    protected void loadConfig(Element rootElem) {
        for (Element formElem : Dom4j.elements(rootElem, "form")) {
            String formName = formElem.attributeValue("name");
            String formCaption = messages.getMainMessage(formName);
            boolean isDefault = Boolean.parseBoolean(formElem.attributeValue("default"));
            ProcFormDefinition formDefinition = new ProcFormDefinition();
            formDefinition.setName(formName);
            formDefinition.setCaption(formCaption);
            formDefinition.setIsDefault(isDefault);
            List<ProcFormParam> params = new ArrayList<>();
            for (Element paramEl : Dom4j.elements(formElem, "param")) {
                String paramName = paramEl.attributeValue("name");
                String paramValue = paramEl.attributeValue("value");
                ProcFormParam param = new ProcFormParam();
                param.setName(paramName);
                param.setValue(paramValue);
                params.add(param);
            }
            formDefinition.setParams(params);
            forms.add(formDefinition);
        }
    }
}