/*
 * Copyright (c) ${YEAR} ${PACKAGE_NAME}
 */

package com.haulmont.bpm.core;

import com.haulmont.bpm.exception.BpmException;
import com.haulmont.cuba.core.global.MessageTools;
import com.haulmont.cuba.core.global.UserSessionSource;
import org.activiti.bpmn.model.ExtensionElement;

import javax.annotation.ManagedBean;
import javax.annotation.Nullable;
import javax.inject.Inject;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author gorbunkov
 * @version $Id$
 */
@ManagedBean(ProcessMessagesManager.NAME)
public class ProcessMessagesManagerBean implements ProcessMessagesManager{

    public static final String MARK = "msg://";

    @Inject
    protected ExtensionElementsManager extensionElementsManager;

    @Inject
    protected MessageTools messageTools;

    @Inject
    protected UserSessionSource userSessionSource;

    protected Map<String, String> msgCache = new ConcurrentHashMap<>();
    protected Map<String, Properties> propsCache = new ConcurrentHashMap<>();

    @Override
    public String getMessage(String actProcessDefinitionId, String key) {
        return getMessage(actProcessDefinitionId, key, getUserLocale());
    }

    @Override
    public String getMessage(String actProcessDefinitionId, String key, Locale locale) {
        locale = messageTools.trimLocale(locale);

        String cacheKey = makeMsgCacheKey(actProcessDefinitionId, key, locale);

        String msg = msgCache.get(cacheKey);
        if (msg != null)
            return msg;

        Properties properties = getProperties(actProcessDefinitionId, locale);
        msg = properties.getProperty(key);

        if (msg != null) {
            cacheMsg(key, msg);
            return msg;
        }

        if (!messageTools.getDefaultLocale().equals(locale)) {
            msg = getMessage(actProcessDefinitionId, key, messageTools.getDefaultLocale());
            if (msg != null) {
                cacheMsg(key, msg);
                return msg;
            }
        }

        msg = key;
        cacheMsg(key, msg);

        return msg;
    }

    @Override
    public String loadString(String actProcessDefinitionId, String key) {
        return loadString(actProcessDefinitionId, key, getUserLocale());
    }

    @Override
    public String loadString(String actProcessDefinitionId, String key, Locale locale) {
        if (key.startsWith(MARK)) {
            return getMessage(actProcessDefinitionId, key.substring(MARK.length()), locale);
        } else {
            return key;
        }
    }

    protected void cacheMsg(String key, String msg) {
        msgCache.put(key, msg);
    }

    protected Properties getProperties(String actProcessDefinitionId, Locale locale) {
        String key = makePropsCacheKey(actProcessDefinitionId, locale);
        Properties properties = propsCache.get(key);
        if (properties != null)
            return properties;

        properties = getLocalizationProperties(actProcessDefinitionId, locale);
        if (properties != null) {
            propsCache.put(key, properties);
            return properties;
        }

        return new Properties();
    }

    protected String makeMsgCacheKey(String actProcessDefinitionId, String key, Locale locale) {
        return actProcessDefinitionId + "/" + locale + "/" + key;
    }

    protected String makePropsCacheKey(String actProcessDefinitionId, Locale locale) {
        return actProcessDefinitionId + "/" + locale;
    }

    @Nullable
    protected Properties getLocalizationProperties(String actProcessDefinitionId, Locale locale) {
        Map<String, List<ExtensionElement>> processExtensionElements = extensionElementsManager.getProcessExtensionElements(actProcessDefinitionId);
        List<ExtensionElement> localizationsElements = processExtensionElements.get("localizations");
        if (localizationsElements != null) {
            ExtensionElement localizationsElement = localizationsElements.get(0);
            List<ExtensionElement> localizationElements = localizationsElement.getChildElements().get("localization");
            if (localizationElements != null) {
                for (ExtensionElement localizationElement : localizationElements) {
                    if (locale.getLanguage().equals(localizationElement.getAttributeValue(null, "lang"))) {
                        String localization = localizationElement.getElementText();
                        Properties properties = new Properties();
                        try {
                            properties.load(new StringReader(localization));
                        } catch (IOException e) {
                            throw new BpmException("Error when reading process localization", e);
                        }
                        return properties;
                    }
                }
            }
        }

        return null;
    }

    protected Locale getUserLocale() {
        return userSessionSource.checkCurrentUserSession() ?
                userSessionSource.getUserSession().getLocale() :
                messageTools.getDefaultLocale();
    }
}
