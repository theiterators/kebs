## Version 1.4.1

> 21.03.2017

Dependencies changed:
- `kebs-play-json` depends on `play-json` `2.5.13` in `2.11`; `1.6.0-M5` in `2.12`
- `kebs-slick`     depends on `slick`     `3.1.1`  in `2.11`; `3.2.0`    in `2.12`

It's safe to bump up slick to `3.2.0` manually if you are on `2.11` - `kebs-slick` does not depend on anything slick-version-specific

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
