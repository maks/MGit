package me.sheimi.sgit.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.StringCharacterIterator;

/**
 * Created by sheimi on 8/6/13.
 */
public class RepoDbHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "repo.db";

    public RepoDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(RepoContract.REPO_ENTRY_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i2) {
        sqLiteDatabase.execSQL(RepoContract.REPO_ENTRY_DROP);
        onCreate(sqLiteDatabase);
    }

    public static String addSlashes(String text) {
        final StringBuffer sb = new StringBuffer(text.length() * 2);
        final StringCharacterIterator iterator = new StringCharacterIterator(
                text);

        char character = iterator.current();

        while (character != StringCharacterIterator.DONE) {
            if (character == '"')
                sb.append("\\\"");
            else if (character == '\'')
                sb.append("\'\'");
            else if (character == '\\')
                sb.append("\\\\");
            else if (character == '\n')
                sb.append("\\n");
            else if (character == '{')
                sb.append("\\{");
            else if (character == '}')
                sb.append("\\}");
            else
                sb.append(character);

            character = iterator.next();
        }

        return sb.toString();
    }
}
