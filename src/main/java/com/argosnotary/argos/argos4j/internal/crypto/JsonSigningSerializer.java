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
package com.argosnotary.argos.argos4j.internal.crypto;

import com.argosnotary.argos.argos4j.Argos4jError;
import com.argosnotary.argos.argos4j.rest.api.model.Artifact;
import com.argosnotary.argos.argos4j.rest.api.model.Link;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.mapstruct.factory.Mappers;

import static java.util.Comparator.comparing;

public class JsonSigningSerializer implements SigningSerializer {

    @Override
    public String serialize(Link link) {
        Link linkClone = Mappers.getMapper(Cloner.class).clone(link);
        linkClone.getMaterials().sort(comparing(Artifact::getUri));
        linkClone.getProducts().sort(comparing(Artifact::getUri));
        return serializeSignable(linkClone);
    }

    private String serializeSignable(Object signable) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        try {
            return objectMapper.writeValueAsString(signable);
        } catch (JsonProcessingException e) {
            throw new Argos4jError(e.getMessage(), e);
        }
    }

}
