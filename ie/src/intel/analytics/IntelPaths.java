package intel.analytics;


public class IntelPaths {

    public static String Regex_NER_cased = "data/RegexNER/regexner_cased.tab";
    public static String Regex_NER_caseless = "data/RegexNER/regexner_caseless.tab";
    public static String Regex_NER_department_cased = "data/RegexNER/regexner_department_cased.tab";

    public static String combined = Regex_NER_caseless
            + ";" + Regex_NER_cased;

}
