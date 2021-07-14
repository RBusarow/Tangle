module.exports = {
  title: "Tangle",
  tagline: "Android dependency injection using Anvil",
  url: "https://rbusarow.github.io/",
  baseUrl: "/Tangle/",
  onBrokenLinks: "throw",
  onBrokenMarkdownLinks: "warn",
  favicon: "img/favicon.ico",
  organizationName: "rbusarow", // Usually your GitHub org/user name.
  projectName: "Tangle", // Usually your repo name.
  themeConfig: {
    hideableSidebar: true,
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
      //      logo: {
      //        alt: 'Tangle Logo',
      //        src: 'img/logo.svg',
      //      },
      items: [
        {
          type: "doc",
          docId: "quickstart",
          label: "Basics",
          position: "left",
        },
        {
          type: "docsVersionDropdown",
          position: "right",
          dropdownActiveClassDisabled: true,
          dropdownItemsAfter: [
            {
              // to: "/versions",
              // label: "All versions",
            },
          ],
        },
        {
          label: "Twitter",
          href: "https://twitter.com/rbusarow",
          position: "right",
        },
        {
          href: "https://github.com/rbusarow/Tangle/",
          label: "GitHub",
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
      additionalLanguages: ["kotlin", "groovy"],
    },
  },
  presets: [
    [
      "@docusaurus/preset-classic",
      {
        docs: {
          sidebarPath: require.resolve("./sidebars.js"),
          // Please change this to your repo.
          editUrl: "https://github.com/rbusarow/Tangle/",
        },
        blog: {
          showReadingTime: true,
          // Please change this to your repo.
          editUrl: "https://github.com/rbusarow/Tangle/",
        },
        theme: {
          customCss: require.resolve("./src/css/custom.css"),
        },
      },
    ],
  ],
};
