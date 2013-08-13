android-db-commons
==================

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
