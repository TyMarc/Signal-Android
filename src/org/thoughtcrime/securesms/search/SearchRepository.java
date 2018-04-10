package org.thoughtcrime.securesms.search;

import android.Manifest;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;

import com.annimon.stream.Stream;

import org.thoughtcrime.securesms.contacts.ContactAccessor;
import org.thoughtcrime.securesms.contacts.ContactsDatabase;
import org.thoughtcrime.securesms.database.Address;
import org.thoughtcrime.securesms.database.SearchDatabase;
import org.thoughtcrime.securesms.database.ThreadDatabase;
import org.thoughtcrime.securesms.database.model.ThreadRecord;
import org.thoughtcrime.securesms.permissions.Permissions;
import org.thoughtcrime.securesms.search.model.ContactResult;
import org.thoughtcrime.securesms.search.model.ConversationResult;
import org.thoughtcrime.securesms.search.model.MessageResult;
import org.thoughtcrime.securesms.search.model.SearchResult;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

/**
 * Manages data retrieval for search.
 */
public class SearchRepository {

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
      String                   cleanQuery    = Uri.encode(query);
      List<ContactResult>      contacts      = queryContacts(cleanQuery);
      List<ConversationResult> conversations = queryConversations(cleanQuery);
      List<MessageResult>      messages      = queryMessages(cleanQuery);

      callback.onResult(new SearchResult(contacts, conversations, messages));
    });
  }


  private List<ContactResult> queryContacts(String query) {
    List<ContactResult> contacts = new ArrayList<>();

    if (!Permissions.hasAny(context, Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS)) {
      return contacts;
    }

    try (Cursor cursor = contactsDatabase.queryTextSecureContacts(query)) {
      while(cursor.moveToNext()) {
        contacts.add(new ContactResult(cursor.getString(0), cursor.getString(1)));
      }
    }

    try (android.database.Cursor cursor = contactsDatabase.querySystemContacts(query)) {
      while(cursor.moveToNext()) {
        contacts.add(new ContactResult(cursor.getString(0), cursor.getString(1)));
      }
    }

    return contacts;
  }

  private List<ConversationResult> queryConversations(@NonNull String query) {
    List<String>  numbers   = contactAccessor.getNumbersForThreadSearchFilter(context, query);
    List<Address> addresses = Stream.of(numbers).map(number -> Address.fromExternal(context, number)).toList();


    List<ConversationResult> conversations = new ArrayList<>();
    try (ThreadDatabase.Reader reader = threadDatabase.readerFor(threadDatabase.getFilteredConversationList(addresses))) {
      ThreadRecord thread;
      while((thread = reader.getNext()) != null) {
        conversations.add(new ConversationResult(thread.getRecipient().getName(), thread.getBody()));
      }
    }
    return conversations;
  }

  private List<MessageResult> queryMessages(@NonNull String query) {
    List<MessageResult> messages = new ArrayList<>();
    try(Cursor cursor = searchDatabase.querySms(query, 0)) {
      while (cursor.moveToNext()) {
        Address address    = Address.fromSerialized(cursor.getString(0));
        String  body       = cursor.getString(1);
        long    receivedMs = cursor.getLong(2);

        messages.add(new MessageResult(null, body, receivedMs));
      }
    }
    return messages;
  }


  public interface Callback {
    void onResult(@NonNull SearchResult result);
  }
}
