Change Log
==========

Version 0.13.3 *(2016-11-22)*
----------------------------

 * Provide more info when exception is thrown from `AbstractLoader.onNewDataDelivered()`

Version 0.13.2 *(2016-05-17)*
----------------------------

 * Provide more info when exception is thrown from `ComposedCursorLoader.loadInBackground()`

Version 0.13.1 *(2015-12-05)*
----------------------------

 * Fix compilation warnings when using varargs APIs

Version 0.13.0 *(2015-09-23)*
----------------------------

 * Batcher API changes: removed `Batcher.append(Batcher)`, added `Batcher.decorateUrisWith(UriDecorator)` and `selectionBackRef` support.

Version 0.12.1 *(2015-09-17)*
----------------------------

 * Another fix for compound queries builder

Version 0.12.0 *(2015-09-07)*
----------------------------

 * Add `QueryBuilder.collate(CollatingSequence collate)` builder method
 * Fix nested compound queries builder
