package argus.langdetect;

/**
 * {@link TagExtractor} is a class which extracts inner texts of specified tag.
 *
 * @author Nakatani Shuyo
 */
class TagExtractor {
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

    public String getTag() {
        return tag_;
    }

    public void setTag(String tag) {
        tag_ = tag;
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
