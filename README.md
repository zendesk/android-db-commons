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
CursorLoaderBuilder.forUri(uri)
  .projection(People.NAME)
  .where(People.AGE + ">?", 18)
  .wrap(new Function<Cursor, RealResult>() {
    @Override public RealResult apply(Cursor cursor) {
      return RealResult.veryExpensiveOperationOnMainUiThread(result);
    }
  })
  .build(getActivity());
```
Wanna transform your Cursor into a collection of something? Easy.
```java
CursorLoaderBuilder.forUri(uri)
  .projection(People.NAME)
  .where(People.AGE + ">?", 18)
  .transform(new Function<Cursor, String>() {
    @Override public RealResult apply(Cursor cursor) {
      return cursor.getString(0);
    }
  })
  .build(getActivity());
```
Your Loader will return LazyCursorList<String> as a result in this case. Yes, it's lazy. We do not iterate through your 100K-rows Cursor. Even if everything is still happening on the background thread. 

Sure, you can still wrap() your transformed() result.
```java
CursorLoaderBuilder.forUri(uri)
  .projection(People.NAME)
  .where(People.AGE + ">?", 18)
  .transform(new Function<Cursor, String>() {
    @Override public RealResult apply(Cursor cursor) {
      return cursor.getString(0);
    }
  })
  .transform(new Function<String, Integer>() {
    @Override public Integer apply(String name) {
      return name.length();
    }
  })
  .wrap(new Function<LazyCursorList<Integer>, RealResult>() {
    @Override public RealResult apply(LazyCursorList<Integer> lazyList) {
      return RealResult.factoryFactory(lazyList);
    }
  })
  .build(getActivity());
```

Building
--------
This is standard maven project. To build it just execute:
```shell
mvn clean package
```
in directory with pom.xml.

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
