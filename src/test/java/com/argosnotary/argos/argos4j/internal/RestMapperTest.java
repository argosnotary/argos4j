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

import com.argosnotary.argos.argos4j.internal.mapper.RestMapper;
import com.argosnotary.argos.argos4j.rest.api.model.RestLinkMetaBlock;
import com.argosnotary.argos.domain.link.Link;
import com.argosnotary.argos.domain.link.LinkMetaBlock;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class RestMapperTest {

    @Test
    void testOnNullValues() {
        RestMapper mapper = Mappers.getMapper(RestMapper.class);
        LinkMetaBlock linkMetaBlock = LinkMetaBlock.builder().link(Link.builder().build()).build();
        RestLinkMetaBlock metablock = mapper.convertToRestLinkMetaBlock(linkMetaBlock);

        assertNotNull(metablock.getLink().getMaterials());
        assertNotNull(metablock.getLink().getProducts());

    }
}
