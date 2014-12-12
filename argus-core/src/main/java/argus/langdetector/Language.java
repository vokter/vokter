package argus.langdetector;

/**
 * {@link argus.langdetector.Language} is to store the detected language.
 * {@link LanguageDetector#getProbabilities()} returns an {@link java.util.ArrayList} of
 * {@link argus.langdetector.Language}s.
 * <p>
 * Available Language for Detection
 * af		Afrikaans
 * ar		Arabic
 * bg		Bulgarian
 * bn		Bengali
 * cs		Czech
 * da		Danish
 * de		German
 * el		Greek
 * en		English
 * es		Spanish
 * et		Estonian
 * fa		Persian
 * fi		Finnish
 * fr		French
 * gu		Gujarati
 * he		Hebrew
 * hi		Hindi
 * hr		Croatian
 * hu		Hungarian
 * id		Indonesian
 * it		Italian
 * ja		Japanese
 * kn		Kannada
 * ko		Korean
 * lt		Lithuanian
 * lv		Latvian
 * mk		Macedonian
 * ml		Malayalam
 * mr		Marathi
 * ne		Nepali
 * nl		Dutch
 * no		Norwegian
 * pa		Punjabi
 * pl		Polish
 * pt		Portuguese
 * ro		Romanian
 * ru		Russian
 * sk		Slovak
 * sl		Slovene
 * so		Somali
 * sq		Albanian
 * sv		Swedish
 * sw		Swahili
 * ta		Tamil
 * te		Telugu
 * th		Thai
 * tl		Tagalog
 * tr		Turkish
 * uk		Ukrainian
 * ur		Urdu
 * vi		Vietnamese
 * zh-cn	Simplified Chinese
 * zh-tw	Traditional Chinese
 *
 * @author Nakatani Shuyo
 * @see LanguageDetector#getProbabilities()
 */
public class Language {
    public String lang;
    public double prob;

    public Language(String lang, double prob) {
        this.lang = lang;
        this.prob = prob;
    }

    public String toString() {
        if (lang == null) return "";
        return lang + ":" + prob;
    }
}
