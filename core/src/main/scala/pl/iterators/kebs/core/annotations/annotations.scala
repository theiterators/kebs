package pl.iterators.kebs.core.annotations

import scala.annotation.StaticAnnotation

final class fieldName(val name: String) extends StaticAnnotation

final class transientField extends StaticAnnotation
