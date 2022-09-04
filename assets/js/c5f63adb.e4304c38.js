"use strict";(self.webpackChunktangle=self.webpackChunktangle||[]).push([[6970],{3905:function(e,n,t){t.d(n,{Zo:function(){return u},kt:function(){return g}});var r=t(7294);function i(e,n,t){return n in e?Object.defineProperty(e,n,{value:t,enumerable:!0,configurable:!0,writable:!0}):e[n]=t,e}function o(e,n){var t=Object.keys(e);if(Object.getOwnPropertySymbols){var r=Object.getOwnPropertySymbols(e);n&&(r=r.filter((function(n){return Object.getOwnPropertyDescriptor(e,n).enumerable}))),t.push.apply(t,r)}return t}function a(e){for(var n=1;n<arguments.length;n++){var t=null!=arguments[n]?arguments[n]:{};n%2?o(Object(t),!0).forEach((function(n){i(e,n,t[n])})):Object.getOwnPropertyDescriptors?Object.defineProperties(e,Object.getOwnPropertyDescriptors(t)):o(Object(t)).forEach((function(n){Object.defineProperty(e,n,Object.getOwnPropertyDescriptor(t,n))}))}return e}function l(e,n){if(null==e)return{};var t,r,i=function(e,n){if(null==e)return{};var t,r,i={},o=Object.keys(e);for(r=0;r<o.length;r++)t=o[r],n.indexOf(t)>=0||(i[t]=e[t]);return i}(e,n);if(Object.getOwnPropertySymbols){var o=Object.getOwnPropertySymbols(e);for(r=0;r<o.length;r++)t=o[r],n.indexOf(t)>=0||Object.prototype.propertyIsEnumerable.call(e,t)&&(i[t]=e[t])}return i}var p=r.createContext({}),c=function(e){var n=r.useContext(p),t=n;return e&&(t="function"==typeof e?e(n):a(a({},n),e)),t},u=function(e){var n=c(e.components);return r.createElement(p.Provider,{value:n},e.children)},d={inlineCode:"code",wrapper:function(e){var n=e.children;return r.createElement(r.Fragment,{},n)}},s=r.forwardRef((function(e,n){var t=e.components,i=e.mdxType,o=e.originalType,p=e.parentName,u=l(e,["components","mdxType","originalType","parentName"]),s=c(t),g=i,m=s["".concat(p,".").concat(g)]||s[g]||d[g]||o;return t?r.createElement(m,a(a({ref:n},u),{},{components:t})):r.createElement(m,a({ref:n},u))}));function g(e,n){var t=arguments,i=n&&n.mdxType;if("string"==typeof e||i){var o=t.length,a=new Array(o);a[0]=s;var l={};for(var p in n)hasOwnProperty.call(n,p)&&(l[p]=n[p]);l.originalType=e,l.mdxType="string"==typeof e?e:i,a[1]=l;for(var c=2;c<o;c++)a[c]=t[c];return r.createElement.apply(null,a)}return r.createElement.apply(null,t)}s.displayName="MDXCreateElement"},8530:function(e,n,t){t.r(n),t.d(n,{assets:function(){return u},contentTitle:function(){return p},default:function(){return g},frontMatter:function(){return l},metadata:function(){return c},toc:function(){return d}});var r=t(3117),i=t(102),o=(t(7294),t(3905)),a=(t(8209),["components"]),l={id:"configuration",sidebar_label:"Configuration"},p=void 0,c={unversionedId:"configuration",id:"version-0.11.1/configuration",title:"configuration",description:"The TangleGraph holder needs to be initialized with an application-scoped Dagger Component in",source:"@site/versioned_docs/version-0.11.1/configuration.mdx",sourceDirName:".",slug:"/configuration",permalink:"/Tangle/docs/0.11.1/configuration",draft:!1,editUrl:"https://github.com/rbusarow/Tangle/blob/main/website/versioned_docs/version-0.11.1/configuration.mdx",tags:[],version:"0.11.1",frontMatter:{id:"configuration",sidebar_label:"Configuration"},sidebar:"version-0.11.1/Docs",previous:{title:"Quick Start",permalink:"/Tangle/docs/0.11.1/"},next:{title:"ViewModels",permalink:"/Tangle/docs/0.11.1/viewModels/"}},u={},d=[{value:"Gradle plugin",id:"gradle-plugin",level:3},{value:"Explicit dependencies",id:"explicit-dependencies",level:3}],s={toc:d};function g(e){var n=e.components,t=(0,i.Z)(e,a);return(0,o.kt)("wrapper",(0,r.Z)({},s,t,{components:n,mdxType:"MDXLayout"}),(0,o.kt)("p",null,"The ",(0,o.kt)("inlineCode",{parentName:"p"},"TangleGraph")," holder needs to be initialized with an application-scoped Dagger Component in\norder to complete the graph."),(0,o.kt)("pre",null,(0,o.kt)("code",{parentName:"pre",className:"language-kotlin"},"import android.app.Application\nimport tangle.inject.TangleGraph\n\nclass MyApplication : Application() {\n\n  override fun onCreate() {\n    super.onCreate()\n\n    val myAppComponent = DaggerAppComponent.factory()\n      .create(this)\n\n    TangleGraph.init(myAppComponent)\n  }\n}\n")),(0,o.kt)("h3",{id:"gradle-plugin"},"Gradle plugin"),(0,o.kt)("p",null,"The simple way to apply Tangle is to just apply the gradle plugin. It will automatically add the\ndependencies and perform some basic validation of your module's configuration."),(0,o.kt)("pre",null,(0,o.kt)("code",{parentName:"pre",className:"language-kotlin"},"// settings.gradle.kts\n\npluginManagement {\n  repositories {\n    gradlePluginPortal()\n  }\n}\n")),(0,o.kt)("pre",null,(0,o.kt)("code",{parentName:"pre",className:"language-kotlin"},'// top-level build.gradle.kts\n\nplugins {\n  id("com.rickbusarow.tangle") version "0.11.1"\n}\n')),(0,o.kt)("pre",null,(0,o.kt)("code",{parentName:"pre",className:"language-kotlin"},'// any Android module\'s build.gradle.kts\n\nplugins {\n  id("android-library") // or application, etc.\n  kotlin("android")\n  id("com.squareup.anvil")\n  id("com.rickbusarow.tangle")\n}\n\n// optional\ntangle {\n  composeEnabled.set(true) // default is false\n  fragmentsEnabled.set(true) // default is true\n  viewModelsEnabled.set(true) // default is true\n}\n')),(0,o.kt)("h3",{id:"explicit-dependencies"},"Explicit dependencies"),(0,o.kt)("p",null,"You can also just add dependencies yourself, without applying the plugin."),(0,o.kt)("p",null,"Note that Tangle is specifically for Android and has Android-specific dependencies,\nso it should only be added to Android modules."),(0,o.kt)("pre",null,(0,o.kt)("code",{parentName:"pre",className:"language-kotlin"},'// any Android module\'s build.gradle.kts\n\nplugins {\n  id("android-library") // or application, etc.\n  kotlin("android")\n  id("com.squareup.anvil")\n}\n\ndependencies {\n\n  // Fragments\n  api("com.rickbusarow.tangle:tangle-fragment-api:0.11.1")\n  anvil("com.rickbusarow.tangle:tangle-fragment-compiler:0.11.1")\n\n  // ViewModels\n  api("com.rickbusarow.tangle:tangle-viewmodel-api:0.11.1")\n  anvil("com.rickbusarow.tangle:tangle-viewmodel-compiler:0.11.1")\n\n  // optional Compose support\n  implementation("com.rickbusarow.tangle:tangle-viewmodel-compose:0.11.1")\n}\n')))}g.isMDXComponent=!0},8209:function(e,n,t){t(7294)}}]);