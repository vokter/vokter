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
 * @author Nakatani Shuyo
 */
public class LanguageDetectorException extends Exception {
    private static final long serialVersionUID = 1L;

    private ErrorCode code;


    public LanguageDetectorException(ErrorCode code, String message) {
        super(message);
        this.code = code;
    }


    public ErrorCode getCode() {
        return code;
    }


    public static enum ErrorCode {
        NoTextError, FormatError, FileLoadError, DuplicateLangError,
        NeedLoadProfileError, CantDetectError, CantOpenTrainData, TrainDataFormatError,
        InitParamError
    }
}
