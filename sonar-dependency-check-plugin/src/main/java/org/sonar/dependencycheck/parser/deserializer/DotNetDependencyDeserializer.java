/*
 * Dependency-Check Plugin for SonarQube
 * Copyright (C) 2015-2023 dependency-check
 * philipp.dallig@gmail.com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.dependencycheck.parser.deserializer;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.sonar.dependencycheck.reason.dotnet.DotNetDependencyLocation;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import edu.umd.cs.findbugs.annotations.Nullable;

public class DotNetDependencyDeserializer extends StdDeserializer<List<DotNetDependencyLocation>>{

    /**
     *
     */
    private static final long serialVersionUID = 7327963954335906342L;

    protected DotNetDependencyDeserializer() {
        this(null);
    }

    protected DotNetDependencyDeserializer(@Nullable Class<?> vc) {
        super(vc);
    }

    @Override
    public List<DotNetDependencyLocation> deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        List<DotNetDependencyLocation> dependencies = new LinkedList<>();
        while (jsonParser.nextToken() != JsonToken.END_OBJECT) {
            if (StringUtils.equalsIgnoreCase("PackageReference", jsonParser.getCurrentName())) {
                // We found a dependency
                String name = "";
                String version = "";
                int startLineNr = jsonParser.getCurrentLocation().getLineNr();
                while (jsonParser.nextToken() != JsonToken.END_OBJECT) {
                    if (StringUtils.equalsIgnoreCase("artifactId", jsonParser.getCurrentName())) {
                        name = jsonParser.getValueAsString();
                    }

                    if (StringUtils.equalsIgnoreCase("version", jsonParser.getCurrentName())) {
                        version = jsonParser.getValueAsString();
                    }
                }
                int endLineNr = jsonParser.getCurrentLocation().getLineNr();
                dependencies.add(new DotNetDependencyLocation(groupId, artifactId, version, startLineNr, endLineNr));
            }
        }
        return dependencies;
    }
}
