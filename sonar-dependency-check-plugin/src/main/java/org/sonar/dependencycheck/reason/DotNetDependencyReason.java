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

package org.sonar.dependencycheck.reason;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.lang3.StringUtils;
import org.sonar.api.batch.fs.InputComponent;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.dependencycheck.base.DependencyCheckUtils;
import org.sonar.dependencycheck.parser.CSProjParserHelper;
import org.sonar.dependencycheck.parser.ReportParserException;
import org.sonar.dependencycheck.parser.element.Confidence;
import org.sonar.dependencycheck.parser.element.Dependency;
import org.sonar.dependencycheck.parser.element.IncludedBy;
import org.sonar.dependencycheck.reason.dotnet.DotNetDependency;
import org.sonar.dependencycheck.reason.dotnet.DotNetDependencyLocation;
import org.sonar.dependencycheck.reason.dotnet.DotNetCSProjModel;

import edu.umd.cs.findbugs.annotations.NonNull;

public class DotNetDependencyReason extends DependencyReason {

    private final InputFile csproj;
    private final Map<Dependency, TextRangeConfidence> dependencyMap;
    private DotNetCSProjModel csprojModel;

    private static final Logger LOGGER = LoggerFactory.getLogger(DotNetDependencyReason.class);

    public DotNetDependencyReason(@NonNull InputFile csproj) {
        super(csproj, Language.C_SHARP);
        this.csproj = csproj;
        dependencyMap = new HashMap<>();
        csprojModel = null;
        try {
            csprojModel = CSProjParserHelper.parse(csproj.inputStream());
        } catch (ReportParserException | IOException e) {
            LOGGER.warn("Parsing {} failed", csproj);
            LOGGER.debug(e.getMessage(), e);
        }
    }

    @Override
    @NonNull
    public TextRangeConfidence getBestTextRange(@NonNull Dependency dependency) {
        if (!dependencyMap.containsKey(dependency)) {
            //Optional<DotNetDependency> dotNetDependncy = DependencyCheckUtils.getDotNetDependency(dependency);
            Optional<DotNetDependency> dotNetDependncy = DependencyCheckUtils.getDependency(dependency, DotNetDependency.class);
            if (dotNetDependncy.isPresent()) {
                fillArtifactMatch(dependency, dotNetDependncy.get());
            } else {
                LOGGER.debug("No Identifier with type NuGet found for Dependency {}", dependency.getFileName());
            }
            Optional<Collection<IncludedBy>> includedBys = dependency.getIncludedBy();
            if (includedBys.isPresent()) {
                workOnIncludedBy(dependency, includedBys.get());
            }
            dependencyMap.computeIfAbsent(dependency, k -> addDependencyToFirstLine(k, csproj));
        }
        return dependencyMap.get(dependency);
    }

    private void workOnIncludedBy(@NonNull Dependency dependency, Collection<IncludedBy> includedBys) {
        for (IncludedBy includedBy : includedBys) {
            String reference = includedBy.getReference();
            if (StringUtils.isNotBlank(reference)) {
                Optional<SoftwareDependency> softwareDependency = DependencyCheckUtils.convertToSoftwareDependency(reference);
                if (softwareDependency.isPresent() && DependencyCheckUtils.isDotNetDependency(softwareDependency.get())) {
                    fillArtifactMatch(dependency, (DotNetDependency) softwareDependency.get());
                }
            }
        }
    }
    /**
     *
     * @param dependency
     * @param dotnetDependency
     */
    private void fillArtifactMatch(@NonNull Dependency dependency, DotNetDependency dotNetDependency) {
        for (DotNetDependencyLocation dotNetDependencyLocation : csprojModel.getDependencies()) {
            checkCsProjDependency(dotNetDependency, dotNetDependencyLocation)
                .ifPresent(textRange -> putDependencyMap(dependency, textRange));
        }
    }

    private void putDependencyMap(@NonNull Dependency dependency, TextRangeConfidence newTextRange) {
        if (dependencyMap.containsKey(dependency)) {
            TextRangeConfidence oldTextRange = dependencyMap.get(dependency);
            if (oldTextRange.getConfidence().compareTo(newTextRange.getConfidence()) > 0) {
                dependencyMap.put(dependency, newTextRange);
            }
        } else {
            dependencyMap.put(dependency, newTextRange);
        }
    }

    private Optional<TextRangeConfidence> checkCsProjDependency(DotNetDependency dotNetDependency, DotNetDependencyLocation dotNetDependencyLocation) {
        if (StringUtils.equals(dotNetDependency.getName(), dotNetDependencyLocation.getName())) {
            Optional<String> depVersion = dotNetDependency.getVersion();
            Optional<String> depLocVersion = dotNetDependencyLocation.getVersion();
            if (depVersion.isPresent() && depLocVersion.isPresent() &&
                StringUtils.equals(depVersion.get(), depLocVersion.get())) {
                LOGGER.debug("Found a name and version match in {} ({} - {})", csproj, dotNetDependencyLocation.getStartLineNr(), dotNetDependencyLocation.getEndLineNr());
                return Optional.of(new TextRangeConfidence(csproj.newRange(csproj.selectLine(dotNetDependencyLocation.getStartLineNr()).start(), csproj.selectLine(dotNetDependencyLocation.getEndLineNr()).end()), Confidence.HIGHEST));
            }
            LOGGER.debug("Found a name match in {} ({} - {})", csproj, dotNetDependencyLocation.getStartLineNr(), dotNetDependencyLocation.getEndLineNr());
            return Optional.of(new TextRangeConfidence(csproj.newRange(csproj.selectLine(dotNetDependencyLocation.getStartLineNr()).start(), csproj.selectLine(dotNetDependencyLocation.getEndLineNr()).end()), Confidence.HIGH));
        }
        return Optional.empty();
    }

    /**
     * Checks if we have a CSProj File and this CSProj file is readable and has content
     * CSProj Files contains the in NuGet builds the dependencies
     */
    @Override
    public boolean isReasonable() {
        return csproj != null && csprojModel != null;
    }

    /**
     * returns csproj file
     */
    @NonNull
    @Override
    public InputComponent getInputComponent() {
        return csproj;
    }
}
