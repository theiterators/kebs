## Version 1.4.0

> 11.03.2017

This version adds support for Scala 2.12 (using `play-json 1.6.0-M4`)
* Support for `ValueEnumEntry` and `ValueEnum` for `slick` and `spray-json`
* Removed `play-json` support for `enumeratum` - use `enumeratum-play-json` instead


## Version 1.3.0

> 06.02.2017

First published version of `kebs`:
* Generation of slick mappers for 1-element case classes and lists
* Generation of `spray-json` formats for case classes
* Generation of `play-json` flat formats for 1-element case classes
* Support for `enumeratum` mappers in `slick`, `spray-json` and `play-json`
