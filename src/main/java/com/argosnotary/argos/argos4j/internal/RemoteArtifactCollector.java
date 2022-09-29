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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.argosnotary.argos.argos4j.Argos4jError;
import com.argosnotary.argos.argos4j.RemoteCollector;
import com.argosnotary.argos.argos4j.RemoteCollectorCollector;
import com.argosnotary.argos.argos4j.RemoteFileCollector;
import com.argosnotary.argos.argos4j.RemoteZipFileCollector;
import com.argosnotary.argos.argos4j.internal.crypto.HashUtil;
import com.argosnotary.argos.argos4j.rest.api.model.Artifact;

import feign.Client;
import feign.Request;
import feign.RequestTemplate;
import feign.Response;
import feign.auth.BasicAuthRequestInterceptor;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.nio.charset.StandardCharsets.UTF_8;

@Slf4j
public class RemoteArtifactCollector implements ArtifactCollector {

    private final RemoteCollector remoteCollector;

    public RemoteArtifactCollector(RemoteFileCollector remoteCollector) {
        this.remoteCollector = remoteCollector;
    }

    public RemoteArtifactCollector(RemoteZipFileCollector remoteCollector) {
        this.remoteCollector = remoteCollector;
    }
    
    public RemoteArtifactCollector(RemoteCollectorCollector remoteCollector) {
        this.remoteCollector = remoteCollector;
    }

    @Override
    public List<Artifact> collect() {
        RequestTemplate requestTemplate;
        try {
            requestTemplate = createRequest();
        } catch (URISyntaxException e) {
            throw new Argos4jError("Creation of request returned error: "+e.getMessage());
        }
        Client client = new Client.Default(null, null);
        Request request = requestTemplate.resolve(new HashMap<>()).request();
        log.info("execute request: {}", request.url());
        try (Response response = client.execute(request, new Request.Options())) {
            if (response.status() == 200) {
                return getArtifactsFromResponse(response);
            } else {
                String bodyAsString = Optional.ofNullable(response.body()).map(this::convert).filter(body -> !body.isEmpty()).map(body -> " with body : " + body).orElse("");
                throw new Argos4jError("call to " + request.url() + " returned " + response.status() + bodyAsString);
            }
        } catch (IOException e) {
            throw new Argos4jError(request.url() + " got error " + e.getMessage(), e);
        }
    }

    public String convert(Response.Body body) {
        try (BufferedReader br = new BufferedReader(body.asReader(UTF_8))) {
            return br.lines().collect(Collectors.joining("\n"));
        } catch (IOException e) {
            return e.getMessage();
        }
    }

    private List<Artifact> getArtifactsFromResponse(Response response) throws IOException {
        if (remoteCollector.getClass() == RemoteZipFileCollector.class) {
            return new ZipStreamArtifactCollector(remoteCollector).collect(response.body().asInputStream());
        } else if (remoteCollector.getClass() == RemoteFileCollector.class) {
            String fileName = Optional.ofNullable(((RemoteFileCollector) remoteCollector).getArtifactUri())
                    .orElseGet(() -> remoteCollector.getUrl().getPath().substring(remoteCollector.getUrl().getPath().lastIndexOf('/') + 1));
            String hash = HashUtil.createHash(response.body().asInputStream(), fileName, remoteCollector.isNormalizeLineEndings());
            
            Artifact artifact = new Artifact();
            artifact.setUri(fileName);
            artifact.setHash(hash);
            
            return Collections.singletonList(artifact);
        } else if (remoteCollector.getClass() == RemoteCollectorCollector.class) {
            ObjectMapper objectMapper = new ObjectMapper();            
            return objectMapper.readValue(response.body().asInputStream(), new TypeReference<List<Artifact>>(){});
        } else {
            throw new Argos4jError("not implemented");
        }
    }

    private RequestTemplate createRequest() throws URISyntaxException {
        RequestTemplate requestTemplate = new RequestTemplate();
        String url = remoteCollector.getUrl().toString();
        if (remoteCollector.getClass() == RemoteCollectorCollector.class) {
            requestTemplate.method(Request.HttpMethod.POST);
            try {
                requestTemplate.body(
                        Request.Body.create((new ObjectMapper()
                                .writeValueAsString(
                                        ((RemoteCollectorCollector) remoteCollector).getParameterMap())), 
                                    Charset.defaultCharset()));
            } catch (JsonProcessingException e) {
                throw new Argos4jError(e.getMessage());
            }

        } else {
            requestTemplate.method(Request.HttpMethod.GET);
            url = appendToUrl(url, remoteCollector.getParameterMap());
        }
        requestTemplate.target(url);
        addAuthorization(requestTemplate);
        return requestTemplate;
    }


    private void addAuthorization(RequestTemplate requestTemplate) {
        Optional.ofNullable(remoteCollector.getUsername())
                .ifPresent(userInfo -> new BasicAuthRequestInterceptor(remoteCollector.getUsername(),
                        new String(remoteCollector.getPassword())).apply(requestTemplate));
    }
    
    private String appendToUrl(String url, Map<String, String> parameters) throws URISyntaxException {
        URI uri = new URI(url);
        String query = uri.getQuery();

        StringBuilder builder = new StringBuilder();

        if (query != null)
            builder.append(query);

        if (parameters != null) {
            parameters.forEach((key, value) -> {
                String keyValueParam = key + "=" + value;
                if (!builder.toString().isEmpty())
                    builder.append("&");    
                builder.append(keyValueParam);
            });
        }

        URI newUri = new URI(uri.getScheme(), uri.getAuthority(), uri.getPath(), builder.toString(), uri.getFragment());
        return newUri.toString();
    }
}
