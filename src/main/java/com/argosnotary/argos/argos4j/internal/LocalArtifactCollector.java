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
import com.argosnotary.argos.argos4j.LocalFileCollector;
import com.argosnotary.argos.domain.crypto.HashUtil;
import com.argosnotary.argos.domain.link.Artifact;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
public class LocalArtifactCollector implements ArtifactCollector {

    private final LocalFileCollector fileCollector;
    private final Optional<Path> optionalBasePath;
    private List<Artifact> artifacts = new ArrayList<>();
    private final PathMatcher excludeMatcher;


    public LocalArtifactCollector(LocalFileCollector fileCollector) {
        this.fileCollector = fileCollector;

        this.excludeMatcher = FileSystems.getDefault().getPathMatcher("glob:" + this.fileCollector.getExcludePatterns());

        optionalBasePath = Optional.ofNullable(fileCollector.getBasePath());

        if (optionalBasePath.map(Path::toFile).filter(file -> !file.exists()).isPresent()) {
            throw new Argos4jError("Base path " + fileCollector.getBasePath() + " doesn't exist");
        }
    }

    @Override
    public List<Artifact> collect() {
        Path path = fileCollector.getPath();
        if (optionalBasePath.isPresent() && !path.startsWith(optionalBasePath.get())) {
            throw new Argos4jError("uri does not contain base path");
        }
        recurseAndCollect(path);
        return artifacts;
    }

    private void recurseAndCollect(Path path) {
        if (excludeMatcher.matches(path)) {
            return;
        }

        if (!path.toFile().exists()) {
            log.warn("path: {} does not exist, skipping..", path);
        } else {
            if (Files.isRegularFile(path)) {
                // normalize path separator and create Artifact
                String uri = optionalBasePath.map(basePath -> basePath.relativize(path)).map(Path::toString).orElse(path.toString());

                this.artifacts.add(Artifact.builder().uri(uri.replace("\\", "/"))
                        .hash(createHash(path.toString())).build());
            } else {
                if ((Files.isSymbolicLink(path) && fileCollector.isFollowSymlinkDirs())
                        || (path.toFile().isDirectory() && !Files.isSymbolicLink(path))) {
                    collectDirectory(path);
                }
            }
        }
    }

    private void collectDirectory(Path path) {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(path)) {
            stream.spliterator().forEachRemaining(this::recurseAndCollect);
        } catch (IOException e) {
            throw new Argos4jError(e.getMessage());
        }
    }

    private String createHash(String filename) {
        try (FileInputStream fis = new FileInputStream(filename);
             BufferedInputStream bis = new BufferedInputStream(fis)) {
            return HashUtil.createHash(bis, filename, fileCollector.isNormalizeLineEndings());
        } catch (IOException e) {
            throw new Argos4jError("The file " + filename + " couldn't be recorded: " + e.getMessage());
        }
    }

}
