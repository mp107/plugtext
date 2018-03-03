package pl.mp107.plugtext.components;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatEditText;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Selection;
import android.text.Spannable;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.ReplacementSpan;
import android.util.AttributeSet;
import android.util.Log;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import pl.mp107.plugtext.exceptions.RegexCreatorException;
import pl.mp107.plugtext.utils.RegexCreatorUtil;

public class CodeEditor extends AppCompatEditText {

    public interface OnTextChangedListener {
        void onTextChanged(String text);
    }

    private Pattern patternLine = Pattern.compile(
            ".*\\n");
    private Pattern patternNumbers = null/*Pattern.compile(
            "\\b(\\d*[.]?\\d+)\\b")*/;
    private Pattern patternPreprocessors = null/*Pattern.compile(
            "^[\t ]*(#define|#undef|#if|#ifdef|#ifndef|#else|#elif|#endif|" +
                    "#error|#pragma|#extension|#version|#line)\\b",
            Pattern.MULTILINE)*/;
    private Pattern patternKeywords = null/*Pattern.compile(
            "\\b(attribute|const|uniform|varying|break|continue|" +
                    "do|for|while|if|else|in|out|inout|float|int|void|bool|true|false|" +
                    "lowp|mediump|highp|precision|invariant|discard|return|mat2|mat3|" +
                    "mat4|vec2|vec3|vec4|ivec2|ivec3|ivec4|bvec2|bvec3|bvec4|sampler2D|" +
                    "samplerCube|struct|gl_Vertex|gl_FragCoord|gl_FragColor)\\b")*/;
    private Pattern patternBuiltins = null/*Pattern.compile(
            "\\b(radians|degrees|sin|cos|tan|asin|acos|atan|pow|" +
                    "exp|log|exp2|log2|sqrt|inversesqrt|abs|sign|floor|ceil|fract|mod|" +
                    "min|max|clamp|mix|step|smoothstep|length|distance|dot|cross|" +
                    "normalize|faceforward|reflect|refract|matrixCompMult|lessThan|" +
                    "lessThanEqual|greaterThan|greaterThanEqual|equal|notEqual|any|all|" +
                    "not|dFdx|dFdy|fwidth|texture2D|texture2DProj|texture2DLod|" +
                    "texture2DProjLod|textureCube|textureCubeLod)\\b")*/;
    private Pattern patternComments = null/*Pattern.compile(
            "/\\*(?:.|[\\n\\r])*?\\*//*|//.*")*/;
    private Pattern patternSearchedString = null/*Pattern.compile(
            "/\\*(?:.|[\\n\\r])*?\\*//*|//.*")*/;
    private Pattern PATTERN_TRAILING_WHITE_SPACE = Pattern.compile(
            "[\\t ]+$",
            Pattern.MULTILINE);
    private Pattern PATTERN_INSERT_UNIFORM = Pattern.compile(
            "^([ \t]*uniform.+)$",
            Pattern.MULTILINE);
    private Pattern PATTERN_ENDIF = Pattern.compile(
            "(#endif)\\b");
    private static final String TAG = "CodeEditor";

    private final Handler updateHandler = new Handler();
    private final Runnable updateRunnable = new Runnable() {
        @Override
        public void run() {
            Editable e = getText();

            if (onTextChangedListener != null) {
                onTextChangedListener.onTextChanged(
                        e.toString());
            }

            highlightWithoutChange(e);
        }
    };

    private OnTextChangedListener onTextChangedListener;
    private int updateDelay = 1000;
    private boolean dirty = false;
    private boolean modified = true;
    private int colorNumber;
    private int colorKeyword;
    private int colorPreprocessors;
    private int colorBuiltin;
    private int colorComment;
    private int colorSearch = Color.RED;
    private int tabWidthInCharacters = 0;
    private int tabWidth = 0;
    private HistoryItem[] changesHistory = new HistoryItem[8];
    private int changesHistoryIndex = 0;
    private boolean isRightAfterHistoryRestore = false;

    public CodeEditor(Context context) {
        super(context);
        init(context);
    }

