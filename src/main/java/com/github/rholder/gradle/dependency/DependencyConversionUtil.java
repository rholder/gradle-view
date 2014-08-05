/*
 * Copyright 2013 Ray Holder
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.rholder.gradle.dependency;

import com.github.rholder.gradle.acumen.AcumenTreeModel;
import com.github.rholder.gradle.acumen.GradleTreeNode;
import com.github.rholder.gradle.log.ToolingLogger;
import com.google.common.collect.Maps;
import org.apache.commons.io.IOUtils;
import org.gradle.tooling.BuildAction;
import org.gradle.tooling.BuildActionExecuter;
import org.gradle.tooling.BuildController;
import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.ProgressEvent;
import org.gradle.tooling.ProgressListener;
import org.gradle.tooling.ProjectConnection;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Collections;
import java.util.Map;

/**
 * This is a utility class to handle conversion from the dependency tree output
 * given by 'gradle dependencies' into a node graph built on GradleNode
 * entities.
 */
public class DependencyConversionUtil {

    /**
     * Use the Gradle Tooling API to extract dependency information on the
     * given path, returning the root node of the dependency graph.
     *
     * @param projectPath   the path to the target project to load
     * @param toolingLogger instance to use for Gradle tooling log messages
     */
    public static Map<String, GradleNode> loadProjectDependenciesFromModel(String projectPath, final ToolingLogger toolingLogger) {
        if (projectPath == null) {
            return Collections.singletonMap("root", new GradleNode("No Gradle project directory selected..."));
        }

        ProjectConnection connection = GradleConnector.newConnector()
                .forProjectDirectory(new File(projectPath))
                .connect();

        Map<String, GradleNode> dependencyMap = Maps.newHashMap();
        AcumenTreeModel atm;
        try {
            @SuppressWarnings("unchecked")
            BuildActionExecuter<AcumenTreeModel> b = connection.action(new AcumenModelAction());
            b.addProgressListener(new ProgressListener() {
                public void statusChanged(ProgressEvent event) {
                    toolingLogger.log(event.getDescription());
                }
            });

            // TODO this needs to be cached

            // extract from classpath
            File initAcumenFile = File.createTempFile("init-acumen", ".gradle");
            initAcumenFile.deleteOnExit();

            // use a custom plugin, if this value is set
            File extractedJarFile;
            String devGradleAcumen = System.getProperty("gradle.view.debug.acumen.jar");
            if(devGradleAcumen == null) {
                extractedJarFile = File.createTempFile("gradle-acumen", "jar");
                extractedJarFile.deleteOnExit();
                dumpFromClasspath("/gradle-acumen-0.1.0.jar", extractedJarFile);
            } else {
                extractedJarFile = new File(devGradleAcumen);
            }

            acumenTemplateFromClasspath(extractedJarFile, initAcumenFile);

            b.withArguments("--init-script", initAcumenFile.getAbsolutePath());
            atm = b.run();

            GradleNode rootNode = convertToGradleNode(atm);
            dependencyMap.put("root", rootNode);
        } catch (Exception e) {
            // TODO add errors to toolingLogger
            e.printStackTrace();
            throw new RuntimeException(e);
        } finally {
            connection.close();
        }

        return dependencyMap;
    }

    private static void acumenTemplateFromClasspath(File extractedJarFile, File outputFile) throws IOException {
        InputStream input = DependencyConversionUtil.class.getResourceAsStream("/init-acumen.gradle");
        String processed = IOUtils.toString(input).replace("#ACUMEN_JAR#", extractedJarFile.getAbsolutePath());
        input.close();

        InputStream processedInput = IOUtils.toInputStream(processed);

        OutputStream output = new BufferedOutputStream(new FileOutputStream(outputFile));
        IOUtils.copy(processedInput, output);

        output.close();
        processedInput.close();
    }

    private static void dumpFromClasspath(String classpath, File outputFile) throws IOException {
        InputStream input = DependencyConversionUtil.class.getResourceAsStream(classpath);
        OutputStream output = new BufferedOutputStream(new FileOutputStream(outputFile));
        IOUtils.copy(input, output);
        input.close();
        output.close();
    }

    private static GradleNode convertToGradleNode(AcumenTreeModel atm) {
        return convertToGradleNode(null, atm.getNodeTree());
    }

    private static GradleNode convertToGradleNode(GradleNode parent, GradleTreeNode treeNode) {
        GradleNode gradleNode;
        if (treeNode.getName() == null) {
            gradleNode = new GradleNode(parent, treeNode.getGroup(), treeNode.getId(), treeNode.getVersion());
            gradleNode.omitted = treeNode.getSeenBefore();
        } else {
            gradleNode = new GradleNode(treeNode.getName());
        }

        for (GradleTreeNode c : treeNode.getChildren()) {
            gradleNode.dependencies.add(convertToGradleNode(gradleNode, c));
        }

        return gradleNode;
    }

    private static class AcumenModelAction implements Serializable, BuildAction {
        @Override
        public AcumenTreeModel execute(BuildController controller) {
            return controller.getModel(AcumenTreeModel.class);
        }
    }
}
