package argus.langdetect.util;

/**
 * {@link argus.langdetect.util.TagExtractor} is a class which extracts inner texts of specified tag.
 * Users don't use this class directly.
 * @author Nakatani Shuyo
 */
public class TagExtractor {
    public final String target_;
    public final int threshold_;
    public final StringBuffer buf_;
    private String tag_;
    private int count_;

    public TagExtractor(String tag, int threshold) {
        target_ = tag;
        threshold_ = threshold;
        count_ = 0;
        buf_ = new StringBuffer();
        tag_ = null;
    }
    public int count() {
        return count_;
    }
    public void clear() {
        buf_.delete(0, buf_.length());
        tag_ = null;
    }
    public void setTag(String tag){
        tag_ = tag;
    }

    public String getTag() {
        return tag_;
    }

    public void add(String line) {
        if (tag_ == target_ && line != null) {
            buf_.append(line);
        }
    }
    public String closeTag() {
        String st = null;
        if (tag_ == target_ && buf_.length() > threshold_) {
            st = buf_.toString();
            ++count_;
        }
        clear();
        return st;
    }

}
