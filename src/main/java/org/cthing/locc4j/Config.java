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

/**
 * Provides information for configuring the count.
 */
public class Config {

    private boolean countDocStrings = true;

    /**
     * Indicates whether documentation strings should be counted as comments.
     *
     * @return {@code true} to count documentation strings as comments. {@code false} to ignore documentation strings.
     */
    public boolean isCountDocStrings() {
        return this.countDocStrings;
    }

    /**
     * Sets whether to count documentation string as comments or ignore them.
     *
     * @param countDocStrings {@code true} to count documentation strings as comments or {@code false} to ignore them.
     */
    public void setCountDocStrings(final boolean countDocStrings) {
        this.countDocStrings = countDocStrings;
    }
}
