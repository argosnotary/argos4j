/*
 * Argos Notary - A new way to secure the Software Supply Chain
 *
 * Copyright (C) 2019 - 2020 Rabobank Nederland
 * Copyright (C) 2019 - 2021 Gerard Borst <gerard.borst@argosnotary.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.argosnotary.argos.argos4j;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;

import javax.annotation.Nullable;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public abstract class RemoteCollector extends FileCollector {
    
    private static final String USERNAME_FIELD = "username";
    private static final String PASSWORD_FIELD = "password";
    private static final String URL_FIELD = "url";

    /**
     * optional for basic authentication
     */
    private String username;

    private char[] password;
    
    private Map<String, String> parameterMap;

    /**
     * the url of the remote file
     */
    private URL url;

    public RemoteCollector(
            @Nullable String excludePatterns, 
            @Nullable Boolean normalizeLineEndings, 
            @Nullable String username, 
            char[] password, 
            @NonNull URL url,
            @Nullable Map<String, String> parameterMap) {
        super(excludePatterns, normalizeLineEndings);
        this.username = username;
        this.password = password;
        this.url = url;
        this.parameterMap = parameterMap;
    }

    @Override
    public void enrich(Map<String, String> configMap) {
        super.enrich(configMap);
        if (configMap.containsKey(USERNAME_FIELD)) {
            this.username = configMap.get(USERNAME_FIELD);
            configMap.remove(USERNAME_FIELD);
        }
        if (configMap.containsKey(PASSWORD_FIELD)) {
            this.password = configMap.get(PASSWORD_FIELD).toCharArray();
            configMap.remove(PASSWORD_FIELD);
        }
        if (configMap.containsKey(URL_FIELD)) {
            try {
                this.url = new URL(configMap.get(URL_FIELD));
            } catch (MalformedURLException e) {
                throw new Argos4jError(String.format("Error in url [%s]: [%s]", configMap.get(URL_FIELD), e.getMessage()));
            }
            configMap.remove(URL_FIELD);
        }
        if (parameterMap == null) {
            parameterMap = new HashMap<>();
        }
        parameterMap.putAll(configMap);
        configMap.clear();
    }
}
