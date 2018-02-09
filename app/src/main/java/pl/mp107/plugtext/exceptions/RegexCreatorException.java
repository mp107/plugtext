package pl.mp107.plugtext.exceptions;

import android.content.res.Resources;

import pl.mp107.plugtext.R;

public class RegexCreatorException extends Exception {
    public RegexCreatorException() {
        super(Resources.getSystem().getString(R.string.regex_creator_exception));
    }

    public RegexCreatorException(String message) {
        super(message);
    }
}
