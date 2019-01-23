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

package com.haulmont.bpm.core;

import java.io.Serializable;
import java.util.*;

/**
 * Class is used for storing a result of process user task. It stores a number of decisions
 * made during the task execution
 */
public class ProcTaskResult implements Serializable {

    protected Map<String, List<UUID>> outcomes = new HashMap<>();

    /**
     * Adds a new decision to the result
     * @param outcome outcome name
     * @param userId id of the user who made a decision
     */
    public void addOutcome(String outcome, UUID userId) {
        List<UUID> users = outcomes.get(outcome);
        if (users == null) {
            users = new ArrayList<>();
            outcomes.put(outcome, users);
        }
        users.add(userId);
    }

    /**
     * Returns a number of process actors who finished the task with a given outcome.
     * @param outcome outcome name
     * @return a number of process actors who finished the task with a given outcome
     */
    public int count(String outcome) {
        List<UUID> users = outcomes.get(outcome);
        return users == null ? 0 : users.size();
    }

    /**
     * Checks whether someone completed the task with a given outcome
     * @param outcome outcome name
     * @return true if someone completed the task with a given outcome
     */
    public boolean exists(String outcome) {
        List<UUID> users = outcomes.get(outcome);
        return users != null && users.size() > 0;
    }

    /**
     * @return a map with all outcomes made during a task execution. The key is an outcome name
     * and the value is a list of users ids
     */
    public Map<String, List<UUID>> getOutcomes() {
        return outcomes;
    }
}
