package org.thoughtcrime.securesms.database;

import android.content.Context;
import android.support.annotation.NonNull;

import net.sqlcipher.Cursor;
import net.sqlcipher.database.SQLiteDatabase;

import org.thoughtcrime.securesms.database.helpers.SQLCipherOpenHelper;

/**
 * Contains all databases necessary for full-text search (FTS).
 */
public class SearchDatabase extends Database {

  public static final String SMS_FTS_TABLE_NAME = "sms_fts";
  public static final String MMS_FTS_TABLE_NAME = "mms_fts";

  public static final String ID   = "rowid";
  public static final String BODY = MmsSmsColumns.BODY;

  public static final String[] CREATE_TABLE = {
      "CREATE VIRTUAL TABLE " + SMS_FTS_TABLE_NAME + " USING fts5(" + BODY + ", content=" + SmsDatabase.TABLE_NAME + ", content_rowid=" + SmsDatabase.ID + ");",

      "CREATE TRIGGER sms_ai AFTER INSERT ON " + SmsDatabase.TABLE_NAME + " BEGIN\n" +
          "  INSERT INTO " + SMS_FTS_TABLE_NAME + "(" + ID + ", " + BODY + ") VALUES (new." + SmsDatabase.ID + ", new." + SmsDatabase.BODY + ");\n" +
          "END;\n",
      "CREATE TRIGGER sms_ad AFTER DELETE ON " + SmsDatabase.TABLE_NAME + " BEGIN\n" +
          "  INSERT INTO " + SMS_FTS_TABLE_NAME + "(" + SMS_FTS_TABLE_NAME + ", " + ID + ", " + BODY + ") VALUES('delete', old." + SmsDatabase.ID + ", old." + SmsDatabase.BODY + ");\n" +
          "END;\n",
      "CREATE TRIGGER sms_au AFTER UPDATE ON " + SmsDatabase.TABLE_NAME + " BEGIN\n" +
          "  INSERT INTO " + SMS_FTS_TABLE_NAME + "(" + SMS_FTS_TABLE_NAME + ", " + ID + ", " + BODY + ") VALUES('delete', old." + SmsDatabase.ID + ", old." + SmsDatabase.BODY + ");\n" +
          "  INSERT INTO " + SMS_FTS_TABLE_NAME + "(" + ID + ", " + BODY + ") VALUES (new." + SmsDatabase.ID + ", new." + SmsDatabase.BODY + ");\n" +
          "END;",


      "CREATE VIRTUAL TABLE " + MMS_FTS_TABLE_NAME + " USING fts5(" + BODY + ", content=" + MmsDatabase.TABLE_NAME + ", content_rowid=" + MmsDatabase.ID + ");",

      "CREATE TRIGGER mms_ai AFTER INSERT ON " + MmsDatabase.TABLE_NAME + " BEGIN\n" +
          "  INSERT INTO " + MMS_FTS_TABLE_NAME + "(" + ID + ", " + BODY + ") VALUES (new." + MmsDatabase.ID + ", new." + MmsDatabase.BODY + ");\n" +
          "END;\n",
      "CREATE TRIGGER mms_ad AFTER DELETE ON " + MmsDatabase.TABLE_NAME + " BEGIN\n" +
          "  INSERT INTO " + MMS_FTS_TABLE_NAME + "(" + MMS_FTS_TABLE_NAME + ", " + ID + ", " + BODY + ") VALUES('delete', old." + MmsDatabase.ID + ", old." + MmsDatabase.BODY + ");\n" +
          "END;\n",
      "CREATE TRIGGER mms_au AFTER UPDATE ON " + MmsDatabase.TABLE_NAME + " BEGIN\n" +
          "  INSERT INTO " + MMS_FTS_TABLE_NAME + "(" + MMS_FTS_TABLE_NAME + ", " + ID + ", " + BODY + ") VALUES('delete', old." + MmsDatabase.ID + ", old." + MmsDatabase.BODY + ");\n" +
          "  INSERT INTO " + MMS_FTS_TABLE_NAME + "(" + ID + ", " + BODY + ") VALUES (new." + MmsDatabase.ID + ", new." + MmsDatabase.BODY + ");\n" +
          "END;"
  };

  public SearchDatabase(@NonNull Context context, @NonNull SQLCipherOpenHelper databaseHelper) {
    super(context, databaseHelper);
  }

  public Cursor querySms(@NonNull String query, int limit) {
    SQLiteDatabase db = databaseHelper.getReadableDatabase();

    String sql = "SELECT " + SmsDatabase.ADDRESS + ", " + SmsDatabase.BODY + ", " + SmsDatabase.DATE_RECEIVED + " " +
                 "FROM " + SmsDatabase.TABLE_NAME + " " +
                 "WHERE " + SmsDatabase.ID + " IN " +
                 "(SELECT " + ID + " FROM " + SMS_FTS_TABLE_NAME + " WHERE " + SMS_FTS_TABLE_NAME + " MATCH '" + query + "*')";
    return db.rawQuery(sql, null);
  }
}

