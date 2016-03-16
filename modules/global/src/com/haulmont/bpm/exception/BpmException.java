/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.bpm.exception;

/**
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
