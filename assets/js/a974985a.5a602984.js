"use strict";(self.webpackChunktangle=self.webpackChunktangle||[]).push([[3331],{3905:function(e,n,t){t.d(n,{Zo:function(){return c},kt:function(){return d}});var a=t(7294);function r(e,n,t){return n in e?Object.defineProperty(e,n,{value:t,enumerable:!0,configurable:!0,writable:!0}):e[n]=t,e}function o(e,n){var t=Object.keys(e);if(Object.getOwnPropertySymbols){var a=Object.getOwnPropertySymbols(e);n&&(a=a.filter((function(n){return Object.getOwnPropertyDescriptor(e,n).enumerable}))),t.push.apply(t,a)}return t}function i(e){for(var n=1;n<arguments.length;n++){var t=null!=arguments[n]?arguments[n]:{};n%2?o(Object(t),!0).forEach((function(n){r(e,n,t[n])})):Object.getOwnPropertyDescriptors?Object.defineProperties(e,Object.getOwnPropertyDescriptors(t)):o(Object(t)).forEach((function(n){Object.defineProperty(e,n,Object.getOwnPropertyDescriptor(t,n))}))}return e}function l(e,n){if(null==e)return{};var t,a,r=function(e,n){if(null==e)return{};var t,a,r={},o=Object.keys(e);for(a=0;a<o.length;a++)t=o[a],n.indexOf(t)>=0||(r[t]=e[t]);return r}(e,n);if(Object.getOwnPropertySymbols){var o=Object.getOwnPropertySymbols(e);for(a=0;a<o.length;a++)t=o[a],n.indexOf(t)>=0||Object.prototype.propertyIsEnumerable.call(e,t)&&(r[t]=e[t])}return r}var s=a.createContext({}),p=function(e){var n=a.useContext(s),t=n;return e&&(t="function"==typeof e?e(n):i(i({},n),e)),t},c=function(e){var n=p(e.components);return a.createElement(s.Provider,{value:n},e.children)},u={inlineCode:"code",wrapper:function(e){var n=e.children;return a.createElement(a.Fragment,{},n)}},m=a.forwardRef((function(e,n){var t=e.components,r=e.mdxType,o=e.originalType,s=e.parentName,c=l(e,["components","mdxType","originalType","parentName"]),m=p(t),d=r,g=m["".concat(s,".").concat(d)]||m[d]||u[d]||o;return t?a.createElement(g,i(i({ref:n},c),{},{components:t})):a.createElement(g,i({ref:n},c))}));function d(e,n){var t=arguments,r=n&&n.mdxType;if("string"==typeof e||r){var o=t.length,i=new Array(o);i[0]=m;var l={};for(var s in n)hasOwnProperty.call(n,s)&&(l[s]=n[s]);l.originalType=e,l.mdxType="string"==typeof e?e:r,i[1]=l;for(var p=2;p<o;p++)i[p]=t[p];return a.createElement.apply(null,i)}return a.createElement.apply(null,t)}m.displayName="MDXCreateElement"},5162:function(e,n,t){t.d(n,{Z:function(){return i}});var a=t(7294),r=t(6010),o="tabItem_Ymn6";function i(e){var n=e.children,t=e.hidden,i=e.className;return a.createElement("div",{role:"tabpanel",className:(0,r.Z)(o,i),hidden:t},n)}},5488:function(e,n,t){t.d(n,{Z:function(){return d}});var a=t(3117),r=t(7294),o=t(6010),i=t(2389),l=t(7392),s=t(7094),p=t(2466),c="tabList__CuJ",u="tabItem_LNqP";function m(e){var n,t,i=e.lazy,m=e.block,d=e.defaultValue,g=e.values,f=e.groupId,v=e.className,y=r.Children.map(e.children,(function(e){if((0,r.isValidElement)(e)&&"value"in e.props)return e;throw new Error("Docusaurus error: Bad <Tabs> child <"+("string"==typeof e.type?e.type:e.type.name)+'>: all children of the <Tabs> component should be <TabItem>, and every <TabItem> should have a unique "value" prop.')})),h=null!=g?g:y.map((function(e){var n=e.props;return{value:n.value,label:n.label,attributes:n.attributes}})),b=(0,l.l)(h,(function(e,n){return e.value===n.value}));if(b.length>0)throw new Error('Docusaurus error: Duplicate values "'+b.map((function(e){return e.value})).join(", ")+'" found in <Tabs>. Every value needs to be unique.');var k=null===d?d:null!=(n=null!=d?d:null==(t=y.find((function(e){return e.props.default})))?void 0:t.props.value)?n:y[0].props.value;if(null!==k&&!h.some((function(e){return e.value===k})))throw new Error('Docusaurus error: The <Tabs> has a defaultValue "'+k+'" but none of its children has the corresponding value. Available values are: '+h.map((function(e){return e.value})).join(", ")+". If you intend to show no default tab, use defaultValue={null} instead.");var C=(0,s.U)(),w=C.tabGroupChoices,N=C.setTabGroupChoices,F=(0,r.useState)(k),T=F[0],A=F[1],j=[],M=(0,p.o5)().blockElementScrollPositionUntilNextRender;if(null!=f){var O=w[f];null!=O&&O!==T&&h.some((function(e){return e.value===O}))&&A(O)}var x=function(e){var n=e.currentTarget,t=j.indexOf(n),a=h[t].value;a!==T&&(M(n),A(a),null!=f&&N(f,String(a)))},I=function(e){var n,t=null;switch(e.key){case"ArrowRight":var a,r=j.indexOf(e.currentTarget)+1;t=null!=(a=j[r])?a:j[0];break;case"ArrowLeft":var o,i=j.indexOf(e.currentTarget)-1;t=null!=(o=j[i])?o:j[j.length-1]}null==(n=t)||n.focus()};return r.createElement("div",{className:(0,o.Z)("tabs-container",c)},r.createElement("ul",{role:"tablist","aria-orientation":"horizontal",className:(0,o.Z)("tabs",{"tabs--block":m},v)},h.map((function(e){var n=e.value,t=e.label,i=e.attributes;return r.createElement("li",(0,a.Z)({role:"tab",tabIndex:T===n?0:-1,"aria-selected":T===n,key:n,ref:function(e){return j.push(e)},onKeyDown:I,onFocus:x,onClick:x},i,{className:(0,o.Z)("tabs__item",u,null==i?void 0:i.className,{"tabs__item--active":T===n})}),null!=t?t:n)}))),i?(0,r.cloneElement)(y.filter((function(e){return e.props.value===T}))[0],{className:"margin-top--md"}):r.createElement("div",{className:"margin-top--md"},y.map((function(e,n){return(0,r.cloneElement)(e,{key:n,hidden:e.props.value!==T})}))))}function d(e){var n=(0,i.Z)();return r.createElement(m,(0,a.Z)({key:String(n)},e))}},4961:function(e,n,t){t.r(n),t.d(n,{assets:function(){return m},contentTitle:function(){return c},default:function(){return f},frontMatter:function(){return p},metadata:function(){return u},toc:function(){return d}});var a=t(3117),r=t(102),o=(t(7294),t(3905)),i=(t(8209),t(5488)),l=t(5162),s=["components"],p={title:"Fragments",sidebar_label:"Fragments"},c=void 0,u={unversionedId:"fragments/fragments",id:"version-0.13.0/fragments/fragments",title:"Fragments",description:"Tangle performs Fragment injection using constructor injection, just like the rest of a",source:"@site/versioned_docs/version-0.13.0/fragments/fragments.mdx",sourceDirName:"fragments",slug:"/fragments/",permalink:"/Tangle/docs/0.13.0/fragments/",draft:!1,editUrl:"https://github.com/rbusarow/Tangle/blob/main/website/versioned_docs/version-0.13.0/fragments/fragments.mdx",tags:[],version:"0.13.0",frontMatter:{title:"Fragments",sidebar_label:"Fragments"},sidebar:"version-0.13.0/Docs",previous:{title:"Compose",permalink:"/Tangle/docs/0.13.0/viewModels/compose"},next:{title:"Bundle Injection",permalink:"/Tangle/docs/0.13.0/fragments/bundles"}},m={},d=[{value:"1. Set up Gradle",id:"1-set-up-gradle",level:3},{value:"2. Use Anvil for the app-scoped Component",id:"2-use-anvil-for-the-app-scoped-component",level:3},{value:"3. Set the custom FragmentFactory",id:"3-set-the-custom-fragmentfactory",level:3},{value:"4. Contribute Fragments to the graph",id:"4-contribute-fragments-to-the-graph",level:3},{value:"5. Create Fragments with the FragmentManager",id:"5-create-fragments-with-the-fragmentmanager",level:3},{value:"Next step -- &quot;Assisted&quot; Bundle injection",id:"next-step----assisted-bundle-injection",level:3}],g={toc:d};function f(e){var n=e.components,t=(0,r.Z)(e,s);return(0,o.kt)("wrapper",(0,a.Z)({},g,t,{components:n,mdxType:"MDXLayout"}),(0,o.kt)("p",null,"Tangle performs Fragment injection using ",(0,o.kt)("strong",{parentName:"p"},"constructor")," injection, just like the rest of a\ntypical Dagger/Anvil graph. There are several steps to configuration,\nwith two different paths at the end."),(0,o.kt)("h3",{id:"1-set-up-gradle"},"1. Set up Gradle"),(0,o.kt)(i.Z,{groupId:"language",defaultValue:"Kotlin Plugin",values:[{label:"Kotlin Plugin",value:"Kotlin Plugin"},{label:"Groovy Plugin",value:"Groovy Plugin"},{label:"Kotlin dependencies block",value:"Kotlin dependencies block"},{label:"Groovy dependencies block",value:"Groovy dependencies block"}],mdxType:"Tabs"},(0,o.kt)(l.Z,{value:"Kotlin Plugin",mdxType:"TabItem"},(0,o.kt)("pre",null,(0,o.kt)("code",{parentName:"pre",className:"language-kotlin"},'// any Android module\'s build.gradle.kts\nplugins {\n  id("android-library") // or application, etc.\n  kotlin("android")\n  id("com.squareup.anvil")\n  id("com.rickbusarow.tangle")\n}\n\ntangle {\n  fragmentsEnabled = true // default is true\n}\n'))),(0,o.kt)(l.Z,{value:"Groovy Plugin",mdxType:"TabItem"},(0,o.kt)("pre",null,(0,o.kt)("code",{parentName:"pre",className:"language-groovy"},"// any Android module's build.gradle\nplugins {\n  id 'android-library' // or application, etc.\n  kotlin(\"android\")\n  id 'com.squareup.anvil'\n  id 'com.rickbusarow.tangle'\n}\n\n// optional\ntangle {\n  fragmentsEnabled true // default is true\n}\n"))),(0,o.kt)(l.Z,{value:"Kotlin dependencies block",mdxType:"TabItem"},(0,o.kt)("pre",null,(0,o.kt)("code",{parentName:"pre",className:"language-kotlin"},'// any Android module\'s build.gradle.kts\nplugins {\n  id("android-library") // or application, etc.\n  kotlin("android")\n  id("com.squareup.anvil")\n}\n\ndependencies {\n  api("com.rickbusarow.tangle:tangle-fragment-api:0.13.0")\n  anvil("com.rickbusarow.tangle:tangle-fragment-compiler:0.13.0")\n}\n'))),(0,o.kt)(l.Z,{value:"Groovy dependencies block",mdxType:"TabItem"},(0,o.kt)("pre",null,(0,o.kt)("code",{parentName:"pre",className:"language-groovy"},"// any Android module's build.gradle\nplugins {\n  id 'android-library' // or application, etc.\n  kotlin(\"android\")\n  id 'com.squareup.anvil'\n}\n\ndependencies {\n  api 'com.rickbusarow.tangle:tangle-fragment-api:0.13.0'\n  anvil 'com.rickbusarow.tangle:tangle-fragment-compiler:0.13.0'\n}\n")))),(0,o.kt)("h3",{id:"2-use-anvil-for-the-app-scoped-component"},"2. Use Anvil for the app-scoped Component"),(0,o.kt)("p",null,"Tangle uses the ",(0,o.kt)("a",{parentName:"p",href:"https://github.com/square/anvil#scopes"},"MergeComponent")," annotation from ",(0,o.kt)("a",{parentName:"p",href:"https://github.com/square/anvil"},"Anvil")," to identify the application's Component\nand add its own dependencies to the Dagger graph."),(0,o.kt)("p",null,"For anyone already using Anvil, there's probably nothing to be done here."),(0,o.kt)("p",null,"Anvil uses ",(0,o.kt)("inlineCode",{parentName:"p"},"KClass<T>")," references to define scopes.  A common pattern is to define an ",(0,o.kt)("inlineCode",{parentName:"p"},"AppScope"),"\nclass specifically for this purpose in a low-level shared (Gradle) module:"),(0,o.kt)("pre",null,(0,o.kt)("code",{parentName:"pre",className:"language-kotlin"},"package myApp.core.anvil\n\nabstract class AppScope private constructor()\n")),(0,o.kt)("p",null,"Then at your application Component, use ",(0,o.kt)("inlineCode",{parentName:"p"},"MergeComponent")," with this scope:"),(0,o.kt)("pre",null,(0,o.kt)("code",{parentName:"pre",className:"language-kotlin"},"package myApp.app\n\nimport com.squareup.anvil.annotations.MergeComponent\nimport myApp.core.anvil.AppScope\n\n@MergeComponent(AppScope::class)\ninterface MyAppComponent\n")),(0,o.kt)("h3",{id:"3-set-the-custom-fragmentfactory"},"3. Set the custom FragmentFactory"),(0,o.kt)("p",null,"New Fragment instances are provided by ",(0,o.kt)("a",{parentName:"p",href:"https://rbusarow.github.io/Tangle/api/tangle-fragment-api/tangle.fragment/-tangle-fragment-factory"},"TangleFragmentFactory"),".  This custom factory\nis automatically added to any Dagger graph for any ",(0,o.kt)("inlineCode",{parentName:"p"},"@MergeComponent"),"-annotated Component."),(0,o.kt)("admonition",{type:"note"},(0,o.kt)("p",{parentName:"admonition"},"If a requested Fragment is not contained within Tangle's bindings, ",(0,o.kt)("inlineCode",{parentName:"p"},"TangleFragmentFactory")," will\nfall back to using the default initialization with an empty constructor.  This means that large\nprojects can be migrated gradually."),(0,o.kt)("p",{parentName:"admonition"},"If a project was already doing Fragment constructor injection using vanilla Dagger, they were\nprobably already binding into a\n",(0,o.kt)("inlineCode",{parentName:"p"},"Map<Class<out Fragment>, Provider<@JvmSuppressWildcards Fragment>>"),". That is what Tangle uses,\nso existing multi-bound graphs will often support gradual migrations as well.")),(0,o.kt)("p",null,"Any ",(0,o.kt)("a",{parentName:"p",href:"https://developer.android.com/reference/kotlin/androidx/fragment/app/FragmentManager"},"FragmentManager")," used within the application will need to have its ",(0,o.kt)("inlineCode",{parentName:"p"},"fragmentFactory"),"\nproperty set to a ",(0,o.kt)("inlineCode",{parentName:"p"},"TangleFragmentFactory")," instance.  This means the\n",(0,o.kt)("inlineCode",{parentName:"p"},"AppCompatActivity.supportFragmentManager"),", and possibly ",(0,o.kt)("inlineCode",{parentName:"p"},"Fragment.childFragmentManager")," as well.\nThis is easiest if your application uses an abstract base class."),(0,o.kt)("pre",null,(0,o.kt)("code",{parentName:"pre",className:"language-kotlin"},"abstract class BaseActivity : AppCompatActivity() {\n\n  override fun onCreate(savedInstanceState: Bundle?) {\n    supportFragmentManager.fragmentFactory = Components.get<BaseActivityComponent>()\n                                              .tangleFragmentFactory\n    super.onCreate(savedInstanceState)\n  }\n}\n")),(0,o.kt)("details",null,(0,o.kt)("summary",null,"Click to see how ",(0,o.kt)("code",null,"Components")," works"),(0,o.kt)("p",null,"In a core module, define this singleton."),(0,o.kt)("pre",null,(0,o.kt)("code",{parentName:"pre",className:"language-kotlin"},"package myApp.core.anvil\n\nobject Components {\n  @PublishedApi\n  internal val _components = mutableSetOf<Any>()\n\n  /** Set by Application class after creating app component */\n  fun add(component: Any) {\n    _components.add(component)\n  }\n\n  inline fun <reified T> get(): T = _components\n    .filterIsInstance<T>()\n    .single()\n}\n")),(0,o.kt)("p",null,"In your application, save off the AppComponent instance."),(0,o.kt)("pre",null,(0,o.kt)("code",{parentName:"pre",className:"language-kotlin"},"package myApp.core.anvil\n\nclass MyApplication : Application() {\n\n  override fun onCreate() {\n    val component = DaggerMyAppComponent.factory()\n                      .create(/*...*/)\n\n    Components.add(component)\n\n    super.onCreate()\n  }\n}\n")),(0,o.kt)("p",null,'Anywhere you need to, create a "component" interface with whatever dependency properties you need,\nand annotate it with ',(0,o.kt)("inlineCode",{parentName:"p"},"@ContributesTo(<some scope definition>)"),".  Your AppComponent will\nautomatically implement this interface,\nwhich means that an implementation of it will be stored in ",(0,o.kt)("inlineCode",{parentName:"p"},"Components"),"."),(0,o.kt)("pre",null,(0,o.kt)("code",{parentName:"pre",className:"language-kotlin"},"import com.squareup.anvil.annotations.ContributesTo\n\n@ContributesTo(AppScope::class)\ninterface BaseActivityComponent {\n  val tangleFragmentFactory: TangleFragmentFactory\n}\n")),(0,o.kt)("p",null,"Now, ",(0,o.kt)("inlineCode",{parentName:"p"},"Components.get<BaseActivityComponent>()")," will return ",(0,o.kt)("inlineCode",{parentName:"p"},"MyAppComponent"),"\nsafely cast to ",(0,o.kt)("inlineCode",{parentName:"p"},"BaseActivityComponent"),", and you can access its properties."),(0,o.kt)("pre",null,(0,o.kt)("code",{parentName:"pre",className:"language-kotlin"},"val fragmentFactory = Components.get<BaseActivityComponent>()\n                                .tangleFragmentFactory\n"))),(0,o.kt)("h3",{id:"4-contribute-fragments-to-the-graph"},"4. Contribute Fragments to the graph"),(0,o.kt)("p",null,"Finally, add the Fragments themselves.  For basic injection, the only difference\nfrom any other constructor-injected class is that you must add the ",(0,o.kt)("a",{parentName:"p",href:"https://rbusarow.github.io/Tangle/api/tangle-fragment-api/tangle.fragment/-contributes-fragment/index.html"},"ContributesFragment")," annotation.\nThis will ensure that the Fragment is included in the ",(0,o.kt)("a",{parentName:"p",href:"https://rbusarow.github.io/Tangle/api/tangle-fragment-api/tangle.fragment/-tangle-fragment-factory"},"TangleFragmentFactory"),"."),(0,o.kt)("pre",null,(0,o.kt)("code",{parentName:"pre",className:"language-kotlin"},"import tangle.fragment.ContributesFragment\n\n@ContributesFragment(AppScope::class)\nclass MyFragment @Inject constructor(\n  val myRepository: MyRepository\n) : Fragment() {\n  // ...\n}\n")),(0,o.kt)("h3",{id:"5-create-fragments-with-the-fragmentmanager"},"5. Create Fragments with the FragmentManager"),(0,o.kt)("p",null,"All the pieces are now in place, and your FragmentManagers are able to create Fragments with Dagger\ndependencies."),(0,o.kt)("pre",null,(0,o.kt)("code",{parentName:"pre",className:"language-kotlin"},"class MyActivity: BaseActivity() {\n\n  override fun onCreate(savedInstanceState: Bundle?) {\n    super.onCreate(savedInstanceState)\n    supportFragmentManager.beginTransaction()\n      .add<MyFragment>(R.id.fragmentContainer)\n      .commit()\n  }\n}\n")),(0,o.kt)("h3",{id:"next-step----assisted-bundle-injection"},'Next step -- "Assisted" Bundle injection'),(0,o.kt)("p",null,"Tangle is able to generate type-safe factories for Bundle arguments, similar to ",(0,o.kt)("a",{parentName:"p",href:"https://dagger.dev/dev-guide/assisted-injection"},"AssistedInject"),".\nRead about this more in ",(0,o.kt)("a",{parentName:"p",href:"bundles"},"bundle injection"),"."),(0,o.kt)("pre",null,(0,o.kt)("code",{parentName:"pre",className:"language-kotlin"},'@ContributesFragment(AppScope::class)\nclass MyFragment @FragmentInject constructor() : Fragment() {\n\n  val name by arg<String>("name")\n\n  @FragmentInjectFactory\n  interface Factory {\n    fun create(@TangleParam("name") name: String): MyFragment\n  }\n}\n')))}f.isMDXComponent=!0},8209:function(e,n,t){t(7294)}}]);