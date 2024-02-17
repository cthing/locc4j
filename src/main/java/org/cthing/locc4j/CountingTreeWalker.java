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

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cthing.filevisitor.MatchHandler;
import org.cthing.filevisitor.MatchingTreeWalker;


/**
 * Walks a file system tree counting the lines in the encountered files. Provides Git glob pattern matching.
 */
public class CountingTreeWalker {

    private static class CountHandler implements MatchHandler {

        private final FileCounter counter;
        private final Map<Path, Map<Language, Stats>> counts;

        CountHandler() {
            this.counter = new FileCounter();
            this.counts = new HashMap<>();
        }

        void countDocStrings(final boolean enable) {
            this.counter.countDocStrings(enable);
        }

        Map<Path, Map<Language, Stats>> getCounts() {
            return Collections.unmodifiableMap(this.counts);
        }

        @Override
        public boolean file(final Path file, final BasicFileAttributes basicFileAttributes) throws IOException {
            final Map<Language, Stats> count = this.counter.count(file);
            this.counts.put(file, count);
            return true;
        }
    }

    private final CountHandler handler;
    private final MatchingTreeWalker walker;

    /**
     * Constructs a counting file system tree walker.
     *
     * @param start Directory in which to start the walk
     * @param matchPatterns Glob patterns to match files and directories.
     *      See <a href="https://git-scm.com/docs/gitignore">git-ignore</a> for the format of these patterns.
     *      Note that these patterns include files and directories rather than excluding them. As with Git
     *      ignore files, patterns later in the list are matched first. If no patterns are specified, all
     *      files and directories are considered a match.
     */
    public CountingTreeWalker(final Path start, final String... matchPatterns) {
        this(start, List.of(matchPatterns));
    }

    /**
     * Constructs a counting file system tree walker.
     *
     * @param start Directory in which to start the walk
     * @param matchPatterns Glob patterns to match files and directories.
     *      See <a href="https://git-scm.com/docs/gitignore">git-ignore</a> for the format of these patterns.
     *      Note that these patterns include files and directories rather than excluding them. As with Git
     *      ignore files, patterns later in the list are matched first. If no patterns are specified, all
     *      files and directories are considered a match.
     */
    public CountingTreeWalker(final Path start, final List<String> matchPatterns) {
        this.handler = new CountHandler();
        this.walker = new MatchingTreeWalker(start, this.handler, matchPatterns);
    }

    /**
     * Specifies whether to exclude hidden files and directories from the walk. By default, hidden files and
     * directories are excluded.
     *
     * @param excludeHidden {@code true} to exclude hidden files
     * @return This walker
     */
    public CountingTreeWalker excludeHidden(final boolean excludeHidden) {
        this.walker.excludeHidden(excludeHidden);
        return this;
    }

    /**
     * Specifies whether to honor Git ignore files to exclude files and directories from the walk. The default is
     * {@code false}, which means to not honor Git ignore files. If enabled, all parent ignore files, and any global
     * ignore file is honored.
     *
     * @param respectGitignore {@code true} to honor git ignore files during the walk
     * @return This walker
     */
    public CountingTreeWalker respectGitignore(final boolean respectGitignore) {
        this.walker.respectGitignore(respectGitignore);
        return this;
    }

    /**
     * Specifies a maximum depth for the walk. The default is {@link Integer#MAX_VALUE}, which means that there is
     * no depth limit.
     *
     * @param maxDepth Maximum tree depth for the walk or {@link Integer#MAX_VALUE} for unlimited depth
     * @return This walker
     */
    public CountingTreeWalker maxDepth(final int maxDepth) {
        this.walker.maxDepth(maxDepth);
        return this;
    }

    /**
     * Specifies whether to follow symbolic links. The default is to not follow symbolic links.
     *
     * @param followLinks {@code true} to follow symbolic links
     * @return This walker
     */
    public CountingTreeWalker followLinks(final boolean followLinks) {
        this.walker.followLinks(followLinks);
        return this;
    }

    /**
     * Sets whether to count documentation string as comments or ignore them.
     *
     * @param enable {@code true} to count documentation strings as comments or {@code false} to ignore them.
     *      The default is to count documentation strings as comments.
     * @return This walker
     */
    public CountingTreeWalker countDocStrings(final boolean enable) {
        this.handler.countDocStrings(enable);
        return this;
    }

    /**
     * Performs the walk of the file system tree and the counting of file lines.
     *
     * @return The line counts for each file encountered on the walk.
     * @throws IOException if a problem was encountered during the walk.
     */
    public Map<Path, Map<Language, Stats>> count() throws IOException {
        this.walker.walk();
        return this.handler.getCounts();
    }
}