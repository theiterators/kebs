package pl.iterators.kebs

import slick.lifted.Isomorphism

trait Kebs {
  import macros.KebsMacros
  implicit def valueColumnType[CC <: Product, B]: Isomorphism[CC, B] = macro KebsMacros.materializeValueColumn[CC, B]
  implicit def listValueColumnType[CC <: Product, B](implicit iso: Isomorphism[CC, B]): Isomorphism[List[CC], List[B]] =
    new Isomorphism[List[CC], List[B]](_.map(iso.map), _.map(iso.comap))
  implicit def seqValueColumnType[CC <: Product, B](implicit iso: Isomorphism[CC, B]): Isomorphism[Seq[CC], List[B]] = {
    new Isomorphism[Seq[CC], List[B]](_.map(iso.map).toList, _.map(iso.comap))
  }
}
