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

import com.argosnotary.argos.argos4j.Argos4jSettings;
import com.argosnotary.argos.argos4j.ArtifactListBuilder;
import com.argosnotary.argos.argos4j.FileCollector;
import com.argosnotary.argos.argos4j.ReleaseBuilder;
import com.argosnotary.argos.domain.link.Artifact;
import com.argosnotary.argos.domain.release.ReleaseResult;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@RequiredArgsConstructor
@Slf4j
public class ReleaseBuilderImpl implements ReleaseBuilder {

    private final Argos4jSettings settings;

    private final ArtifactListBuilder artifactListBuilder;

    @Override
    public ReleaseBuilder addFileCollector(FileCollector collector) {
        artifactListBuilder.addFileCollector(collector);
        return this;
    }

    @Override
    public ReleaseResult release(char[] keyPassphrase) {
        List<List<Artifact>> artifactsList = artifactListBuilder.collectAsArtifactLists();
        log.info("release artifacts {}", artifactsList);
        return new ArgosServiceClient(settings, keyPassphrase).release(artifactsList);
    }

}
