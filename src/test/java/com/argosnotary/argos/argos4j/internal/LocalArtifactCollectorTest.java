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
import com.argosnotary.argos.domain.link.Artifact;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.Comparator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.condition.OS.WINDOWS;

class LocalArtifactCollectorTest {

    @TempDir
    static File sharedTempDir;

    private static File onFileDir;
    private static File multilevelDir;
    private static File ignoredFile;

    @BeforeAll
    static void setUp() throws IOException {
        onFileDir = new File(sharedTempDir, "on file dir");
        onFileDir.mkdir();
        FileUtils.write(new File(onFileDir, "text.txt"), "cool dit\r\nan other line", "UTF-8");
        ignoredFile = new File(onFileDir, "notMe.git");
        FileUtils.write(ignoredFile, "ignore", "UTF-8");

        multilevelDir = new File(sharedTempDir, "multilevel dir");
        multilevelDir.mkdir();
        FileUtils.write(new File(multilevelDir, "level1.txt"), "level 1 file\ran other line", "UTF-8");
        File level1 = new File(multilevelDir, "level1");
        level1.mkdir();
        FileUtils.write(new File(level1, "level2.txt"), "level 2 file\nan other line", "UTF-8");
        FileUtils.writeByteArrayToFile(new File(level1, "level2.zip"), createZip("i am in a zip file"));

        File level1Empty = new File(multilevelDir, "level1Empty");
        level1Empty.mkdir();
        
        File level1Gone = new File(multilevelDir, "level1Gone");
        level1Gone.mkdir();

        try {
            Files.createSymbolicLink(new File(onFileDir, "linkdir").toPath(), level1.toPath());
            Files.createSymbolicLink(new File(onFileDir, "linkdirGone").toPath(), level1Gone.toPath());
        } catch (FileSystemException e){
            System.out.println("probably the test is running on windows ignore this error");
        }

        assertTrue(level1Gone.delete());

    }

