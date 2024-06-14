/*
 * Copyright 2024 C Thing Software
 * SPDX-License-Identifier: Apache-2.0
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
 * Utility methods for manipulating file line counts.
 */
@SuppressWarnings("Convert2streamapi")
public final class CountUtils {

    @NoCoverageGenerated
    private CountUtils() {
    }

    /**
     * Calculates the line counts for each language in the specified counts.
     *
     * @param counts Line counts for each language in individual files
     * @return Line counts for each language in the specified counts
     */
    public static Map<Language, Counts> byLanguage(final Map<Path, Map<Language, Counts>> counts) {
        final Map<Language, Counts> langMap = new EnumMap<>(Language.class);
        for (final Map<Language, Counts> map : counts.values()) {
            for (final Map.Entry<Language, Counts> entry : map.entrySet()) {
                langMap.computeIfAbsent(entry.getKey(), l -> new Counts())
                       .add(entry.getValue());
            }
        }
        return Collections.unmodifiableMap(langMap);
    }

    /**
     * Groups the file by language.
     *
     * @param counts Line counts for each language in individual files
     * @return Files grouped by language. Note that because a single file my contain multiple languages
     *      (e.g. CSS in HTML), the same file may be associated with more than one language.
     */
    public static Map<Language, Set<Path>> byLanguageGroupedFile(final Map<Path, Map<Language, Counts>> counts) {
        final Map<Language, Set<Path>> groupedMap = new EnumMap<>(Language.class);
        for (final Map.Entry<Path, Map<Language, Counts>> pathEntry : counts.entrySet()) {
            for (final Map.Entry<Language, Counts> langEntry : pathEntry.getValue().entrySet()) {
                groupedMap.computeIfAbsent(langEntry.getKey(), l -> new HashSet<>())
                          .add(pathEntry.getKey());
            }
        }
        return Collections.unmodifiableMap(groupedMap);
    }

    /**
     * Calculates the line counts for each file in the specified counts.
     *
     * @param counts Line counts for each language in individual files
     * @return Line counts for each file regardless of language
     */
    public static Map<Path, Counts> byFile(final Map<Path, Map<Language, Counts>> counts) {
        final Map<Path, Counts> fileMap = new HashMap<>();
        for (final Map.Entry<Path, Map<Language, Counts>> pathEntry : counts.entrySet()) {
            for (final Map.Entry<Language, Counts> langEntry : pathEntry.getValue().entrySet()) {
                fileMap.computeIfAbsent(pathEntry.getKey(), p -> new Counts())
                       .add(langEntry.getValue());
            }
        }
        return Collections.unmodifiableMap(fileMap);
    }

    /**
     * Obtains the languages in the specified counts.
     *
     * @param counts Line counts for each language in the specified files
     * @return All languages in the specified counts
     */
    public static Set<Language> languages(final Map<Path, Map<Language, Counts>> counts) {
        final Set<Language> langs = new HashSet<>();
        for (final Map<Language, Counts> langMap : counts.values()) {
            langs.addAll(langMap.keySet());
        }
        return Collections.unmodifiableSet(langs);
    }

    /**
     * Obtains the files in the specified counts.
     *
     * @param counts Line counts for each language in the specified files
     * @return All files in the specified counts
     */
    public static Set<Path> files(final Map<Path, Map<Language, Counts>> counts) {
        return Collections.unmodifiableSet(counts.keySet());
    }

    /**
     * Obtains the files that were not recognized and therefore produced no counts.
     *
     * @param counts Line counts for each language in the specified file
     * @return Files that were not recognized. If the primary language of a file cannot be determined or
     *      is not supported by this library, the entry for the file contains an empty language map.
     */
    public static Set<Path> unrecognized(final Map<Path, Map<Language, Counts>> counts) {
        final Set<Path> unrecog = new HashSet<>();
        for (final Map.Entry<Path, Map<Language, Counts>> entry : counts.entrySet()) {
            if (entry.getValue().isEmpty()) {
                unrecog.add(entry.getKey());
            }
        }
        return Collections.unmodifiableSet(unrecog);
    }

    /**
     * Calculates the total line count based on the specified counts.
     *
     * @param counts Line counts for each language in the specified files
     * @return Total line count for all files and languages.
     */
    public static Counts total(final Map<Path, Map<Language, Counts>> counts) {
        final Counts s = new Counts();
        for (final Map<Language, Counts> map : counts.values()) {
            for (final Counts entryCounts : map.values()) {
                s.add(entryCounts);
            }
        }
        return s;
    }
}
