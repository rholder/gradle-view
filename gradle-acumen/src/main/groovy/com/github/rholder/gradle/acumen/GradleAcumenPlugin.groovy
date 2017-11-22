package com.github.rholder.gradle.acumen

import com.github.rholder.gradle.acumen.api.AcumenTreeModel
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.component.ModuleComponentSelector
import org.gradle.api.artifacts.result.DependencyResult
import org.gradle.api.artifacts.result.ResolvedDependencyResult
import org.gradle.tooling.provider.model.ToolingModelBuilder
import org.gradle.tooling.provider.model.ToolingModelBuilderRegistry

import javax.inject.Inject

class GradleAcumenPlugin implements Plugin<Project> {
    final ToolingModelBuilderRegistry registry;

    @Inject
    public GradleAcumenPlugin(ToolingModelBuilderRegistry registry) {
        this.registry = registry;
    }

    @Override
    void apply(Project project) {
        registry.register(new AcumenToolingModelBuilder())
    }

    // note: if this method is inside the private AcumenToolingModelBuilder below, it causes issues with Groovy
    static DefaultGradleTreeNode resolveDependency(DefaultGradleTreeNode parentNode, DependencyResult result, Set<DefaultGradleTreeNode> existingDeps) {
        DefaultGradleTreeNode node = new DefaultGradleTreeNode()
        if (result instanceof ResolvedDependencyResult) {
            ResolvedDependencyResult r = result
            node.parent = parentNode
            node.group = r.selected.moduleVersion.group
            node.id = r.selected.moduleVersion.name
            node.version = r.selected.moduleVersion.version
            node.reason = r.selected.selectionReason.description

            if(r.requested instanceof ModuleComponentSelector) {
                node.requestedVersion = ((ModuleComponentSelector)r.requested).version
            }

            node.nodeType = "dependency"

            if (existingDeps.add(node)) {
                // only process children if we haven't seen this dep before
                r.selected.dependencies.each { DependencyResult subDep ->
                    DefaultGradleTreeNode childNode = resolveDependency(node, subDep, existingDeps)
                    node.children.add(childNode)
                }
            } else {
                node.seenBefore = true
            }
        } else {
            // TODO this is where a fix for #10 will start, consider setting node.wasResolved = false or some such
            node.name = "Could not resolve $result.requested.displayName"
        }

        return node
    }

    static boolean canBeResolved(Configuration conf) {
        try {
            // this method doesn't exist before Gradle 3
            return conf.isCanBeResolved();
        } catch (Exception e) {
            // assume everything can be resolved for Gradle < 3
            return true;
        }
    }

    static DefaultGradleTreeNode generateProjectTree(Project project) {
        DefaultGradleTreeNode rootNode = new DefaultGradleTreeNode(
                name: project.name,
                group: project.group,
                version: project.version,
                nodeType: "project"
        )

        project.subprojects.each {
            rootNode.children.add(generateProjectTree(it))
        }

        //noinspection GroovyAssignabilityCheck
        project.configurations.each { Configuration conf ->
            DefaultGradleTreeNode configurationNode = new DefaultGradleTreeNode(
                    name: conf.name,
                    nodeType: "configuration"
            )

            // reprocessing existing deps can overflow the stack when there are cycles
            Set<DefaultGradleTreeNode> existingDeps = new LinkedHashSet<DefaultGradleTreeNode>()
            if(canBeResolved(conf)) {
                conf.incoming.resolutionResult.root.dependencies.each { DependencyResult dr ->
                    DefaultGradleTreeNode dependencyNode = resolveDependency(configurationNode, dr, existingDeps)
                    configurationNode.children.add(dependencyNode)
                }

                rootNode.children.add(configurationNode)
            }
        }

        return rootNode
    }

    private static class AcumenToolingModelBuilder implements ToolingModelBuilder {

        public boolean canBuild(String modelName) {
            modelName.equals(AcumenTreeModel.class.getName())
        }

        public Object buildAll(String modelName, Project project) {
            new DefaultAcumenTreeModel(
                    nodeTree: generateProjectTree(project)
            )
        }
    }
}
