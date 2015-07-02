/*
 * Copyright (c) 2008-2015 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.bpm.core;

import com.haulmont.bali.util.Dom4j;
import com.haulmont.bpm.form.ProcFormDefinition;
import com.haulmont.bpm.form.ProcFormParam;
import com.haulmont.cuba.core.global.Resources;
import com.haulmont.cuba.core.sys.AppContext;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.text.StrTokenizer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Element;
import org.springframework.core.io.Resource;

import javax.annotation.ManagedBean;
import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author gorbunkov
 * @version $Id$
 */
@ManagedBean
public class ProcessFormRepository {

    private static final String BPM_FORMS_CONFIG_PROP_NAME = "bpm.formsConfig";
    @Inject
    protected Resources resources;

    protected List<ProcFormDefinition> forms = new ArrayList<>();

    protected boolean initialized = false;

    protected ReadWriteLock lock = new ReentrantReadWriteLock();

    protected Log log = LogFactory.getLog(ProcessFormRepository.class);

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
        String configName = AppContext.getProperty(BPM_FORMS_CONFIG_PROP_NAME);
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
                log.warn("Resource " + location + " not found, ignore it");
            }
        }
    }

    protected void loadConfig(Element rootElem) {
        for (Element serviceElem : Dom4j.elements(rootElem, "form")) {
            String formName = serviceElem.attributeValue("name");
            ProcFormDefinition formDefinition = new ProcFormDefinition();
            formDefinition.setName(formName);
            List<ProcFormParam> params = new ArrayList<>();
            for (Element paramEl : Dom4j.elements(serviceElem, "param")) {
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
