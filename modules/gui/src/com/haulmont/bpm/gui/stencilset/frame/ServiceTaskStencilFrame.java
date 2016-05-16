/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.bpm.gui.stencilset.frame;

import com.google.common.base.Strings;
import com.haulmont.bpm.entity.stencil.ServiceTaskStencil;
import com.haulmont.bpm.entity.stencil.StencilMethodArg;
import com.haulmont.bpm.entity.stencil.Stencil;
import com.haulmont.bpm.service.StencilSetService;
import com.haulmont.cuba.core.app.scheduled.MethodInfo;
import com.haulmont.cuba.core.app.scheduled.MethodParameterInfo;
import com.haulmont.cuba.core.entity.FileDescriptor;
import com.haulmont.cuba.core.entity.ScheduledTask;
import com.haulmont.cuba.core.global.DataManager;
import com.haulmont.cuba.core.global.FileStorageException;
import com.haulmont.cuba.core.global.LoadContext;
import com.haulmont.cuba.core.global.Metadata;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.data.CollectionDatasource;
import com.haulmont.cuba.gui.export.ExportDisplay;
import com.haulmont.cuba.gui.export.ExportFormat;
import com.haulmont.cuba.gui.upload.FileUploadingAPI;
import com.haulmont.cuba.gui.xml.layout.ComponentsFactory;
import org.apache.commons.lang.StringUtils;

import javax.inject.Inject;
import java.util.*;

public class ServiceTaskStencilFrame extends AbstractStencilFrame<ServiceTaskStencil> {

    @Inject
    protected CollectionDatasource<StencilMethodArg, UUID> methodArgsDs;

    @Inject
    protected Metadata metadata;

    @Inject
    protected FieldGroup fieldGroup;

    @Inject
    protected ComponentsFactory componentsFactory;

    @Inject
    protected FileUploadingAPI fileUploadingAPI;

    @Inject
    protected DataManager dataManager;

    @Inject
    protected ExportDisplay exportDisplay;

    @Inject
    protected CollectionDatasource<Stencil, UUID> stencilsDs;

    protected LinkButton downloadIconBtn;

    @Inject
    protected StencilSetService stencilSetService;
    private LookupField methodNameField;
    private List<MethodInfo> availableMethods;

