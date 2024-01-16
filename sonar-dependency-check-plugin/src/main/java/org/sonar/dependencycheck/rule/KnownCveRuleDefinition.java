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
package org.sonar.dependencycheck.rule;

import javax.annotation.ParametersAreNonnullByDefault;

import org.sonar.api.issue.impact.Severity;
import org.sonar.api.issue.impact.SoftwareQuality;
import org.sonar.api.rule.RuleStatus;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.dependencycheck.base.DependencyCheckConstants;

public class KnownCveRuleDefinition implements RulesDefinition {

    private static final int CWE_937 = 937;

    @Override
    @ParametersAreNonnullByDefault
    public void define(Context context) {
        NewRepository repo = context.createRepository(DependencyCheckConstants.REPOSITORY_KEY,
                DependencyCheckConstants.LANGUAGE_KEY);
        repo.setName("OWASP");

        NewRule rule = repo.createRule(DependencyCheckConstants.RULE_KEY);
        fillOWASPRule(rule);
        rule.addDeprecatedRuleKey(DependencyCheckConstants.REPOSITORY_KEY, DependencyCheckConstants.RULE_KEY_WITH_SECURITY_HOTSPOT);
        repo.done();
    }

    private void fillOWASPRule(NewRule rule) {
        rule.addTags("cwe-937", "cwe", "cve", "owasp-a9", "security", "vulnerability");
        rule.setName("Using Components with Known Vulnerabilities");
        rule.addDefaultImpact(SoftwareQuality.SECURITY, Severity.MEDIUM);
        rule.setStatus(RuleStatus.READY);
        rule.addOwaspTop10(OwaspTop10Version.Y2017, OwaspTop10.A9);
        rule.addOwaspTop10(OwaspTop10Version.Y2021, OwaspTop10.A6);
        rule.addCwe(CWE_937);

        String description = "<p>Components, such as libraries, frameworks, and other software modules, "
                + "almost always run with full privileges. If a vulnerable component is exploited, such "
                + "an attack can facilitate serious data loss or server takeover. Applications using "
                + "components with known vulnerabilities may undermine application defenses and enable "
                + "a range of possible attacks and impacts.</p>"
                + "<h3>References:</h3>"
                + "<ul><li>OWASP Top 10 2013-A9: <a href=\"https://www.owasp.org/index.php/Top_10_2013-A9-Using_Components_with_Known_Vulnerabilities\">Using Components with Known Vulnerabilities</a></li>"
                + "<li><a href=\"https://cwe.mitre.org/data/definitions/937.html\">Common Weakness Enumeration CWE-937</a></li>"
                + "<p>This issue was generated by <a href=\"https://www.owasp.org/index.php/OWASP_Dependency_Check\">Dependency-Check</a>";
        rule.setHtmlDescription(description);
    }

}