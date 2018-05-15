/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.bpm.gui.form.standard;

import com.google.common.base.Strings;
import com.haulmont.bpm.entity.ProcActor;
import com.haulmont.bpm.entity.ProcAttachment;
import com.haulmont.bpm.entity.ProcInstance;
import com.haulmont.bpm.entity.ProcTask;
import com.haulmont.bpm.form.ProcFormDefinition;
import com.haulmont.bpm.form.ProcFormParam;
import com.haulmont.bpm.gui.form.AbstractProcForm;
import com.haulmont.bpm.gui.procactor.ProcActorsFrame;
import com.haulmont.bpm.gui.procattachment.ProcAttachmentsFrame;
import com.haulmont.cuba.gui.WindowParam;
import com.haulmont.cuba.gui.components.Label;
import com.haulmont.cuba.gui.components.TextArea;
import com.haulmont.cuba.gui.data.CollectionDatasource;
import com.haulmont.cuba.gui.data.Datasource;

import javax.inject.Inject;
import java.util.Map;
import java.util.UUID;

/**
 * Standard process form that is used for:
 * <ol>
 *     <li>entering procTask comment</li>
 *     <li>adding process attachment</li>
 *     <li>setting process actors</li>
 * </ol>
 * Visibility of components (comment field, frames for attachments and actors) are defined by
 * parameters of {@code formDefinition}
 */
public class StandardProcForm extends AbstractProcForm {

    @Inject
    protected TextArea comment;

    @Inject
    protected Label procAttachmentsLabel;

    @Inject
    protected ProcAttachmentsFrame procAttachmentsFrame;

    @Inject
    protected Label procActorsLabel;

    @Inject
    protected ProcActorsFrame procActorsFrame;

    @WindowParam(name = "procTask")
    protected ProcTask procTask;

    @WindowParam(name = "procInstance", required = true)
    protected ProcInstance procInstance;

    @WindowParam(name = "formDefinition", required = true)
    protected ProcFormDefinition formDefinition;

    @WindowParam
    protected String caption;

    @WindowParam(name = "isStartForm")
    protected Boolean isStartForm;

    @Inject
    protected Datasource<ProcInstance> procInstanceDs;

    @Inject
    protected CollectionDatasource<ProcActor, UUID> procActorsDs;

    @Inject
    protected CollectionDatasource<ProcAttachment, UUID> procAttachmentsDs;

    protected static final String COMMENT_REQUIRED_PARAM = "commentRequired";
    protected static final String PROC_ACTORS_VISIBLE_PARAM = "procActorsVisible";
    protected static final String ATTACHMENTS_VISIBLE_PARAM = "attachmentsVisible";

    protected boolean procActorsVisible;
    protected boolean procAttachmentsVisible;

    @Override
    public void init(Map<String, Object> params) {
        super.init(params);

        getDialogOptions()
                .setResizable(true)
                .setWidth("700px");

        ProcFormParam commentRequiredParam = formDefinition.getParam(COMMENT_REQUIRED_PARAM);
        if (commentRequiredParam != null && "true".equals(commentRequiredParam.getValue())) {
            comment.setRequired(true);
        }

        ProcFormParam procActorsVisibleParam = formDefinition.getParam(PROC_ACTORS_VISIBLE_PARAM);
        procActorsVisible = procActorsVisibleParam != null && "true".equals(procActorsVisibleParam.getValue());

        ProcFormParam procAttachmentsVisibleParam = formDefinition.getParam(ATTACHMENTS_VISIBLE_PARAM);
        procAttachmentsVisible = procAttachmentsVisibleParam != null && "true".equals(procAttachmentsVisibleParam.getValue());

        procActorsLabel.setVisible(procActorsVisible);
        procActorsFrame.setVisible(procActorsVisible);

        procAttachmentsLabel.setVisible(procAttachmentsVisible);
        procAttachmentsFrame.setVisible(procAttachmentsVisible);

        if (procAttachmentsVisible && (procTask != null || procInstance != null)) {
            procAttachmentsFrame.setProcTask(procTask);
            procAttachmentsFrame.setProcInstance(procInstance != null ? procInstance : procTask.getProcInstance());
        }

        if (procActorsVisible) {
            procActorsFrame.setProcInstance(procInstance);
        }

        if (!Strings.isNullOrEmpty(caption)) {
            setCaption(caption);
        }

        procInstanceDs.setItem(procInstance);

        //if the current form is used as a start process form, we shouldn't persist entities. They will be persisted
        //by the ProcessRuntimeManager.startProcess() method
        if (Boolean.TRUE.equals(isStartForm)) {
            procInstanceDs.setAllowCommit(false);
            procActorsDs.setAllowCommit(false);
            procAttachmentsDs.setAllowCommit(false);
        }
    }

    @Override
    public void ready() {
        super.ready();
    }

    @Override
    public String getComment() {
        return comment.getValue();
    }

    public void onWindowCommit() {
        if (!validateAll()) {
            return;
        }
        getDsContext().commit();
        procAttachmentsFrame.putFilesIntoStorage();
        close(COMMIT_ACTION_ID);
    }

    public void onWindowClose() {
        close(CLOSE_ACTION_ID);
    }
}