/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.bpm.gui.stencilset.helper;

import java.util.ArrayList;
import java.util.List;

/**
 * Class is used by GSON as an intermediate object when transforming stencilst json to the {@link com.haulmont.bpm.entity.stencil.Stencil} entity
 */
public class GsonStencil {

    private String type;
    private String id;
    private String title;
    private String description;
    private String view;
    private String icon;
    private String customIconId;
    private List<String> groups;
    private List<String> propertyPackages;
    private List<String> mainPropertyPackages;
    private List<String> hiddenPropertyPackages;
    private List<String> roles;
    private Custom custom;

    static class Custom {
        private String type;
        private String beanName;
        private String methodName;
        private List<MethodArg> methodArgs = new ArrayList<>();

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getBeanName() {
            return beanName;
        }

        public void setBeanName(String beanName) {
            this.beanName = beanName;
        }

        public String getMethodName() {
            return methodName;
        }

        public void setMethodName(String methodName) {
            this.methodName = methodName;
        }

        public List<MethodArg> getMethodArgs() {
            return methodArgs;
        }

        public void setMethodArgs(List<MethodArg> methodArgs) {
            this.methodArgs = methodArgs;
        }
    }

    static class MethodArg {
        private String propertyPackageName;
        private String type;

        public String getPropertyPackageName() {
            return propertyPackageName;
        }

        public void setPropertyPackageName(String propertyPackageName) {
            this.propertyPackageName = propertyPackageName;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getView() {
        return view;
    }

    public void setView(String view) {
        this.view = view;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public List<String> getGroups() {
        return groups;
    }

    public void setGroups(List<String> groups) {
        this.groups = groups;
    }

    public List<String> getPropertyPackages() {
        return propertyPackages;
    }

    public void setPropertyPackages(List<String> propertyPackages) {
        this.propertyPackages = propertyPackages;
    }

    public List<String> getMainPropertyPackages() {
        return mainPropertyPackages;
    }

    public void setMainPropertyPackages(List<String> mainPropertyPackages) {
        this.mainPropertyPackages = mainPropertyPackages;
    }

    public List<String> getHiddenPropertyPackages() {
        return hiddenPropertyPackages;
    }

    public void setHiddenPropertyPackages(List<String> hiddenPropertyPackages) {
        this.hiddenPropertyPackages = hiddenPropertyPackages;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public Custom getCustom() {
        return custom;
    }

    public void setCustom(Custom custom) {
        this.custom = custom;
    }

    public String getCustomIconId() {
        return customIconId;
    }

    public void setCustomIconId(String customIconId) {
        this.customIconId = customIconId;
    }

    //    public ServiceTaskStencil fillCustomStencil(ServiceTaskStencil customStencil) {
//        customStencil.setStencilId(id);
//        customStencil.setName(title);
//        if (custom == null) {
//            customStencil.setType(StencilType.STANDARD);
//        } else {
//            customStencil.setType(StencilType.CUSTOM);
//            customStencil.setBeanName(custom.getBeanName());
//            customStencil.setMethodName(custom.getMethodName());
//        }
//        customStencil.setType(custom == null ? StencilType.STANDARD : StencilType.CUSTOM);
//    }
}
