/*
 * Copyright (c) ${YEAR} ${PACKAGE_NAME}
 */

package com.haulmont.bpm.core;

/**
 * @author gorbunkov
 * @version $Id$
 */
public class BpmException extends RuntimeException {
    public BpmException() {
    }

    public BpmException(String message) {
        super(message);
    }

    public BpmException(String message, Throwable cause) {
        super(message, cause);
    }

    public BpmException(Throwable cause) {
        super(cause);
    }

    public BpmException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
