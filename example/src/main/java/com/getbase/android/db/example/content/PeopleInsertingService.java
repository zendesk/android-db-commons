package com.getbase.android.db.example.content;

import com.google.common.collect.Lists;

import android.app.IntentService;
import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.content.SharedPreferences;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class PeopleInsertingService extends IntentService {

  private static final List<String> NAMES = Lists.newArrayList(
      "Keneth Mclemore",
      "Venita Papineau",
      "Reanna Dvorak",
      "Kati Veney",
      "Diedra Zufelt",
      "Isela Mcbride",
      "Shaquita Moncrief",
      "Alonso Pannell",
      "Rolland Mealey",
      "Estelle Latham",
      "Shawna Friend",
      "Melvina Middleton",
      "Myriam Seats",
      "Berna Llanas",
      "Ronda Dunkelberger",
      "Bernardina Countess",
      "Antonio Ranney",
      "Joline Luedke"
  );

  public static void schedule(Context context) {
    context.startService(new Intent(context, PeopleInsertingService.class));
  }

  private static final String NAME = PeopleInsertingService.class.getSimpleName();
  private static final String KEY_ALREADY_INSERTED = "inserted";

  public PeopleInsertingService() {
    super(NAME);
  }

  @Override
  protected void onHandleIntent(Intent intent) {
    final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
    if (!prefs.getBoolean(KEY_ALREADY_INSERTED, false)) {
      insertStuff();
      prefs.edit().putBoolean(KEY_ALREADY_INSERTED, true).commit();
    }
  }

  private void insertStuff() {
    ArrayList<ContentProviderOperation> operations = Lists.newArrayList();
    for (String name : NAMES) {
      String[] firstAndSecond = name.split(" ");
      final ContentValues values = new ContentValues();
      values.put(Contract.People.FIRST_NAME, firstAndSecond[0]);
      values.put(Contract.People.SECOND_NAME, firstAndSecond[1]);
      final ContentProviderOperation operation = ContentProviderOperation
          .newInsert(Contract.People.CONTENT_URI)
          .withValues(values)
          .build();
      operations.add(operation);
    }
    try {
      getContentResolver().applyBatch(Contract.AUTHORITY, operations);
    } catch (RemoteException e) {
      Log.e(NAME, "RemoteException: ", e);
    } catch (OperationApplicationException e) {
      Log.e(NAME, "OperationApplicationException: ", e);
    }
  }
}