    @Override
    public void init(Map<String, Object> params) {
        super.init(params);
        fieldGroup.addCustomField("icon", (datasource, propertyId) -> {
            HBoxLayout hbox = componentsFactory.createComponent(HBoxLayout.class);
            hbox.setWidth("100%");
            downloadIconBtn = componentsFactory.createComponent(LinkButton.class);
            downloadIconBtn.setAlignment(Alignment.MIDDLE_LEFT);
            FileUploadField uploadField = componentsFactory.createComponent(FileUploadField.class);
            uploadField.addFileUploadSucceedListener(e -> {
                FileDescriptor fd = uploadField.getFileDescriptor();
                try {
                    fileUploadingAPI.putFileIntoStorage(uploadField.getFileId(), fd);
                } catch (FileStorageException ex) {
                    throw new RuntimeException("Error saving file to FileStorage", ex);
                }
                dataManager.commit(fd);
                stencilDs.getItem().setIconFileId(fd.getId());
                stencilDs.getItem().setIconFile(fd);
                downloadIconBtn.setCaption(fd.getName());
            });
            uploadField.setAlignment(Alignment.MIDDLE_RIGHT);

            downloadIconBtn.setAction(new AbstractAction("download") {
                @Override
                public void actionPerform(Component component) {
                    ServiceTaskStencil stencil = stencilDs.getItem();
                    if (stencil.getIconFile() != null) {
                        exportDisplay.show(stencil.getIconFile(), ExportFormat.OCTET_STREAM);
                    }
                }
            });

            hbox.add(downloadIconBtn);
            hbox.add(uploadField);
            return hbox;
        });

        Map<String, List<MethodInfo>> availableBeans = stencilSetService.getAvailableBeans();

        fieldGroup.addCustomField("beanName", (datasource, propertyId) -> {
            LookupField beanNameField = componentsFactory.createComponent(LookupField.class);
            beanNameField.setOptionsList(new ArrayList<>(availableBeans.keySet()));
            beanNameField.setDatasource(stencilDs, "beanName");

            beanNameField.addValueChangeListener(e -> {
                methodNameField.setValue(null);
                if (e.getValue() == null) {
                    methodNameField.setOptionsList(Collections.emptyList());
                } else {
                    availableMethods = availableBeans.get(e.getValue());

                    if (availableMethods != null) {
                        HashMap<String, Object> optionsMap = new HashMap<>();
                        for (MethodInfo availableMethod : availableMethods) {
                            optionsMap.put(availableMethod.getMethodSignature(), availableMethod);
                        }
                        methodNameField.setOptionsMap(optionsMap);
                    }
                }
            });

            return beanNameField;
        });

        fieldGroup.addCustomField("methodName", (datasource, propertyId) -> {
            methodNameField = componentsFactory.createComponent(LookupField.class);
            methodNameField.setRequired(true);
            methodNameField.setRequiredMessage(getMessage("modelNameRequired"));
            methodNameField.addValueChangeListener(e -> {
                methodArgsDs.clear();
                MethodInfo methodInfo = (MethodInfo) e.getValue();
                if (methodInfo != null) {
                    stencilDs.getItem().setMethodName(methodInfo.getName());
                    for (MethodParameterInfo parameterInfo : methodInfo.getParameters()) {
                        StencilMethodArg stencilMethodArg = metadata.create(StencilMethodArg.class);
                        stencilMethodArg.setStencil(stencilDs.getItem());
                        stencilMethodArg.setType(parameterInfo.getTypeName());
                        stencilMethodArg.setPropertyPackageTitle(StringUtils.capitalize(parameterInfo.getName()));
                        methodArgsDs.addItem(stencilMethodArg);
                    }
                }
            });
            return methodNameField;
        });

        fieldGroup.addValidator("stencilId", value -> {
            if (value == null) return;
            boolean stencilIdUniquenessViolated = stencilsDs.getItems().stream()
                    .anyMatch(stencil -> stencil != stencilDs.getItem() && value.equals(stencil.getStencilId()));
            if (stencilIdUniquenessViolated) throw new ValidationException(formatMessage("stencilWithIdExists", value));
        });
    }

    @Override
    public void setStencil(ServiceTaskStencil stencil) {
        super.setStencil(stencil);
        if (stencil.getIconFileId() != null && stencil.getIconFile() == null) {
            LoadContext<FileDescriptor> ctx = new LoadContext<>(FileDescriptor.class).setId(stencil.getIconFileId());
            FileDescriptor fd = dataManager.load(ctx);
            stencil.setIconFile(fd);
        }

        if (!Strings.isNullOrEmpty(stencil.getMethodName())) {
            setInitialMethodNameValue(stencil);
        }

        String downloadIconBtnCaption = stencil.getIconFile() != null ? stencil.getIconFile().getName() : getMessage("notUploaded");
        downloadIconBtn.setCaption(downloadIconBtnCaption);
    }

    /**
     * Method reads values of methodName and parameters from item,
     * finds appropriate MethodInfo object in methodInfoField's optionsList
     * and sets found value to methodInfoField
     */
    protected void setInitialMethodNameValue(ServiceTaskStencil stencil) {
        if (availableMethods == null)
            return;

        for (MethodInfo availableMethod : availableMethods) {
            if (methodsDefinitionEquals(availableMethod, stencil)) {
                methodNameField.setValue(availableMethod);
            }
        }
    }

    public boolean methodsDefinitionEquals(MethodInfo methodInfo, ServiceTaskStencil stencil) {
        if (!methodInfo.getName().equals(stencil.getMethodName()))
            return false;
        if (stencil.getMethodArgs().size() != methodInfo.getParameters().size())
            return false;

        for (int i = 0; i < methodInfo.getParameters().size(); i++) {
            String typeName1 = methodInfo.getParameters().get(i).getTypeName();
            String typeName2 = stencil.getMethodArgs().get(i).getType();
            if (!typeName1.equals(typeName2))
                return false;
        }

        return true;
    }

    @Override
    public void requestFocus() {
        fieldGroup.requestFocus();
    }
}
