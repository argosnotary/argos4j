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

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.matching.RequestPattern;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import com.argosnotary.argos.argos4j.Argos4jError;
import com.argosnotary.argos.argos4j.RemoteCollectorCollector;
import com.argosnotary.argos.argos4j.RemoteFileCollector;
import com.argosnotary.argos.argos4j.RemoteZipFileCollector;
import com.argosnotary.argos.argos4j.rest.api.model.Artifact;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.notFound;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.status;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.startsWith;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RemoteArtifactCollectorTest {

    private ArtifactCollector collector;
    private WireMockServer wireMockServer;
    private Integer randomPort;

    @BeforeEach
    void setUp() throws IOException {
        randomPort = findRandomPort();
        wireMockServer = new WireMockServer(randomPort);
        wireMockServer.start();
    }

    @AfterEach
    public void teardown() {
        wireMockServer.stop();
    }

    @Test
    void collectRemoteZip() throws IOException {
        createZipCollector();
        wireMockServer.stubFor(get(urlEqualTo("/argos-test-app-1.0-SNAPSHOT.dar"))
                .willReturn(ok().withBody(IOUtils.toByteArray(getClass().getResourceAsStream("/argos-test-app-1.0-SNAPSHOT.dar")))));
        List<Artifact> collect = collector.collect();

    	Artifact artifact1 = new Artifact();
    	artifact1.setUri("META-INF/MANIFEST.MF");
    	artifact1.setHash("53e5e0a85a6aefa827e2fe34748cd1030c02a492bd9b309dc2f123258a218901");
        Artifact artifact2 = new Artifact();
    	artifact2.setUri("argos-test-app.war/argos-test-app.war");
    	artifact2.setHash("f5e94511d66ffbd76e164b7a5c8ec91727f6435dabce365b53e7f4221edd88ae");
        Artifact artifact3 = new Artifact();
    	artifact3.setUri("deployit-manifest.xml");
    	artifact3.setHash("9c1a8531bbd86414d6cc9929daa19d06a05cf3ca335b4ca7abe717c8f2b5f3ec");
        assertThat(collect, contains(
                artifact1,
                artifact2,
                artifact3));

        List<LoggedRequest> requests = wireMockServer.findRequestsMatching(RequestPattern.everything()).getRequests();
        assertThat(requests.get(0).getHeader("Authorization"), is("Basic YmFydDpzZWNyZXQ="));
    }

    @Test
    void collectEncryptedRemoteZip() throws IOException {
        createZipCollector();
        wireMockServer.stubFor(get(urlEqualTo("/argos-test-app-1.0-SNAPSHOT.dar")).willReturn(ok().withBody(IOUtils.toByteArray(getClass().getResourceAsStream("/with-password.zip")))));
        Argos4jError error = assertThrows(Argos4jError.class, () -> collector.collect());
        assertThat(error.getMessage(), is("encrypted ZIP entry not supported"));
    }
    
    @Test
    void collectCollector() throws IOException {
    	Artifact artifact1 = new Artifact();
    	artifact1.setUri("path1");
    	artifact1.setHash("hash1");
        Artifact artifact2 = new Artifact();
    	artifact2.setUri("path2");
    	artifact2.setHash("hash2");
        createCollectorCollector();
        wireMockServer.stubFor(post(urlPathEqualTo("/collect"))
                .withRequestBody(equalToJson("{\"key1\": \"value1\",\"key2\": \"value2\"}"))
                .willReturn(ok().withBody("[{\"uri\":\"path1\",\"hash\": \"hash1\"},{\"uri\":\"path2\",\"hash\": \"hash2\"}]")));
        List<Artifact> collect = collector.collect();
        assertThat(collect, contains(
                artifact1,
                artifact2));
    }

    @Test
    void collectNotFound() throws MalformedURLException {
        createZipCollector();
        wireMockServer.stubFor(get(urlEqualTo("/argos-test-app-1.0-SNAPSHOT.dar")).willReturn(notFound()));
        Argos4jError error = assertThrows(Argos4jError.class, () -> collector.collect());
        assertThat(error.getMessage(), is("call to http://localhost:" + randomPort + "/argos-test-app-1.0-SNAPSHOT.dar returned 404"));
    }


    @Test
    void collectNotAuthorized() throws MalformedURLException {
        createZipCollector();
        wireMockServer.stubFor(get(urlEqualTo("/argos-test-app-1.0-SNAPSHOT.dar")).willReturn(status(401).withBody("Not authorized")));
        Argos4jError error = assertThrows(Argos4jError.class, () -> collector.collect());
        assertThat(error.getMessage(), is("call to http://localhost:" + randomPort + "/argos-test-app-1.0-SNAPSHOT.dar returned 401 with body : Not authorized"));
    }

    @Test
    void collectConnectionRefused() throws MalformedURLException {
        randomPort = 33321;
        createZipCollector();
        wireMockServer.stubFor(get(urlEqualTo("/argos-test-app-1.0-SNAPSHOT.dar")).willReturn(status(401).withBody("Not authorized")));
        Argos4jError error = assertThrows(Argos4jError.class, () -> collector.collect());
        assertThat(error.getMessage(), startsWith("http://localhost:33321/argos-test-app-1.0-SNAPSHOT.dar got error Connection refused"));
    }

    private void createZipCollector() throws MalformedURLException {
        collector = ArtifactCollectorFactory.build(RemoteZipFileCollector.builder().username("bart")
                .password("secret".toCharArray()).url(new URL("http://localhost:" + randomPort + "/argos-test-app-1.0-SNAPSHOT.dar")).build());
    }

    private void createFileCollector(String artifactUri) throws MalformedURLException {
        collector = ArtifactCollectorFactory.build(RemoteFileCollector.builder()
                .artifactUri(artifactUri).url(new URL("http://localhost:" + randomPort + "/argos-test-app-1.0-SNAPSHOT.dar")).build());
    }
    
    private void createCollectorCollector() throws MalformedURLException {
        Map<String, String> configMap = new HashMap<>();
        configMap.put("key1", "value1");
        configMap.put("key2", "value2");
        collector = ArtifactCollectorFactory.build(RemoteCollectorCollector.builder()
                .parameterMap(configMap).url(new URL("http://localhost:" + randomPort + "/collect")).build());
    }

    @Test
    void collectRemoteFileWithConfiguredArtifactName() throws IOException {

        createFileCollector("other.war");

        wireMockServer.stubFor(get(urlEqualTo("/argos-test-app-1.0-SNAPSHOT.dar"))
                .willReturn(ok().withBody(IOUtils.toByteArray(getClass().getResourceAsStream("/argos-test-app-1.0-SNAPSHOT.dar")))));
        List<Artifact> collect = collector.collect();
        Artifact artifact1 = new Artifact();
    	artifact1.setUri("other.war");
    	artifact1.setHash("95540f95db610e211bed84c09f1badb42560806d940e7f4d8209c4f2d3880b7d");
        assertThat(collect, contains(
                artifact1));

        List<LoggedRequest> requests = wireMockServer.findRequestsMatching(RequestPattern.everything()).getRequests();
        assertThat(requests.get(0).getHeader("Authorization"), nullValue());
    }

    @Test
    void collectRemoteFile() throws IOException {

        createFileCollector(null);

        wireMockServer.stubFor(get(urlEqualTo("/argos-test-app-1.0-SNAPSHOT.dar"))
                .willReturn(ok().withBody(IOUtils.toByteArray(getClass().getResourceAsStream("/argos-test-app-1.0-SNAPSHOT.dar")))));
        List<Artifact> collect = collector.collect();
        Artifact artifact1 = new Artifact();
    	artifact1.setUri("argos-test-app-1.0-SNAPSHOT.dar");
    	artifact1.setHash("95540f95db610e211bed84c09f1badb42560806d940e7f4d8209c4f2d3880b7d");
        assertThat(collect, contains(
                artifact1));
        List<LoggedRequest> requests = wireMockServer.findRequestsMatching(RequestPattern.everything()).getRequests();
        assertThat(requests.get(0).getHeader("Authorization"), nullValue());
    }

    private static Integer findRandomPort() throws IOException {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        }
    }
}