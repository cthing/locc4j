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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;


/**
 * Counts lines in a file.
 */
public class FileCounter {

    private boolean countDocStrings = true;

    /**
     * Sets whether to count documentation string as comments or ignore them.
     *
     * @param enable {@code true} to count documentation strings as comments or {@code false} to ignore them.
     *      The default is to count documentation strings as comments.
     * @return This counter.
     */
    public FileCounter countDocStrings(final boolean enable) {
        this.countDocStrings = enable;
        return this;
    }

    /**
     * Counts the number of lines in the specified files.
     *
     * @param pathnames Files whose lines are to be counted
     * @return The line counts for each pathname. If the language of a file cannot be determined, an empty
     *      language map is returned for that file.
     * @throws IOException if there was a problem reading the files
     * @throws IllegalArgumentException if the collection is empty or a file is a directory
     */
    public Map<Path, Map<Language, Counts>> count(final String... pathnames) throws IOException {
        if (pathnames.length == 1) {
            final Path path = Path.of(pathnames[0]);
            return Map.of(path, count(path));
        }
        return count(Arrays.stream(pathnames).map(Path::of).toList());
    }

    /**
     * Counts the number of lines in the specified files.
     *
     * @param files Files whose lines are to be counted
     * @return The line counts for each file. If the language of a file cannot be determined, an empty
     *      language map is returned for that file.
     * @throws IOException if there was a problem reading the files
     * @throws IllegalArgumentException if the collection is empty or a file is a directory
     */
    public Map<Path, Map<Language, Counts>> count(final Path... files) throws IOException {
        return files.length == 1 ? Map.of(files[0], count(files[0])) : count(List.of(files));
    }

    /**
     * Counts the number of lines in the specified files.
     *
     * @param files Files whose lines are to be counted
     * @return The line counts for each file. If the language of a file cannot be determined, an empty
     *      language map is returned for that file.
     * @throws IOException if there was a problem reading the files
     * @throws IllegalArgumentException if the collection is empty or a file is a directory
     */
    public Map<Path, Map<Language, Counts>> count(final Collection<Path> files) throws IOException {
        if (files.isEmpty()) {
            throw new IllegalArgumentException("At least one pathname must be specified.");
        }

        if (files.size() == 1) {
            final Path path = files.iterator().next();
            return Map.of(path, count(path));
        }

        final Map<Path, Map<Language, Counts>> results = new HashMap<>(files.size());
        for (final Path file : files) {
            results.put(file, count(file));
        }
        return results;
    }

    /**
     * Counts the number of lines in the specified file.
     *
     * @param file File whose lines are to be counted
     * @return Map of the languages found in the specified file and their counts. If the language of the file
     *      cannot be determined, an empty map is returned.
     * @throws IOException if there was a problem reading the file
     * @throws IllegalArgumentException if the specified file is a directory
     */
    private Map<Language, Counts> count(final Path file) throws IOException {
        if (Files.isDirectory(file)) {
            throw new IllegalArgumentException("Specified path must be a file");
        }

        if (!Files.exists(file)) {
            throw new FileNotFoundException("Could not find file: " + file);
        }

        if (!Files.isReadable(file)) {
            return Map.of();
        }

        final Optional<Language> languageOpt = Language.fromFile(file);
        if (languageOpt.isEmpty()) {
            return Map.of();
        }

        try (InputStream ins = Files.newInputStream(file)) {
            return new Counter(languageOpt.get()).countDocStrings(this.countDocStrings).count(ins);
        }
    }
}
