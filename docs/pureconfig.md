---
sidebar_position: 12
title: PureConfig
---

# kebs-pureconfig

PureConfig `ConfigReader` / `ConfigWriter` instances.

## Setup

```scala
libraryDependencies += "pl.iterators" %% "kebs-pureconfig" % kebsVersion
```

## Usage

```scala
import pl.iterators.kebs.pureconfig._
import pl.iterators.kebs.core.macros.CaseClass1ToValueClass
```

Or as a mixin:

```scala
import pl.iterators.kebs.pureconfig.KebsPureConfig

object MyConf extends KebsPureConfig with CaseClass1ToValueClass {
  // ConfigReader[MyValueClass] and ConfigWriter[MyValueClass] are now available
}
```
