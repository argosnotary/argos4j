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

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

@Getter
@ToString(callSuper = true)
@JsonDeserialize(builder = LocalZipFileCollector.LocalZipFileCollectorBuilder.class)
@EqualsAndHashCode(callSuper = true)
public class LocalZipFileCollector extends FileCollector {
    
    private static final String PATH_FIELD = "path";

    /**
     * is the zip file path
     */
    private Path path;

    @Builder
    public LocalZipFileCollector(
            @JsonProperty("excludePatterns") @Nullable String excludePatterns, 
            @JsonProperty("normalizeLineEndings") @Nullable Boolean normalizeLineEndings,
            @JsonProperty("path") @NonNull Path path) {
        super(excludePatterns, normalizeLineEndings);
        this.path = path;
    }

    @Override
    public void enrich(Map<String, String> configMap) {
        super.enrich(configMap);
        if (configMap.containsKey(PATH_FIELD)) {
            this.path = Paths.get(configMap.get(PATH_FIELD));
            configMap.remove(PATH_FIELD);
        }
    }
}
