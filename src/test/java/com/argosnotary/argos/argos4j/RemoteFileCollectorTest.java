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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

class RemoteFileCollectorTest {

    @Test
    void enrichTest() throws MalformedURLException {
        Map<String,String> paramMap = new HashMap<String,String>();
        paramMap.put("applicationVersion", "version");
        paramMap.put("foo", "foo");
        paramMap.put("bar", "bar");
        Map<String,String> configMap = new HashMap<String,String>();
        configMap.putAll(paramMap);

        configMap.put("username", "username");
        configMap.put("password", "password");
        configMap.put("artifactUri", "otherArtifactUri");
        RemoteFileCollector collector = RemoteFileCollector.builder()
                .artifactUri("artifactUri")
                .url(new URL("http://remote-collector"))
                    .build();
        collector.enrich(configMap);
        assertTrue(configMap.isEmpty());
        assertThat(collector.getUrl(), is(new URL("http://remote-collector")));
        assertThat(collector.getArtifactUri(), is("otherArtifactUri"));
        assertEquals(paramMap, collector.getParameterMap());
        
    }
    
    @Test
    void jsonTest() throws MalformedURLException, JsonMappingException, JsonProcessingException {
        String json = "{\"type\": \"RemoteFileCollector\",\n" + 
                "\"url\": \"http://remote-collector\",\"artifactUri\": \"artifactUri\"}";

        RemoteFileCollector expectedCollector = RemoteFileCollector.builder()
                    .url(new URL("http://remote-collector"))
                    .artifactUri("artifactUri")
                    .build();
        ObjectMapper objectMapper = new ObjectMapper();
        RemoteFileCollector collector = objectMapper.readValue(json, RemoteFileCollector.class );
        assertEquals(expectedCollector, collector);
    }
}
