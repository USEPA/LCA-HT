/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.jena.atlas.web.auth;

import java.net.URI;

/**
 * <p>
 * A HTTP Authenticator which provides authentication via user name and password
 * combinations. Works with the basic and digest HTTP authentication schemes.
 * </p>
 * <p>
 * See {@link SimpleNTAuthenticator} for an implementation that works for the
 * NTLM authentication scheme.
 * </p>
 * <p>
 * This authenticator will present the given credentials to any server that
 * request authentication even though the credentials may not be valid there. It
 * is typically more secure to use the {@link ScopedAuthenticator} instead which
 * only presents credentials to specific servers.
 * </p>
 * 
 */
public class SimpleAuthenticator extends AbstractCredentialsAuthenticator {

    String username;
    char[] password;

    /**
     * Creates a new authenticator
     * 
     * @param username
     *            Username
     * @param password
     *            Password
     */
    public SimpleAuthenticator(String username, char[] password) {
        this.username = username;
        this.password = password;
    }

    @Override
    protected boolean hasUserName(URI target) {
        return this.username != null;
    }

    @Override
    protected String getUserName(URI target) {
        return this.username;
    }

    @Override
    protected boolean hasPassword(URI target) {
        return this.password != null;
    }

    @Override
    protected char[] getPassword(URI target) {
        return this.password;
    }

}
