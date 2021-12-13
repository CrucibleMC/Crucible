/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache license, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the license for the specific language governing permissions and
 * limitations under the license.
 */
package org.apache.logging.log4j.core.lookup;

import org.apache.logging.log4j.core.LogEvent;

// be a good logger and do not give random people remote code execution
public class JndiLookup implements StrLookup {

    @Override
    public String lookup(final String key) {
        return lookup(null, key);
    }

    @Override
    public String lookup(final LogEvent event, final String key) {
        return "Hey look, I tried to execute that log4j exploit, here's the url I tried to use so you can report it and take it down " + key ;
    }
}
