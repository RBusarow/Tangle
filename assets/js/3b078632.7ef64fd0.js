"use strict";(self.webpackChunktangle=self.webpackChunktangle||[]).push([[4169],{3905:function(e,n,t){t.d(n,{Zo:function(){return s},kt:function(){return m}});var a=t(7294);function r(e,n,t){return n in e?Object.defineProperty(e,n,{value:t,enumerable:!0,configurable:!0,writable:!0}):e[n]=t,e}function i(e,n){var t=Object.keys(e);if(Object.getOwnPropertySymbols){var a=Object.getOwnPropertySymbols(e);n&&(a=a.filter((function(n){return Object.getOwnPropertyDescriptor(e,n).enumerable}))),t.push.apply(t,a)}return t}function l(e){for(var n=1;n<arguments.length;n++){var t=null!=arguments[n]?arguments[n]:{};n%2?i(Object(t),!0).forEach((function(n){r(e,n,t[n])})):Object.getOwnPropertyDescriptors?Object.defineProperties(e,Object.getOwnPropertyDescriptors(t)):i(Object(t)).forEach((function(n){Object.defineProperty(e,n,Object.getOwnPropertyDescriptor(t,n))}))}return e}function o(e,n){if(null==e)return{};var t,a,r=function(e,n){if(null==e)return{};var t,a,r={},i=Object.keys(e);for(a=0;a<i.length;a++)t=i[a],n.indexOf(t)>=0||(r[t]=e[t]);return r}(e,n);if(Object.getOwnPropertySymbols){var i=Object.getOwnPropertySymbols(e);for(a=0;a<i.length;a++)t=i[a],n.indexOf(t)>=0||Object.prototype.propertyIsEnumerable.call(e,t)&&(r[t]=e[t])}return r}var d=a.createContext({}),c=function(e){var n=a.useContext(d),t=n;return e&&(t="function"==typeof e?e(n):l(l({},n),e)),t},s=function(e){var n=c(e.components);return a.createElement(d.Provider,{value:n},e.children)},u={inlineCode:"code",wrapper:function(e){var n=e.children;return a.createElement(a.Fragment,{},n)}},p=a.forwardRef((function(e,n){var t=e.components,r=e.mdxType,i=e.originalType,d=e.parentName,s=o(e,["components","mdxType","originalType","parentName"]),p=c(t),m=r,v=p["".concat(d,".").concat(m)]||p[m]||u[m]||i;return t?a.createElement(v,l(l({ref:n},s),{},{components:t})):a.createElement(v,l({ref:n},s))}));function m(e,n){var t=arguments,r=n&&n.mdxType;if("string"==typeof e||r){var i=t.length,l=new Array(i);l[0]=p;var o={};for(var d in n)hasOwnProperty.call(n,d)&&(o[d]=n[d]);o.originalType=e,o.mdxType="string"==typeof e?e:r,l[1]=o;for(var c=2;c<i;c++)l[c]=t[c];return a.createElement.apply(null,l)}return a.createElement.apply(null,t)}p.displayName="MDXCreateElement"},1611:function(e,n,t){t.r(n),t.d(n,{assets:function(){return s},contentTitle:function(){return d},default:function(){return m},frontMatter:function(){return o},metadata:function(){return c},toc:function(){return u}});var a=t(3117),r=t(102),i=(t(7294),t(3905)),l=(t(8209),["components"]),o={title:"SavedStateHandle injection",sidebar_label:"SavedStateHandle Injection"},d=void 0,c={unversionedId:"viewModels/savedStateHandle",id:"version-0.13.1/viewModels/savedStateHandle",title:"SavedStateHandle injection",description:"When using the tangleViewModel delegate function, a scoped subcomponent is created",source:"@site/versioned_docs/version-0.13.1/viewModels/savedStateHandle.md",sourceDirName:"viewModels",slug:"/viewModels/savedStateHandle",permalink:"/Tangle/docs/0.13.1/viewModels/savedStateHandle",draft:!1,editUrl:"https://github.com/rbusarow/Tangle/blob/main/website/versioned_docs/version-0.13.1/viewModels/savedStateHandle.md",tags:[],version:"0.13.1",frontMatter:{title:"SavedStateHandle injection",sidebar_label:"SavedStateHandle Injection"},sidebar:"version-0.13.1/Docs",previous:{title:"ViewModels",permalink:"/Tangle/docs/0.13.1/viewModels/"},next:{title:"Compose",permalink:"/Tangle/docs/0.13.1/viewModels/compose"}},s={},u=[],p={toc:u};function m(e){var n=e.components,t=(0,r.Z)(e,l);return(0,i.kt)("wrapper",(0,a.Z)({},p,t,{components:n,mdxType:"MDXLayout"}),(0,i.kt)("p",null,"When using the ",(0,i.kt)("inlineCode",{parentName:"p"},"tangleViewModel")," delegate function, a scoped subcomponent is created\nwith a binding for ",(0,i.kt)("a",{parentName:"p",href:"https://developer.android.com/topic/libraries/architecture/viewmodel-savedstate"},"SavedStateHandle"),".  This ",(0,i.kt)("inlineCode",{parentName:"p"},"SavedStateHandle")," is provided\nby the ViewModel's owning ",(0,i.kt)("inlineCode",{parentName:"p"},"Fragment"),", ",(0,i.kt)("inlineCode",{parentName:"p"},"Activity"),", or ",(0,i.kt)("inlineCode",{parentName:"p"},"NavBackStackEntry"),"."),(0,i.kt)("p",null,"This ",(0,i.kt)("inlineCode",{parentName:"p"},"SavedStateHandle")," may then be included as a dependency in injected constructors,\njust as it can in ",(0,i.kt)("a",{parentName:"p",href:"https://dagger.dev/hilt/view-model.html"},"Hilt"),"."),(0,i.kt)("pre",null,(0,i.kt)("code",{parentName:"pre",className:"language-kotlin"},"import androidx.lifecycle.SavedStateHandle\nimport androidx.lifecycle.ViewModel\nimport tangle.viewmodel.VMInject\n\nclass MyViewModel @VMInject constructor(\n  val savedState: SavedStateHandle\n) : ViewModel()\n")),(0,i.kt)("p",null,"In addition, Tangle can automatically extract arguments from the ",(0,i.kt)("inlineCode",{parentName:"p"},"SavedStateHandle"),"\nand inject them into the constructor, through use of the ",(0,i.kt)("inlineCode",{parentName:"p"},"TangleParam")," annotation."),(0,i.kt)("p",null,"If the constructor argument's type is not nullable, then Tangle will assert that the argument is in\nthe bundle while creating the ViewModel."),(0,i.kt)("p",null,"If the argument is marked as nullable, then Tangle will gracefully handle a missing argument and\njust inject ",(0,i.kt)("inlineCode",{parentName:"p"},"null"),"."),(0,i.kt)("p",null,"Given this code:"),(0,i.kt)("pre",null,(0,i.kt)("code",{parentName:"pre",className:"language-kotlin"},'import androidx.lifecycle.ViewModel\nimport tangle.inject.TangleParam\nimport tangle.viewmodel.VMInject\n\nclass MyViewModel @VMInject constructor(\n  @TangleParam("userId")\n  val userId: String, // must be present in the SavedStateHandle\n  @TangleParam("address")\n  val addressOrNull: String? // can safely be null\n) : ViewModel()\n')),(0,i.kt)("p",null,"Tangle will generate the following:"),(0,i.kt)("pre",null,(0,i.kt)("code",{parentName:"pre",className:"language-kotlin"},'import androidx.lifecycle.SavedStateHandle\nimport javax.inject.Inject\nimport javax.inject.Provider\n\npublic class MyViewModel_Factory @Inject constructor(\n  internal val savedStateHandleProvider: Provider<SavedStateHandle>\n) {\n  public fun create(): MyViewModel {\n    val userId = savedStateHandleProvider.get().get<String>("userId")\n    checkNotNull(userId) {\n      "Required parameter with name `userId` " +\n        "and type `kotlin.String` is missing from SavedStateHandle."\n    }\n    val addressOrNull = savedStateHandleProvider.get().get<String?>("address")\n    return MyViewModel(userId, addressOrNull)\n  }\n}\n')))}m.isMDXComponent=!0},8209:function(e,n,t){t(7294)}}]);