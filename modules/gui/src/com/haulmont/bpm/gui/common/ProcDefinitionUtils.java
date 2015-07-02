/*
 * Copyright (c) 2008-2015 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.bpm.gui.common;

import com.google.common.base.Strings;
import com.haulmont.bali.util.Dom4j;
import org.dom4j.Document;
import org.dom4j.Element;

/**
 * @author gorbunkov
 * @version $Id$
 */
public class ProcDefinitionUtils {
    public static String getProcessKeyFromXml(String xml) {
        Document document = Dom4j.readDocument(xml);
        Element process = document.getRootElement().element("process");
        if (process == null) {
            throw new IllegalArgumentException("Process xml doesn't contain 'process' element");
        }

        String id = process.attributeValue("id");
        if (Strings.isNullOrEmpty(id)) {
            throw new IllegalArgumentException("Process id is not defined");
        }

        return id;
    }

}
