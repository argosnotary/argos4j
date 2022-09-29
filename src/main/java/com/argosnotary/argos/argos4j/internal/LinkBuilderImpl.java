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
import com.argosnotary.argos.argos4j.internal.crypto.JsonSigningSerializer;
import com.argosnotary.argos.argos4j.internal.crypto.Signer;
import com.argosnotary.argos.argos4j.rest.api.model.Artifact;
import com.argosnotary.argos.argos4j.rest.api.model.KeyPair;
import com.argosnotary.argos.argos4j.rest.api.model.Link;
import com.argosnotary.argos.argos4j.rest.api.model.LinkMetaBlock;
import com.argosnotary.argos.argos4j.rest.api.model.ServiceAccountKeyPair;
import com.argosnotary.argos.argos4j.rest.api.model.Signature;

import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

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
        Link link = new Link();
        link.setMaterials(materials);
        link.setProducts(products);
        link.setStepName(linkBuilderSettings.getStepName());
        
        ArgosServiceClient argosServiceClient = new ArgosServiceClient(settings, signingKeyPassphrase);
        ServiceAccountKeyPair serviceAccountKeyPair = argosServiceClient.getKeyPair();
        Signature signature;
        KeyPair keyPair = new KeyPair();
        keyPair.setEncryptedPrivateKey(serviceAccountKeyPair.getEncryptedPrivateKey());
        try {
            signature = Signer.sign((KeyPair)keyPair, signingKeyPassphrase, new JsonSigningSerializer().serialize(link));
        } catch (Argos4jError e) {
            throw new Argos4jError("The Link object couldn't be signed: "+ e.getMessage());
        }
        LinkMetaBlock block = new LinkMetaBlock();
        block.setLink(link);
        block.setSignature(signature);
        return block;
    }
    
    @Override
    public void store(char[] signingKeyPassphrase) {
        ArgosServiceClient argosServiceClient = new ArgosServiceClient(settings, signingKeyPassphrase);
        argosServiceClient.uploadLinkMetaBlockToService(create(signingKeyPassphrase));
    }
}
