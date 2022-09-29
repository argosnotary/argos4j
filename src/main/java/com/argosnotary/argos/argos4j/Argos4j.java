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

import com.argosnotary.argos.argos4j.internal.ArtifactListBuilderImpl;
import com.argosnotary.argos.argos4j.internal.LinkBuilderImpl;
import com.argosnotary.argos.argos4j.internal.ReleaseBuilderImpl;
import com.argosnotary.argos.argos4j.internal.VerifyBuilderImpl;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.Serializable;
import java.util.List;

@RequiredArgsConstructor
@EqualsAndHashCode
public class Argos4j implements Serializable {

    @Getter
    private final Argos4jSettings settings;

    public LinkBuilder getLinkBuilder(LinkBuilderSettings linkBuilderSettings) {
        return new LinkBuilderImpl(settings, linkBuilderSettings);
    }

    public VerifyBuilder getVerifyBuilder(List<String> paths) {
        return new VerifyBuilderImpl(settings, getArtifactListBuilder(), paths);
    }
    
    public VerifyBuilder getVerifyBuilder() {
        return this.getVerifyBuilder(null);
    }

    public ReleaseBuilder getReleaseBuilder() {
        return new ReleaseBuilderImpl(settings, getArtifactListBuilder());
    }
    
    public static ArtifactListBuilder getArtifactListBuilder() {
        return new ArtifactListBuilderImpl();
    }

    public static String getVersion() {
        return VersionInfo.getInfo();
    }
}
