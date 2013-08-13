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
