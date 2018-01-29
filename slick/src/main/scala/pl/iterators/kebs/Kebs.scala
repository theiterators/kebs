package pl.iterators.kebs

import pl.iterators.kebs.macros.CaseClass1Rep
import slick.lifted.Isomorphism

trait Kebs {
  implicit def valueColumnType[CC <: Product, B](implicit rep1: CaseClass1Rep[CC, B]): Isomorphism[CC, B] =
    new Isomorphism[CC, B](rep1.unapply, rep1.apply)
  implicit def listValueColumnType[CC <: Product, B](implicit iso: Isomorphism[CC, B]): Isomorphism[List[CC], List[B]] =
    new Isomorphism[List[CC], List[B]](_.map(iso.map), _.map(iso.comap))
  implicit def seqValueColumnType[CC <: Product, B](implicit iso: Isomorphism[CC, B]): Isomorphism[Seq[CC], List[B]] = {
    new Isomorphism[Seq[CC], List[B]](_.map(iso.map).toList, _.map(iso.comap))
  }
  implicit def mapValueColumnType[CC1 <: Product, CC2 <: Product, A, B](implicit iso1: Isomorphism[CC1, A],
                                                                        iso2: Isomorphism[CC2, B]): Isomorphism[Map[CC1, CC2], Map[A, B]] =
    new Isomorphism[Map[CC1, CC2], Map[A, B]](_.map { case (cc1, cc2) => (iso1.map(cc1), iso2.map(cc2)) }, _.map {
      case (a, b)                                                     => (iso1.comap(a), iso2.comap(b))
    })

  private class StringMapIsomorphism[A](comap: String => A)
      extends Isomorphism[Map[String, A], Map[String, String]](_.mapValues(_.toString), _.mapValues(comap))
  implicit final val intMapValueColumnType: Isomorphism[Map[String, Int], Map[String, String]]   = new StringMapIsomorphism[Int](_.toInt)
  implicit final val longMapValueColumnType: Isomorphism[Map[String, Long], Map[String, String]] = new StringMapIsomorphism[Long](_.toLong)
  implicit final val boolMapValueColumnType: Isomorphism[Map[String, Boolean], Map[String, String]] =
    new StringMapIsomorphism[Boolean](_.toBoolean)

  implicit def hstoreColumnType[CC1 <: Product, CC2 <: Product, A](
      implicit iso1: Isomorphism[Map[CC1, CC2], Map[String, A]],
      iso2: Isomorphism[Map[String, A], Map[String, String]]): Isomorphism[Map[CC1, CC2], Map[String, String]] =
    new Isomorphism[Map[CC1, CC2], Map[String, String]](iso1.map andThen iso2.map, iso2.comap andThen iso1.comap)

}
