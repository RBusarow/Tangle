import React from "react";
import clsx from "clsx";
import Layout from "@theme/Layout";
import CodeBlock from '@theme/CodeBlock';
import Link from "@docusaurus/Link";
import useDocusaurusContext from "@docusaurus/useDocusaurusContext";
import useBaseUrl from "@docusaurus/useBaseUrl";
import styles from "./styles.module.css";

const features = [
  {
    title: "Fragments",
    code: (
      `@ContributesFragment(AppScope::class)
class MyFragment @FragmentInject constructor(
  val repository: MyRepository
) : MidFragment() {

  val name: String by arg("name")

  @FragmentInjectFactory
  interface Factory {
    fun create(
      @TangleParam("name") name: String
    ): MyFragment
  }
}`
    ),
    description: (
      <>
        Use constructor injection in <code>Fragment</code>s, with optional AssistedInject-like
        factories for type-safe <code>Bundle</code> arguments.  Bindings are created automatically.
      </>
    ),
    dest: "docs/next/fragments/fragments"
  },
  {
    title: "ViewModels",
    code: (
      `class MyViewModel @VMInject constructor(
  val repository: MyRepository,
  @TangleParam("userId")
  val userId: String
) : ViewModel()

class MyFragment : Fragment() {
  val viewModel: MyViewModel by tangleViewModel()
}`
    ),
    description: (
      <>
        Inject <code>ViewModel</code>s, including scoped <code>SavedStateHandle</code> arguments.
        Use the <code>TangleParam</code> annotation to automatically extract
        navigation/<code>Bundle</code> arguments and inject them explicitly.
      </>
    ),
    dest: "docs/viewModels/viewModels"
  },
  {
    title: "Compose",
    code: (
      `@Composable
fun MyComposable(
  navController: NavController,
  viewModel: MyViewModel = tangleViewModel()
) {
  // ...
}`
    ),
    description: (
      <>
        Use the <code>tangleViewModel</code> composable function to inject scoped
        <code>ViewModel</code>s.
      </>
    ),
    dest: "docs/next/viewModels/compose"
  },
];

function Feature({imageUrl, title, description, code, dest}) {
  const imgUrl = useBaseUrl(imageUrl);
  return (
    <div className={clsx("col col--4", styles.feature)}>
      {imgUrl && (
        <div className="text--center">
          <img className={styles.featureImage} src={imgUrl} alt={title}/>
        </div>
      )}
      <h1 align="center">{title}</h1>
      <div>
        <CodeBlock className="language-kotlin">{code}</CodeBlock>
      </div>
      <p>{description}</p>
      <a href={dest}>Read more</a>
    </div>
  );
}

function Home() {
  const context = useDocusaurusContext();
  const {siteConfig = {}} = context;
  return (
    <Layout
      title={`${siteConfig.title}`}
      description="Android dependency injection using Anvil and Dagger"
    >
      <header className={clsx('hero hero--primary', styles.heroBanner)}>
        <div className="container">
          <p className={clsx(styles.heroSlogan)}>
            <strong>Tangle</strong> generates Android bindings using Anvil.
          </p>
          <div className={styles.buttons}>
            <Link
              className={clsx(
                'button button--outline button--secondary button--lg',
                styles.gettingStartedButton,
              )}
              to={useBaseUrl('docs')}>
              Get Started
            </Link>

            &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;

            {/*            <iframe
              src="https://ghbtns.com/github-btn.html?user=rbusarow&repo=tangle&type=star&count=true&size=large"
              frameBorder="0" scrolling="0" width="170" height="30" title="GitHub"/>*/}

          </div>
        </div>
      </header>
      <main>
        <div className={styles.badges}>
          <div className="container">
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
        </div>
      </main>
      <main>
        <section className={styles.features}>
          <div className="container">
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
