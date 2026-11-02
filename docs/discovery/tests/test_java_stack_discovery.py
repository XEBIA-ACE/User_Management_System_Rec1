"""Characterization checks for the Java stack discovery document (story 821e2e5f-1c36-562c-80a5-309b01416304)."""

import re
import unittest
from pathlib import Path

REPO_ROOT = Path(__file__).resolve().parents[3]
DOC = REPO_ROOT / "docs" / "discovery" / "java-stack-discovery.md"
STORY_DIR = REPO_ROOT / "specs" / "821e2e5f-1c36-562c-80a5-309b01416304"


def section(text, heading_prefix):
    match = re.search(rf"^## {re.escape(heading_prefix)}.*?$(.*?)(?=^## |\Z)", text, re.S | re.M)
    if not match:
        raise AssertionError(f"missing section: {heading_prefix}")
    return match.group(1)


class JavaStackDiscoveryDocumentTest(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.text = DOC.read_text(encoding="utf-8")
        cls.pom = (REPO_ROOT / "app" / "pom.xml").read_text(encoding="utf-8")
        cls.dockerfile = (REPO_ROOT / "app" / "Dockerfile").read_text(encoding="utf-8")

    def test_s1_ac1_java_runtime_version_documented(self):
        """[S1-AC1] / FR-001: Java version for compile and runtime, with dev/prod differences."""
        body = section(self.text, "Java runtime version (FR-001 / S1-AC1)")
        pom_version = re.search(r"<java.version>(\d+)</java.version>", self.pom).group(1)
        runtime_version = re.search(r"eclipse-temurin:(\d+)-jre", self.dockerfile).group(1)
        self.assertIn(f"<java.version>{pom_version}</java.version>", body)
        self.assertIn(f"eclipse-temurin:{runtime_version}-jre-alpine", body)
        self.assertIn("Dev vs. prod", body)

    def test_s1_ac2_build_tool_and_dependency_management_documented(self):
        """[S1-AC2] / FR-002: build tool, descriptor location, dependency management, unknowns."""
        body = section(self.text, "Build tool and dependency management (FR-002 / S1-AC2)")
        self.assertIn("Maven", body)
        self.assertIn("`app/pom.xml`", body)
        self.assertTrue((REPO_ROOT / "app" / "pom.xml").is_file())
        self.assertIn("spring-boot-starter-parent:3.2.5", body)
        self.assertIn("Dependency management", body)
        self.assertIn("U-3", body)

    def test_s1_ac3_framework_and_interfaces_documented(self):
        """[S1-AC3] / FR-003: primary framework, HTTP/other interfaces, recorded uncertainty."""
        body = section(self.text, "Primary framework and interfaces (FR-003 / S1-AC3)")
        boot_version = re.search(
            r"<artifactId>spring-boot-starter-parent</artifactId>\s*<version>([^<]+)</version>", self.pom
        ).group(1)
        self.assertIn(f"Spring Boot {boot_version}", body)
        self.assertIn("HTTP interface", body)
        self.assertIn("AMQP", body)
        self.assertIn("Uncertainty", body)

    def test_s1_ac4_packaging_and_deployment_documented(self):
        """[S1-AC4] / FR-004: packaging format, hosting model, explicit containerization status."""
        body = section(self.text, "Packaging and deployment model (FR-004 / S1-AC4)")
        self.assertIn("<packaging>jar</packaging>", self.pom)
        self.assertIn("**JAR**", body)
        self.assertIn("standalone JVM", body)
        self.assertIn("Containerization status", body)
        self.assertTrue((REPO_ROOT / "app" / "Dockerfile").is_file())
        self.assertIn("U-2", body)

    def test_s1_ac5_summary_unknowns_and_baseline_reference(self):
        """[S1-AC5] / FR-005: concise summary, unresolved unknowns, baseline reference from story dir."""
        summary = section(self.text, "Summary")
        for topic in ("Java runtime", "Build tool", "Primary framework", "Packaging", "Hosting model", "Containerized?"):
            self.assertIn(topic, summary)
        unknowns = section(self.text, "Unresolved unknowns")
        self.assertRegex(unknowns, r"\| U-1 \|")
        section(self.text, "Baseline reference (FR-005 / S1-AC5)")
        story_readme = (STORY_DIR / "README.md").read_text(encoding="utf-8")
        self.assertIn("docs/discovery/java-stack-discovery.md", story_readme)
        self.assertIn("docs/discovery/java-stack-discovery.md", (REPO_ROOT / "README.md").read_text(encoding="utf-8"))


if __name__ == "__main__":
    unittest.main()
