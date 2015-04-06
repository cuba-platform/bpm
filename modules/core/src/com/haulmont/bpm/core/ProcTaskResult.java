/*
 * Copyright (c) ${YEAR} ${PACKAGE_NAME}
 */

package com.haulmont.bpm.core;

import javax.xml.crypto.dsig.spec.HMACParameterSpec;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author gorbunkov
 * @version $Id$
 */
public class ProcTaskResult implements Serializable {

    protected Map<String, List<String>> outcomes = new HashMap<>();

    public void addOutcome(String outcome, String userLogin) {
        List<String> users = outcomes.get(outcome);
        if (users == null) {
            users = new ArrayList<>();
            outcomes.put(outcome, users);
        }
        users.add(userLogin);
    }

    public int getOutcomesCount(String outcome) {
        List<String> users = outcomes.get(outcome);
        return users == null ? 0 : users.size();
    }

    public Map<String, List<String>> getOutcomes() {
        return outcomes;
    }
}
