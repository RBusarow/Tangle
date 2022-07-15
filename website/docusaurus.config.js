module.exports = {
  title: "Tangle",
  tagline: "Life's too short to wait for Kapt.",
  url: "https://rbusarow.github.io/",
  baseUrl: "/Tangle/",
  onBrokenLinks: 'warn',
  onBrokenMarkdownLinks: "warn",
  favicon: "img/favicon.ico",
  organizationName: "rbusarow", // Usually your GitHub org/user name.
  projectName: "Tangle", // Usually your repo name.
  themeConfig: {
    docs: {
      sidebar: {
        hideable: true,
      }
    },
    colorMode: {
      defaultMode: "light",
      disableSwitch: false,
      respectPrefersColorScheme: true,
    },
    announcementBar: {
      id: "supportus",
      content:
        '⭐️ If you like Tangle, give it a star on <a target="_blank" rel="noopener noreferrer" href="https://github.com/rbusarow/Tangle/">GitHub</a>! ⭐️',
    },
    navbar: {
      title: "Tangle",
      logo: {
        alt: 'Tangle Logo',
        src: 'img/logo.png',
      },
      items: [
        {
          label: "Docs",
          type: "doc",
          docId: "configuration",
          position: "left",
        },
        {
          to: 'CHANGELOG',
          label: 'ChangeLog',
          position: 'left'
        },
        {
          type: "docsVersionDropdown",
          position: "left",
          dropdownActiveClassDisabled: true,
          dropdownItemsAfter: [
            {
              to: "/changelog",
              label: "CHANGELOG",
            },
          ],
        },
        {
          label: "Api",
          href: 'pathname:///api/index.html',
          position: "left",
        },
        {
          label: "Twitter",
          href: "https://twitter.com/rbusarow",
          position: "right",
        },
        {
          label: "GitHub",
          href: "https://github.com/rbusarow/Tangle/",
          position: "right",
        },
      ],
    },
    footer: {
      copyright: `Copyright © ${new Date().getFullYear()} Rick Busarow, Built with Docusaurus.`,
    },
    prism: {
      theme: require("prism-react-renderer/themes/github"),
      darkTheme: require("prism-react-renderer/themes/dracula"),
      additionalLanguages: ["kotlin", "groovy", "java"],
    },
  },
  plugins: [
    [
      '@docusaurus/plugin-client-redirects',
      {
        toExtensions: ['html'],
      },
    ],
  ],
  presets: [
    [
      "@docusaurus/preset-classic",
      {
        docs: {
          /**
           * Path to data on filesystem relative to site dir.
           */
          path: 'docs',
          /**
           * Base url to edit your site.
           * Docusaurus will compute the final editUrl with "editUrl + relativeDocPath"
           */
          editUrl: "https://github.com/rbusarow/Tangle/blob/main/website",
          /**
           * Useful if you commit localized files to git.
           * When Markdown files are localized, the edit url will target the localized file,
           * instead of the original unlocalized file.
           * Note: this option is ignored when editUrl is a function
           */
          editLocalizedFiles: false,
          /**
           * Useful if you don't want users to submit doc pull-requests to older versions.
           * When docs are versioned, the edit url will link to the doc
           * in current version, instead of the versioned doc.
           * Note: this option is ignored when editUrl is a function
           */
          editCurrentVersion: true,
          /**
           * URL route for the docs section of your site.
           * *DO NOT* include a trailing slash.
           * INFO: It is possible to set just `/` for shipping docs without base path.
           */
          routeBasePath: 'docs',
          include: ['**/*.md', '**/*.mdx'], // Extensions to include.
          /**
           * Path to sidebar configuration for showing a list of markdown pages.
           */
          sidebarPath: require.resolve("./sidebars.js"),
          /**
           * Theme components used by the docs pages
           */
          docLayoutComponent: '@theme/DocPage',
          docItemComponent: '@theme/DocItem',
          /**
           * Remark and Rehype plugins passed to MDX
           */
          remarkPlugins: [
            /* require('remark-math') */
          ],
          rehypePlugins: [],
          /**
           * Custom Remark and Rehype plugins passed to MDX before
           * the default Docusaurus Remark and Rehype plugins.
           */
          beforeDefaultRemarkPlugins: [],
          beforeDefaultRehypePlugins: [],
          /**
           * Whether to display the author who last updated the doc.
           */
          showLastUpdateAuthor: false,
          /**
           * Whether to display the last date the doc was updated.
           */
          showLastUpdateTime: false,
          /**
           * By default, versioning is enabled on versioned sites.
           * This is a way to explicitly disable the versioning feature.
           * This will only include the "current" version (the `/docs` directory)
           */
          disableVersioning: false,
          /**
           * Include the "current" version of your docs (the `/docs` directory)
           * Tip: turn it off if the current version is a work-in-progress, not ready to be published
           */
          includeCurrentVersion: true,
          /**
           * The last version is the one we navigate to in priority on versioned sites
           * It is the one displayed by default in docs navbar items
           * By default, the last version is the first one to appear in versions.json
           * By default, the last version is at the "root" (docs have path=/docs/myDoc)
           * Note: it is possible to configure the path and label of the last version
           * Tip: using lastVersion: 'current' make sense in many cases
           */
//          lastVersion: undefined,
          /**
           * The docusaurus versioning defaults don't make sense for all projects
           * This gives the ability customize the properties of each version independantly
           * - label: the label of the version
           * - path: the route path of the version
           * - banner: the banner to show at the top of a doc of that version: "none" | "unreleased" | "unmaintained"
           */
          versions: {
            /*
            Example configuration:
            current: {
              label: 'Android SDK v2.0.0 (WIP)',
              path: 'android-2.0.0',
              banner: 'none',
            },
            '1.0.0': {
              label: 'Android SDK v1.0.0',
              path: 'android-1.0.0',
              banner: 'unmaintained',
            },
            */
          },
          /**
           * Sometimes you only want to include a subset of all available versions.
           * Tip: limit to 2 or 3 versions to improve startup and build time in dev and deploy previews
           */
          onlyIncludeVersions: undefined, // ex: ["current", "1.0.0", "2.0.0"]
        },
        blog: {
          showReadingTime: true,
          editUrl: 'https://github.com/RBusarow/Tangle',
        },
        theme: {
          customCss: require.resolve("./src/css/custom.css"),
        },
      },
    ],
  ],
};
