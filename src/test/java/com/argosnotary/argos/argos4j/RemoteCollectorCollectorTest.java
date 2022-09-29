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
import static org.hamcrest.CoreMatchers.startsWith;
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

import static org.hamcrest.MatcherAssert.assertThat;

class RemoteCollectorCollectorTest {
    
    @Test
    void enrichTest() throws MalformedURLException {
        Map<String,String> paramMap = new HashMap<String,String>();
        paramMap.put("username", "username");
        paramMap.put("password", "password");
        paramMap.put("applicationVersion", "version");
        paramMap.put("foo", "foo");
        paramMap.put("bar", "bar");
        Map<String,String> configMap = new HashMap<String,String>();
        configMap.putAll(paramMap);
        configMap.put("url", "http://xld-other-collector");
        RemoteCollectorCollector collector = RemoteCollectorCollector.builder()
                    .url(new URL("http://xld-collector"))
                    .build();
        collector.enrich(configMap);
        assertTrue(configMap.isEmpty());
        assertThat(collector.getUrl(), is(new URL("http://xld-other-collector")));
        assertEquals(paramMap, collector.getParameterMap());
        
    }
    
    @Test
    void jsonTest() throws MalformedURLException, JsonMappingException, JsonProcessingException {
        String json = "{\"type\": \"RemoteCollectorCollector\",\n" + 
                "                \"url\": \"http://xld-collector\"}";

        RemoteCollectorCollector expectedCollector = RemoteCollectorCollector.builder()
                    .url(new URL("http://xld-collector"))
                    .build();
        ObjectMapper objectMapper = new ObjectMapper();
        RemoteCollectorCollector collector = objectMapper.readValue(json, RemoteCollectorCollector.class );
        assertEquals(expectedCollector, collector);
    }
    
    @Test
    void throwErrorReadingFileTest() throws MalformedURLException {
        RemoteCollectorCollector collector = RemoteCollectorCollector.builder()
                .url(new URL("http://foo-collector"))
                .build();
        Map<String,String> configMap = new HashMap<String,String>();
        configMap.put("url", "no-protocol-url");
        Argos4jError argosError = assertThrows(Argos4jError.class, () -> {
            collector.enrich(configMap);
        });
        assertThat(argosError.getMessage(), startsWith("Error in url [no-protocol-url]: [no protocol: no-protocol-url]"));
    }

}
