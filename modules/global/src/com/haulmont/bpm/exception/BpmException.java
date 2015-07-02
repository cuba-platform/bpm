/*
 * Copyright (c) 2008-2015 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.bpm.exception;

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
