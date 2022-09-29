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

import com.argosnotary.argos.argos4j.ArtifactListBuilder;
import com.argosnotary.argos.argos4j.FileCollector;
import com.argosnotary.argos.domain.link.Artifact;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ArtifactListBuilderImpl implements ArtifactListBuilder {

	private final List<FileCollector> fileCollectors = new ArrayList<>();

	@Override
	public void addFileCollector(FileCollector collector) {
		fileCollectors.add(collector);
	}

	@Override
	public List<Artifact> collect() {
		return fileCollectors.stream().map(ArtifactCollectorFactory::build).map(ArtifactCollector::collect).flatMap(List::stream).collect(Collectors.toList());
	}

	@Override
	public List<List<Artifact>> collectAsArtifactLists() {
		return fileCollectors.stream().map(ArtifactCollectorFactory::build).map(ArtifactCollector::collect).collect(Collectors.toList());
	}

}
