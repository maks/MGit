package me.sheimi.android.utils;

import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import me.sheimi.sgit.BuildConfig;

import static org.junit.Assert.assertEquals;

/**
 *
 */
@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class)
public class CodeGuesserTest {

    @org.junit.Test
    public void testGuessCodeType() throws Exception {
        assertEquals("expect to recognise java files", "text/x-java", CodeGuesser.guessCodeType("test.java"));

    }
}