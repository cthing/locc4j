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
     * Counts the number of lines in the specified file.
     *
     * @param file File whose lines are to be counted
     * @return Map of the languages found in the specified file and their counts.
     * @throws IOException if there was a problem reading the file
     * @throws IllegalArgumentException if the specified file is a directory
     */
    public Map<Language, Stats> count(final Path file) throws IOException {
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
