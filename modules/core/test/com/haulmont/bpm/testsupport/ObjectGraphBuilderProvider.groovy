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

package com.haulmont.bpm.testsupport

import com.haulmont.cuba.core.EntityManager
import com.haulmont.cuba.core.global.AppBeans
import com.haulmont.cuba.core.global.Metadata
import org.codehaus.groovy.runtime.InvokerHelper

/**
 *
 */
class ObjectGraphBuilderProvider {

    static final String DEFAULT_ENTITY_PACKAGE = "com.haulmont.bpm.entity"

    /**
     * Creates {@link ObjectGraphBuilder} that persists all created objects
     * @param em - entityManager
     * @return
     */
    static ObjectGraphBuilder createBuilder(EntityManager em) {
        def builder = new ObjectGraphBuilder()

        builder.classNameResolver = DEFAULT_ENTITY_PACKAGE

        builder.identifierResolver = {nodeName -> "testId"}

        builder.newInstanceResolver = { klass, attributes ->
            def metadata = AppBeans.get(Metadata.class)
            def createdObject = metadata.create(klass)
            em.persist(createdObject)
            return createdObject
        }

        //childPropertySetter can create empty collections and place node object to it
        //if collection property of parent object is null
        builder.childPropertySetter = {parent, child, parentName, propertyName ->
            try {
                def field = parent.class.getDeclaredField(propertyName)
                if (Collection.class.isAssignableFrom(field.type)) {
                    def collectionProperty = InvokerHelper.getProperty(parent, propertyName)
                    if (collectionProperty == null) {
                        InvokerHelper.setProperty(parent, propertyName, [child])
                    } else {
                        ((Collection) collectionProperty).add(child)
                    }
                } else {
                    InvokerHelper.setProperty(parent, propertyName, child)
                }
            } catch (MissingPropertyException mpe) {
                // ignore
            }
        }

        return builder
    }
}
