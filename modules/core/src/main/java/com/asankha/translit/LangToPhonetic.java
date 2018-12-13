package com.asankha.translit;

/**
 * @author asankha
 */
public class LangToPhonetic {

    private String rule;
    private Integer gender;
    private Integer length;
    private String phonetic;

    public LangToPhonetic(String rule, String gender, String length, String phonetic) {
        this.rule = rule;
        try {
            this.gender = Integer.parseInt(gender);
        } catch (NumberFormatException ignore) {}
        try {
            this.length = Integer.parseInt(length);
        } catch (NumberFormatException ignore) {}
        this.phonetic = phonetic;
    }

    public String getRule() {
        return rule;
    }

    public int getGender() {
        return gender;
    }

    public int getLength() {
        return length;
    }

    public String getPhonetic() {
        return phonetic;
    }
}
