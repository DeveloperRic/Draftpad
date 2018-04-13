package com.rictacius.draftpad;

/**
 * Created by Victor on 30/03/2018.
 */
enum ErrorCode {
    NO_ERROR(-1),
    FILE_NO_ACCESS(1),
    UNKNOWN_ERROR(2),
    NOTE_MALFORMED(3),
    SAVE_ERROR(4);

    private final int code;

    ErrorCode(int i) {
        code = i;
    }

    int getCode() {
        return code;
    }
}
