/*
 * Copyright 2024 C Thing Software
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

import java.nio.file.Path;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.cthing.annotations.NoCoverageGenerated;


/**
 * Utility methods for manipulating file line count stats.
 */
@SuppressWarnings("Convert2streamapi")
public final class StatsUtils {

    @NoCoverageGenerated
    private StatsUtils() {
    }

    /**
     * Calculates the line count stats for each language in the specified stats.
     *
     * @param stats Line count stats for each language in individual files
     * @return Line count stats for each language in the specified stats
     */
    public static Map<Language, Stats> byLanguage(final Map<Path, Map<Language, Stats>> stats) {
        final Map<Language, Stats> langMap = new EnumMap<>(Language.class);
        for (final Map<Language, Stats> map : stats.values()) {
            for (final Map.Entry<Language, Stats> entry : map.entrySet()) {
                langMap.computeIfAbsent(entry.getKey(), l -> new Stats()).add(entry.getValue());
            }
        }
        return Collections.unmodifiableMap(langMap);
    }

    /**
     * Calculates the line count stats for each file in the specified stats.
     *
     * @param stats Line count stats for each language in individual files
     * @return Line count stats for each file regardless of language
     */
    public static Map<Path, Stats> byFile(final Map<Path, Map<Language, Stats>> stats) {
        final Map<Path, Stats> fileMap = new HashMap<>();
        for (final Map.Entry<Path, Map<Language, Stats>> pathEntry : stats.entrySet()) {
            for (final Map.Entry<Language, Stats> langEntry : pathEntry.getValue().entrySet()) {
                fileMap.computeIfAbsent(pathEntry.getKey(), p -> new Stats()).add(langEntry.getValue());
            }
        }
        return Collections.unmodifiableMap(fileMap);
    }

    /**
     * Obtains the languages in the specified stats.
     *
     * @param stats Line count stats for each language in the specified files
     * @return All languages in the specified stats
     */
    public static Set<Language> languages(final Map<Path, Map<Language, Stats>> stats) {
        final Set<Language> langs = new HashSet<>();
        for (final Map<Language, Stats> langMap : stats.values()) {
            langs.addAll(langMap.keySet());
        }
        return Collections.unmodifiableSet(langs);
    }

    /**
     * Obtains the files in the specified stats.
     *
     * @param stats Line count stats for each language in the specified files
     * @return All files in the specified stats
     */
    public static Set<Path> files(final Map<Path, Map<Language, Stats>> stats) {
        return Collections.unmodifiableSet(stats.keySet());
    }

    /**
     * Calculates the total line count based on the specified stats.
     *
     * @param stats Line count stats for each language in the specified files
     * @return Total line count for all files and languages.
     */
    public static Stats total(final Map<Path, Map<Language, Stats>> stats) {
        final Stats s = new Stats();
        for (final Map<Language, Stats> map : stats.values()) {
            for (final Stats entryStats : map.values()) {
                s.add(entryStats);
            }
        }
        return s;
    }
}
