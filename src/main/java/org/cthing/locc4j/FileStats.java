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

package org.cthing.locc4j;

import java.io.File;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.concurrent.ThreadSafe;


/**
 * Counts for a specific file.
 */
@ThreadSafe
public class FileStats {

    private final Map<Language, LanguageStats> languages;
    private final File file;

    public FileStats(final File file) {
        this.file = file;
        this.languages = Collections.synchronizedMap(new EnumMap<>(Language.class));
    }

    /**
     * Obtains the file to which the counts apply.
     *
     * @return File to which the counts apply.
     */
    public File getFile() {
        return this.file;
    }

    /**
     * Obtains a read-only map of the languages present in this file and their counts. While most files consist
     * of a single language, some can be comprised of multiple languages. For example, an HTML file can contain
     * CSS and JavaScript in addition to markup.
     *
     * @return Map of languages present in this file and their counts.
     */
    public Map<Language, LanguageStats> getStats() {
        return Collections.unmodifiableMap(this.languages);
    }

    /**
     * Obtains the counts for the specified language.
     *
     * @param language Language whose counts are desired
     * @return Counts for the specified language. If the language has not yet been counted, a new
     *      {@link LanguageStats} instance will be created, added to this class and returned.
     */
    LanguageStats stats(final Language language) {
        return this.languages.computeIfAbsent(language, LanguageStats::new);
    }

    @Override
    public String toString() {
        final String indent = " ".repeat(4);
        return this.file + ":\n" + this.languages.values()
                                                 .stream()
                                                 .map(stats -> indent + stats)
                                                 .collect(Collectors.joining("\n"));
    }
}
