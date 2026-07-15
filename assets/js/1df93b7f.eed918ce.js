"use strict";(self.webpackChunkwebsite=self.webpackChunkwebsite||[]).push([["452"],{2029(e,s,a){a.r(s),a.d(s,{default:()=>b});var l=a(4848),i=a(4164),r=a(5310),c=a(898),n=a(1085),t=a(2072);let d=[{title:"Zero boilerplate",description:(0,l.jsxs)(l.Fragment,{children:["Stop writing ",(0,l.jsx)("code",{children:"MappedColumnType.base"}),", ",(0,l.jsx)("code",{children:"deriveEncoder"}),", and ",(0,l.jsx)("code",{children:"Unmarshaller.strict"})," for every wrapper type. Kebs derives all of that at compile time from a single typeclass."]})},{title:"Use strong types everywhere",description:(0,l.jsx)(l.Fragment,{children:"Value classes, opaque types, tagged types, and enums just work with Slick, Doobie, Circe, Spray JSON, Play JSON, Akka HTTP, Pekko HTTP, http4s, ScalaCheck, PureConfig, and more."})},{title:"Scala 2 & 3",description:(0,l.jsx)(l.Fragment,{children:"Full cross-build support for Scala 2.13 and Scala 3, including JVM, Scala.js, and Scala Native where applicable."})}];function o({title:e,description:s}){return(0,l.jsx)("div",{className:(0,i.A)("col col--4"),children:(0,l.jsxs)("div",{className:"text--center padding-horiz--md",style:{paddingTop:"2rem"},children:[(0,l.jsx)(t.A,{as:"h3",children:e}),(0,l.jsx)("p",{children:s})]})})}function p(){return(0,l.jsx)("section",{className:"features_t9lD",children:(0,l.jsx)("div",{className:"container",children:(0,l.jsx)("div",{className:"row",children:d.map((e,s)=>(0,l.jsx)(o,{...e},s))})})})}var h=a(1113);let u=`case class UserId(value: String) extends AnyVal
case class Email(value: String) extends AnyVal

// Slick
implicit val userIdCol: BaseColumnType[UserId] =
  MappedColumnType.base(_.value, UserId.apply)
implicit val emailCol: BaseColumnType[Email] =
  MappedColumnType.base(_.value, Email.apply)

// Circe
implicit val userIdEnc: Encoder[UserId] =
  Encoder[String].contramap(_.value)
implicit val userIdDec: Decoder[UserId] =
  Decoder[String].map(UserId.apply)
// ... repeat for every type \xd7 every library`,m=`case class UserId(value: String) extends AnyVal
case class Email(value: String) extends AnyVal

// That's it. Slick, Circe, Akka HTTP, Doobie,
// http4s, Play JSON, etc. all just work.`;function x(){let{siteConfig:e}=(0,c.A)();return(0,l.jsx)("header",{className:(0,i.A)("hero hero--primary","heroBanner_qdFl"),children:(0,l.jsxs)("div",{className:"container",children:[(0,l.jsx)(t.A,{as:"h1",className:"hero__title",children:e.title}),(0,l.jsx)("p",{className:"hero__subtitle",children:e.tagline}),(0,l.jsx)("div",{className:"buttons_AeoN",children:(0,l.jsx)(r.A,{className:"button button--secondary button--lg",to:"/docs/intro",children:"Get Started"})})]})})}function j(){return(0,l.jsx)("section",{style:{padding:"2rem 0"},children:(0,l.jsx)("div",{className:"container",children:(0,l.jsxs)("div",{className:"row",children:[(0,l.jsxs)("div",{className:(0,i.A)("col col--6"),children:[(0,l.jsx)(t.A,{as:"h3",children:"Without Kebs"}),(0,l.jsx)(h.A,{language:"scala",children:u})]}),(0,l.jsxs)("div",{className:(0,i.A)("col col--6"),children:[(0,l.jsx)(t.A,{as:"h3",children:"With Kebs"}),(0,l.jsx)(h.A,{language:"scala",children:m})]})]})})})}function y(){return(0,l.jsx)("section",{style:{padding:"1rem 0 2rem"},children:(0,l.jsx)("div",{className:"container",children:(0,l.jsx)("div",{className:"row",children:(0,l.jsxs)("div",{className:(0,i.A)("col col--8 col--offset-2"),children:[(0,l.jsx)(t.A,{as:"h3",style:{textAlign:"center"},children:"Quick start"}),(0,l.jsx)(h.A,{language:"scala",children:`// build.sbt \u{2014} pick the modules you need
libraryDependencies ++= Seq(
  "pl.iterators" %% "kebs-circe"     % kebsVersion,
  "pl.iterators" %% "kebs-slick"     % kebsVersion,
  "pl.iterators" %% "kebs-pekko-http" % kebsVersion,
  "pl.iterators" %% "kebs-instances" % kebsVersion
)`})]})})})})}function b(){return(0,l.jsxs)(n.A,{title:"Kebs \u2014 Scala library for eliminating boilerplate",description:"Kebs automatically derives typeclass instances (JSON codecs, DB column mappings, HTTP unmarshallers) for your Scala domain types.",children:[(0,l.jsx)(x,{}),(0,l.jsxs)("main",{children:[(0,l.jsx)(p,{}),(0,l.jsx)(j,{}),(0,l.jsx)(y,{})]})]})}}}]);