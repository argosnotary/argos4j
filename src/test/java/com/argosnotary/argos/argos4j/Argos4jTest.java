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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.matching.RequestPattern;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import com.argosnotary.argos.argos4j.internal.ArtifactCollectorFactory;
import com.argosnotary.argos.argos4j.internal.ArtifactListBuilderImpl;
import com.argosnotary.argos.argos4j.rest.api.model.RestKeyPair;
import com.argosnotary.argos.argos4j.rest.api.model.RestReleaseDossierMetaData;
import com.argosnotary.argos.argos4j.rest.api.model.RestReleaseResult;
import com.argosnotary.argos.domain.release.ReleaseResult;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.Arrays;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.badRequest;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.noContent;
import static com.github.tomakehurst.wiremock.client.WireMock.notFound;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.serverError;
import static com.github.tomakehurst.wiremock.client.WireMock.status;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.sameInstance;
import static org.hamcrest.Matchers.startsWith;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Argos4jTest {

    public static final char[] KEY_PASSPHRASE = "password".toCharArray();
    private Argos4j argos4j;
    private WireMockServer wireMockServer;

    @TempDir
    static File sharedTempDir;
    private String keyId;
    private String restKeyPairRest;
    private LinkBuilder linkBuilder;
    private VerifyBuilder verifyBuilder;
    private ReleaseBuilder releaseBuilder;
    private String releaseResult;
    private Argos4jSettings settings;

    @BeforeAll
    static void setUpBefore() throws IOException {
        FileUtils.write(new File(sharedTempDir, "text.txt"), "cool dit\r\nan other line", "UTF-8");
    }

    @AfterEach
    public void teardown() {
        wireMockServer.stop();
    }

    @BeforeEach
    void setUp() throws IOException {
        Integer randomPort = findRandomPort();
        wireMockServer = new WireMockServer(randomPort);
        wireMockServer.start();

        RestKeyPair restKeyPair = new ObjectMapper().readValue(this.getClass().getResourceAsStream("/keypair.json"), RestKeyPair.class);
        keyId = restKeyPair.getKeyId();

        restKeyPairRest = new ObjectMapper().writeValueAsString(restKeyPair);

        settings = Argos4jSettings.builder()
                .argosServerBaseUrl("http://localhost:" + randomPort + "/api")
                .supplyChainName("supplyChainName")
                .path(Arrays.asList("rootLabel", "subLabel"))
                .keyId(keyId)
                .build();

        argos4j = new Argos4j(settings);
        linkBuilder = argos4j.getLinkBuilder(LinkBuilderSettings.builder().stepName("build").build());
        verifyBuilder = argos4j.getVerifyBuilder(null);
        releaseBuilder = argos4j.getReleaseBuilder();
        RestReleaseDossierMetaData restReleaseDossierMetaData = new RestReleaseDossierMetaData().addReleaseArtifactsItem(Arrays.asList("cb6bdad36690e8024e7df13e6796ae6603f2cb9cf9f989c9ff939b2ecebdcb91"));
        releaseResult = new ObjectMapper().writeValueAsString(new RestReleaseResult().releaseIsValid(true).releaseDossierMetaData(restReleaseDossierMetaData));
        
        
    }

    private static Integer findRandomPort() throws IOException {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        }
    }

    @Test
    void storeMetablockLinkForDirectory() {
        wireMockServer.stubFor(get(urlEqualTo("/api/supplychain?name=supplyChainName&path=rootLabel&path=subLabel"))
                .willReturn(ok().withBody("{\"name\":\"supplyChainName\",\"id\":\"supplyChainId\",\"parentLabelId\":\"parentLabelId\"}")));
        wireMockServer.stubFor(post(urlEqualTo("/api/supplychain/supplyChainId/link")).willReturn(noContent()));
        wireMockServer.stubFor(get(urlEqualTo("/api/serviceaccount/me/activekey")).willReturn(ok().withBody(restKeyPairRest)));
        FileCollector fileCollector = LocalFileCollector.builder().path(sharedTempDir.toPath()).basePath(sharedTempDir.toPath()).build();
        linkBuilder.collectMaterials(fileCollector);
        linkBuilder.collectProducts(fileCollector);
        linkBuilder.store(KEY_PASSPHRASE);
        List<LoggedRequest> requests = wireMockServer.findRequestsMatching(RequestPattern.everything()).getRequests();
        assertThat(requests, hasSize(3));
        assertThat(requests.get(2).getBodyAsString(), endsWith(",\"link\":{\"stepName\":\"build\",\"materials\":[{\"uri\":\"text.txt\",\"hash\":\"cb6bdad36690e8024e7df13e6796ae6603f2cb9cf9f989c9ff939b2ecebdcb91\"}],\"products\":[{\"uri\":\"text.txt\",\"hash\":\"cb6bdad36690e8024e7df13e6796ae6603f2cb9cf9f989c9ff939b2ecebdcb91\"}]}}"));
    }

    @Test
    void addArtifactsAndStoreMetablockLinkForDirectory() {
        wireMockServer.stubFor(get(urlEqualTo("/api/supplychain?name=supplyChainName&path=rootLabel&path=subLabel"))
                .willReturn(ok().withBody("{\"name\":\"supplyChainName\",\"id\":\"supplyChainId\",\"parentLabelId\":\"parentLabelId\"}")));
        wireMockServer.stubFor(post(urlEqualTo("/api/supplychain/supplyChainId/link")).willReturn(noContent()));
        wireMockServer.stubFor(get(urlEqualTo("/api/serviceaccount/me/activekey")).willReturn(ok().withBody(restKeyPairRest)));
        FileCollector fileCollector = LocalFileCollector.builder().path(sharedTempDir.toPath()).basePath(sharedTempDir.toPath()).build();
        linkBuilder.addMaterials(ArtifactCollectorFactory.build(fileCollector).collect());
        linkBuilder.addProducts(ArtifactCollectorFactory.build(fileCollector).collect());
        linkBuilder.store(KEY_PASSPHRASE);
        List<LoggedRequest> requests = wireMockServer.findRequestsMatching(RequestPattern.everything()).getRequests();
        assertThat(requests, hasSize(3));
        assertThat(requests.get(2).getBodyAsString(), endsWith(",\"link\":{\"stepName\":\"build\",\"materials\":[{\"uri\":\"text.txt\",\"hash\":\"cb6bdad36690e8024e7df13e6796ae6603f2cb9cf9f989c9ff939b2ecebdcb91\"}],\"products\":[{\"uri\":\"text.txt\",\"hash\":\"cb6bdad36690e8024e7df13e6796ae6603f2cb9cf9f989c9ff939b2ecebdcb91\"}]}}"));
    }

    @Test
    void storeMetablockLinkForDirectoryFailed() {
        wireMockServer.stubFor(get(urlEqualTo("/api/supplychain?name=supplyChainName&path=rootLabel&path=subLabel"))
                .willReturn(ok().withBody("{\"name\":\"supplyChainName\",\"id\":\"supplyChainId\",\"parentLabelId\":\"parentLabelId\"}")));
        wireMockServer.stubFor(get(urlEqualTo("/api/serviceaccount/me/activekey")).willReturn(ok().withBody(restKeyPairRest)));
        wireMockServer.stubFor(post(urlEqualTo("/api/supplychain/supplyChainId/link")).willReturn(serverError()));
        Argos4jError error = assertThrows(Argos4jError.class, () -> linkBuilder.store(KEY_PASSPHRASE));
        assertThat(error.getMessage(), containsString("500"));
    }

    @Test
    void storeMetaBlockLinkForDirectoryUnexpectedResponse() {
        wireMockServer.stubFor(get(urlEqualTo("/api/serviceaccount/me/activekey")).willReturn(ok().withBody(restKeyPairRest)));
        wireMockServer.stubFor(get(urlEqualTo("/api/supplychain?name=supplyChainName&path=rootLabel&path=subLabel"))
                .willReturn(badRequest()));
        Argos4jError error = assertThrows(Argos4jError.class, () -> linkBuilder.store(KEY_PASSPHRASE));
        assertThat(error.getMessage(), containsString("400"));
    }

    @Test
    void storeMetaBlockLinkForDirectoryUnknownKeyId() {
        wireMockServer.stubFor(get(urlEqualTo("/api/serviceaccount/me/activekey")).willReturn(notFound()));
        Argos4jError error = assertThrows(Argos4jError.class, () -> linkBuilder.store(KEY_PASSPHRASE));
        assertThat(error.getMessage(), containsString("404"));
    }

    @Test
    void verify() {
        wireMockServer.stubFor(get(urlEqualTo("/api/supplychain/verification?artifactHashes=cb6bdad36690e8024e7df13e6796ae6603f2cb9cf9f989c9ff939b2ecebdcb91"))
                .willReturn(ok().withBody("{\"runIsValid\":true}")));

        assertThat(verifyBuilder.addFileCollector(LocalFileCollector.builder().path(sharedTempDir.toPath()).basePath(sharedTempDir.toPath()).build())
                .verify().isRunIsValid(), is(true));

        List<LoggedRequest> requests = wireMockServer.findRequestsMatching(RequestPattern.everything()).getRequests();
        assertThat(requests, hasSize(1));
    }

    @Test
    void verifyBadRequest() {
        wireMockServer.stubFor(get(urlEqualTo("/api/supplychain/verification?artifactHashes=cb6bdad36690e8024e7df13e6796ae6603f2cb9cf9f989c9ff939b2ecebdcb91"))
                .willReturn(status(400)));

        Argos4jError error = assertThrows(Argos4jError.class, () -> verifyBuilder.addFileCollector(LocalFileCollector.builder()
                .path(sharedTempDir.toPath()).basePath(sharedTempDir.toPath()).build())
                .verify());
        assertThat(error.getMessage(), startsWith("[400 Bad Request] during [GET] to [http://localhost:"));
    }
    
    @Test
    void verifyNotValid() {
        wireMockServer.stubFor(get(urlEqualTo("/api/supplychain/verification?artifactHashes=cb6bdad36690e8024e7df13e6796ae6603f2cb9cf9f989c9ff939b2ecebdcb91"))
                .willReturn(ok().withBody("{\"runIsValid\":false}")));

        assertThat(verifyBuilder.addFileCollector(LocalFileCollector.builder().path(sharedTempDir.toPath()).basePath(sharedTempDir.toPath()).build())
                .verify().isRunIsValid(), is(false));

        List<LoggedRequest> requests = wireMockServer.findRequestsMatching(RequestPattern.everything()).getRequests();
        assertThat(requests, hasSize(1));
    }
    
    @Test
    void release() {
        wireMockServer.stubFor(get(urlEqualTo("/api/supplychain?name=supplyChainName&path=rootLabel&path=subLabel"))
                .willReturn(ok().withBody("{\"name\":\"supplyChainName\",\"id\":\"supplyChainId\",\"parentLabelId\":\"parentLabelId\"}")));
        wireMockServer.stubFor(post(urlEqualTo("/api/supplychain/supplyChainId/release")).willReturn(ok().withBody(releaseResult)));
        
        ReleaseResult result = releaseBuilder
                .addFileCollector(LocalFileCollector
                        .builder().path(sharedTempDir.toPath()).basePath(sharedTempDir.toPath()).build())
                .release("test".toCharArray());

        assertThat(result.isReleaseIsValid(), is(true));

        List<LoggedRequest> requests = wireMockServer.findRequestsMatching(RequestPattern.everything()).getRequests();
        assertThat(requests, hasSize(2));
        assertThat(requests.get(1).getBodyAsString(), is("{\"releaseArtifacts\":[[{\"uri\":\"text.txt\",\"hash\":\"cb6bdad36690e8024e7df13e6796ae6603f2cb9cf9f989c9ff939b2ecebdcb91\"}]]}"));
    }
    
    @Test
    void getArtifactListBuilder() {
        assertTrue(Argos4j.getArtifactListBuilder() instanceof ArtifactListBuilderImpl);
    }

    @Test
    void version() {
        assertThat(Argos4j.getVersion().length() > 20, is(true));
    }

    @Test
    void settings() {
        assertThat(argos4j.getSettings(), sameInstance(settings));
        assertThat(linkBuilder.getSettings(), sameInstance(settings));
    }
}
