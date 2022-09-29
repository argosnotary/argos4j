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

import com.argosnotary.argos.argos4j.Argos4jError;
import com.argosnotary.argos.argos4j.LocalZipFileCollector;
import com.argosnotary.argos.domain.link.Artifact;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class ZipArtifactCollector implements ArtifactCollector {


    private final ZipStreamArtifactCollector zipStreamArtifactCollector;
    private final Path zipPath;

    public ZipArtifactCollector(LocalZipFileCollector fileCollector) {
        zipPath = fileCollector.getPath();
        zipStreamArtifactCollector = new ZipStreamArtifactCollector(fileCollector);
    }

    @Override
    public List<Artifact> collect() {
        try (FileInputStream fis = new FileInputStream(zipPath.toFile())) {
            return zipStreamArtifactCollector.collect(fis);
        } catch (IOException e) {
            throw new Argos4jError(e.getMessage(), e);
        }
    }
}
