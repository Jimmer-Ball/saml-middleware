package com.timepoorprogrammer.saml;

import com.timepoorprogrammer.common.utilities.io.ArtifactHelper;
import com.timepoorprogrammer.common.utilities.io.Common;
import com.timepoorprogrammer.common.utilities.io.artifacts.Artifact;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Test helper for finding resources somewhere in our classpath, whether we are running under maven or not.
 *
 * @author Jim Ball
 */
public class TestHelper {
    /**
     * Given the input pattern go pickup any artifacts found under the test-classes output directory that match
     * whether we are running under maven or not.
     *
     * @param pattern pattern to look for on the class path
     * @return artifacts(s)
     */
    public static List<Artifact> getArtifacts(final String pattern) {
        ArtifactHelper artifactHelper = new ArtifactHelper();
        List<Artifact> artifacts;
        if (!Common.runningUnderMaven()) {
            artifacts = artifactHelper.getArtifactRecords(pattern, "^.*test-classes.*$");
        } else {
            artifacts = artifactHelper.getArtifactRecords(pattern, "^.*test-classes.*$", artifactHelper.getMavenRunnerManifest().getClassPathResources());
        }
        return artifacts;
    }

    /**
     * Given the input pattern go pickup any artifacts found under the test-classes output directory that match
     * whether we are running under maven or not.
     *
     * @param pattern         pattern to look for on the class path
     * @param resourcePattern Limiting pattern applied to overall classpath available to limit the scope of the search
     * @return artifacts(s)
     */
    public static List<Artifact> getArtifacts(final String pattern, final String resourcePattern) {
        ArtifactHelper artifactHelper = new ArtifactHelper();
        List<Artifact> artifacts;
        if (!Common.runningUnderMaven()) {
            artifacts = artifactHelper.getArtifactRecords(pattern, resourcePattern);
        } else {
            artifacts = artifactHelper.getArtifactRecords(pattern, resourcePattern, artifactHelper.getMavenRunnerManifest().getClassPathResources());
        }
        return artifacts;
    }

    /**
     * Get the full path to what is expected to be a unique file
     *
     * @param pattern pattern to look for on the class path
     * @return file path
     */
    public static String getFullPath(final String pattern) {
        List<Artifact> artifacts = TestHelper.getArtifacts(pattern);
        assertThat(artifacts.size(), is(1));
        return artifacts.get(0).getPath();
    }

    /**
     * Get the full path to what is expected to be a unique file
     *
     * @param pattern         pattern to look for on the class path
     * @param resourcePattern Limiting pattern applied to overall classpath available to limit the scope of the search
     * @return file path
     */
    public static String getFullPath(final String pattern, final String resourcePattern) {
        List<Artifact> artifacts = TestHelper.getArtifacts(pattern, resourcePattern);
        assertThat(artifacts.size(), is(1));
        return artifacts.get(0).getPath();
    }
}

