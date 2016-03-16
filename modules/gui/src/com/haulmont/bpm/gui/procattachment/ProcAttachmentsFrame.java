/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.bpm.gui.procattachment;

import com.haulmont.bpm.entity.ProcAttachment;
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

import javax.inject.Inject;
import java.util.*;

/**
 */
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
    protected Table procAttachmentsTable;

    @Inject
    protected UserSessionSource userSessionSource;

    protected ProcInstance procInstance;

    protected ProcTask procTask;

    protected Map<FileDescriptor, UUID> temporaryFileIds = new HashMap<>();

    @Override
    public void init(Map<String, Object> params) {
        super.init(params);

        FileDownloadHelper.initGeneratedColumn(procAttachmentsTable, "file");
        initUploadField();

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

        initParentDsContextCommitListener();
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
                    UUID fileId = temporaryFileIds.get(procAttachment.getFile());
                    if (fileId != null) {
                        try {
                            fileUploadingAPI.putFileIntoStorage(fileId, procAttachment.getFile());
                        } catch (FileStorageException e) {
                            new RuntimeException("Error while uploading file", e);
                        }
                    }
                }
            }

            if (!fileDescriptorsToCommit.isEmpty()) {
                commitInstances.addAll(fileDescriptorsToCommit);
                context.setCommitInstances(commitInstances);
            }
        });
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

    public void refresh() {
        Map<String, Object> params = new HashMap<>();
        if (procInstance != null)
            params.put("procInstance", procInstance);
        if (procTask != null)
            params.put("procTask", procTask);
        procAttachmentsDs.refresh(params);
    }

    public void commit() {
        getDsContext().commit();
    }
}