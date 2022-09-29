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
import com.argosnotary.argos.argos4j.FileCollector;
import com.argosnotary.argos.argos4j.LocalFileCollector;
import com.argosnotary.argos.argos4j.LocalZipFileCollector;
import com.argosnotary.argos.argos4j.RemoteCollectorCollector;
import com.argosnotary.argos.argos4j.RemoteFileCollector;
import com.argosnotary.argos.argos4j.RemoteZipFileCollector;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ArtifactCollectorFactory {
    
    private ArtifactCollectorFactory() {}

    private static final Map<Class<? extends FileCollector>, Class<? extends ArtifactCollector>> MAPPING = new HashMap<>();

    static {
        MAPPING.put(LocalFileCollector.class, LocalArtifactCollector.class);
        MAPPING.put(LocalZipFileCollector.class, ZipArtifactCollector.class);
        MAPPING.put(RemoteFileCollector.class, RemoteArtifactCollector.class);
        MAPPING.put(RemoteZipFileCollector.class, RemoteArtifactCollector.class);
        MAPPING.put(RemoteCollectorCollector.class, RemoteArtifactCollector.class);
    }

    public static ArtifactCollector build(FileCollector fileCollector) {
        Class<? extends ArtifactCollector> artifactCollectorClass = Objects.requireNonNull(MAPPING.get(fileCollector.getClass()), "not implemented");
        try {
            return artifactCollectorClass.getConstructor(fileCollector.getClass()).newInstance(fileCollector);
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException e) {
            throw new Argos4jError(e.getMessage(), e);
        } catch (InvocationTargetException e) {
            throw new Argos4jError(e.getCause().getMessage(), e.getCause());
        }
    }
}
