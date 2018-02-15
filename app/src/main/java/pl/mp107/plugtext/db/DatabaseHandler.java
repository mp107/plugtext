package pl.mp107.plugtext.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class DatabaseHandler extends SQLiteOpenHelper {
    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "syntax_manager";

    // Syntax schemas table names
    private static final String TABLE_SYNTAX_SCHEMAS = "syntax_schemas";

    // Syntax schemas Columns names
    /* syntax_schemas */
    private static final String KEY_SYNTAX_SCHEMAS_ID = "id";
    private static final String KEY_SYNTAX_SCHEMAS_VERSION = "version";
    private static final String KEY_SYNTAX_SCHEMAS_NAME = "name";
    private static final String KEY_SYNTAX_SCHEMAS_DESCRIPTION = "description";
    private static final String KEY_SYNTAX_SCHEMAS_PATTERN_BUILTINS = "pattern_builtins";
    private static final String KEY_SYNTAX_SCHEMAS_PATTERN_COMMENTS = "pattern_comments";
    private static final String KEY_SYNTAX_SCHEMAS_PATTERN_FILE_EXTENSIONS = "pattern_file_extensions";
    private static final String KEY_SYNTAX_SCHEMAS_PATTERN_KEYWORDS = "pattern_keywords";
    private static final String KEY_SYNTAX_SCHEMAS_PATTERN_LINES = "pattern_lines";
    private static final String KEY_SYNTAX_SCHEMAS_PATTERN_NUMBERS = "pattern_numbers";
    private static final String KEY_SYNTAX_SCHEMAS_PATTERN_PREPROCESSORS = "pattern_preprocessors";

    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Creating Tables
    @Override
    public synchronized void onCreate(SQLiteDatabase db) {
        String CREATE_SYNTAX_SCHEMAS_TABLE = "CREATE TABLE " + TABLE_SYNTAX_SCHEMAS + "("
                + KEY_SYNTAX_SCHEMAS_ID + " INTEGER PRIMARY KEY,"
                + KEY_SYNTAX_SCHEMAS_VERSION + " INTEGER,"
                + KEY_SYNTAX_SCHEMAS_NAME + " TEXT,"
                + KEY_SYNTAX_SCHEMAS_DESCRIPTION + " TEXT,"
                + KEY_SYNTAX_SCHEMAS_PATTERN_BUILTINS + " TEXT,"
                + KEY_SYNTAX_SCHEMAS_PATTERN_COMMENTS + " TEXT,"
                + KEY_SYNTAX_SCHEMAS_PATTERN_FILE_EXTENSIONS + " TEXT,"
                + KEY_SYNTAX_SCHEMAS_PATTERN_KEYWORDS + " TEXT,"
                + KEY_SYNTAX_SCHEMAS_PATTERN_LINES + " TEXT,"
                + KEY_SYNTAX_SCHEMAS_PATTERN_NUMBERS + " TEXT,"
                + KEY_SYNTAX_SCHEMAS_PATTERN_PREPROCESSORS + " TEXT" + ")";
        db.execSQL(CREATE_SYNTAX_SCHEMAS_TABLE);
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SYNTAX_SCHEMAS);

        // Create tables again
        onCreate(db);
    }

    public void clearDatabase() {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_SYNTAX_SCHEMAS, null, null);
    }

    // Adding new syntax schema
    public boolean addSyntaxSchema(SyntaxSchema schema) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_SYNTAX_SCHEMAS_NAME, schema.getName());
        values.put(KEY_SYNTAX_SCHEMAS_DESCRIPTION, schema.getDescription());
        values.put(KEY_SYNTAX_SCHEMAS_PATTERN_BUILTINS, schema.getPatternBuiltins());
        values.put(KEY_SYNTAX_SCHEMAS_PATTERN_COMMENTS, schema.getPatternComments());
        values.put(KEY_SYNTAX_SCHEMAS_PATTERN_FILE_EXTENSIONS, schema.getPatternFileExtensions());
        values.put(KEY_SYNTAX_SCHEMAS_PATTERN_KEYWORDS, schema.getPatternKeywords());
        values.put(KEY_SYNTAX_SCHEMAS_PATTERN_LINES, schema.getPatternLines());
        values.put(KEY_SYNTAX_SCHEMAS_PATTERN_NUMBERS, schema.getPatternNumbers());
        values.put(KEY_SYNTAX_SCHEMAS_PATTERN_PREPROCESSORS, schema.getPatternPreprocessors());
        values.put(KEY_SYNTAX_SCHEMAS_VERSION, schema.getVersion());

        // Inserting Row
        db.insert(TABLE_SYNTAX_SCHEMAS, null, values);
        db.close(); // Closing database connection

        return true;
    }

    public SyntaxSchema getSyntaxSchemaById(int id) {
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = db.query(TABLE_SYNTAX_SCHEMAS,
                new String[]{
                        KEY_SYNTAX_SCHEMAS_ID, KEY_SYNTAX_SCHEMAS_NAME, KEY_SYNTAX_SCHEMAS_DESCRIPTION,
                        KEY_SYNTAX_SCHEMAS_PATTERN_BUILTINS, KEY_SYNTAX_SCHEMAS_PATTERN_COMMENTS, KEY_SYNTAX_SCHEMAS_PATTERN_FILE_EXTENSIONS,
                        KEY_SYNTAX_SCHEMAS_PATTERN_KEYWORDS, KEY_SYNTAX_SCHEMAS_PATTERN_LINES, KEY_SYNTAX_SCHEMAS_PATTERN_NUMBERS,
                        KEY_SYNTAX_SCHEMAS_PATTERN_PREPROCESSORS, KEY_SYNTAX_SCHEMAS_VERSION
                }, KEY_SYNTAX_SCHEMAS_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null, null);

        if (cursor != null)
            cursor.moveToFirst();

        SyntaxSchema schema = new SyntaxSchema(
                Integer.parseInt(cursor.getString(0)),
                cursor.getString(3),
                cursor.getString(4),
                cursor.getString(5),
                cursor.getString(6),
                cursor.getString(7),
                cursor.getString(8),
                cursor.getString(9),
                cursor.getString(2),
                cursor.getString(1),
                Integer.parseInt(cursor.getString(10))
        );
        return schema;
    }

    public SyntaxSchema getSyntaxSchemaByName(String name) {
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = db.query(TABLE_SYNTAX_SCHEMAS,
                new String[]{
                        KEY_SYNTAX_SCHEMAS_ID, KEY_SYNTAX_SCHEMAS_NAME, KEY_SYNTAX_SCHEMAS_DESCRIPTION,
                        KEY_SYNTAX_SCHEMAS_PATTERN_BUILTINS, KEY_SYNTAX_SCHEMAS_PATTERN_COMMENTS, KEY_SYNTAX_SCHEMAS_PATTERN_FILE_EXTENSIONS,
                        KEY_SYNTAX_SCHEMAS_PATTERN_KEYWORDS, KEY_SYNTAX_SCHEMAS_PATTERN_LINES, KEY_SYNTAX_SCHEMAS_PATTERN_NUMBERS,
                        KEY_SYNTAX_SCHEMAS_PATTERN_PREPROCESSORS, KEY_SYNTAX_SCHEMAS_VERSION
                }, KEY_SYNTAX_SCHEMAS_NAME + "=?",
                new String[]{name}, null, null, null, null);

        if (cursor != null)
            cursor.moveToFirst();

        SyntaxSchema schema = new SyntaxSchema(
                Integer.parseInt(cursor.getString(0)),
                cursor.getString(3),
                cursor.getString(4),
                cursor.getString(5),
                cursor.getString(6),
                cursor.getString(7),
                cursor.getString(8),
                cursor.getString(9),
                cursor.getString(2),
                cursor.getString(1),
                Integer.parseInt(cursor.getString(10))
        );
        return schema;
    }

    public List<SyntaxSchema> getAllSyntaxSchemas() {
        List<SyntaxSchema> syntaxSchemasList = new ArrayList<SyntaxSchema>();

        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_SYNTAX_SCHEMAS;

        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.query(TABLE_SYNTAX_SCHEMAS,
                new String[]{
                        KEY_SYNTAX_SCHEMAS_ID, KEY_SYNTAX_SCHEMAS_NAME, KEY_SYNTAX_SCHEMAS_DESCRIPTION,
                        KEY_SYNTAX_SCHEMAS_PATTERN_BUILTINS, KEY_SYNTAX_SCHEMAS_PATTERN_COMMENTS, KEY_SYNTAX_SCHEMAS_PATTERN_FILE_EXTENSIONS,
                        KEY_SYNTAX_SCHEMAS_PATTERN_KEYWORDS, KEY_SYNTAX_SCHEMAS_PATTERN_LINES, KEY_SYNTAX_SCHEMAS_PATTERN_NUMBERS,
                        KEY_SYNTAX_SCHEMAS_PATTERN_PREPROCESSORS, KEY_SYNTAX_SCHEMAS_VERSION
                }, null, null, null, null, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                SyntaxSchema schema = new SyntaxSchema(
                        Integer.parseInt(cursor.getString(0)),
                        cursor.getString(3),
                        cursor.getString(4),
                        cursor.getString(5),
                        cursor.getString(6),
                        cursor.getString(7),
                        cursor.getString(8),
                        cursor.getString(9),
                        cursor.getString(2),
                        cursor.getString(1),
                        Integer.parseInt(cursor.getString(10))
                );
                // Adding contact to list
                syntaxSchemasList.add(schema);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return syntaxSchemasList;
    }

    public SyntaxSchema getSyntaxSchemaByFileExtension(String fileExtension) {
        SQLiteDatabase db = this.getWritableDatabase();

        List<SyntaxSchema> syntaxSchemasList = getAllSyntaxSchemas();

        for (SyntaxSchema schema : syntaxSchemasList) {
            if (Pattern.compile(schema.getPatternFileExtensions()).matcher(fileExtension).matches())
                return schema;
        }
        return null;
    }
}
