# Scalacture

[![Build Status](https://travis-ci.org/okumin/scalacture.svg?branch=master)](https://travis-ci.org/okumin/scalacture)

Scalacture is a project to implement all the data structures by Scala.

## Projects

### scalacture-core

The main project.
Stable APIs are here.

* scalacture.immutable.queue
  * BatchedQueue
  * HoodMelvilleQueue

### scalacture-scalaz

Provides Scalaz extensions.
You can import type class instances for data structues implemented in Scalacture.

For example, if you would like to import type class instances for `scalacture.immutable.queue.HoodMelvilleQueue`,

```scala
import scalacture.scalaz.immutable.queue.hoodMelvilleQueue._
```
