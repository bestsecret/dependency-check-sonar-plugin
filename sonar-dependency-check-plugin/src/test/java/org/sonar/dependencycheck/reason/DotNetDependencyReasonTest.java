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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.sonar.dependencycheck.parser.element.Confidence;
import org.sonar.dependencycheck.parser.element.Dependency;
import org.sonar.dependencycheck.parser.element.Identifier;

class DotNetDependencyReasonTest extends DependencyReasonTestHelper {

    @Test
    void isReasonable() throws IOException {
        DotNetDependencyReason csproj = new DotNetDependencyReason(inputFile("ExampleCSProj.csproj"));
        assertTrue(csproj.isReasonable());
        assertNotNull(csproj.getInputComponent());
    }

    @Test
    void isReasonableWithoutContent() throws IOException {
        DefaultInputFile csprojFile = mock(DefaultInputFile.class, RETURNS_DEEP_STUBS);
        when(csprojFile.contents()).thenReturn("");
        DotNetDependencyReason dotNetProject = new DotNetDependencyReason(csprojFile);
        assertFalse(dotNetProject.isReasonable());
        assertNotNull(dotNetProject.getInputComponent());
    }

    @Test
    void constructorWithIOException() throws IOException {
        DefaultInputFile csprojFile = mock(DefaultInputFile.class, RETURNS_DEEP_STUBS);
        when(csprojFile.contents()).thenThrow(new IOException());
        DotNetDependencyReason dotNetProject = new DotNetDependencyReason(csprojFile);
        assertFalse(dotNetProject.isReasonable());
        assertNotNull(dotNetProject.getInputComponent());
    }

    @Test
    void foundDependency() throws IOException {
        DotNetDependencyReason csprojFile = new DotNetDependencyReason(inputFile("ExampleCSProj.csproj"));
        // Create Dependency
        Identifier identifier1 = new Identifier("pkg:dotnet/IdentityServer@2.3.1", Confidence.HIGHEST);
        Collection<Identifier> packageidentifiers1 = new ArrayList<>();
        packageidentifiers1.add(identifier1);
        Dependency dependency = new Dependency(null, null, null, null, Collections.emptyMap(),Collections.emptyList(), packageidentifiers1, Collections.emptyList(), null);
        TextRangeConfidence textRangeConfidence = csprojFile.getBestTextRange(dependency);
        assertTrue(csprojFile.isReasonable());
        assertNotNull(textRangeConfidence);
        assertEquals(17, textRangeConfidence.getTextRange().start().line());
        assertEquals(0, textRangeConfidence.getTextRange().start().lineOffset());
        assertEquals(17, textRangeConfidence.getTextRange().end().line());
        assertEquals(65, textRangeConfidence.getTextRange().end().lineOffset());
        assertEquals(Confidence.HIGHEST, textRangeConfidence.getConfidence());
        // verify that same dependency points to the same TextRange, use of HashMap
        assertEquals(csprojFile.getBestTextRange(dependency), csprojFile.getBestTextRange(dependency));
    }

    @Test
    void foundDependencyOnlyWithName() throws IOException {
        DotNetDependencyReason csprojFile = new DotNetDependencyReason(inputFile("ExampleCSProj.csproj"));
        // Create Dependency
        Identifier identifier = new Identifier("pkg:dotnet/IdentityServer@9.9.9", Confidence.HIGHEST);
        Collection<Identifier> packageidentifiers1 = new ArrayList<>();
        packageidentifiers1.add(identifier);
        Dependency dependency = new Dependency(null, null, null, null, Collections.emptyMap(),Collections.emptyList(), packageidentifiers1, Collections.emptyList(), null);
        TextRangeConfidence textRangeConfidence = csprojFile.getBestTextRange(dependency);
        assertTrue(csprojFile.isReasonable());
        assertNotNull(textRangeConfidence);
        assertEquals(17, textRangeConfidence.getTextRange().start().line());
        assertEquals(0, textRangeConfidence.getTextRange().start().lineOffset());
        assertEquals(17, textRangeConfidence.getTextRange().end().line());
        assertEquals(65, textRangeConfidence.getTextRange().end().lineOffset());
        assertEquals(Confidence.HIGH, textRangeConfidence.getConfidence());
        // verify that same dependency points to the same TextRange, use of HashMap
        assertEquals(csprojFile.getBestTextRange(dependency), csprojFile.getBestTextRange(dependency));
    }

    @Test
    void foundDependencyWithoutVersion() throws IOException {
        DotNetDependencyReason csprojFile = new DotNetDependencyReason(inputFile("ExampleCSProj.csproj"));
        // Create Dependency
        Identifier identifier = new Identifier("pkg:dotnet/IdentityServer", Confidence.HIGHEST);
        Collection<Identifier> identifiersCollected = new ArrayList<>();
        identifiersCollected.add(identifier);
        Dependency dependency = new Dependency(null, null, null, null, Collections.emptyMap(),Collections.emptyList(), identifiersCollected, Collections.emptyList(), null);
        TextRangeConfidence textRangeConfidence = csprojFile.getBestTextRange(dependency);
        assertTrue(csprojFile.isReasonable());
        assertNotNull(textRangeConfidence);
        assertEquals(17, textRangeConfidence.getTextRange().start().line());
        assertEquals(0, textRangeConfidence.getTextRange().start().lineOffset());
        assertEquals(17, textRangeConfidence.getTextRange().end().line());
        assertEquals(65, textRangeConfidence.getTextRange().end().lineOffset());
        assertEquals(Confidence.HIGH, textRangeConfidence.getConfidence());
        // verify that same dependency points to the same TextRange, use of HashMap
        assertEquals(csprojFile.getBestTextRange(dependency), csprojFile.getBestTextRange(dependency));
    }

        @Test
    void foundNoDependency() throws IOException {
        DotNetDependencyReason csprojFile = new DotNetDependencyReason(inputFile("ExampleCSProj.csproj"));
        // Create Dependency
        Identifier identifier1 = new Identifier("pkg:dotnet/Dependency16/@1.2.8", Confidence.HIGHEST);
        Collection<Identifier> packageidentifiers1 = new ArrayList<>();
        packageidentifiers1.add(identifier1);
        Dependency dependency = new Dependency(null, null, null, null, Collections.emptyMap(),Collections.emptyList(), packageidentifiers1, Collections.emptyList(), null);
        TextRangeConfidence textRangeConfidence = csprojFile.getBestTextRange(dependency);
        // Check for default location, first line in file with low confidence
        assertNotNull(textRangeConfidence);
        assertEquals(LINE_NOT_FOUND, textRangeConfidence.getTextRange().start().line());
        assertEquals(Confidence.LOW, textRangeConfidence.getConfidence());
        // verify that same dependency points to the same TextRange, use of HashMap
        assertEquals(csprojFile.getBestTextRange(dependency), csprojFile.getBestTextRange(dependency));
    }
}
