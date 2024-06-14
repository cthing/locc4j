/*
 * Copyright 2024 C Thing Software
 * SPDX-License-Identifier: Apache-2.0
 */

package org.cthing.locc4j;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.aggregator.ArgumentsAccessor;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIOException;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.entry;


public class FileCounterTest {

    @Test
    public void testDirectory(@TempDir final Path tempDir) {
        final FileCounter counter = new FileCounter();
        assertThatIllegalArgumentException().isThrownBy(() -> counter.count(tempDir));
    }

    @Test
    public void testMissingFile() {
        final FileCounter counter = new FileCounter();
        assertThatIOException().isThrownBy(() -> counter.count(Path.of("_notFound__")));
    }

    @Test
    public void testUnknownFile(@TempDir final Path tempDir) throws IOException {
        final Path file = tempDir.resolve("_foo_");
        Files.writeString(file, "");
        final FileCounter counter = new FileCounter();
        assertThat(counter.count(file)).containsExactly(entry(file, Map.of()));
    }

    @Test
    public void testUnreadableFile(@TempDir final Path tempDir) throws IOException {
        final Path file = tempDir.resolve("foo");
        Files.writeString(file, "hello");

        try (MockedStatic<Files> files = Mockito.mockStatic(Files.class)) {
            files.when(() -> Files.exists(file)).thenReturn(true);
            files.when(() -> Files.isReadable(file)).thenReturn(false);

            final FileCounter counter = new FileCounter();
            assertThat(counter.count(file)).containsExactly(entry(file, Map.of()));
        }
    }

    @ParameterizedTest
    @ArgumentsSource(FileDataProvider.class)
    public void testCount(final ArgumentsAccessor accessor) throws IOException {
        final int numLanguageParams = accessor.size() - 2;
        if (numLanguageParams % 4 != 0) {
            throw new IllegalArgumentException("Incorrect number of arguments. Filename plus 4 parameters per language");
        }

        final URL url = getClass().getResource("/data/" + accessor.getString(0));
        assert url != null;

        final FileCounter counter = new FileCounter().countDocStrings(accessor.getBoolean(1));
        final Map<Language, Counts> actualCounts = counter.count(url.getPath()).values().iterator().next();

        assertThat(actualCounts).as("Incorrect number of languages counted").hasSize(numLanguageParams / 4);

        for (int i = 0; i < numLanguageParams; i += 4) {
            final Language language = accessor.get(i + 2, Language.class);
            final int codeLines = accessor.getInteger(i + 3);
            final int commentLines = accessor.getInteger(i + 4);
            final int blankLines = accessor.getInteger(i + 5);
            assertThat(actualCounts).hasEntrySatisfying(language, languageCounts -> {
                assertThat(languageCounts.codeLines).as(language + ": Code lines").isEqualTo(codeLines);
                assertThat(languageCounts.commentLines).as(language + ": Comment lines").isEqualTo(commentLines);
                assertThat(languageCounts.blankLines).as(language + ": Blank lines").isEqualTo(blankLines);
            });
        }
    }
}
