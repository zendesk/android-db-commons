android-db-commons
==================

WARNING: This library is under heavy development. We can't guarantee both stability of the library itself and the API. However, if you'll find some troubles, bugs, problems please submit an issue here so we can fix it!

Some common utilities for ContentProvider/ContentResolver/Cursor and other db-related android stuff

Currently it's just a builder for ContentResolver-related crap.
If you feel tired of this:
```java
getContentResolver().query(uri, 
  new String[] { People.NAME, People.AGE }, 
  People.NAME + "=? AND " + People.AGE + ">?", 
  new String[] { "Ian", "18" }, 
  null
);
```
or:
```java
getContentResolver().query(uri, null, null, null, null);
```
Using this lib you can replace it with something like:
```java
ProviderAction.newQuery(uri)
  .projection(People.NAME, People.AGE)
  .where(People.NAME + "=?", "Ian")
  .where(People.AGE + ">?", 18)
  .perform(getContentResolver());
```

What's next? You may want to transform your Cursor to some collection of something. Using this util you can easily do:

```java
ProviderAction.newQuery(uri)
  .projection(People.NAME, People.AGE)
  .where(People.NAME + "=?", "Ian")
  .where(People.AGE + ">?", 18)
  .perform(getContentResolver());
  .transform(new Function<Cursor, String>() {
    @Override public String apply(Cursor cursor) {
      return cursor.getString(cursor.getColumnIndexOrThrow(People.NAME));
    }
  })
  .filter(new Predicate<String>() {
    @Override public boolean apply(String string) {
      return string.length()%2 == 0;
    }
  });
  
```
Loaders
-------
Loaders are fine. They do some hard work for you which otherwise you would need to do manually. But maybe they can be even funnier? 

This is a standard way of creating CursorLoader.
```java
long age = 18L;
final CursorLoader loader = new CursorLoader(getActivity());
loader.setUri(uri);
loader.setProjection(new String[] { People.NAME });
loader.setSelection(People.AGE + ">?");
loader.setSelectionArgs(new String[] { String.valueOf(age) });
```
Using android-db-commons you can build it using this builder:
```java
CursorLoaderBuilder.forUri(uri)
  .projection(People.NAME)
  .where(People.AGE + ">?", 18)
  .build(getActivity());
```
Looks nice, isn't it? Yeah, but it's still not a big change. Anyway, all of us know this:
```java
@Override public void onLoadFinished(Loader<Cursor> loader, Cursor result) {
  RealResult result = ReaulResult.veryExpensiveOperationOnMainUiThread(result);
  myFancyView.setResult(result);
}
```
Using this library you are able to perform additional operations inside Loader's doInBackground().
```java
private static final Function<Cursor,RealResult> TRANSFORM = new Function<Cursor, RealResult>() {
  @Override public RealResult apply(Cursor input) {
    return RealResult.veryExpensiveOperationOnMainUiThread(result);
  }
};

CursorLoaderBuilder.forUri(uri)
  .projection(People.NAME)
  .where(People.AGE + ">?", 18)
  .transform(TRANSFORM)
  .build(getActivity());
```
Wanna transform your Cursor into a collection of something? Easy.
```java

private static final Function<Cursor,String> ROW_TRANSFORM = new Function<Cursor, String>() {
    @Override public String apply(Cursor cursor) {
      return cursor.getString(cursor.getColumnIndexOrThrow(People.NAME));
    }
  };

CursorLoaderBuilder.forUri(uri)
  .projection(People.NAME)
  .where(People.AGE + ">?", 18)
  .transformRow(ROW_TRANSFORM)
  .build(getActivity());
```
Your Loader will return List<String> as a result in this case. But we still can do better - instead of writing simple row transformations by hand we can use a factory method:
```java
import static com.getbase.android.db.cursors.SingleRowTransforms.getColumn;

CursorLoaderBuilder.forUri(uri)
  .projection(People.NAME)
  .where(People.AGE + ">?", 18)
  .transformRow(getColumn(People.NAME).asString())
  .build(getActivity());
```
What if the Cursor has 100K rows? Transforming it into collection would take way too much time and would have huge memory footprint.
```java
CursorLoaderBuilder.forUri(uri)
  .projection(People.NAME)
  .where(People.AGE + ">?", 18)
  .transformRow(getColumn(People.NAME).asString())
  .lazy()
  .build(getActivity());
```
The result of this loader is lazy list. We do not iterate through your 100K-rows Cursor. Every row's transformation is calculated at its access time.

