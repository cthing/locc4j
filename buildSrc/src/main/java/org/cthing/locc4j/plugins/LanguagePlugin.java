/*
 * Copyright 2023 C Thing Software
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
