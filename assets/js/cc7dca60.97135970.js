"use strict";(self.webpackChunkdiscord4j_documentation=self.webpackChunkdiscord4j_documentation||[]).push([[202],{9733:(e,n,a)=>{a.r(n),a.d(n,{assets:()=>l,contentTitle:()=>s,default:()=>h,frontMatter:()=>i,metadata:()=>o,toc:()=>c});var t=a(4848),r=a(8453);const i={id:"lambda-tutorial",title:"Lambda Tutorial",sidebar_label:"Lambda Tutorial",slug:"/lambda-tutorial"},s=void 0,o={id:"Conceptual Guides/lambda-tutorial",title:"Lambda Tutorial",description:"Introduction",source:"@site/docs/06-Conceptual Guides/lambda-tutorial.mdx",sourceDirName:"06-Conceptual Guides",slug:"/lambda-tutorial",permalink:"/lambda-tutorial",draft:!1,unlisted:!1,editUrl:"https://github.com/Discord4J/documentation/edit/master/docs/06-Conceptual Guides/lambda-tutorial.mdx",tags:[],version:"current",lastUpdatedAt:163120654e4,frontMatter:{id:"lambda-tutorial",title:"Lambda Tutorial",sidebar_label:"Lambda Tutorial",slug:"/lambda-tutorial"},sidebar:"mySidebar",previous:{title:"What's new in v3.2",permalink:"/whats-new-in-v3-2"},next:{title:"Reactive (Reactor) Tutorial",permalink:"/reactive-reactor-tutorial"}},l={},c=[{value:"Introduction",id:"introduction",level:2},{value:"Syntax and Usage",id:"syntax-and-usage",level:2},{value:"Functional Programming Lite",id:"functional-programming-lite",level:2}];function d(e){const n={a:"a",code:"code",em:"em",h2:"h2",li:"li",ol:"ol",p:"p",pre:"pre",...(0,r.R)(),...e.components};return(0,t.jsxs)(t.Fragment,{children:[(0,t.jsx)(n.h2,{id:"introduction",children:"Introduction"}),"\n",(0,t.jsxs)(n.p,{children:["A ",(0,t.jsx)(n.a,{href:"https://docs.oracle.com/javase/tutorial/java/javaOO/lambdaexpressions.html",children:"lambda"})," is like a function in a variable. It is an alternative way to define a certain kind of ",(0,t.jsx)(n.a,{href:"https://docs.oracle.com/javase/tutorial/java/javaOO/anonymousclasses.html",children:"anonymous class"})," to help make your code a lot shorter, cleaner, and easier to understand. You can create a lambda for a type which meets the following criteria:"]}),"\n",(0,t.jsxs)(n.ol,{children:["\n",(0,t.jsxs)(n.li,{children:["The type must be an ",(0,t.jsx)(n.a,{href:"https://docs.oracle.com/javase/tutorial/java/concepts/interface.html",children:"interface"}),"."]}),"\n",(0,t.jsx)(n.li,{children:"The interface must have exactly one abstract method."}),"\n"]}),"\n",(0,t.jsx)(n.p,{children:"For example, the following can never be expressed as a lambda:"}),"\n",(0,t.jsx)(n.pre,{children:(0,t.jsx)(n.code,{className:"language-java",children:"class CannotBeALambdaBecauseAClass {\r\n}\n"})}),"\n",(0,t.jsx)(n.pre,{children:(0,t.jsx)(n.code,{className:"language-java",children:'interface CannotBeALambdaBecauseNoAbstractMethod {\r\n    default void printHelloWorld() {\r\n        System.out.println("Hello World");\r\n    }\r\n}\n'})}),"\n",(0,t.jsx)(n.pre,{children:(0,t.jsx)(n.code,{className:"language-java",children:"interface CannotBeALambdaBecauseTooManyAbstractMethods {\r\n    void printHelloWorld();\r\n    int returnAnswerToTheUniverse();\r\n}\n"})}),"\n",(0,t.jsx)(n.p,{children:"However, the following can be expressed as a lambda:"}),"\n",(0,t.jsx)(n.pre,{children:(0,t.jsx)(n.code,{className:"language-java",children:"interface CanBeALambda {\r\n    void printHelloWorld();\r\n}\n"})}),"\n",(0,t.jsx)(n.pre,{children:(0,t.jsx)(n.code,{className:"language-java",children:"interface CanBeALambda {\r\n    void printHelloWorld();\r\n    default int returnAnswerToTheUniverse() {\r\n        return 42;\r\n    }\r\n}\n"})}),"\n",(0,t.jsxs)(n.p,{children:["To guarantee that a type can be expressed as a lambda, you may optionally annotate it with ",(0,t.jsx)(n.a,{href:"https://docs.oracle.com/javase/8/docs/api/java/lang/FunctionalInterface.html",children:"@FunctionalInterface"}),'. A "functional interface" is an interface that meets the requirements defined and demonstrated above. Some built-in functional interfaces in the JDK are ',(0,t.jsx)(n.code,{children:"Runnable"}),", ",(0,t.jsx)(n.code,{children:"Callable"}),", and ",(0,t.jsx)(n.code,{children:"Comparator"}),". You can find many other useful interfaces in the ",(0,t.jsx)(n.a,{href:"https://docs.oracle.com/javase/8/docs/api/java/util/function/package-summary.html",children:"java.util.function"})," package."]}),"\n",(0,t.jsx)(n.h2,{id:"syntax-and-usage",children:"Syntax and Usage"}),"\n",(0,t.jsxs)(n.p,{children:["Let's assume the following method which uses ",(0,t.jsx)(n.a,{href:"https://docs.oracle.com/javase/8/docs/api/java/util/function/Function.html",children:"Function"}),":"]}),"\n",(0,t.jsx)(n.pre,{children:(0,t.jsx)(n.code,{className:"language-java",children:"String process(Function<Integer, String> f) {\r\n    return f.apply(42);\r\n}\n"})}),"\n",(0,t.jsxs)(n.p,{children:["Let's walk through this. We have a method, ",(0,t.jsx)(n.code,{children:"process"}),", which accepts a ",(0,t.jsx)(n.a,{href:"https://docs.oracle.com/javase/8/docs/api/java/util/function/Function.html",children:(0,t.jsx)(n.code,{children:"Function"})})," as a parameter and returns a ",(0,t.jsx)(n.code,{children:"String"}),". The parameter, ",(0,t.jsx)(n.code,{children:"f"}),", is ",(0,t.jsx)(n.em,{children:"itself"})," a function which accepts an ",(0,t.jsx)(n.code,{children:"Integer"})," as a parameter and returns a ",(0,t.jsx)(n.code,{children:"String"}),'. A function which accepts another function as a parameter is sometimes called "higher order" (i.e., ',(0,t.jsx)(n.code,{children:"process"})," is a higher order function). We'll see ",(0,t.jsx)(n.em,{children:"a lot"})," of these in the future."]}),"\n",(0,t.jsxs)(n.p,{children:["All the ",(0,t.jsx)(n.code,{children:"process"})," method does is call the function ",(0,t.jsx)(n.code,{children:"f"})," with a constant parameter ",(0,t.jsx)(n.code,{children:"42"}),". Although it is not valid syntax, you can think of this as being ",(0,t.jsx)(n.code,{children:"f(42)"}),"."]}),"\n",(0,t.jsx)(n.p,{children:"Before Java 8, this method would have to be called like so:"}),"\n",(0,t.jsx)(n.pre,{children:(0,t.jsx)(n.code,{className:"language-java",children:'process(new Function<Integer, String>() {\r\n    @Override\r\n    public String apply(Integer t) {\r\n        return t == 42 ? "The Universe" : "Doesn\'t matter!";\r\n    }\r\n});\n'})}),"\n",(0,t.jsx)(n.p,{children:"However, since Java 8, this method can be called with a statement lambda like so:"}),"\n",(0,t.jsx)(n.pre,{children:(0,t.jsx)(n.code,{className:"language-java",children:'process(t -> {\r\n    return t == 42 ? "The Universe" : "Doesn\'t matter!"\r\n});\n'})}),"\n",(0,t.jsxs)(n.p,{children:["The ",(0,t.jsx)(n.code,{children:"t"})," is the input parameter, same as the ",(0,t.jsx)(n.code,{children:"t"})," in the previous example. Since the ",(0,t.jsx)(n.code,{children:"Function"})," defined in our example allows for a ",(0,t.jsx)(n.code,{children:"String"})," return, we just simply make sure our statement return a String. Since in our current example our lambda only contains 1 statement, it can be shortened to an expression lambda:"]}),"\n",(0,t.jsx)(n.pre,{children:(0,t.jsx)(n.code,{className:"language-java",children:'process(t -> t == 42 ? "The Universe" : "Doesn\'t matter!");\n'})}),"\n",(0,t.jsxs)(n.p,{children:["If our ",(0,t.jsx)(n.code,{children:"process"})," method required multiple parameters, we'd add to the ",(0,t.jsx)(n.code,{children:"t"})," like ",(0,t.jsx)(n.code,{children:"(t, anotherParameter) ->"})," and if there were no parameters, we'd use ",(0,t.jsx)(n.code,{children:"() ->"})," instead."]}),"\n",(0,t.jsx)(n.p,{children:"Now, assume we had another method defined somewhere that looked like this:"}),"\n",(0,t.jsx)(n.pre,{children:(0,t.jsx)(n.code,{className:"language-java",children:'String processInteger(Integer t) {\r\n    return t == 42 ? "The Universe" : "Doesn\'t matter!";\r\n}\n'})}),"\n",(0,t.jsxs)(n.p,{children:["We can now call our ",(0,t.jsx)(n.code,{children:"process"}),' method with a "method reference" like so:']}),"\n",(0,t.jsx)(n.pre,{children:(0,t.jsx)(n.code,{className:"language-java",children:"process(this::processInteger);\n"})}),"\n",(0,t.jsx)(n.h2,{id:"functional-programming-lite",children:"Functional Programming Lite"}),"\n",(0,t.jsx)(n.p,{children:"As mentioned earlier, lambdas can be seen as a way to store functions in variables. For example, the following is valid:"}),"\n",(0,t.jsx)(n.pre,{children:(0,t.jsx)(n.code,{className:"language-java",children:'Runnable runnable = () -> System.out.println("Hello World");\n'})}),"\n",(0,t.jsxs)(n.p,{children:["The ",(0,t.jsx)(n.code,{children:"runnable"})," variable can now be passed and then called anywhere. The variable itself doesn't ",(0,t.jsx)(n.em,{children:"do"})," anything, instead it defines ",(0,t.jsx)(n.em,{children:"how"})," to do something. This idea of storing functions and passing them around is the starting block for ",(0,t.jsxs)(n.a,{href:"https://en.wikipedia.org/wiki/Functional_programming",children:[(0,t.jsx)(n.em,{children:"function"}),"al programming"]})," which will be utilized a lot in moving forward with Discord4J."]})]})}function h(e={}){const{wrapper:n}={...(0,r.R)(),...e.components};return n?(0,t.jsx)(n,{...e,children:(0,t.jsx)(d,{...e})}):d(e)}},8453:(e,n,a)=>{a.d(n,{R:()=>s,x:()=>o});var t=a(6540);const r={},i=t.createContext(r);function s(e){const n=t.useContext(i);return t.useMemo((function(){return"function"==typeof e?e(n):{...n,...e}}),[n,e])}function o(e){let n;return n=e.disableParentContext?"function"==typeof e.components?e.components(r):e.components||r:s(e.components),t.createElement(i.Provider,{value:n},e.children)}}}]);