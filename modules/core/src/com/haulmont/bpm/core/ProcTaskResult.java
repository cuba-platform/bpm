/*
 * Copyright (c) 2008-2015 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.bpm.core;

import java.io.Serializable;
import java.util.*;

/**
 * @author gorbunkov
 * @version $Id$
 */
public class ProcTaskResult implements Serializable {

    protected Map<String, List<UUID>> outcomes = new HashMap<>();

    public void addOutcome(String outcome, UUID userId) {
        List<UUID> users = outcomes.get(outcome);
        if (users == null) {
            users = new ArrayList<>();
            outcomes.put(outcome, users);
        }
        users.add(userId);
    }

    public int count(String outcome) {
        List<UUID> users = outcomes.get(outcome);
        return users == null ? 0 : users.size();
    }

    public boolean exists(String outcome) {
        List<UUID> users = outcomes.get(outcome);
        return users != null && users.size() > 0;
    }

    public Map<String, List<UUID>> getOutcomes() {
        return outcomes;
    }
}
