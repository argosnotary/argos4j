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

import static org.hamcrest.core.Is.is;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class Argos4jSettingsTest {
    
    private Path ARGOS_SETTINGS_FILE_PATH = Paths.get("src","test","resources","argos-settings.json");
    private Map<String, Map<String,String>> configMaps;
    private List<ReleaseCollector> releaseCollectors1;
    private List<ReleaseCollector> expectedCollectors;
    private List<ReleaseCollector> expectedCollectors2;

    @BeforeEach
    void setUp() throws Exception {
        configMaps = new HashMap<>();
        Map<String,String> xldConfig = new HashMap<String,String>();
        xldConfig.put("username", "username");
        xldConfig.put("password", "password");
        xldConfig.put("applicationVersion", "version");
        Map<String,String> xldParams = new HashMap<String,String>();
        xldParams.put("username", "username");
        xldParams.put("password", "password");
        xldParams.put("applicationVersion", "version");
        Map<String,String> gitConfig = new HashMap<String,String>();
        gitConfig.put("foo", "foo");
        gitConfig.put("bar", "bar");
        Map<String,String> gitParams = new HashMap<String,String>();
        gitParams.put("foo", "foo");
        gitParams.put("bar", "bar");
        Map<String,String> localFileCollector = new HashMap<String,String>();
        localFileCollector.put("path", "bar");
        Map<String,String> localZipFileCollector = new HashMap<String,String>();
        localZipFileCollector.put("path", "bar");
        configMaps.put("xld-collector", xldConfig);
        configMaps.put("git-collector", gitConfig);
        configMaps.put("local-file-collector", localFileCollector);
        configMaps.put("local-zip-file-collector", localZipFileCollector);
        releaseCollectors1 = new ArrayList<>();
        releaseCollectors1.add(
                ReleaseCollector.builder()
                .name("local-file-collector")
                .collector(LocalFileCollector.builder()
                        .path(Paths.get("foo"))
                        .build()).build());
        releaseCollectors1.add(
                ReleaseCollector.builder()
                .name("local-zip-file-collector")
                .collector(LocalZipFileCollector.builder()
                        .path(Paths.get("foo"))
                        .build()).build());
        releaseCollectors1.add(ReleaseCollector.builder()
                .name("xld-collector")
                .collector(RemoteCollectorCollector.builder()
                        .url(new URL("http://xld-collector"))
                        .build()).build());
        releaseCollectors1.add(ReleaseCollector.builder()
                .name("git-collector")
                .collector(RemoteCollectorCollector.builder()
                        .url(new URL("http://git-collector"))                        
                        .build()).build());
        releaseCollectors1.add(ReleaseCollector.builder()
                .name("remote-file-collector")
                .collector(RemoteFileCollector.builder()
                        .url(new URL("http://remote-file-collector"))
                        .build()).build());
        releaseCollectors1.add(ReleaseCollector.builder()
                .name("remote-zip-file-collector")
                .collector(RemoteZipFileCollector.builder()
                        .url(new URL("http://remote-zip-file-collector"))
                        .build()).build());
        expectedCollectors2 = new ArrayList<>();
        expectedCollectors2.add(
                ReleaseCollector.builder()
                .name("local-file-collector")
                .collector(LocalFileCollector.builder()
                        .path(Paths.get("foo"))
                        .build()).build());
        expectedCollectors2.add(
                ReleaseCollector.builder()
                .name("local-zip-file-collector")
                .collector(LocalZipFileCollector.builder()
                        .path(Paths.get("foo"))
                        .build()).build());
        expectedCollectors2.add(ReleaseCollector.builder()
                .name("xld-collector")
                .collector(RemoteCollectorCollector.builder()
                        .url(new URL("http://xld-collector"))
                        .build()).build());
        expectedCollectors2.add(ReleaseCollector.builder()
                .name("git-collector")
                .collector(RemoteCollectorCollector.builder()
                        .url(new URL("http://git-collector"))
                        .build()).build());
        expectedCollectors2.add(ReleaseCollector.builder()
                .name("remote-file-collector")
                .collector(RemoteFileCollector.builder()
                        .url(new URL("http://remote-file-collector"))
                        .build()).build());
        expectedCollectors2.add(ReleaseCollector.builder()
                .name("remote-zip-file-collector")
                .collector(RemoteZipFileCollector.builder()
                        .url(new URL("http://remote-zip-file-collector"))
                        .build()).build());

        expectedCollectors = new ArrayList<>();
        expectedCollectors.add(
                ReleaseCollector.builder()
                .name("local-file-collector")
                .collector(LocalFileCollector.builder()
                        .path(Paths.get("bar"))
                        .build()).build());
        expectedCollectors.add(
                ReleaseCollector.builder()
                .name("local-zip-file-collector")
                .collector(LocalZipFileCollector.builder()
                        .path(Paths.get("bar"))
                        .build()).build());
        expectedCollectors.add(ReleaseCollector.builder()
                .name("xld-collector")
                .collector(RemoteCollectorCollector.builder()
                        .url(new URL("http://xld-collector"))
                        .parameterMap(xldParams)
                        .build()).build());
        expectedCollectors.add(ReleaseCollector.builder()
                .name("git-collector")
                .collector(RemoteCollectorCollector.builder()
                        .url(new URL("http://git-collector"))
                        .parameterMap(gitParams)
                        .build()).build());
        expectedCollectors.add(ReleaseCollector.builder()
                .name("remote-file-collector")
                .collector(RemoteFileCollector.builder()
                        .url(new URL("http://remote-file-collector"))
                        .build()).build());
        expectedCollectors.add(ReleaseCollector.builder()
                .name("remote-zip-file-collector")
                .collector(RemoteZipFileCollector.builder()
                        .url(new URL("http://remote-zip-file-collector"))
                        .build()).build());
        
    }
    
    @Test
    void readSettingsTest() {
        Argos4jSettings expectedSettings = Argos4jSettings.builder()
                .argosServerBaseUrl("argos-service-url")
                .path(Arrays.asList("root", "label"))
                .keyId("keyId")
                .supplyChainName("supplyChainName")
                .releaseCollectors(releaseCollectors1)
                .build();
        Argos4jSettings settings = Argos4jSettings.readSettings(ARGOS_SETTINGS_FILE_PATH);
        assertEquals(expectedSettings, settings);
    }

    @Test
    void enrichReleaseCollectorsTest() {
        Argos4jSettings expectedSettings = Argos4jSettings.builder()
                .argosServerBaseUrl("argos-service-url")
                .path(Arrays.asList("root", "label"))
                .keyId("keyId")
                .supplyChainName("supplyChainName")
                .releaseCollectors(expectedCollectors)
                .build();
        Argos4jSettings settings = Argos4jSettings.builder()
                .argosServerBaseUrl("argos-service-url")
                .path(Arrays.asList("root", "label"))
                .keyId("keyId")
                .supplyChainName("supplyChainName")
                .releaseCollectors(releaseCollectors1)
                .build();
        settings.enrichReleaseCollectors(configMaps);
        assertEquals(expectedSettings, settings);
        
    }
    
    @Test
    void enrichReleaseCollectorsNullsTest() {
        Argos4jSettings expectedSettings = Argos4jSettings.builder()
                .argosServerBaseUrl("argos-service-url")
                .path(Arrays.asList("root", "label"))
                .keyId("keyId")
                .supplyChainName("supplyChainName")
                .releaseCollectors(expectedCollectors2)
                .build();
        Argos4jSettings settings = Argos4jSettings.builder()
                .argosServerBaseUrl("argos-service-url")
                .path(Arrays.asList("root", "label"))
                .keyId("keyId")
                .supplyChainName("supplyChainName")
                .releaseCollectors(releaseCollectors1)
                .build();
        
        settings.enrichReleaseCollectors(null);
        assertEquals(expectedSettings, settings);
        settings.enrichReleaseCollectors(new HashMap<>());
        assertEquals(expectedSettings, settings);
        expectedSettings.setReleaseCollectors(new ArrayList<>());
        settings.setReleaseCollectors(new ArrayList<>());
        settings.enrichReleaseCollectors(configMaps);
        assertEquals(expectedSettings, settings);
        expectedSettings.setReleaseCollectors(null);
        settings.setReleaseCollectors(null);
        assertEquals(expectedSettings, settings);
        
    }
    
    @Test
    void throwErrorReadingFileTest() {
    	Path path = Paths.get("zomaar-wat");
        Argos4jError argosError = assertThrows(Argos4jError.class, () -> {
            Argos4jSettings.readSettings(path);
        });
        assertThat(argosError.getMessage(), startsWith("Error on reading config file zomaar-wat: "));
    }

}
