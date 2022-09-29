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

import com.argosnotary.argos.argos4j.rest.api.model.Artifact;
import com.argosnotary.argos.argos4j.rest.api.model.Link;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Arrays;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

class JsonSigningSerializerTest {

    @Test
    void serializeLink() throws IOException {
		Artifact artifact1 = new Artifact();
		artifact1.setUri("zbc.jar");
		artifact1.setHash("hash1");
		Artifact artifact2 = new Artifact();
		artifact2.setUri("abc.jar");
		artifact2.setHash("hash2");
		Artifact artifact3 = new Artifact();
		artifact3.setUri("_bc.jar");
		artifact3.setHash("hash3");
		Artifact artifact4 = new Artifact();
		artifact4.setUri("_abc.jar");
		artifact4.setHash("hash4");
		Link link = new Link();
		link.setStepName("stepName");
		link.setMaterials(Arrays.asList(
                        artifact1,
                        artifact2));
		link.setProducts(Arrays.asList(
                        artifact3,
                        artifact4));
		String serialized = new JsonSigningSerializer().serialize(link); 
        assertThat(serialized, is(getExpectedJson("/expectedLinkSigning.json")));
    }
    
    private String getExpectedJson(String name) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readValue(getClass().getResourceAsStream(name), JsonNode.class);
        return jsonNode.toString();
    }
}
