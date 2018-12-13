/*
 * A Sinhala, Tamil and English Transliterator
 *
 * Copyright (c) 2010 Asankha Chamath Perera. (http://www.asankha.com). All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.asankha.translit;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.InputStreamReader;
import java.util.*;

/**
 * Implemented my own Transliterator re-using rules from the ICTA transliterator which had severe code/architectural
 * issues making it unsuitable for any real work
 *
 * This implementation attempts to be not-dependent on any external libraries - and hence debug and error logging are
 * limited and not based on SLF4J or Log4J etc. This is 'by choice'!
 *
 * This implementation also does NOT use a database. The original ICTA implementation executed hundreds of SQLs for
 * each word. This implementation caches all words in memory, loaded from files found in the classpath. Hence an
 * update of rules is possible by placing updated rules files on the classpath com/asankha/translit/resources
 * followed by a restart.
 *
 * The implementation is optimized for performance, and hence uses RAM to keep rules and the dictionary cached. Thus
 * the implementation will roughly take about 1.25MB of Heap Memory for English/Sinhala/Tamil transliteration.
 *
 * Note: The accuracy of the transliterator can be improved with updated dictionary files, or by tuning the phonetic
 * rules used. The former is strongly encouraged. For example, the name of the town "Galle" cannot be correctly
 * transliterated by only using phonetic rules, as its name in Sinhalese is different from the sound made by the
 * word "Galle" in English. The accuracy is generally better for person names etc. However, the Male/Female or Unknown
 * specifier must be specified to obtain a correct translation where application. (e.g. Avanka - as a male "අවන්ක" or
 * as a female "අවන්කා"
 *
 * e.g. Sample execution of the command line application
 * $java -jar translit-core-1.0.0.jar [-s si|ta|en*] [-t si*|ta|en] [-g m|f|u*] : (* - denotes default)
 * asankha perera
 * අසන්ඛ පෙරේරා
 *
 * Transliterating an input file /tmp/input into /tmp/output using default options
 * $java -jar translit-core-1.0.0.jar < /tmp/input > /tmp/output
 *
 * @author asankha perera (asankha AT gmail DOT com)
 * 6th November 2010
 */
public class Transliterate {

    private static final ClassLoader CLS_LDR = Transliterate.class.getClassLoader();
    private static boolean DEBUG = Boolean.getBoolean("debug");

    public static final int ENGLISH = 0;
    public static final int SINHALA = 1;
    public static final int TAMIL   = 2;

    public static final int UNKNOWN = 0;
    public static final int MALE = 1;
    public static final int FEMALE = 2;

    private static final String END_VOVELS   = ".aeiou#";
    private static final String START_VOVELS = ".aeiou"; 
    private static final String PREFIX = "com/asankha/translit/resources/";

    private static final Map<String, String> engToSinNames = new HashMap<String, String>();
    private static final Map<String, String> engToSinOther = new HashMap<String, String>();

    private static final Map<String, String> sinToEngNames = new HashMap<String, String>();
    private static final Map<String, String> sinToEngOther = new HashMap<String, String>();

    private static final Map<String, String> engToTamNames = new HashMap<String, String>();
    private static final Map<String, String> engToTamOther = new HashMap<String, String>();

    private static final Map<String, String> tamToEngNames = new HashMap<String, String>();
    private static final Map<String, String> tamToEngOther = new HashMap<String, String>();

    private static final Map<String, String> sinToTamNames = new HashMap<String, String>();
    private static final Map<String, String> sinToTamOther = new HashMap<String, String>();

    private static final Map<String, String> tamToSinNames = new HashMap<String, String>();
    private static final Map<String, String> tamToSinOther = new HashMap<String, String>();

    private static LangToPhonetic[] enToPhRules;
    private static LangToPhonetic[] siToPhRules;
    private static LangToPhonetic[] taToPhRules;

