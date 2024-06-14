/*
 * Copyright 2024 C Thing Software
 * SPDX-License-Identifier: Apache-2.0
 */

package org.cthing.locc4j.plugins;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskProvider;


/**
 * Plugin to generate the Language.java enum from the languages.json file.
 */
public class LanguagePlugin implements Plugin<Project> {

    @Override
    public void apply(final Project project) {
        project.getPluginManager().apply("java");

        // Create the task to generate the Language enum from the languages.json file.
        final TaskProvider<LanguageTask> languageTaskProvider = project.getTasks().register("generateLanguage",
                                                                                            LanguageTask.class);

        // Run the generate task before compilation.
        project.getTasks().named("compileJava").configure(compileTask -> compileTask.dependsOn(languageTaskProvider));
    }
}
