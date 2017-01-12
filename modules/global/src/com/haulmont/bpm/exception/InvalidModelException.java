/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.bpm.exception;

import com.haulmont.cuba.core.global.SupportedByClient;

@SupportedByClient
public class InvalidModelException extends RuntimeException {

    public InvalidModelException(String message) {
        super(message);
    }

    public InvalidModelException(String message, Throwable cause) {
        super(message, cause);
    }
}