import React from "react";
import clsx from "clsx";
import Layout from "@theme/Layout";
import Link from "@docusaurus/Link";
import useDocusaurusContext from "@docusaurus/useDocusaurusContext";
import useBaseUrl from "@docusaurus/useBaseUrl";
import styles from "./styles.module.css";

const features = [
  {
    title: "Builds upon Anvil and Dagger",
    imageUrl: "img/syringe-solid.svg",
    description: (
      <>
        Docusaurus was designed from the ground up to be easily installed and
        used to get your website up and running quickly.
      </>
    ),
  },
  {
    title: "Android ViewModels",
    imageUrl: "img/power-off-solid.svg",
    description: (
      <>
        Docusaurus lets you focus on your docs, and we&apos;ll do the chores. Go
        ahead and move your docs into the <code>docs</code> directory.
      </>
    ),
  },
  {
    title: "Fragments",
    imageUrl: "img/coffee-solid.svg",
    description: (
      <>
        Extend or customize your website layout by reusing React. Docusaurus can
        be extended while reusing the same header and footer.
      </>
    ),
  },
  {
    title: "Compose",
    imageUrl: "img/undraw_docusaurus_react.svg",
    description: (
      <>
        Extend or customize your website layout by reusing React. Docusaurus can
        be extended while reusing the same header and footer.
      </>
    ),
  },
];

function Feature({ imageUrl, title, description }) {
  const imgUrl = useBaseUrl(imageUrl);
  return (
    <div className={clsx("col col--3", styles.feature)}>
      {imgUrl && (
        <div className="text--center">
          <img className={styles.featureImage} src={imgUrl} alt={title} />
        </div>
      )}
      <h3>{title}</h3>
      <p>{description}</p>
    </div>
  );
}

function Home() {
   const context = useDocusaurusContext();
   const {siteConfig = {}} = context;
   return (
      <Layout
         title={`${siteConfig.title}`}
         description="Description will go into a meta tag in <head />"
         >
         <header className={clsx('hero hero--primary', styles.heroBanner)}>
            <div className="container">
               <p className="hero__subtitle">{siteConfig.tagline}</p>
               <div className={styles.buttons}>
                  <Link
                     className={clsx(
                        'button button--outline button--secondary button--lg',
                        styles.gettingStartedButton,
                     )}
                     to={useBaseUrl('docs/quickstart')}>
                     Get Started
                  </Link>

                  &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;

                  <iframe
                     src="https://ghbtns.com/github-btn.html?user=rbusarow&repo=tangle&type=star&count=true&size=large"
                     frameBorder="0" scrolling="0" width="170" height="30" title="GitHub"/>

               </div>
            </div>
         </header>
         <main>
            <section className={styles.features}>
               <div className="container">
                  <div className="row">
                     <a href="https://search.maven.org/search?q=g:com.rickbusarow.tangle">
                        <img
                           src="https://img.shields.io/maven-central/v/com.rickbusarow.tangle/tangle-api.svg?label=release&style=for-the-badge&color=aa0055"
                           alt="version badge"/>
                     </a>

                     &nbsp;

                     <a href="https://github.com/rbusarow/tangle/blob/main/LICENSE">
                        <img
                           src="https://img.shields.io/badge/license-apache2.0-blue?style=for-the-badge"
                           alt="license"/>
                     </a>
                  </div>
                  <div className="row">
                     {features.map((props, idx) => (
                        <Feature key={idx} {...props} />
                     ))}
                  </div>
               </div>
            </section>
         </main>
      </Layout>
   );
}

export default Home;
