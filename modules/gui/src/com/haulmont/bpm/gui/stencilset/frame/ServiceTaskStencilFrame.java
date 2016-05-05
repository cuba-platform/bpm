/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.bpm.gui.stencilset.frame;

import com.haulmont.bpm.entity.stencil.ServiceTaskStencil;
import com.haulmont.bpm.entity.stencil.ServiceTaskStencilMethodArg;
import com.haulmont.cuba.core.entity.FileDescriptor;
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

import javax.inject.Inject;
import java.util.Map;
import java.util.UUID;

public class ServiceTaskStencilFrame extends AbstractStencilFrame<ServiceTaskStencil> {

    @Inject
    private CollectionDatasource<ServiceTaskStencilMethodArg, UUID> methodArgsDs;

    @Inject
    private Metadata metadata;

    @Inject
    private FieldGroup fieldGroup;

    @Inject
    protected ComponentsFactory componentsFactory;

    @Inject
    private FileUploadingAPI fileUploadingAPI;

    @Inject
    private DataManager dataManager;

    @Inject
    private ExportDisplay exportDisplay;
    private LinkButton downloadIconBtn;

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
    }

    @Override
    public void setStencil(ServiceTaskStencil stencil) {
        super.setStencil(stencil);
        if (stencil.getIconFileId() != null && stencil.getIconFile() == null) {
            LoadContext<FileDescriptor> ctx = new LoadContext<>(FileDescriptor.class).setId(stencil.getIconFileId());
            FileDescriptor fd = dataManager.load(ctx);
            stencil.setIconFile(fd);
        }

        String downloadIconBtnCaption = stencil.getIconFile() != null ? stencil.getIconFile().getName() : getMessage("notUploaded");
        downloadIconBtn.setCaption(downloadIconBtnCaption);

    }

    public void addMethodArg() {
        ServiceTaskStencilMethodArg serviceTaskStencilMethodArg = metadata.create(ServiceTaskStencilMethodArg.class);
        serviceTaskStencilMethodArg.setStencil(stencilDs.getItem());
        methodArgsDs.addItem(serviceTaskStencilMethodArg);
    }

    @Override
    public void requestFocus() {
        fieldGroup.requestFocus();
    }
}
