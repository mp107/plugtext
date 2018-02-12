package pl.mp107.plugtext.db;

public class FiletypeSyntaxAssignment {
    private int _id;
    private String fileExtension;
    private SyntaxSchema syntaxSchema;

    public FiletypeSyntaxAssignment() {
    }

    public FiletypeSyntaxAssignment(String fileExtension, SyntaxSchema syntaxSchema) {
        this._id = _id;
        this.fileExtension = fileExtension;
        this.syntaxSchema = syntaxSchema;
    }

    public FiletypeSyntaxAssignment(int _id, String fileExtension, SyntaxSchema syntaxSchema) {
        this._id = _id;
        this.fileExtension = fileExtension;
        this.syntaxSchema = syntaxSchema;
    }

    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public String getFileExtension() {
        return fileExtension;
    }

    public void setFileExtension(String fileExtension) {
        this.fileExtension = fileExtension;
    }

    public SyntaxSchema getSyntaxSchema() {
        return syntaxSchema;
    }

    public void setSyntaxSchema(SyntaxSchema syntaxSchema) {
        this.syntaxSchema = syntaxSchema;
    }
}
