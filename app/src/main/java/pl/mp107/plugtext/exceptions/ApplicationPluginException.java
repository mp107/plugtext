package pl.mp107.plugtext.exceptions;

import android.content.res.Resources;

import pl.mp107.plugtext.R;

public class ApplicationPluginException extends Exception {
    public ApplicationPluginException() {
        super(Resources.getSystem().getString(R.string.plugin_load_exception));
    }

    public ApplicationPluginException(String message) {
        super(message);
    }
}
