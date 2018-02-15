package pl.mp107.plugtext.plugins;

import java.util.regex.Pattern;

public abstract class BaseApplicationPlugin {
    protected String[] authors;
    protected String description;
    protected String name;
    protected int version = -1;
    protected Pattern patternBuiltins;
    protected Pattern patternComments;
    protected Pattern patternFileExtensions;
    protected Pattern patternKeywords;
    protected Pattern patternLines;
    protected Pattern patternNumbers;
    protected Pattern patternPreprocessors;

    public BaseApplicationPlugin(String[] authors, String description, String name, int version, Pattern patternBuiltins, Pattern patternComments, Pattern patternFileExtensions, Pattern patternKeywords, Pattern patternLines, Pattern patternNumbers, Pattern patternPreprocessors) {
        this.authors = authors;
        this.description = description;
        this.name = name;
        this.version = version;
        this.patternBuiltins = patternBuiltins;
        this.patternComments = patternComments;
        this.patternFileExtensions = patternFileExtensions;
        this.patternKeywords = patternKeywords;
        this.patternLines = patternLines;
        this.patternNumbers = patternNumbers;
        this.patternPreprocessors = patternPreprocessors;
    }

    public int getVersion() {
        return version;
    }

    public Pattern getPatternBuiltins() {
        return patternBuiltins;
    }

    public Pattern getPatternComments() {
        return patternComments;
    }

    public Pattern getPatternFileExtensions() {
        return patternFileExtensions;
    }

    public void setPatternFileExtensions(Pattern patternFileExtensions) {
        this.patternFileExtensions = patternFileExtensions;
    }

    public Pattern getPatternKeywords() {
        return patternKeywords;
    }

    public Pattern getPatternLines() {
        return patternLines;
    }

    public Pattern getPatternNumbers() {
        return patternNumbers;
    }

    public Pattern getPatternPreprocessors() {
        return patternPreprocessors;
    }

    public String[] getAuthors() {
        return authors;
    }

    public void setAuthors(String[] authors) {
        this.authors = authors;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setVersion(int version) {
        this.version = version;
    }
}
