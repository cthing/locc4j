/*
 * Copyright 2024 C Thing Software
 * SPDX-License-Identifier: Apache-2.0
 */

package org.cthing.locc4j;

import java.io.Serializable;


/**
 * Represents a start and end delimiter pair (e.g. block comment delimiters).
 *
 * @param start Specifies the beginning delimiter
 * @param end Specifies the ending delimiter
 */
record BlockDelimiter(String start, String end) implements Serializable {
}
