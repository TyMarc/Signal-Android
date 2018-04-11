package org.thoughtcrime.securesms.search;

import android.Manifest;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.support.annotation.NonNull;
import android.util.Log;

import com.annimon.stream.Stream;


import org.thoughtcrime.securesms.contacts.ContactAccessor;
import org.thoughtcrime.securesms.contacts.ContactsDatabase;
import org.thoughtcrime.securesms.database.Address;
import org.thoughtcrime.securesms.database.SearchDatabase;
import org.thoughtcrime.securesms.database.ThreadDatabase;
import org.thoughtcrime.securesms.database.model.ThreadRecord;
import org.thoughtcrime.securesms.permissions.Permissions;
import org.thoughtcrime.securesms.recipients.Recipient;
import org.thoughtcrime.securesms.search.model.MessageResult;
import org.thoughtcrime.securesms.search.model.SearchResult;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;

/**
 * Manages data retrieval for search.
 */
public class SearchRepository {

  private static final Set<Character> BANNED_CHARACTERS = new HashSet<>();
  static {
    // Several ranges of invalid ASCII characters
    for (int i = 33; i <= 47; i++) {
      BANNED_CHARACTERS.add((char) i);
    }
    for (int i = 58; i <= 64; i++) {
      BANNED_CHARACTERS.add((char) i);
    }
    for (int i = 91; i <= 96; i++) {
      BANNED_CHARACTERS.add((char) i);
    }
    for (int i = 123; i <= 126; i++) {
      BANNED_CHARACTERS.add((char) i);
    }
  }

  private final Context          context;
  private final SearchDatabase   searchDatabase;
  private final ContactsDatabase contactsDatabase;
  private final ThreadDatabase   threadDatabase;
  private final ContactAccessor  contactAccessor;
  private final Executor         executor;

  public SearchRepository(@NonNull Context          context,
                          @NonNull SearchDatabase   searchDatabase,
                          @NonNull ContactsDatabase contactsDatabase,
                          @NonNull ThreadDatabase   threadDatabase,
                          @NonNull ContactAccessor  contactAccessor,
                          @NonNull Executor         executor)
  {
    this.context          = context.getApplicationContext();
    this.searchDatabase   = searchDatabase;
    this.contactsDatabase = contactsDatabase;
    this.threadDatabase   = threadDatabase;
    this.contactAccessor  = contactAccessor;
    this.executor         = executor;
  }

  @NonNull
  public void query(@NonNull String query, @NonNull Callback callback) {
    executor.execute(() -> {
      String              cleanQuery    = sanitizeQuery(query);
      List<Recipient>     contacts      = queryContacts(cleanQuery);
      List<ThreadRecord>  conversations = queryConversations(cleanQuery);
      List<MessageResult> messages      = queryMessages(cleanQuery);

      callback.onResult(new SearchResult(contacts, conversations, messages));
    });
  }

  private List<Recipient> queryContacts(String query) {
    List<Recipient> contacts = new ArrayList<>();

    if (!Permissions.hasAny(context, Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS)) {
      return contacts;
    }

    try (Cursor cursor = contactsDatabase.queryTextSecureContacts(query)) {
      while(cursor.moveToNext()) {
        Address address = Address.fromExternal(context, cursor.getString(1));
        contacts.add(Recipient.from(context, address, false));
      }
    }

    try (android.database.Cursor cursor = contactsDatabase.querySystemContacts(query)) {
      while(cursor.moveToNext()) {
        Address address = Address.fromExternal(context, cursor.getString(1));
        contacts.add(Recipient.from(context, address, false));
      }
    }

    return contacts;
  }

  private List<ThreadRecord> queryConversations(@NonNull String query) {
    List<String>  numbers   = contactAccessor.getNumbersForThreadSearchFilter(context, query);
    List<Address> addresses = Stream.of(numbers).map(number -> Address.fromExternal(context, number)).toList();


    List<ThreadRecord> conversations = new ArrayList<>();
    try (ThreadDatabase.Reader reader = threadDatabase.readerFor(threadDatabase.getFilteredConversationList(addresses))) {
      ThreadRecord thread;
      while((thread = reader.getNext()) != null) {
        conversations.add(thread);
      }
    }
    return conversations;
  }

  private List<MessageResult> queryMessages(@NonNull String query) {
    List<MessageResult> messages = new ArrayList<>();
    try(Cursor cursor = searchDatabase.queryMessages(query, 0)) {
      while (cursor.moveToNext()) {
        Address   address    = Address.fromSerialized(cursor.getString(0));
        Recipient recipient  = Recipient.from(context, address, false);
        String    body       = cursor.getString(1);
        long      receivedMs = cursor.getLong(2);

        Log.e("SPIDERMAN", "profile avatar: " + recipient.getProfileAvatar());
        Log.e("SPIDERMAN", "uri: " + recipient.getContactUri());

        messages.add(new MessageResult(recipient, body, receivedMs));
      }
    }
    return messages;
  }

  /**
   * Unfortunately {@link DatabaseUtils#sqlEscapeString(String)} is not sufficient for our purposes.
   * MATCH queries have a separate format of their own that disallow most "special" characters.
   */
  private String sanitizeQuery(@NonNull String query) {
    StringBuilder out = new StringBuilder();

    for (int i = 0; i < query.length(); i++) {
      char c = query.charAt(i);
      if (!BANNED_CHARACTERS.contains(c)) {
        out.append(c);
      }
    }

    return out.toString();
  }


  public interface Callback {
    void onResult(@NonNull SearchResult result);
  }
}
