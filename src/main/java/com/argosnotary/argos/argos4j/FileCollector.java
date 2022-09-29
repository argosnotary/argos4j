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
import lombok.ToString;

import javax.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.io.Serializable;
import java.util.Map;
import java.util.Optional;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME, 
        include = JsonTypeInfo.As.PROPERTY, 
        property = "type")
      @JsonSubTypes({ 
        @Type(value = LocalFileCollector.class, name = "LocalFileCollector"), 
        @Type(value = LocalZipFileCollector.class, name = "LocalZipFileCollector"), 
        @Type(value = RemoteCollectorCollector.class, name = "RemoteCollectorCollector"), 
        @Type(value = RemoteFileCollector.class, name = "RemoteFileCollector"), 
        @Type(value = RemoteZipFileCollector.class, name = "RemoteZipFileCollector")
      })
@Getter
@EqualsAndHashCode
@ToString
public abstract class FileCollector implements Serializable {

    public static final String DEFAULT_EXCLUDE_PATTERNS = "{**.git/**,**.git\\\\**}";
    
    private static final String EXCLUDE_PATTERNS_FIELD = "excludePatterns";

    private String excludePatterns;

    private boolean normalizeLineEndings;

    
    public FileCollector(@Nullable String excludePatterns, @Nullable Boolean normalizeLineEndings) {
        this.excludePatterns = Optional.ofNullable(excludePatterns).orElse(DEFAULT_EXCLUDE_PATTERNS);
        this.normalizeLineEndings = Optional.ofNullable(normalizeLineEndings).orElse(false);
    }

    public void enrich(Map<String, String> configMap) {
        if (configMap.containsKey(EXCLUDE_PATTERNS_FIELD)) {
            this.excludePatterns = configMap.get(EXCLUDE_PATTERNS_FIELD);
            configMap.remove(EXCLUDE_PATTERNS_FIELD);
        }
    }
}
