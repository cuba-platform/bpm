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

package com.haulmont.bpm.gui.procattachment;

import com.haulmont.bpm.entity.ProcAttachment;
import com.haulmont.bpm.entity.ProcAttachmentType;
import com.haulmont.bpm.entity.ProcInstance;
import com.haulmont.bpm.entity.ProcTask;
import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.cuba.core.entity.FileDescriptor;
import com.haulmont.cuba.core.global.FileStorageException;
import com.haulmont.cuba.core.global.Metadata;
import com.haulmont.cuba.core.global.PersistenceHelper;
import com.haulmont.cuba.core.global.UserSessionSource;
import com.haulmont.cuba.gui.WindowManager;
import com.haulmont.cuba.gui.app.core.file.FileDownloadHelper;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.components.actions.EditAction;
import com.haulmont.cuba.gui.data.CollectionDatasource;
import com.haulmont.cuba.gui.upload.FileUploadingAPI;
import com.haulmont.cuba.gui.xml.layout.ComponentsFactory;

import javax.inject.Inject;
import java.util.*;

public class ProcAttachmentsFrame extends AbstractFrame {

    @Inject
    protected FileMultiUploadField uploadField;

    @Inject
    protected FileUploadingAPI fileUploadingAPI;

    @Inject
    protected Metadata metadata;

    @Inject
    protected CollectionDatasource<ProcAttachment, UUID> procAttachmentsDs;

    @Inject
    protected Table<ProcAttachment> procAttachmentsTable;

    @Inject
    protected UserSessionSource userSessionSource;

    protected ProcInstance procInstance;

    protected ProcTask procTask;

    protected Map<FileDescriptor, UUID> temporaryFileIds = new HashMap<>();

    @Inject
    protected ComponentsFactory componentsFactory;

    @Inject
    protected CollectionDatasource<ProcAttachmentType, UUID> procAttachmentTypesDs;

    @Override
    public void init(Map<String, Object> params) {
        super.init(params);
        initProcAttachmentsTable();
        initUploadField();
        initParentDsContextCommitListener();
    }

    protected void initProcAttachmentsTable() {
        procAttachmentsTable.addGeneratedColumn("type", entity -> {
            LookupField<ProcAttachmentType> lookupField = componentsFactory.createComponent(LookupField.class);
            lookupField.setOptionsDatasource(procAttachmentTypesDs);
            lookupField.setValue(entity.getType());
            lookupField.addValueChangeListener(e -> {
                entity.setType((ProcAttachmentType) e.getValue());
            });
            return lookupField;
        });

        FileDownloadHelper.initGeneratedColumn(procAttachmentsTable, "file");

        //replace standard edit action because we want to pass parent datasource to attachment editor
        procAttachmentsTable.addAction(new EditAction(procAttachmentsTable) {
            @Override
            public void actionPerform(Component component) {
                final Window.Editor editor = openEditor("bpm$ProcAttachment.edit", procAttachmentsDs.getItem(), WindowManager.OpenType.DIALOG, procAttachmentsDs);
                editor.addCloseListener(actionId -> {
                    procAttachmentsDs.updateItem((ProcAttachment) editor.getItem());
                });
            }
        });
    }

    protected void initUploadField() {
        uploadField.addQueueUploadCompleteListener(() -> {
            Map<UUID, String> uploadsMap = uploadField.getUploadsMap();
            for (Map.Entry<UUID, String> entry : uploadsMap.entrySet()) {
                UUID fileId = entry.getKey();
                String fileName = entry.getValue();
                FileDescriptor fd = fileUploadingAPI.getFileDescriptor(fileId, fileName);
                addProcAttachment(fd);
                temporaryFileIds.put(fd, fileId);
            }
            uploadField.clearUploads();
        });
    }

    protected void initParentDsContextCommitListener() {
        getDsContext().addBeforeCommitListener(context -> {
            List<FileDescriptor> fileDescriptorsToCommit = new ArrayList<>();

            Collection<Entity> commitInstances = context.getCommitInstances();
            for (Entity commitInstance : commitInstances) {
                if (commitInstance instanceof ProcAttachment && PersistenceHelper.isNew(commitInstance)) {
                    ProcAttachment procAttachment = (ProcAttachment) commitInstance;
                    fileDescriptorsToCommit.add(procAttachment.getFile());
                }
            }

            if (!fileDescriptorsToCommit.isEmpty()) {
                commitInstances.addAll(fileDescriptorsToCommit);
                context.setCommitInstances(commitInstances);
            }
        });
    }

    public void putFilesIntoStorage() {
        for (ProcAttachment procAttachment : procAttachmentsDs.getItems()) {
            UUID fileId = temporaryFileIds.get(procAttachment.getFile());
            if (fileId != null) {
                try {
                    fileUploadingAPI.putFileIntoStorage(fileId, procAttachment.getFile());
                } catch (FileStorageException e) {
                    throw new RuntimeException("Error while uploading file", e);
                }
            }
        }
    }

    protected void addProcAttachment(FileDescriptor file) {
        ProcAttachment procAttachment = metadata.create(ProcAttachment.class);
        procAttachment.setFile(file);
        procAttachment.setProcInstance(procInstance);
        procAttachment.setProcTask(procTask);
        procAttachment.setAuthor(userSessionSource.getUserSession().getCurrentOrSubstitutedUser());
        procAttachmentsDs.addItem(procAttachment);
    }

    public void setProcInstance(ProcInstance procInstance) {
        this.procInstance = procInstance;
    }

    public void setProcTask(ProcTask procTask) {
        this.procTask = procTask;
    }
}