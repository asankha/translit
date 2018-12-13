package com.asankha.translit;

/**
 * @author asankha
 */
public class PhoneticToLang {

    private String rule;
    private Integer length;
    private String lang;

    public PhoneticToLang(String rule, String lang, String length) {
        this.rule = rule;
        if (!"(null)".equals(lang)) {
            this.lang = lang;
        }
        try {
            this.length = Integer.parseInt(length);
        } catch (NumberFormatException ignore) {}
    }

    public String getRule() {
        return rule;
    }

    public int getLength() {
        return length;
    }

    public String getLang() {
        return lang;
    }
}
