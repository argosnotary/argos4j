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

import java.io.Serializable;
import java.util.List;

import com.argosnotary.argos.argos4j.rest.api.model.Artifact;
import com.argosnotary.argos.argos4j.rest.api.model.LinkMetaBlock;

public interface LinkBuilder extends Serializable {
    Argos4jSettings getSettings();
    
    void addMaterials(List<Artifact> artifacts);
    
    void addProducts(List<Artifact> artifacts);

    void collectMaterials(FileCollector collector);

    void collectProducts(FileCollector collector);

    LinkMetaBlock create(char[] signingKeyPassphrase);

    void store(char[] signingKeyPassphrase);
}
