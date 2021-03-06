/*
 *  Copyright (c) 2020 David Allison <davidallisongithub@gmail.com>
 *
 *  This program is free software; you can redistribute it and/or modify it under
 *  the terms of the GNU General Public License as published by the Free Software
 *  Foundation; either version 3 of the License, or (at your option) any later
 *  version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY
 *  WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 *  PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with
 *  this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.ichi2.anki;

import android.graphics.Color;

import com.ichi2.anki.model.WhiteboardPenColor;
import com.ichi2.libanki.Consts;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import androidx.annotation.CheckResult;
import androidx.annotation.NonNull;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/** A non-parmaterized ReviewerTest - we should probably rename ReviewerTest in future */
@RunWith(AndroidJUnit4.class)
public class ReviewerNoParamTest extends RobolectricTest {
    public static final int DEFAULT_LIGHT_PEN_COLOR = Color.BLACK;
    public static final int ARBITRARY_PEN_COLOR_VALUE = 555;


    @Before
    @Override
    public void setUp() {
        super.setUp();
        // This doesn't do an upgrade in the correct place
        MetaDB.resetDB(getTargetContext());
    }

    @Test
    public void defaultWhiteboardColorIsUsedOnFirstRun() {
        Whiteboard whiteboard = startReviewerForWhiteboard();

        assertThat("Pen color defaults to black", whiteboard.getPenColor(), is(DEFAULT_LIGHT_PEN_COLOR));
    }

    @Test
    public void whiteboardLightModeColorIsUsed() {
        storeLightModeColor(ARBITRARY_PEN_COLOR_VALUE);

        Whiteboard whiteboard = startReviewerForWhiteboard();

        assertThat("Pen color defaults to black", whiteboard.getPenColor(), is(555));
    }

    @Test
    public void whiteboardDarkModeColorIsUsed() {
        storeDarkModeColor(555);
        enableDarkMode();

        Whiteboard whiteboard = startReviewerForWhiteboard();

        assertThat("Pen color defaults to black", whiteboard.getPenColor(), is(555));
    }


    @Test
    public void whiteboardPenColorChangeChangesDatabaseLight() {
        Whiteboard whiteboard = startReviewerForWhiteboard();

        whiteboard.setPenColor(ARBITRARY_PEN_COLOR_VALUE);

        WhiteboardPenColor penColor = getPenColor();
        assertThat("Light pen color is changed", penColor.getLightPenColor(), is(ARBITRARY_PEN_COLOR_VALUE));
    }

    @Test
    public void whiteboardPenColorChangeChangesDatabaseDark() {
        enableDarkMode();

        Whiteboard whiteboard = startReviewerForWhiteboard();

        whiteboard.setPenColor(ARBITRARY_PEN_COLOR_VALUE);

        WhiteboardPenColor penColor = getPenColor();
        assertThat("Dark pen color is changed", penColor.getDarkPenColor(), is(ARBITRARY_PEN_COLOR_VALUE));
    }


    @Test
    public void whiteboardDarkPenColorIsNotUsedInLightMode() {
        storeDarkModeColor(555);

        Whiteboard whiteboard = startReviewerForWhiteboard();

        assertThat("Pen color defaults to black, even if dark mode color is changed", whiteboard.getPenColor(), is(DEFAULT_LIGHT_PEN_COLOR));
    }

    @Test
    public void differentDeckPenColorDoesNotAffectCurrentDeck() {
        long did = 2L;
        storeLightModeColor(ARBITRARY_PEN_COLOR_VALUE, did);

        Whiteboard whiteboard = startReviewerForWhiteboard();

        assertThat("Pen color defaults to black", whiteboard.getPenColor(), is(DEFAULT_LIGHT_PEN_COLOR));
    }


    protected void storeDarkModeColor(@SuppressWarnings("SameParameterValue") int value) {
        MetaDB.storeWhiteboardPenColor(getTargetContext(), Consts.DEFAULT_DECK_ID, false, value);
    }

    protected void storeLightModeColor(@SuppressWarnings("SameParameterValue") int value, Long did) {
        MetaDB.storeWhiteboardPenColor(getTargetContext(), did, false, value);
    }

    protected void storeLightModeColor(@SuppressWarnings("SameParameterValue") int value) {
        MetaDB.storeWhiteboardPenColor(getTargetContext(), Consts.DEFAULT_DECK_ID, true, value);
    }

    private void enableDarkMode() {
        AnkiDroidApp.getSharedPrefs(getTargetContext()).edit().putBoolean("invertedColors", true).apply();
    }

    @NonNull
    protected WhiteboardPenColor getPenColor() {
        return MetaDB.getWhiteboardPenColor(getTargetContext(), Consts.DEFAULT_DECK_ID);
    }

    @CheckResult
    @NonNull
    protected Whiteboard startReviewerForWhiteboard() {
        // we need a card for the reviewer to start
        addNoteUsingBasicModel("Hello", "World");

        Reviewer reviewer = ReviewerTest.startReviewer(this);

        reviewer.toggleWhiteboard();

        Whiteboard whiteboard = reviewer.getWhiteboard();
        if (whiteboard == null) {
            throw new IllegalStateException("Could not get whiteboard");
        }
        return whiteboard;
    }

}
