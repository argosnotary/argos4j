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

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@Builder
@Getter
@Setter
@ToString
@EqualsAndHashCode
@JsonDeserialize(builder = Argos4jSettings.Argos4jSettingsBuilder.class)
@NoArgsConstructor
@AllArgsConstructor
public class Argos4jSettings implements Serializable {
    
    @JsonProperty("supplyChainName")
    private String supplyChainName;
    @JsonProperty("path")
    private List<String> path;
    @JsonProperty("keyId")
    private String keyId;
    @JsonProperty("keyPassphrase")
    private String keyPassphrase;
    @JsonProperty("argosServerBaseUrl")
    private String argosServerBaseUrl;
    @JsonProperty("releaseCollectors")
    private List<ReleaseCollector> releaseCollectors;
    
    public static Argos4jSettings readSettings(Path configFilePath) {
        Argos4jSettings argos4jSettings;
        try {
            String json = IOUtils.toString(configFilePath.toUri(), UTF_8);
            ObjectMapper objectMapper = new ObjectMapper();
            argos4jSettings = objectMapper.readValue(json, Argos4jSettings.class );
        } catch (IOException e) {
                throw new Argos4jError(String.format("Error on reading config file %s: %s", configFilePath.toString(), e.getMessage()));
        }
        return argos4jSettings;
    }
    
    public void enrichReleaseCollectors(Map<String, Map<String, String>> configMaps) {
        if (releaseCollectors == null 
                || releaseCollectors.isEmpty() 
                || configMaps == null 
                || configMaps.isEmpty()) {
            return;
        }
        releaseCollectors.stream()
            .filter(c -> configMaps.containsKey(c.getName()))
            .forEach(c -> c.getCollector().enrich(configMaps.get(c.getName())));
    }

}
