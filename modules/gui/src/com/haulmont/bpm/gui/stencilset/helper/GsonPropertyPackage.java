/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.bpm.gui.stencilset.helper;

import java.util.List;

public class GsonPropertyPackage {

    private String name;

    private List<Property> properties;

    private Custom custom;

    public static class Property {
        private String id;
        private String type;
        private String title;
        private String value;
        private String description;
        private boolean popular;
        private List<String> refToView;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public boolean isPopular() {
            return popular;
        }

        public void setPopular(boolean popular) {
            this.popular = popular;
        }

        public List<String> getRefToView() {
            return refToView;
        }

        public void setRefToView(List<String> refToView) {
            this.refToView = refToView;
        }
    }

    public static class Custom {
        private String type;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Property> getProperties() {
        return properties;
    }

    public void setProperties(List<Property> properties) {
        this.properties = properties;
    }

    public Custom getCustom() {
        return custom;
    }

    public void setCustom(Custom custom) {
        this.custom = custom;
    }
}
