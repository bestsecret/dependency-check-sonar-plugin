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
package org.sonar.dependencycheck.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.io.InputStream;

import org.junit.jupiter.api.Test;
import org.sonar.dependencycheck.reason.dotnet.DotNetDependencyLocation;
import org.sonar.dependencycheck.reason.dotnet.DotNetCSProjModel;

class CSProjParserHerlperTest {

    @Test
    void parseCsProj() throws Exception {
        InputStream csproj = getClass().getClassLoader().getResourceAsStream("reason/ExampleCSProj.csproj");
        DotNetCSProjModel csProjModel = CSProjParserHelper.parse(csproj);
        assertNotNull(csProjModel);
        // check some dependencies
        checkDotNetDependency(csProjModel, "IdentityServer", "2.3.1", 17, 17);
        checkDotNetDependency(csProjModel, "Full.Qualified.Name.Dependency1", "1.0.0", 18, 18);
        checkDotNetDependency(csProjModel, "Dependency2", "13.10.86", 19,  19);
        checkDotNetDependency(csProjModel, "Microsoft.EntityFrameworkCore.Tools", "6.0.3", 20, 23);
    }

    private void checkDotNetDependency(DotNetCSProjModel csProjModel, String name, String version, int startLineNr, int endLineNr) {
        boolean found = false;
        for (DotNetDependencyLocation dotNetDependency : csProjModel.getDependencies()) {
            if (name.equals(dotNetDependency.getName())) {
                found = true;
                assertEquals(name, dotNetDependency.getName());
                assertEquals(version, dotNetDependency.getVersion().get());
                assertEquals(startLineNr, dotNetDependency.getStartLineNr());
                assertEquals(endLineNr, dotNetDependency.getEndLineNr());
            }
        }
        assertTrue(found, "We haven't found dependency " + name);
    }

    @Test
    void parseCsProjIOException() {
        InputStream inputStream = mock(InputStream.class);
        doThrow(IOException.class).when(inputStream);
        ReportParserException exception = assertThrows(ReportParserException.class, () -> CSProjParserHelper.parse(inputStream), "No IOException thrown");
        assertEquals("Could not parse CSProj", exception.getMessage());
    }
}
