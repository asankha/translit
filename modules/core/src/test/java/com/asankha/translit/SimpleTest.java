package com.asankha.translit;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class SimpleTest extends TestCase {

    public SimpleTest(String testName) {
        super(testName);
    }

    public static Test suite() {
        return new TestSuite(SimpleTest.class);
    }

    public void testLine() {
        //System.out.println(Transliterate.translateWord("avanka",
        //    Transliterate.ENGLISH, Transliterate.SINHALA, Transliterate.MALE));

        assertEquals("\"පෙරේරා\" අසන්ඛ-චමත්, #පෙරේරා#", Transliterate.translateLine("\"Perera\" asankha-chamath, #Perera#",
            Transliterate.ENGLISH, Transliterate.SINHALA, Transliterate.MALE));
        assertEquals("කුමාරසිරි", Transliterate.translateWord("Kumarasiri",
            Transliterate.ENGLISH, Transliterate.SINHALA, Transliterate.MALE));
        assertEquals("අවන්ක", Transliterate.translateWord("avanka",
            Transliterate.ENGLISH, Transliterate.SINHALA, Transliterate.MALE));
        assertEquals("අවන්කා", Transliterate.translateWord("AVANKA",
            Transliterate.ENGLISH, Transliterate.SINHALA, Transliterate.FEMALE));
    }
}
