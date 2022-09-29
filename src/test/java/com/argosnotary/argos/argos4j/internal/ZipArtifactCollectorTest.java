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
package com.argosnotary.argos.argos4j.internal;

import com.argosnotary.argos.argos4j.LocalZipFileCollector;
import com.argosnotary.argos.domain.link.Artifact;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;

class ZipArtifactCollectorTest {

    private ArtifactCollector collector;

    @BeforeEach
    void setUp() {
        collector = ArtifactCollectorFactory.build(LocalZipFileCollector
                .builder()
                .path(Paths.get(ZipArtifactCollectorTest.class.getResource("/argos-test-app-1.0-SNAPSHOT.dar").getPath())).build());
    }


    @Test
    void collect() {
        List<Artifact> collect = collector.collect();
        assertThat(collect, contains(
                Artifact.builder().uri("META-INF/MANIFEST.MF").hash("53e5e0a85a6aefa827e2fe34748cd1030c02a492bd9b309dc2f123258a218901").build(),
                Artifact.builder().uri("argos-test-app.war/argos-test-app.war").hash("f5e94511d66ffbd76e164b7a5c8ec91727f6435dabce365b53e7f4221edd88ae").build(),
                Artifact.builder().uri("deployit-manifest.xml").hash("9c1a8531bbd86414d6cc9929daa19d06a05cf3ca335b4ca7abe717c8f2b5f3ec").build()));
    }
}