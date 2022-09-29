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

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

import javax.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.net.URL;
import java.util.Map;

@Getter
@ToString(callSuper = true)
@JsonDeserialize(builder = RemoteFileCollector.RemoteFileCollectorBuilder.class)
@EqualsAndHashCode(callSuper = true)
public class RemoteFileCollector extends RemoteCollector {
    
    private static final String ARTIFACT_URI_FIELD = "artifactUri";

    /**
     * used in the remote file collector to specify the artifact uri when not set the last part of the uri is used
     */
    private String artifactUri;

    @Builder
    public RemoteFileCollector(
            @JsonProperty("excludePatterns") @Nullable String excludePatterns, 
            @JsonProperty("normalizeLineEndings") @Nullable Boolean normalizeLineEndings,
            @JsonProperty("username") @Nullable String username, 
            @JsonProperty("password") @Nullable char[] password, 
            @JsonProperty("url") @NonNull URL url,
            @JsonProperty("configMap") @Nullable Map<String, String> parameterMap,
            @JsonProperty("artifactUri") @Nullable String artifactUri) {
        super(excludePatterns, normalizeLineEndings, username, password, url, parameterMap);
        this.artifactUri = artifactUri;
    }

    @Override
    public void enrich(Map<String, String> configMap) {
        if (configMap.containsKey(ARTIFACT_URI_FIELD)) {
            this.artifactUri = configMap.get(ARTIFACT_URI_FIELD);
            configMap.remove(ARTIFACT_URI_FIELD);
        }
        super.enrich(configMap);
    }
}
