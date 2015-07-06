/*
 * Copyright 2014 Ed Duarte
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package argus.langdetector;

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
