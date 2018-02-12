package pl.mp107.plugtext.db;

import java.util.regex.Pattern;

public class SyntaxSchema {
    private int _id;
    private String patternBuiltins;
    private String patternComments;
    private String patternFileExtensions;
    private String patternKeywords;
    private String patternLines;
    private String patternNumbers;
    private String patternPreprocessors;
    //private String[] authors;
    private String description;
    private String name;
    private int version = -1;

    public SyntaxSchema() {

    }

    public SyntaxSchema(int _id, String patternBuiltins, String patternComments, String patternFileExtensions, String patternKeywords, String patternLines, String patternNumbers, String patternPreprocessors, String description, String name, int version) {
        this._id = _id;
        this.patternBuiltins = patternBuiltins;
        this.patternComments = patternComments;
        this.patternFileExtensions = patternFileExtensions;
        this.patternKeywords = patternKeywords;
        this.patternLines = patternLines;
        this.patternNumbers = patternNumbers;
        this.patternPreprocessors = patternPreprocessors;
        this.description = description;
        this.name = name;
        this.version = version;
    }

    public SyntaxSchema(String patternBuiltins, String patternComments,String patternFileExtensions, String patternKeywords, String patternLines, String patternNumbers, String patternPreprocessors, String description, String name, int version) {
        this.patternBuiltins = patternBuiltins;
        this.patternComments = patternComments;
        this.patternFileExtensions = patternFileExtensions;
        this.patternKeywords = patternKeywords;
        this.patternLines = patternLines;
        this.patternNumbers = patternNumbers;
        this.patternPreprocessors = patternPreprocessors;
        this.description = description;
        this.name = name;
        this.version = version;
    }

    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public String getPatternBuiltins() {
        return patternBuiltins;
    }

    public void setPatternBuiltins(String patternBuiltins) {
        this.patternBuiltins = patternBuiltins;
    }

    public String getPatternComments() {
        return patternComments;
    }

    public void setPatternComments(String patternComments) {
        this.patternComments = patternComments;
    }

    public String getPatternKeywords() {
        return patternKeywords;
    }

    public void setPatternKeywords(String patternKeywords) {
        this.patternKeywords = patternKeywords;
    }

    public String getPatternLines() {
        return patternLines;
    }

    public void setPatternLines(String patternLines) {
        this.patternLines = patternLines;
    }

    public String getPatternNumbers() {
        return patternNumbers;
    }

    public void setPatternNumbers(String patternNumbers) {
        this.patternNumbers = patternNumbers;
    }

    public String getPatternPreprocessors() {
        return patternPreprocessors;
    }

    public void setPatternPreprocessors(String patternPreprocessors) {
        this.patternPreprocessors = patternPreprocessors;
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

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public String getPatternFileExtensions() {
        return patternFileExtensions;
    }

    public void setPatternFileExtensions(String patternFileExtensions) {
        this.patternFileExtensions = patternFileExtensions;
    }
}
