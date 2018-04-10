package org.thoughtcrime.securesms.database.helpers;

import android.support.annotation.NonNull;

import net.sqlcipher.database.SQLiteDatabase;

import org.thoughtcrime.securesms.database.MmsDatabase;
import org.thoughtcrime.securesms.database.SearchDatabase;
import org.thoughtcrime.securesms.database.SmsDatabase;

public class FullTextSearchMigrationHelper {

  public static void migrateToFullTextSearch(@NonNull SQLiteDatabase db) {
    for (String sql : SearchDatabase.CREATE_TABLE) {
      db.execSQL(sql);
    }
    // TODO: Move to foreground service thingy
    db.execSQL("INSERT INTO " + SearchDatabase.SMS_FTS_TABLE_NAME + " (rowid, " + SearchDatabase.BODY + ") " +
               "SELECT " + SmsDatabase.ID + " , " + SmsDatabase.BODY + " FROM " + SmsDatabase.TABLE_NAME);

    db.execSQL("INSERT INTO " + SearchDatabase.MMS_FTS_TABLE_NAME + " (rowid, " + SearchDatabase.BODY + ") " +
               "SELECT " + MmsDatabase.ID + " , " + MmsDatabase.BODY + " FROM " + MmsDatabase.TABLE_NAME);
  }
}
