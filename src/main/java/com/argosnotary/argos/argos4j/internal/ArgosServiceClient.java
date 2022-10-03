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


import com.fasterxml.jackson.annotation.JsonInclude;
import com.argosnotary.argos.argos4j.Argos4jError;
import com.argosnotary.argos.argos4j.Argos4jSettings;
import com.argosnotary.argos.argos4j.VerificationResult;
import com.argosnotary.argos.argos4j.internal.crypto.HashUtil;
import com.argosnotary.argos.argos4j.rest.api.ApiClient;
import com.argosnotary.argos.argos4j.rest.api.client.LinkApi;
import com.argosnotary.argos.argos4j.rest.api.client.ReleaseApi;
import com.argosnotary.argos.argos4j.rest.api.client.ServiceAccountApi;
import com.argosnotary.argos.argos4j.rest.api.client.SupplychainApi;
import com.argosnotary.argos.argos4j.rest.api.client.VerificationApi;
import com.argosnotary.argos.argos4j.rest.api.model.Artifact;
import com.argosnotary.argos.argos4j.rest.api.model.LinkMetaBlock;
import com.argosnotary.argos.argos4j.rest.api.model.ReleaseArtifacts;
import com.argosnotary.argos.argos4j.rest.api.model.ReleaseResult;
import com.argosnotary.argos.argos4j.rest.api.model.ServiceAccountKeyPair;

import feign.FeignException;

import java.util.List;

public class ArgosServiceClient {

    private final Argos4jSettings settings;
    private final ApiClient apiClient;
    
    public ArgosServiceClient(Argos4jSettings settings) {
        this.settings = settings;
        apiClient = new ApiClient().setBasePath(settings.getArgosServerBaseUrl());
        apiClient.getObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    public ArgosServiceClient(Argos4jSettings settings, char[] signingKeyPassphrase) {
        this.settings = settings;
        apiClient = new ApiClient("basicAuth").setBasePath(settings.getArgosServerBaseUrl());

        apiClient.setCredentials(settings.getKeyId(), HashUtil.calculateHashedPassphrase(settings.getKeyId(), new String(signingKeyPassphrase)));
        apiClient.getObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    public void uploadLinkMetaBlockToService(LinkMetaBlock linkMetaBlock) {
        try {
            LinkApi linkApi = apiClient.buildClient(LinkApi.class);
            linkApi.createLink(getSupplyChainId(), linkMetaBlock);
        } catch (FeignException e) {
            throw convertToArgos4jError(e);
        }
    }

    public VerificationResult verify(List<String> hashes, List<String> paths) {
        try {
            VerificationApi verificationApi = apiClient.buildClient(VerificationApi.class);
            return VerificationResult.builder()
                    .runIsValid(verificationApi.getVerification(hashes, paths).getRunIsValid())
                    .build();
        } catch (FeignException e) {
            throw convertToArgos4jError(e);
        }
    }

    public ReleaseResult release(List<List<Artifact>> artifactsList) {
        try {
            ReleaseApi releaseApi = apiClient.buildClient(ReleaseApi.class);
            return releaseApi.createRelease(getSupplyChainId(), new ReleaseArtifacts().releaseArtifacts(artifactsList));
        } catch (FeignException e) {
            throw convertToArgos4jError(e);
        }
    }

    public ServiceAccountKeyPair getKeyPair() {
        try {
            ServiceAccountApi keyApi = apiClient.buildClient(ServiceAccountApi.class);
            return keyApi.getServiceAccountKey();
        } catch (FeignException e) {
            throw convertToArgos4jError(e);
        }
    }

    private Argos4jError convertToArgos4jError(FeignException e) {
        return new Argos4jError(e.getMessage(), e);
    }

    private String getSupplyChainId() {
        SupplychainApi supplychainApi = apiClient.buildClient(SupplychainApi.class);
        return supplychainApi.getSupplyChainByPath(settings.getSupplyChainName(), settings.getPath()).getId();
    }

}
