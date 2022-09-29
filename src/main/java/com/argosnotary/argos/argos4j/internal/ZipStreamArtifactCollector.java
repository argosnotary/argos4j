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
import com.argosnotary.argos.argos4j.FileCollector;
import com.argosnotary.argos.domain.crypto.HashUtil;
import com.argosnotary.argos.domain.link.Artifact;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystems;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ZipStreamArtifactCollector {

    private FileCollector fileCollector;

    private final PathMatcher excludeMatcher;

    public ZipStreamArtifactCollector(FileCollector fileCollector) {
        this.fileCollector = fileCollector;
        this.excludeMatcher = FileSystems.getDefault().getPathMatcher("glob:" + this.fileCollector.getExcludePatterns());
    }

    public List<Artifact> collect(InputStream inputStream) {
        List<Artifact> artifacts = new ArrayList<>();
        try (BufferedInputStream bis = new BufferedInputStream(inputStream);
             ZipInputStream zis = new ZipInputStream(bis)) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                String fileName = entry.getName();
                if (!entry.isDirectory() && !excludeMatcher.matches(Paths.get(fileName))) {
                    artifacts.add(Artifact.builder()
                            .uri(fileName.replace("\\", "/"))
                            .hash(HashUtil.createHash(zis, fileName, fileCollector.isNormalizeLineEndings()))
                            .build());
                }
            }
        } catch (IOException e) {
            throw new Argos4jError(e.getMessage(), e);
        }
        return artifacts;
    }
}
