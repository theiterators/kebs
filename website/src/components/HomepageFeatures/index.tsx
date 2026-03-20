import type {ReactNode} from 'react';
import clsx from 'clsx';
import Heading from '@theme/Heading';
import styles from './styles.module.css';

type FeatureItem = {
  title: string;
  description: ReactNode;
};

const FeatureList: FeatureItem[] = [
  {
    title: 'Zero boilerplate',
    description: (
      <>
        Stop writing <code>MappedColumnType.base</code>, <code>deriveEncoder</code>,
        and <code>Unmarshaller.strict</code> for every wrapper type. Kebs derives
        all of that at compile time from a single typeclass.
      </>
    ),
  },
  {
    title: 'Use strong types everywhere',
    description: (
      <>
        Value classes, opaque types, tagged types, and enums just work with Slick,
        Doobie, Circe, Spray JSON, Play JSON, Akka HTTP, Pekko HTTP, http4s,
        ScalaCheck, PureConfig, and more.
      </>
    ),
  },
  {
    title: 'Scala 2 & 3',
    description: (
      <>
        Full cross-build support for Scala 2.13 and Scala 3, including
        JVM, Scala.js, and Scala Native where applicable.
      </>
    ),
  },
];

function Feature({title, description}: FeatureItem) {
  return (
    <div className={clsx('col col--4')}>
      <div className="text--center padding-horiz--md" style={{paddingTop: '2rem'}}>
        <Heading as="h3">{title}</Heading>
        <p>{description}</p>
      </div>
    </div>
  );
}

export default function HomepageFeatures(): ReactNode {
  return (
    <section className={styles.features}>
      <div className="container">
        <div className="row">
          {FeatureList.map((props, idx) => (
            <Feature key={idx} {...props} />
          ))}
        </div>
      </div>
    </section>
  );
}
