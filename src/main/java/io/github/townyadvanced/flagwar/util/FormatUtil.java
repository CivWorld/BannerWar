/*
 * Copyright (c) 2026 TownyAdvanced
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package io.github.townyadvanced.flagwar.util;

import io.github.townyadvanced.flagwar.config.FlagWarConfig;

import java.time.Duration;

public final class FormatUtil {

    private FormatUtil() {
        // Masking public constructor
    }

    /**
     * Function used to format a {@link Duration} according to the formatting defined in
     * {@link FlagWarConfig#getTimerText()}.
     * @param duration Seed Duration
     * @param formatString Formatting specification: should contain arguments corresponding with seconds, minutes,
     *                     and hours, respectively.
     * @return The formatted string.
     */
    public static String time(final Duration duration, final String formatString) {
        final int hoursInDay = 24;
        final long hours = duration.toHoursPart() + (duration.toDaysPart() * hoursInDay);
        final int minutes = duration.toMinutesPart();
        final int seconds = duration.toSecondsPart();
        return String.format(formatString, seconds, minutes, hours);
    }

    /**
     * Returns the noun with an "s" at the end if the count is not 1, and the count preceding it. <br> <br>
     * For example, the word "apples" and a count of 4 will return "4 apples".
     * Additionally, the word "banana" and a count of 1 will return "1 banana". <br> <br>
     * This function does not work for irregular plurals. <br>
     * For example, the word "mouse" and a count of 3 will return "3 mouses" and not "3 mice".
     * @param word the singular noun
     * @param count the number of items
     */
    public static String tryGetPlural(String word, int count) {
        String out = count == 1 ? word : word + "s";
        return count + " " + out;
    }

    /**
     * Returns a formatted {@link String} to represent the specified {@link Duration}. <br> <br>
     * For example, a duration of 3.5 minutes returns "3 minutes 30 seconds". <br>
     * Additionally, a duration of 3 days and 0 hours and 1 minute returns "3 days 1 minute".
     * @param duration the specified {@link Duration}
     */
    public static String getFormattedTime(Duration duration) {
        String daysPart = duration.toDaysPart() > 0 ? tryGetPlural("day", Math.toIntExact(duration.toDaysPart())) : "";
        String hoursPart = duration.toHoursPart() > 0 ? tryGetPlural("hour", duration.toHoursPart()) : "";
        String minutesPart = duration.toMinutesPart() > 0 ? tryGetPlural("minute", duration.toMinutesPart()) : "";
        String secondsPart = duration.toSecondsPart() > 0 ? tryGetPlural("second", duration.toSecondsPart()) : "";

        String out = daysPart + " " + hoursPart + " " + minutesPart + " " + secondsPart;
        return out.trim();
    }
}
