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

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Map;

import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.TaskExecutionException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import freemarker.cache.ClassTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;


/**
 * Task that reads the languages.json file and generates the Language.java enum.
 */
public class LanguageTask extends DefaultTask {

    private static final Configuration TEMPLATE_CONFIG = new Configuration(Configuration.VERSION_2_3_32);
    static {
        TEMPLATE_CONFIG.setTemplateLoader(new ClassTemplateLoader(LanguageTask.class, "/"));
        TEMPLATE_CONFIG.setDefaultEncoding(StandardCharsets.UTF_8.name());
        TEMPLATE_CONFIG.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        TEMPLATE_CONFIG.setLogTemplateExceptions(false);
        TEMPLATE_CONFIG.setWrapUncheckedExceptions(true);
    }

    private static final String TEMPLATE_PATHNAME = "/org/cthing/locc4j/Language.java.ftl";


    private final File jsonFile;
    private final File languageFile;

    public LanguageTask() {
        setGroup("Build");
        setDescription("Generates the Language enum from the languages.json file");

        final Project rootProject = getProject().getRootProject();
        this.jsonFile = rootProject.file("conf/languages.json");
        this.languageFile = rootProject.file("generated-src/org/cthing/locc4j/Language.java");
    }

    @InputFile
    public File getJsonFile() {
        return this.jsonFile;
    }

    @OutputFile
    public File getLanguageFile() {
        return this.languageFile;
    }

    /**
     * Performs the work of translating the languages.json file to an enum.
     */
    @TaskAction
    @SuppressWarnings("Convert2Diamond")
    public void generate() {
        final ObjectMapper mapper = new ObjectMapper().enable(JsonParser.Feature.INCLUDE_SOURCE_IN_LOCATION);
        try {
            // Read the language.json file.
            final Map<String, Map<String, LanguageEntry>> languages =
                    mapper.readValue(this.jsonFile, new TypeReference<Map<String, Map<String, LanguageEntry>>>() { });

            // Generate the Language enum from the template.
            try (Writer writer = new OutputStreamWriter(Files.newOutputStream(this.languageFile.toPath()),
                                                        StandardCharsets.UTF_8)) {
                final Template temp = TEMPLATE_CONFIG.getTemplate(TEMPLATE_PATHNAME);
                temp.process(languages, writer);
            }
        } catch (final IOException | TemplateException ex) {
            throw new TaskExecutionException(this, ex);
        }
    }
}