    public CodeEditor(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public void setUpdateDelay(int ms) {
        updateDelay = ms;
    }

    public void setTabWidth(int characters) {
        if (tabWidthInCharacters == characters) {
            return;
        }

        tabWidthInCharacters = characters;
        tabWidth = Math.round(getPaint().measureText("m") * characters);
    }

    public boolean isModified() {
        return dirty;
    }

    public String getCleanText() {
        return PATTERN_TRAILING_WHITE_SPACE
                .matcher(getText())
                .replaceAll("");
    }

    private void init(Context context) {
        setHorizontallyScrolling(true);

        setFilters(new InputFilter[]{new InputFilter() {
            @Override
            public CharSequence filter(
                    CharSequence source,
                    int start,
                    int end,
                    Spanned dest,
                    int dstart,
                    int dend) {
                if (modified &&
                        end - start == 1 &&
                        start < source.length() &&
                        dstart < dest.length()) {
                    char c = source.charAt(start);

                    if (c == '\n') {
                        return autoIndent(source, dest, dstart, dend);
                    }
                }

                return source;
            }
        }});

        addTextChangedListener(new TextWatcher() {
            private int start = 0;
            private int count = 0;

            @Override
            public void onTextChanged(
                    CharSequence s,
                    int start,
                    int before,
                    int count) {
                this.start = start;
                this.count = count;
            }

            @Override
            public void beforeTextChanged(
                    CharSequence s,
                    int start,
                    int count,
                    int after) {
            }

            @Override
            public void afterTextChanged(Editable e) {
                cancelUpdate();
                convertTabs(e, start, count);

                if (!modified) {
                    return;
                }

                dirty = true;
                updateHandler.postDelayed(updateRunnable, updateDelay);
            }
        });

        setSyntaxColors();
        setUpdateDelay(1000/*ShaderEditorApp.preferences.getUpdateDelay()*/);
        setTabWidth(4/*ShaderEditorApp.preferences.getTabWidth()*/);
    }

    private void setSyntaxColors() {
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("settings", Context.MODE_MULTI_PROCESS);
        ;

        int newBackgroundColor = sharedPreferences.getInt("editor_background_color", Integer.MIN_VALUE);
        int newBuiltinsColor = sharedPreferences.getInt("editor_builtins_color", Integer.MIN_VALUE);
        int newCommentsColor = sharedPreferences.getInt("editor_comments_color", Integer.MIN_VALUE);
        int newKeywordsColor = sharedPreferences.getInt("editor_keywords_color", Integer.MIN_VALUE);
        int newNormalTextColor = sharedPreferences.getInt("editor_normal_text_color", Integer.MIN_VALUE);
        int newNumbersColor = sharedPreferences.getInt("editor_numbers_color", Integer.MIN_VALUE);
        int newPreprocessorsColor = sharedPreferences.getInt("editor_preprocessors_color", Integer.MIN_VALUE);

        setColorBackground(newBackgroundColor);
        setColorBuiltin(newBuiltinsColor);
        setColorComment(newCommentsColor);
        setColorKeyword(newKeywordsColor);
        setColorNormalText(newNormalTextColor);
        setColorNumber(newNumbersColor);
        setColorPreprocessors(newPreprocessorsColor);
    }

    private void cancelUpdate() {
        updateHandler.removeCallbacks(updateRunnable);
    }


    public void goBackInHistory() {
        if (changesHistoryIndex > 1) {
            changesHistoryIndex--;
            changesHistoryIndex--;
            HistoryItem historyItem = changesHistory[changesHistoryIndex];
            isRightAfterHistoryRestore = true;
            setText(historyItem.getText());
            setSelection(historyItem.getCursorPostition());
            changesHistoryIndex++;
        }
    }

    public void goForwardInHistory() {
        if (changesHistoryIndex < changesHistory.length) {
            HistoryItem historyItem = changesHistory[changesHistoryIndex];
            if (historyItem != null) {
                isRightAfterHistoryRestore = true;
                setText(historyItem.getText());
                setSelection(historyItem.getCursorPostition());
                changesHistoryIndex++;
            }
        }
    }

    private void highlightWithoutChange(Editable e) {
        if (!isRightAfterHistoryRestore) {
            if (changesHistoryIndex < changesHistory.length) {
                changesHistory[changesHistoryIndex] = new HistoryItem(e.toString(), getSelectionStart());
                changesHistoryIndex++;
            } else {
                for (int i = 0; i < changesHistory.length - 1; i++) {
                    changesHistory[i] = changesHistory[i + 1];
                }
                changesHistory[changesHistory.length - 1] = new HistoryItem(e.toString(), getSelectionStart());
            }
        }
        isRightAfterHistoryRestore = false;
        modified = false;
        highlight(e);
        modified = true;
    }

    private Editable highlight(Editable e) {
        try {
            // don't use e.clearSpans() because it will
            // remove too much
            clearSpans(e);

            if (e.length() == 0) {
                return e;
            }

            if (patternNumbers != null)
                for (Matcher m = patternNumbers.matcher(e); m.find(); ) {
                    e.setSpan(
                            new ForegroundColorSpan(colorNumber),
                            m.start(),
                            m.end(),
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }

            if (patternPreprocessors != null)
                for (Matcher m = patternPreprocessors.matcher(e); m.find(); ) {
                    e.setSpan(
                            new ForegroundColorSpan(colorPreprocessors),
                            m.start(),
                            m.end(),
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }

            if (patternKeywords != null)
                for (Matcher m = patternKeywords.matcher(e); m.find(); ) {
                    e.setSpan(
                            new ForegroundColorSpan(colorKeyword),
                            m.start(),
                            m.end(),
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }

            if (patternBuiltins != null)
                for (Matcher m = patternBuiltins.matcher(e); m.find(); ) {
                    e.setSpan(
                            new ForegroundColorSpan(colorBuiltin),
                            m.start(),
                            m.end(),
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }

            if (patternComments != null)
                for (Matcher m = patternComments.matcher(e); m.find(); ) {
                    e.setSpan(
                            new ForegroundColorSpan(colorComment),
                            m.start(),
                            m.end(),
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }

            if (patternSearchedString != null)
                for (Matcher m = patternSearchedString.matcher(e); m.find(); ) {
                    e.setSpan(
                            new ForegroundColorSpan(colorSearch),
                            m.start(),
                            m.end(),
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
        } catch (IllegalStateException ex) {
            // raised by Matcher.start()/.end() when
            // no successful match has been made what
            // shouldn't ever happen because of find()
        }

        return e;
    }

    private static void clearSpans(Editable e) {
        // remove foreground color spans
        {
            ForegroundColorSpan spans[] = e.getSpans(
                    0,
                    e.length(),
                    ForegroundColorSpan.class);

            for (int i = spans.length; i-- > 0; ) {
                e.removeSpan(spans[i]);
            }
        }

        // remove background color spans
        {
            BackgroundColorSpan spans[] = e.getSpans(
                    0,
                    e.length(),
                    BackgroundColorSpan.class);

            for (int i = spans.length; i-- > 0; ) {
                e.removeSpan(spans[i]);
            }
        }
    }

    private CharSequence autoIndent(
            CharSequence source,
            Spanned dest,
            int dstart,
            int dend) {
        String indent = "";
        int istart = dstart - 1;

        // find start of this line
        boolean dataBefore = false;
        int pt = 0;

        for (; istart > -1; --istart) {
            char c = dest.charAt(istart);

            if (c == '\n') {
                break;
            }

            if (c != ' ' && c != '\t') {
                if (!dataBefore) {
                    // indent always after those characters
                    if (c == '{' ||
                            c == '+' ||
                            c == '-' ||
                            c == '*' ||
                            c == '/' ||
                            c == '%' ||
                            c == '^' ||
                            c == '=') {
                        --pt;
                    }

                    dataBefore = true;
                }

                // parenthesis counter
                if (c == '(') {
                    --pt;
                } else if (c == ')') {
                    ++pt;
                }
            }
        }

        // copy indent of this line into the next
        if (istart > -1) {
            char charAtCursor = dest.charAt(dstart);
            int iend;

            for (iend = ++istart; iend < dend; ++iend) {
                char c = dest.charAt(iend);

                // auto expand comments
                if (charAtCursor != '\n' &&
                        c == '/' &&
                        iend + 1 < dend &&
                        dest.charAt(iend) == c) {
                    iend += 2;
                    break;
                }

                if (c != ' ' && c != '\t') {
                    break;
                }
            }

            indent += dest.subSequence(istart, iend);
        }

        // add new indent
        if (pt < 0) {
            indent += "\t";
        }

        // append white space of previous line and new indent
        return source + indent;
    }

    private void convertTabs(Editable e, int start, int count) {
        if (tabWidth < 1) {
            return;
        }

        String s = e.toString();

        for (int stop = start + count;
             (start = s.indexOf("\t", start)) > -1 && start < stop;
             ++start) {
            e.setSpan(
                    new TabWidthSpan(tabWidth),
                    start,
                    start + 1,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }

    private static class TabWidthSpan extends ReplacementSpan {
        private int width;

        public TabWidthSpan(int width) {
            this.width = width;
        }

        @Override
        public int getSize(
                @NonNull Paint paint,
                CharSequence text,
                int start,
                int end,
                Paint.FontMetricsInt fm) {
            return width;
        }

        @Override
        public void draw(
                @NonNull Canvas canvas,
                CharSequence text,
                int start,
                int end,
                float x,
                int top,
                int y,
                int bottom,
                @NonNull Paint paint) {
        }
    }

    public int getColorNumber() {
        return colorNumber;
    }

    public void setColorNumber(int colorNumber) {
        this.colorNumber = colorNumber;
        dirty = true;
    }

    public int getColorKeyword() {
        return colorKeyword;
    }

    public void setColorKeyword(int colorKeyword) {
        this.colorKeyword = colorKeyword;
    }

    public int getColorBuiltin() {
        return colorBuiltin;
    }

    public void setColorBuiltin(int colorBuiltin) {
        this.colorBuiltin = colorBuiltin;
    }

    public int getColorComment() {
        return colorComment;
    }

    public void setColorComment(int colorComment) {
        this.colorComment = colorComment;
    }

    public int getColorBackground() {
        int color = Color.TRANSPARENT;
        Drawable background = getBackground();
        if (background instanceof ColorDrawable)
            color = ((ColorDrawable) background).getColor();
        return color;
    }

    public void setColorBackground(int color) {
        this.setBackgroundColor(color);
        setBackgroundDrawable(new ColorDrawable(color | 0xFF000000));
    }

    public int getColorNormalText() {
        return this.getCurrentTextColor();
    }

    public void setColorNormalText(int color) {
        this.setTextColor(color);
    }

    public int getColorPreprocessors() {
        return colorPreprocessors;
    }

    public void setColorPreprocessors(int colorPreprocessors) {
        this.colorPreprocessors = colorPreprocessors;
    }

    public Pattern getPatternLine() {
        return patternLine;
    }

    public void setPatternLine(Pattern patternLine) {
        this.patternLine = patternLine;
    }

    public Pattern getPatternNumbers() {
        return patternNumbers;
    }

    public void setPatternNumbers(Pattern patternNumbers) {
        this.patternNumbers = patternNumbers;
    }

    public Pattern getPatternPreprocessors() {
        return patternPreprocessors;
    }

    public void setPatternPreprocessors(Pattern patternPreprocessors) {
        this.patternPreprocessors = patternPreprocessors;
    }

    public Pattern getPatternKeywords() {
        return patternKeywords;
    }

    public void setPatternKeywords(Pattern patternKeywords) {
        this.patternKeywords = patternKeywords;
    }

    public Pattern getPatternBuiltins() {
        return patternBuiltins;
    }

    public void setPatternBuiltins(Pattern patternBuiltins) {
        this.patternBuiltins = patternBuiltins;
    }

    public Pattern getPatternComments() {
        return patternComments;
    }

    public void setPatternComments(Pattern patternComments) {
        this.patternComments = patternComments;
    }

    public void setSearchedString(@Nullable String string) {
        if (string == null) {
            patternSearchedString = null;
        } else {
            try {
                patternSearchedString = Pattern.compile(RegexCreatorUtil.createStringSearchRegexFromString(string));
            } catch (RegexCreatorException e) {
                Log.w(TAG, "RegexCreatorException");
            }
        }
    }

    public void refreshSyntaxHighlight() {
        setText(getCleanText());
    }

    public void refreshHistory() {
        changesHistory = new HistoryItem[5];
        changesHistoryIndex = -1;
    }

}