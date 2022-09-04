"use strict";(self.webpackChunktangle=self.webpackChunktangle||[]).push([[8802],{3905:function(e,n,t){t.d(n,{Zo:function(){return p},kt:function(){return m}});var a=t(7294);function r(e,n,t){return n in e?Object.defineProperty(e,n,{value:t,enumerable:!0,configurable:!0,writable:!0}):e[n]=t,e}function i(e,n){var t=Object.keys(e);if(Object.getOwnPropertySymbols){var a=Object.getOwnPropertySymbols(e);n&&(a=a.filter((function(n){return Object.getOwnPropertyDescriptor(e,n).enumerable}))),t.push.apply(t,a)}return t}function o(e){for(var n=1;n<arguments.length;n++){var t=null!=arguments[n]?arguments[n]:{};n%2?i(Object(t),!0).forEach((function(n){r(e,n,t[n])})):Object.getOwnPropertyDescriptors?Object.defineProperties(e,Object.getOwnPropertyDescriptors(t)):i(Object(t)).forEach((function(n){Object.defineProperty(e,n,Object.getOwnPropertyDescriptor(t,n))}))}return e}function l(e,n){if(null==e)return{};var t,a,r=function(e,n){if(null==e)return{};var t,a,r={},i=Object.keys(e);for(a=0;a<i.length;a++)t=i[a],n.indexOf(t)>=0||(r[t]=e[t]);return r}(e,n);if(Object.getOwnPropertySymbols){var i=Object.getOwnPropertySymbols(e);for(a=0;a<i.length;a++)t=i[a],n.indexOf(t)>=0||Object.prototype.propertyIsEnumerable.call(e,t)&&(r[t]=e[t])}return r}var s=a.createContext({}),c=function(e){var n=a.useContext(s),t=n;return e&&(t="function"==typeof e?e(n):o(o({},n),e)),t},p=function(e){var n=c(e.components);return a.createElement(s.Provider,{value:n},e.children)},d={inlineCode:"code",wrapper:function(e){var n=e.children;return a.createElement(a.Fragment,{},n)}},u=a.forwardRef((function(e,n){var t=e.components,r=e.mdxType,i=e.originalType,s=e.parentName,p=l(e,["components","mdxType","originalType","parentName"]),u=c(t),m=r,g=u["".concat(s,".").concat(m)]||u[m]||d[m]||i;return t?a.createElement(g,o(o({ref:n},p),{},{components:t})):a.createElement(g,o({ref:n},p))}));function m(e,n){var t=arguments,r=n&&n.mdxType;if("string"==typeof e||r){var i=t.length,o=new Array(i);o[0]=u;var l={};for(var s in n)hasOwnProperty.call(n,s)&&(l[s]=n[s]);l.originalType=e,l.mdxType="string"==typeof e?e:r,o[1]=l;for(var c=2;c<i;c++)o[c]=t[c];return a.createElement.apply(null,o)}return a.createElement.apply(null,t)}u.displayName="MDXCreateElement"},1958:function(e,n,t){t.r(n),t.d(n,{assets:function(){return p},contentTitle:function(){return s},default:function(){return m},frontMatter:function(){return l},metadata:function(){return c},toc:function(){return d}});var a=t(3117),r=t(102),i=(t(7294),t(3905)),o=(t(8209),["components"]),l={id:"member-injection",sidebar_label:"Member Injection",title:"Member Injection"},s=void 0,c={unversionedId:"member-injection",id:"member-injection",title:"Member Injection",description:"The Android framework has a number of classes which are initialized automatically for us:",source:"@site/docs/member-injection.md",sourceDirName:".",slug:"/member-injection",permalink:"/Tangle/docs/next/member-injection",draft:!1,editUrl:"https://github.com/rbusarow/Tangle/blob/main/website/docs/member-injection.md",tags:[],version:"current",frontMatter:{id:"member-injection",sidebar_label:"Member Injection",title:"Member Injection"},sidebar:"Docs",previous:{title:"Benchmarks",permalink:"/Tangle/docs/next/benchmarks"},next:{title:"ViewModels",permalink:"/Tangle/docs/next/viewModels/"}},p={},d=[{value:"TangleScope adds scope to target classes",id:"tanglescope-adds-scope-to-target-classes",level:2},{value:"Base classes",id:"base-classes",level:2},{value:"Components must be added to TangleGraph",id:"components-must-be-added-to-tanglegraph",level:2}],u={toc:d};function m(e){var n=e.components,t=(0,r.Z)(e,o);return(0,i.kt)("wrapper",(0,a.Z)({},u,t,{components:n,mdxType:"MDXLayout"}),(0,i.kt)("p",null,"The Android framework has a number of classes which are initialized automatically for us:"),(0,i.kt)("ul",null,(0,i.kt)("li",{parentName:"ul"},(0,i.kt)("inlineCode",{parentName:"li"},"Application")),(0,i.kt)("li",{parentName:"ul"},(0,i.kt)("inlineCode",{parentName:"li"},"Activity")),(0,i.kt)("li",{parentName:"ul"},(0,i.kt)("inlineCode",{parentName:"li"},"View")),(0,i.kt)("li",{parentName:"ul"},(0,i.kt)("inlineCode",{parentName:"li"},"Service")),(0,i.kt)("li",{parentName:"ul"},(0,i.kt)("inlineCode",{parentName:"li"},"BroadcastReceiver")),(0,i.kt)("li",{parentName:"ul"},(0,i.kt)("inlineCode",{parentName:"li"},"Fragment")," (these are a special case. See ",(0,i.kt)("a",{parentName:"li",href:"/Tangle/docs/next/fragments/"},"fragments")," for more info)")),(0,i.kt)("p",null,"Because we don't control their initialization, we can't use Dagger's constructor injection to\nprovide their dependencies. Instead, we often choose to get our dependencies\nusing ",(0,i.kt)("a",{parentName:"p",href:"https://dagger.dev/members-injection.html"},"member injection"),"."),(0,i.kt)("pre",null,(0,i.kt)("code",{parentName:"pre",className:"language-kotlin"},'import android.app.Activity\nimport android.os.Bundle\nimport tangle.inject.TangleGraph\nimport tangle.inject.TangleScope\nimport javax.inject.Inject\n\n@TangleScope(UserScope::class) // Dependencies will be provided by the UserScope\nclass UserActivity : Activity() {\n\n  @Inject\n  lateinit var logger: MyLogger\n\n  override fun onCreate(savedInstanceState: Bundle?) {\n    // inject MyLogger\n    TangleGraph.inject(this)\n\n    super.onCreate(savedInstanceState)\n\n    logger.log("started UserActivity")\n  }\n}\n')),(0,i.kt)("p",null,"Tangle's member injection is simple to implement."),(0,i.kt)("ol",null,(0,i.kt)("li",{parentName:"ol"},"Define your dependencies using ",(0,i.kt)("inlineCode",{parentName:"li"},"@Inject lateinit var")),(0,i.kt)("li",{parentName:"ol"},"Annotate your class with ",(0,i.kt)("inlineCode",{parentName:"li"},"@TangleScope(<your scope>::class)")),(0,i.kt)("li",{parentName:"ol"},"Call ",(0,i.kt)("inlineCode",{parentName:"li"},"TangleGraph.inject(this)")," in your class's ",(0,i.kt)("inlineCode",{parentName:"li"},"onCreate(...)"),".")),(0,i.kt)("p",null,"The ",(0,i.kt)("inlineCode",{parentName:"p"},"TangleGraph.inject(...)")," function uses the target's class in order to find the appropriate\nscoped MemberInjector."),(0,i.kt)("h2",{id:"tanglescope-adds-scope-to-target-classes"},"TangleScope adds scope to target classes"),(0,i.kt)("p",null,"In order to perform member injection with ",(0,i.kt)("inlineCode",{parentName:"p"},"TangleGraph.inject(target)"),", the target of the injection\nmust be annotated with ",(0,i.kt)("inlineCode",{parentName:"p"},"@TangleScope(...)"),". This is how Tangle determines where the dependencies are\ncoming from. For instance, your application may have an ",(0,i.kt)("inlineCode",{parentName:"p"},"AppScope")," and a ",(0,i.kt)("inlineCode",{parentName:"p"},"UserScope"),". For those two\nscopes, you would use ",(0,i.kt)("inlineCode",{parentName:"p"},"@TangleScope(AppScope::class)")," or ",(0,i.kt)("inlineCode",{parentName:"p"},"@TangleScope(UserScope::class)"),"\nrespectively."),(0,i.kt)("p",null,"Once a target class has an assigned scope, its dependencies will be validated at compile time. For\nexample, if you scope an activity to ",(0,i.kt)("inlineCode",{parentName:"p"},"AppScope")," but it requires a dependency which is only available\nin ",(0,i.kt)("inlineCode",{parentName:"p"},"UserScope"),', the build will fail with a standard Dagger "MissingBinding" error message.'),(0,i.kt)("p",null,'"Base" classes do not need a TangleScope annotation. The will be injected using the scope of their\nsubclass.'),(0,i.kt)("h2",{id:"base-classes"},"Base classes"),(0,i.kt)("p",null,"Large projects frequently have abstract base classes like a ",(0,i.kt)("inlineCode",{parentName:"p"},"BaseActivity"),". These base classes may\nhave dependencies of their own. Injecting from a base class is supported in Tangle."),(0,i.kt)("pre",null,(0,i.kt)("code",{parentName:"pre",className:"language-kotlin"},'@TangleScope(UserScope::class) // Dependencies will be provided by the UserScope\nclass UserActivity : BaseActivity() {\n\n  @Inject\n  lateinit var logger: MyLogger\n\n  override fun onCreate(savedInstanceState: Bundle?) {\n    super.onCreate(savedInstanceState)\n\n    logger.log("started UserActivity")\n  }\n}\n\nclass BaseActivity : Activity() {\n\n  @Inject\n  lateinit var fragmentFactory: TangleFragmentFactory\n\n  override fun onCreate(savedInstanceState: Bundle?) {\n    // inject this class and the subclass\n    TangleGraph.inject(this)\n\n    super.onCreate(savedInstanceState)\n  }\n}\n')),(0,i.kt)("h2",{id:"components-must-be-added-to-tanglegraph"},"Components must be added to TangleGraph"),(0,i.kt)("p",null,"Tangle must know about your Component instances in order to inject your classes."),(0,i.kt)("p",null,"See ",(0,i.kt)("a",{parentName:"p",href:"configuration#setting-up-the-tangle-graph"},"setting up the TangleGraph")," for a simple example."))}m.isMDXComponent=!0},8209:function(e,n,t){t(7294)}}]);