    static private byte[] createZip(String content) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            ZipEntry entry = new ZipEntry("test.txt");
            entry.setTime(43323342L);
            zos.putNextEntry(entry);
            zos.write(content.getBytes(StandardCharsets.UTF_8));
            zos.closeEntry();
        }
        return baos.toByteArray();
    }

    @Test
    @DisabledOnOs(WINDOWS)
    void collectOnFileWithBasePath() {
        ArtifactCollector collector = ArtifactCollectorFactory.build(LocalFileCollector.builder()
                .basePath(onFileDir.toPath())
                .path(onFileDir.toPath())
                .normalizeLineEndings(true)
                .build());
        List<Artifact> artifacts = sort(collector.collect());
        assertThat(artifacts, hasSize(4));
        checkLevel2File(artifacts.get(0), "linkdir");
        checkLevel2Zip(artifacts.get(1), "linkdir");
        checkTextartifact(artifacts.get(3));
    }
    
    @Test
    void collectNotEmptyDir() throws IOException {
        File notEmptyDir = new File(sharedTempDir, "notEmpty");
        notEmptyDir.mkdir();
        FileUtils.write(new File(notEmptyDir, ".gitignore"), "this should be collected", "UTF-8");
        File gitDir = new File(notEmptyDir, ".git");
        gitDir.mkdir();
        FileUtils.write(new File(gitDir, "not me.txt"), "ignore file", "UTF-8");
        File linkDir = new File(notEmptyDir, "link");
        linkDir.mkdir();
        FileUtils.write(new File(linkDir, "text.txt"), "cool dit\r\nan other line", "UTF-8");
        ArtifactCollector collector = ArtifactCollectorFactory.build(LocalFileCollector.builder()
                .basePath(notEmptyDir.toPath())
                .path(notEmptyDir.toPath())
                .normalizeLineEndings(true)
                .build());
        List<Artifact> artifacts = sort(collector.collect());
        assertThat(artifacts, hasSize(2));
    }

    private void checkTextartifact(Artifact artifact) {
        assertThat(artifact.getUri(), is("text.txt"));
        assertThat(artifact.getHash(), is("616e953d8784d4e15a17055a91ac7539bca32350850ac5157efffdda6a719a7b"));
    }

    @Test
    void collectMultiLevelWithBasePath() {
        ArtifactCollector collector = ArtifactCollectorFactory.build(LocalFileCollector.builder()
                .basePath(multilevelDir.toPath())
                .path(multilevelDir.toPath())
                .normalizeLineEndings(true)
                .build());
        List<Artifact> artifacts = sort(collector.collect());
        assertThat(artifacts, hasSize(3));
        Artifact artifact1 = artifacts.get(0);
        assertThat(artifact1.getUri(), is("level1.txt"));
        assertThat(artifact1.getHash(), is("6f67ddc1ecfc504571641ed25caff36ccf525edf75263c8d91b5e3de66410713"));

        checkLevel2File(artifacts.get(1), "level1");
        checkLevel2Zip(artifacts.get(2), "level1");
    }

    private void checkLevel2Zip(Artifact artifact, String baseDir) {
        assertThat(artifact.getUri(), is(baseDir + "/level2.zip"));
        assertThat(artifact.getHash(), is("86319fa43d73f21d33522a36f1fd75bb0ba48bd1381efa945b3e0f2ca74a4d84"));
    }

    private void checkLevel2File(Artifact artifact, String baseDir) {
        assertThat(artifact.getUri(), is(baseDir + "/level2.txt"));
        assertThat(artifact.getHash(), is("c5721bee86deedfd45ad61431a7e43a184782fd9aaa1620d750de06a72984300"));
    }

    @Test
    void collectOnFileWithBasePathNotFollowLinks() {
        ArtifactCollector collector = ArtifactCollectorFactory.build(LocalFileCollector.builder()
                .basePath(onFileDir.toPath())
                .path(onFileDir.toPath())
                .normalizeLineEndings(true)
                .followSymlinkDirs(false)
                .build());
        List<Artifact> artifacts = sort(collector.collect());
        assertThat(artifacts, hasSize(2));
        checkTextartifact(artifacts.get(1));
    }

    private List<Artifact> sort(List<Artifact> artifacts) {
        artifacts.sort(Comparator.comparing(Artifact::getUri));
        return artifacts;
    }

    @Test
    void collectOnFileWithBasePathNotFollowLinksAndNormalizeLineEndings() {

        ArtifactCollector collector = ArtifactCollectorFactory.build(LocalFileCollector.builder()
                .basePath(onFileDir.toPath())
                .path(onFileDir.toPath())
                .normalizeLineEndings(false)
                .followSymlinkDirs(false)
                .build());
        List<Artifact> artifacts = sort(collector.collect());
        assertThat(artifacts, hasSize(2));
        Artifact artifact = artifacts.get(1);
        assertThat(artifact.getUri(), is("text.txt"));
        assertThat(artifact.getHash(), is("cb6bdad36690e8024e7df13e6796ae6603f2cb9cf9f989c9ff939b2ecebdcb91"));
    }

    @Test
    void collectOnFileWithExcludePattern() {
        ArtifactCollector collector = ArtifactCollectorFactory.build(LocalFileCollector.builder()
                .basePath(onFileDir.toPath())
                .path(onFileDir.toPath())
                .excludePatterns("**.txt")
                .followSymlinkDirs(false)
                .build());
        List<Artifact> artifacts = sort(collector.collect());
        assertThat(artifacts, hasSize(1));
        Artifact artifact = artifacts.get(0);
        assertThat(artifact.getUri(), endsWith("notMe.git"));
        assertThat(artifact.getHash(), is("5f0af516936c6ab13dfce52362f84a3c0aa8d87aca8f2bcaf55ad4e1e0178034"));
    }

    @Test
    void collectOnFileWithDefaultExcludePattern() throws IOException {
        File testDir = new File(sharedTempDir, "test");
        testDir.mkdir();
        ArtifactCollector collector = ArtifactCollectorFactory.build(LocalFileCollector.builder()
                .basePath(testDir.toPath())
                .path(testDir.toPath())
                .build());
        File gitDir = new File(testDir, ".git");
        gitDir.mkdir();
        File dir2 = new File(testDir, "dir2");
        dir2.mkdir();
        FileUtils.write(new File(gitDir, "foo.txt"), "cool dit\r\nan other line", "UTF-8");
        FileUtils.write(new File(testDir, ".gitignore"), "cool dit\r\nan other line", "UTF-8");
        FileUtils.write(new File(dir2, ".gitignore"), "cool dit\r\nan other line", "UTF-8");
        FileUtils.write(new File(testDir, "foo.link"), "cool dit\r\nan other line", "UTF-8");
        List<Artifact> artifacts = sort(collector.collect());
        assertThat(artifacts, hasSize(3));
        Artifact artifact = artifacts.get(0);
        assertThat(artifact.getUri(), is(".gitignore"));
        assertThat(artifact.getHash(), is("cb6bdad36690e8024e7df13e6796ae6603f2cb9cf9f989c9ff939b2ecebdcb91"));
        artifact = artifacts.get(1);
        assertThat(artifact.getUri(), is("dir2/.gitignore"));
        assertThat(artifact.getHash(), is("cb6bdad36690e8024e7df13e6796ae6603f2cb9cf9f989c9ff939b2ecebdcb91"));
        artifact = artifacts.get(2);
        assertThat(artifact.getUri(), is("foo.link"));
        assertThat(artifact.getHash(), is("cb6bdad36690e8024e7df13e6796ae6603f2cb9cf9f989c9ff939b2ecebdcb91"));
    }

    @Test
    void collectWrongBasePath() {
        Argos4jError error = assertThrows(Argos4jError.class, () -> ArtifactCollectorFactory.build(LocalFileCollector.builder()
                .basePath(Paths.get(URI.create("notthere").getPath()))
                .path(onFileDir.toPath())
                .build()));
        assertThat(error.getMessage(), is("Base path notthere doesn't exist"));
    }

    @Test
    void collectOnFileWithoutBasePathNotFollowLinks() {
        ArtifactCollector collector = ArtifactCollectorFactory.build(LocalFileCollector.builder()
                .path(onFileDir.toPath())
                .followSymlinkDirs(false)
                .normalizeLineEndings(true)
                .followSymlinkDirs(false)
                .build());
        List<Artifact> artifacts = sort(collector.collect());
        assertThat(artifacts, hasSize(2));
        Artifact artifact = artifacts.get(1);
        assertThat(artifact.getUri(), endsWith("on file dir/text.txt"));
        assertThat(artifact.getHash(), is("616e953d8784d4e15a17055a91ac7539bca32350850ac5157efffdda6a719a7b"));
    }

    @Test
    void collectOneFileThatIsInTheIgnoreFilter() {
        ArtifactCollector collector = ArtifactCollectorFactory.build(LocalFileCollector.builder()
                .excludePatterns("**"+ignoredFile.getName())
                .path(ignoredFile.toPath())
                .followSymlinkDirs(false)
                .normalizeLineEndings(true)
                .followSymlinkDirs(false)
                .build());
        List<Artifact> artifacts = sort(collector.collect());
        assertThat(artifacts, hasSize(0));
    }
    
    @Test
    void checkShaIndependentOfImplementation() {
        String dirname = "src/test/resources/artifactcollectertest";
        String expectedHash = this.createHash(dirname + "/argos-test-app.war");
        File artifactCollecterTestDir = new File(dirname);
        ArtifactCollector collector = ArtifactCollectorFactory.build(LocalFileCollector.builder()
                .basePath(artifactCollecterTestDir.toPath())
                .path(artifactCollecterTestDir.toPath())
                .build());
        List<Artifact> artifacts = sort(collector.collect());
        assertEquals(expectedHash, artifacts.get(0).getHash());
    }
    
    private String createHash(String filename) {
        MessageDigest digest = DigestUtils.getSha256Digest();
        byte[] result = new byte[digest.getDigestLength()];
        try (InputStream file = new FileInputStream(filename)) {
            int length;
            while ((length = file.read(result)) != -1) {
                digest.update(result, 0, length);
            }
        } catch (IOException e) {
            throw new Argos4jError("The file " + filename + " couldn't be recorded: " + e.getMessage());
        }
        return Hex.encodeHexString(digest.digest());
    }

}
