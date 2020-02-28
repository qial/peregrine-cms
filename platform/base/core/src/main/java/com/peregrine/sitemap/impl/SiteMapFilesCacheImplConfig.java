package com.peregrine.sitemap.impl;

/*-
 * #%L
 * platform base - Core
 * %%
 * Copyright (C) 2017 headwire inc.
 * %%
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * #L%
 */

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition(name = "%config.name", description = "%config.description")
public @interface SiteMapFilesCacheImplConfig {

    @AttributeDefinition(name = "%location.name", description = "%location.description")
    String location() default "/var/sitemaps/files";

    @AttributeDefinition(name = "%maxFileSize.name", description = "%maxFileSize.description")
    int maxFileSize() default 52428800;

    @AttributeDefinition(name = "%maxEntriesCount.name", description = "%maxEntriesCount.description")
    int maxEntriesCount() default 50000;
}
