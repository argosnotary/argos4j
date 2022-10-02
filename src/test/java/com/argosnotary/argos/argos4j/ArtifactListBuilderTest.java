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
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.hamcrest.MatcherAssert.assertThat;

import static java.util.Collections.emptyList;

import com.argosnotary.argos.argos4j.internal.LocalArtifactCollector;
import com.argosnotary.argos.argos4j.rest.api.model.Artifact;

@ExtendWith(MockitoExtension.class)
class ArtifactListBuilderTest {
	
	@TempDir
    static File sharedTempDir;
	
	LocalFileCollector collector1;
	
	LocalFileCollector collector2;
	
	ArtifactListBuilder artifactListBuilder;

    private static File fileDir1;
    private static File fileDir2;
    
    Artifact artifact1;
    Artifact artifact2;

	@BeforeEach
	void setUp() throws Exception {
		artifact1 = new Artifact();
		artifact1.setUri(sharedTempDir.getPath()+"/filedir1/text1.txt");
		artifact1.setHash("cb6bdad36690e8024e7df13e6796ae6603f2cb9cf9f989c9ff939b2ecebdcb91");
	    artifact2 = new Artifact();
		artifact2.setUri(sharedTempDir.getPath()+"/filedir2/text2.txt");
		artifact2.setHash("c1013e3865e452515184b3db2cb812872b429c5b5cf35bafdf17ae41c02a93cf");
		artifactListBuilder = Argos4j.getArtifactListBuilder();
        fileDir1 = new File(sharedTempDir, "filedir1");
        fileDir1.mkdir();
        FileUtils.write(new File(fileDir1, "text1.txt"), "cool dit\r\nan other line", "UTF-8");
        
        collector1 = LocalFileCollector.builder().path(fileDir1.toPath()).build();

        fileDir2 = new File(sharedTempDir, "filedir2");
        fileDir2.mkdir();
        FileUtils.write(new File(fileDir2, "text2.txt"), "dit is ook cool\r\nan other line", "UTF-8");
        
        collector2 = LocalFileCollector.builder().path(fileDir2.toPath()).build();
		
	}

	@Test
	void addFileCollectorAndCollectEmpty() {
		List<Artifact> artifacts = artifactListBuilder.collect();
		assertThat(artifacts, is(emptyList()));
		artifactListBuilder.addFileCollector(collector1);
		artifacts = artifactListBuilder.collect();
		assertEquals(1, artifacts.size());
		assertThat(artifacts, is(Arrays.asList(artifact1)));
		artifactListBuilder.addFileCollector(collector2);
		artifacts = artifactListBuilder.collect();
		assertEquals(2, artifacts.size());
		assertThat(artifacts, is(Arrays.asList(artifact1, artifact2)));
		
	}

}