    private static PhoneticToLang[] phToSiRules;
    private static PhoneticToLang[] phToTaRules;
    private static PhoneticToLang[] phToEnRules;

    static {
        try {
            enToPhRules = loadLangToPhoneticFile(PREFIX + "rules-en.txt");
            siToPhRules = loadLangToPhoneticFile(PREFIX + "rules-si.txt");
            taToPhRules = loadLangToPhoneticFile(PREFIX + "rules-ta.txt");

            phToSiRules = loadPhoneticToLangFile(PREFIX + "phonetic-si.txt");
            phToTaRules = loadPhoneticToLangFile(PREFIX + "phonetic-ta.txt");
            phToEnRules = loadPhoneticToLangFile(PREFIX + "phonetic-en.txt");

            loadMappingFile(PREFIX + "en-to-si.txt", engToSinNames, engToSinOther, sinToEngNames, sinToEngOther);
            loadMappingFile(PREFIX + "en-to-ta.txt", engToTamNames, engToTamOther, tamToEngNames, tamToEngOther);
            loadMappingFile(PREFIX + "si-to-ta.txt", sinToTamNames, sinToTamOther, tamToSinNames, tamToSinOther);

            if (DEBUG) {
                System.out.println("En to Ph rules : " + enToPhRules.length);
                System.out.println("Si to Ph rules : " + siToPhRules.length);
                System.out.println("Ta to Ph rules : " + taToPhRules.length);
                System.out.println("Ph to Si rules : " + phToSiRules.length);
                System.out.println("Ph to Ta rules : " + phToTaRules.length);
                System.out.println("Ph to En rules : " + phToEnRules.length);
                System.out.println("En to Si mappings : " + engToSinNames.size() + " : " + engToSinOther.size());
                System.out.println("En to Ta mappings : " + engToTamNames.size() + " : " + engToTamOther.size());
                System.out.println("Si to Ta mappings : " + sinToTamNames.size() + " : " + sinToTamOther.size());
            }

        } catch (Exception e) {
            System.out.println("Error during the initialization of the Transliterator : " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {

        //System.out.println(translateLine("\"Kumarasiri\" asankha-chamath, #Perera#", ENGLISH, SINHALA, MALE));

        int src = ENGLISH;
        int dst = SINHALA;
        int gender = UNKNOWN;

        System.out.println("java -jar translit.jar [-s si|ta|en*] [-t si*|ta|en] [-g m|f|u*] : (* - denotes default)");

        for (int i=0; i<args.length; i++) {
            if (args[i].equals("-s")) {
                if (i+1 < args.length) {
                    src = "si".equals(args[i+1]) ? SINHALA : "ta".equals(args[i+1]) ? TAMIL : ENGLISH;
                }
            }
            if (args[i].equals("-t")) {
                if (i+1 < args.length) {
                    dst = "si".equals(args[i+1]) ? SINHALA : "ta".equals(args[i+1]) ? TAMIL : ENGLISH;
                }
            }
            if (args[i].equals("-g")) {
                if (i+1 < args.length) {
                    gender = "m".equals(args[i+1]) ? MALE : "f".equals(args[i+1]) ? FEMALE : UNKNOWN;
                }
            }
        }

        Scanner sc = new Scanner(System.in);
        String s = null;
        try {
            while ((s = sc.nextLine()) != null) {
                System.out.println(translateLine(s, src, dst, gender));
            }
        } catch (NoSuchElementException ignore) {}
    }

    private static LangToPhonetic[] loadLangToPhoneticFile(String filename) throws Exception {
        DataInputStream in = new DataInputStream(CLS_LDR.getResourceAsStream(filename));
        BufferedReader br = new BufferedReader(new InputStreamReader(in));

        String s;
        List<LangToPhonetic> list = new ArrayList<LangToPhonetic>();
        while ((s = br.readLine()) != null) {
            String[] p = s.split(",");
            list.add(new LangToPhonetic(p[2].replaceAll("%", ".*"), p[0], p[3], p[4].replaceAll("%", "")));
        }
        in.close();
        return list.toArray(new LangToPhonetic[list.size()]);
    }

    private static PhoneticToLang[] loadPhoneticToLangFile(String filename) throws Exception {
        DataInputStream in = new DataInputStream(CLS_LDR.getResourceAsStream(filename));
        BufferedReader br = new BufferedReader(new InputStreamReader(in));

        String s;
        List<PhoneticToLang> list = new ArrayList<PhoneticToLang>();
        while ((s = br.readLine()) != null) {
            String[] p = s.split(",");
            list.add(new PhoneticToLang(p[0].replaceAll("\\.", "\\\\.").replaceAll("%", ".*"), p[1].replaceAll("%", ""), p[2]));
        }
        in.close();
        return list.toArray(new PhoneticToLang[list.size()]);
    }

    private static void loadMappingFile(String filename,
        Map<String, String> namesOne, Map<String, String> otherOne,
        Map<String, String> namesTwo, Map<String, String> otherTwo) throws Exception {

        DataInputStream in = new DataInputStream(CLS_LDR.getResourceAsStream(filename));
        BufferedReader br = new BufferedReader(new InputStreamReader(in));

        String s;
        while ((s = br.readLine()) != null) {
            String[] p = s.toLowerCase().split(",");
            if ("1".equals(p[2]) || "1".equals(p[3])) {
                // this is a person name
                if (!namesOne.containsKey(p[0])) {
                    namesOne.put(p[0], p[1]);
                }
                if (!namesTwo.containsKey(p[1])) {
                    namesTwo.put(p[1], p[0]);
                }
            } else {
                if (!otherOne.containsKey(p[0])) {
                    otherOne.put(p[0], p[1]);
                }
                if (!otherTwo.containsKey(p[1])) {
                    otherTwo.put(p[1], p[0]);
                }
            }
        }
        in.close();
    }

    //----------------------------- translate a phrase from one language to another ------------------------------------
    public static String translateLine(String s, int src, int dst, int gender) {

        if (DEBUG) {
            System.out.println("Src : " + src + " Target : " + dst + " Gender : " + gender);    
        }

        StringTokenizer st = new StringTokenizer(s.toLowerCase(), " ,\\[]#'\"()", true);
        if (st.countTokens() == 1) {
            return translateWord(st.nextToken(), src, dst, gender);

        } else {
            StringBuilder sb = new StringBuilder();
            while (st.hasMoreTokens()) {
                String t = st.nextToken();
                if (t.length() > 1) {
                    sb.append(translateWord(t, src, dst, gender));
                } else {
                    sb.append(t);
                }
            }
            return sb.toString();
        }
    }

    // ---------------------------- translate one word from one language to another ------------------------------------
    public static String translateWord(String s, int src, int dst, int gender) {

        Map<String, String> otherMap = null;
        Map<String, String> namesMap = null;
        LangToPhonetic[] langToPhonetic = null;
        PhoneticToLang[] phoneticToLang = null;

        s = s.toLowerCase();

        switch (src) {
            case SINHALA:
                switch (dst) {
                    case TAMIL:
                        otherMap = sinToTamOther;
                        namesMap = sinToTamNames;
                        langToPhonetic = siToPhRules;
                        phoneticToLang = phToTaRules;
                        break;
                    case ENGLISH:
                        otherMap = sinToEngOther;
                        namesMap = sinToEngNames;
                        langToPhonetic = siToPhRules;
                        phoneticToLang = phToEnRules;
                        break;
                }
                break;
            case TAMIL:
                switch (dst) {
                    case SINHALA:
                        otherMap = tamToSinOther;
                        namesMap = tamToSinNames;
                        langToPhonetic = taToPhRules;
                        phoneticToLang = phToSiRules;
                        break;
                    case ENGLISH:
                        otherMap = tamToEngOther;
                        namesMap = tamToEngNames;
                        langToPhonetic = taToPhRules;
                        phoneticToLang = phToEnRules;
                        break;
                }
                break;
            case ENGLISH:
                switch (dst) {
                    case TAMIL:
                        otherMap = engToTamOther;
                        namesMap = engToTamNames;
                        langToPhonetic = enToPhRules;
                        phoneticToLang = phToTaRules;
                        break;
                    case SINHALA:
                        otherMap = engToSinOther;
                        namesMap = engToSinNames;
                        langToPhonetic = enToPhRules;
                        phoneticToLang = phToSiRules;
                        break;
                }
                break;
        }

        if (namesMap == null || otherMap == null) {
            System.out.println("Invalid language pair");
            return null;
        }
        
        String result = null;
        if (gender == UNKNOWN) {
            result = otherMap.get(s);
        } else {
            result = namesMap.get(s);
        }
        
        if (result != null) {
            return result;
        } else {
            if (DEBUG) {
                System.out.println("Dictionary lookup failed for : " + s);
            }
            return phoneticToLang(convertToPhonetic(s, gender, langToPhonetic), phoneticToLang);
        }
    }


    // --------------------- rules based translation to / from a language to phonetic ----------------------------------

    private static String convertToPhonetic(String word, int gender, LangToPhonetic[] rules) {

        StringBuilder in = new StringBuilder().append("#").append(word).append("#");
        StringBuilder out = new StringBuilder();

        while (in.length() > 0) {
            boolean found = false;
            String s = in.toString();

            for (LangToPhonetic l2p : rules) {
                boolean c = s.matches(l2p.getRule());
                if (c && (gender == l2p.getGender() || l2p.getGender() == UNKNOWN)) {
                    if (DEBUG) {
                        System.out.println("In : " + s + " matches : " + l2p.getRule() + " with : " + l2p.getPhonetic());
                    }
                    appendPhoneticWithCorrection(out, l2p.getPhonetic());
                    in.delete(0, l2p.getLength());
                    found = true;
                    break;
                }
            }
            if (!found) {
                out.append(in.charAt(0));
                in.delete(0, 1);
            }
        }

        if (DEBUG) {
            System.out.println("convertToPhonetic(" + word + ") = " + out.toString());
        }
        return out.toString();
    }

    private static String phoneticToLang(String word, PhoneticToLang[] rules) {

        StringBuilder in = new StringBuilder().append(word);
        StringBuilder out = new StringBuilder();

        while (in.length() > 0) {
            boolean found = false;
            String s = in.toString();
            for (PhoneticToLang p2l : rules) {
                boolean c = s.matches(p2l.getRule());
                if (c) {
                    if (DEBUG) {
                        System.out.println("In : " + s + " matches : " + p2l.getRule() + " with : " + p2l.getLang());
                    }
                    if (p2l.getLang() != null) {
                        out.append(p2l.getLang());
                    }
                    in.delete(0, p2l.getLength());
                    found = true;
                    break;
                }
            }
            if (!found) {
                out.append(in.charAt(0));
                in.delete(0, 1);
            }
        }

        if (DEBUG) {
            System.out.println("phoneticToLang(" + word + ") = " + out.toString().replaceAll("#", ""));
        }
        return out.toString().replaceAll("#", "");
    }

    private static void appendPhoneticWithCorrection(StringBuilder out, String ph) {
        final int len = out.length();
        if (len > 0) {
            String lastChar = out.substring(len-1);
            if (END_VOVELS.indexOf(lastChar) == -1) {   // not a vowel
                if (ph.length() > 0) {
                    if (START_VOVELS.indexOf(ph.substring(0,1)) == -1) { // not a vowel
                        out.append(".a");
                    }
                } else {
                    out.append(".a");
                }
            }
        }
        out.append(ph);
    }

}
