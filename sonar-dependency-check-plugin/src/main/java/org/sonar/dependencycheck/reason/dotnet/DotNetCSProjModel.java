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

package org.sonar.dependencycheck.reason.dotnet;

import java.util.Collections;
import java.util.List;

import org.sonar.dependencycheck.parser.deserializer.DotNetDependencyDeserializer;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DotNetCSProjModel {
    private final List<DotNetDependencyLocation> dependencies;

    /**
     * @param dependencies
     */
    @JsonCreator
    public DotNetCSProjModel(@JsonDeserialize(using = DotNetDependencyDeserializer.class ) @Nullable List<DotNetDependencyLocation> dependencies) {
        this.dependencies = dependencies == null ? Collections.emptyList() : dependencies;
    }

    @NonNull
    public List<DotNetDependencyLocation> getDependencies() {
        return dependencies;
    }
}