Sure, you can still transform() your transformedRows().
```java
// Functions contants here
CursorLoaderBuilder.forUri(uri)
  .projection(People.NAME)
  .where(People.AGE + ">?", 18)
  .transformRow(CURSOR_ROW_TO_STRING)
  .transformRow(STRING_TO_INTEGER)
  .transform(LIST_OF_INTEGER_TO_REAL_RESULT)
  .build(getActivity());
```
WARNING: Please make sure you don't leak any Fragment/Activities/other resources when constructing your Loader. In Java, all anonymous nested classes are non-static which means that they are holding a reference to the parent class. As the  Function instances are cached in created Loader instance (which is being reused among multiple Activities/Fragments instances) using anonymous classes can lead to awful memory leaks or even crashes in runtime.

Example leaking code (let's assume it's Fragment instance):
```java
@Override
public Loader<List<String>> onCreateLoader(int id, Bundle args) {
  CursorLoaderBuilder.forUri(uri)
    .projection(People.NAME)
    .where(People.AGE + ">?", 18)
    .transformRow(new Function<Cursor, String>() { // Leaking Fragment's instance here. DO NOT DO THAT!
      @Override public String apply(Cursor cursor) {
        return cursor.getString(0);
      }
    })
    .build(getActivity());
}
```

If you don't want to extract all your functions to constants you can use our LoaderHelper that tends to make client's code simpler:
```java
private static final LoaderHelper<List<String>> loaderHelper = new LoaderHelper<List<String>>(LOADER_ID) {
  @Override
  protected Loader<List<String>> onCreateLoader(Context context, Bundle args) {
    return CursorLoaderBuilder.forUri(Contract.People.CONTENT_URI)
        .projection(Contract.People.FIRST_NAME, Contract.People.SECOND_NAME)
        .transformRow(new Function<Cursor, String>() {
          @Override
          public String apply(Cursor cursor) {
            return String.format("%s %s", cursor.getString(0), cursor.getString(1));
          }
        })
        .build(context);
    }
};

```
And then, when you want to initialize your Loader (let's say in fragment):
```java
// let's assume that 'this' implements LoaderHelper.LoaderDataCallbacks<Result> interface.
loaderHelper.initLoader(getActivity(), bundleArgs, this); 
```
LoaderHelper.LoaderDataCallbacks' interface is very similar to the one provided by default support-library's LoaderCallbacks so the convertion will be simple and easy.

Wrap function is applyied in Loader's doInBackground() so you don't have to worry about ANRs in case you want to do something more complex in there.

Fluent SQLite API
-----------------
Concatenating SQL strings is not fun. It's very easy to make a syntax error - miss the space, comma or closing bracket - and cause the runtime error. The other problem arises when you want to modify existing query, for example to apply some filters. To ameliorate this issues, we have created fluent API for basic SQLite operations:

```java
select()
    .columns(People.NAME, People.AGE)
    .from(Tables.PEOPLE)
    .where(column(People.NAME).eq().arg(), "Ian")
    .where(column(People.AGE).gt().arg(), 18)
    .perform(db);
```

Note that `perform()` returns `FluentCursor`, which allows you to easily transform query results into POJOs.

Usage
-----
Just add the dependency to your `build.gradle`:

```groovy
dependencies {
    compile 'com.getbase.android.db:library:0.13.4'
}
```

minSdkVersion = 15
------------------
The last with minSdkVersion = 10 was [v0.10.1](https://github.com/futuresimple/android-db-commons/tree/v0.10.1).
The last with minSdkVersion < 10 Android versions was [v0.6.3](https://github.com/futuresimple/android-db-commons/tree/v0.6.3).

Other libraries
---------------
android-db-commons works even better when combined with some other cool libraries. You may want to try them!

[MicroOrm](https://github.com/chalup/microorm)
```java
CursorLoaderBuilder.forUri(myLittleUri)
  .projection(microOrm.getProjection(Person.class))
  .transform(microOrm.getFunctionFor(Person.class))
  .build(getActivity());
```

License
-------

    Copyright (C) 2013 Mateusz Herych

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

         http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
