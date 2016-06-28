/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.bpm.core;

import com.haulmont.cuba.core.global.MessageTools;
import com.haulmont.cuba.core.global.UserSessionSource;
import org.activiti.bpmn.model.ExtensionElement;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

@Component(ProcessMessagesManager.NAME)
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
    protected Map<String, String> notFoundCache = new ConcurrentHashMap<>();

    @Override
    public String getMessage(String actProcessDefinitionId, String key) {
        return getMessage(actProcessDefinitionId, key, getUserLocale());
    }

    @Override
    public String getMessage(String actProcessDefinitionId, String key, Locale locale) {
        return internalGetMessage(actProcessDefinitionId, key, locale, key);
    }

    @Nullable
    @Override
    public String findMessage(String actProcessDefinitionId, String key) {
        return internalGetMessage(actProcessDefinitionId, key, getUserLocale(), null);
    }

    @Nullable
    @Override
    public String findMessage(String actProcessDefinitionId, String key, Locale locale) {
        return internalGetMessage(actProcessDefinitionId, key, locale, null);
    }

    protected String internalGetMessage(String actProcessDefinitionId, String key, Locale locale, String defaultValue) {
        locale = messageTools.trimLocale(locale);

        String cacheKey = makeMsgCacheKey(actProcessDefinitionId, key, locale);

        String msg = msgCache.get(cacheKey);
        if (msg != null)
            return msg;

        String notFound = notFoundCache.get(cacheKey);
        if (notFound != null)
            return defaultValue;

        Properties properties = getProperties(actProcessDefinitionId, locale);
        msg = properties.getProperty(key);

        if (msg != null) {
            cacheMsg(key, msg);
            return msg;
        }

        if (!messageTools.getDefaultLocale().equals(locale)) {
            msg = internalGetMessage(actProcessDefinitionId, key, messageTools.getDefaultLocale(), defaultValue);
            if (msg != null) {
                cacheMsg(key, msg);
                return msg;
            }
        }

        notFoundCache.put(cacheKey, key);
        return defaultValue;
    }

    @Override
    public String loadString(String actProcessDefinitionId, String ref) {
        return loadString(actProcessDefinitionId, ref, getUserLocale());
    }

    @Override
    public String loadString(String actProcessDefinitionId, String ref, Locale locale) {
        if (ref.startsWith(MARK)) {
            return getMessage(actProcessDefinitionId, ref.substring(MARK.length()), locale);
        } else {
            return ref;
        }
    }

    @Override
    public void clearCaches() {
        msgCache.clear();
        propsCache.clear();
        notFoundCache.clear();
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
        if (properties == null) {
            properties = new Properties();
        }
        propsCache.put(key, properties);

        return properties;
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
                        List<ExtensionElement> msgElements = localizationElement.getChildElements().get("msg");
                        Properties properties = new Properties();
                        if (msgElements != null) {
                            for (ExtensionElement msgElement : msgElements) {
                                properties.put(msgElement.getAttributeValue(null, "key"), msgElement.getAttributeValue(null, "value"));
                            }
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