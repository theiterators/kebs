package pl.iterators.kebs.jsonschema

case class Sample(
    someNumber: Int,
    someText: String,
    arrayOfNumbers: List[Int],
    wrappedNumber: WrappedInt,
    wrappedNumberAnyVal: WrappedIntAnyVal
)
