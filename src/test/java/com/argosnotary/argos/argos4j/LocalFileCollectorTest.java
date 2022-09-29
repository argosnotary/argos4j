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
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

class LocalFileCollectorTest {

    @Test
    void enrichTest() throws MalformedURLException {
        Map<String,String> paramMap = new HashMap<String,String>();
        paramMap.put("applicationVersion", "version");
        paramMap.put("foo", "foo");
        paramMap.put("bar", "bar");

        paramMap.put("username", "username");
        Map<String,String> configMap = new HashMap<String,String>();
        configMap.putAll(paramMap);
        configMap.put("path", "otherPath");
        configMap.put("basePath", "otherBasePath");
        LocalFileCollector collector = LocalFileCollector.builder()
                .path(Paths.get("path"))
                    .build();
        collector.enrich(configMap);
        assertThat(collector.getBasePath(), is(Paths.get("otherBasePath")));
        assertThat(collector.getPath(), is(Paths.get("otherPath")));
        assertEquals(configMap, paramMap);
        
    }
    
    @Test
    void jsonTest() throws MalformedURLException, JsonMappingException, JsonProcessingException {
        String json = "{\"type\": \"LocalFileCollector\",\n" + 
                "\"path\": \"path\"}";

        LocalFileCollector expectedCollector = LocalFileCollector.builder()
                .path(Paths.get("path"))
                .build();
        ObjectMapper objectMapper = new ObjectMapper();
        LocalFileCollector collector = objectMapper.readValue(json, LocalFileCollector.class );
        assertEquals(expectedCollector, collector);
    }

}
