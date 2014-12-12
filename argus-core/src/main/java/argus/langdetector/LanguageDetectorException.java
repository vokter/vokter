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
