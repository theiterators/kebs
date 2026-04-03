import type {ReactNode} from 'react';
import clsx from 'clsx';
import Link from '@docusaurus/Link';
import useDocusaurusContext from '@docusaurus/useDocusaurusContext';
import Layout from '@theme/Layout';
import HomepageFeatures from '@site/src/components/HomepageFeatures';
import Heading from '@theme/Heading';
import CodeBlock from '@theme/CodeBlock';

import styles from './index.module.css';

const beforeCode = `case class UserId(value: String) extends AnyVal
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
// ... repeat for every type × every library`;

const afterCode = `case class UserId(value: String) extends AnyVal
case class Email(value: String) extends AnyVal

// That's it. Slick, Circe, Akka HTTP, Doobie,
// http4s, Play JSON, etc. all just work.`;

function HomepageHeader() {
  const {siteConfig} = useDocusaurusContext();
  return (
    <header className={clsx('hero hero--primary', styles.heroBanner)}>
      <div className="container">
        <Heading as="h1" className="hero__title">
          {siteConfig.title}
        </Heading>
        <p className="hero__subtitle">{siteConfig.tagline}</p>
        <div className={styles.buttons}>
          <Link
            className="button button--secondary button--lg"
            to="/docs/intro">
            Get Started
          </Link>
        </div>
      </div>
    </header>
  );
}

function HomepageExample() {
  return (
    <section style={{padding: '2rem 0'}}>
      <div className="container">
        <div className="row">
          <div className={clsx('col col--6')}>
            <Heading as="h3">Without Kebs</Heading>
            <CodeBlock language="scala">{beforeCode}</CodeBlock>
          </div>
          <div className={clsx('col col--6')}>
            <Heading as="h3">With Kebs</Heading>
            <CodeBlock language="scala">{afterCode}</CodeBlock>
          </div>
        </div>
      </div>
    </section>
  );
}

function HomepageInstall() {
  return (
    <section style={{padding: '1rem 0 2rem'}}>
      <div className="container">
        <div className="row">
          <div className={clsx('col col--8 col--offset-2')}>
            <Heading as="h3" style={{textAlign: 'center'}}>Quick start</Heading>
            <CodeBlock language="scala">
              {`// build.sbt — pick the modules you need
libraryDependencies ++= Seq(
  "pl.iterators" %% "kebs-circe"     % kebsVersion,
  "pl.iterators" %% "kebs-slick"     % kebsVersion,
  "pl.iterators" %% "kebs-pekko-http" % kebsVersion,
  "pl.iterators" %% "kebs-instances" % kebsVersion
)`}
            </CodeBlock>
          </div>
        </div>
      </div>
    </section>
  );
}

export default function Home(): ReactNode {
  return (
    <Layout
      title="Kebs — Scala library for eliminating boilerplate"
      description="Kebs automatically derives typeclass instances (JSON codecs, DB column mappings, HTTP unmarshallers) for your Scala domain types.">
      <HomepageHeader />
      <main>
        <HomepageFeatures />
        <HomepageExample />
        <HomepageInstall />
      </main>
    </Layout>
  );
}
