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
import com.argosnotary.argos.argos4j.Argos4jSettings;
import com.argosnotary.argos.argos4j.FileCollector;
import com.argosnotary.argos.argos4j.LinkBuilder;
import com.argosnotary.argos.argos4j.LinkBuilderSettings;
import com.argosnotary.argos.argos4j.internal.mapper.RestMapper;
import com.argosnotary.argos.domain.ArgosError;
import com.argosnotary.argos.domain.crypto.ServiceAccountKeyPair;
import com.argosnotary.argos.domain.crypto.Signature;
import com.argosnotary.argos.domain.crypto.signing.JsonSigningSerializer;
import com.argosnotary.argos.domain.crypto.signing.Signer;
import com.argosnotary.argos.domain.link.Artifact;
import com.argosnotary.argos.domain.link.Link;
import com.argosnotary.argos.domain.link.LinkMetaBlock;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

import org.mapstruct.factory.Mappers;

@RequiredArgsConstructor
public class LinkBuilderImpl implements LinkBuilder {

    private final Argos4jSettings settings;
    private final LinkBuilderSettings linkBuilderSettings;

    private ArrayList<Artifact> materials = new ArrayList<>();
    private ArrayList<Artifact> products = new ArrayList<>();

    @Override
    public Argos4jSettings getSettings() {
        return settings;
    }

    @Override
    public void addMaterials(List<Artifact> artifacts) {
        materials.addAll(artifacts);
    }

    @Override
    public void addProducts(List<Artifact> artifacts) {
        products.addAll(artifacts);
    }

    public void collectMaterials(FileCollector collector) {
        materials.addAll(ArtifactCollectorFactory.build(collector).collect());
    }

    public void collectProducts(FileCollector collector) {
        products.addAll(ArtifactCollectorFactory.build(collector).collect());
    }
    
    @Override
    public LinkMetaBlock create(char[] signingKeyPassphrase) {
        Link link = Link.builder()
                .materials(materials)
                .products(products)
                .stepName(linkBuilderSettings.getStepName()).build();
        ArgosServiceClient argosServiceClient = new ArgosServiceClient(settings, signingKeyPassphrase);
        ServiceAccountKeyPair keyPair = Mappers.getMapper(RestMapper.class).convertFromRestServiceAccountKeyPair(argosServiceClient.getKeyPair());
        Signature signature;
        try {
            signature = Signer.sign(keyPair, signingKeyPassphrase, new JsonSigningSerializer().serialize(link));
        } catch (ArgosError e) {
            throw new Argos4jError("The Link object couldn't be signed: "+ e.getMessage());
        }
        return LinkMetaBlock.builder().link(link).signature(signature).build();
    }
    
    @Override
    public void store(char[] signingKeyPassphrase) {
        ArgosServiceClient argosServiceClient = new ArgosServiceClient(settings, signingKeyPassphrase);
        argosServiceClient.uploadLinkMetaBlockToService(create(signingKeyPassphrase));
    }
}
