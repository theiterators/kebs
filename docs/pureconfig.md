---
sidebar_position: 15
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

Besides value classes, there are readers and writers of:

- types with an `InstanceConverter` (see [instances](instances.md))
- enums and value enums (see [enums](enums.md)); an `EnumNaming` in scope changes the names

```scala
import pl.iterators.kebs.enumeratum.{KebsEnumeratum, KebsValueEnumeratum}
import pl.iterators.kebs.instances.UtilInstances

object MyConf extends KebsPureConfig with KebsEnumeratum with KebsValueEnumeratum with UtilInstances
```
