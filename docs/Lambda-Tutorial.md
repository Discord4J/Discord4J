## Introduction
A [lambda](https://docs.oracle.com/javase/tutorial/java/javaOO/lambdaexpressions.html) is like a function in a variable. It is an alternative way to define a certain kind of [anonymous class](https://docs.oracle.com/javase/tutorial/java/javaOO/anonymousclasses.html) to help make your code a lot shorter, cleaner, and easier to understand. You can create a lambda for a type which meets the following criteria:
1. The type must be an [interface](https://docs.oracle.com/javase/tutorial/java/concepts/interface.html).
2. The interface must have exactly one abstract method.

For example, the following can never be expressed as a lambda:
```java
class CannotBeALambdaBecauseAClass {
}
```
```java
interface CannotBeALambdaBecauseNoAbstractMethod {
    default void printHelloWorld() {
        System.out.println("Hello World");
    }
}
```
```java
interface CannotBeALambdaBecauseTooManyAbstractMethods {
    void printHelloWorld();
    int returnAnswerToTheUniverse();
}
```
However, the following can be expressed as a lambda:
```java
interface CanBeALambda {
    void printHelloWorld();
}
```
```java
interface CanBeALambda {
    void printHelloWorld();
    default int returnAnswerToTheUniverse() {
        return 42;
    }
}
```
To guarantee that a type can be expressed as a lambda, you may optionally annotate it with [@FunctionalInterface](https://docs.oracle.com/javase/8/docs/api/java/lang/FunctionalInterface.html). A "functional interface" is an interface that meets the requirements defined and demonstrated above. Some built-in functional interfaces in the JDK are `Runnable`, `Callable`, and `Comparator`. You can find many other useful interfaces in the [java.util.function](https://docs.oracle.com/javase/8/docs/api/java/util/function/package-summary.html) package.
## Syntax and Usage
Let's assume the following method which uses [Function](https://docs.oracle.com/javase/8/docs/api/java/util/function/Function.html):
```java
String process(Function<Integer, String> f) {
    return f.apply(42);
}
```
Let's walk through this. We have a method, `process`, which accepts a [`Function`](https://docs.oracle.com/javase/8/docs/api/java/util/function/Function.html) as a parameter and returns a `String`. The parameter, `f`, is *itself* a function which accepts an `Integer` as a parameter and returns a `String`. A function which accepts another function as a parameter is sometimes called "higher order" (i.e., `process` is a higher order function). We'll see *a lot* of these in the future.

All the `process` method does is call the function `f` with a constant parameter `42`. Although it is not valid syntax, you can think of this as being `f(42)`.

Before Java 8, this method would have to be called like so:
```java
process(new Function<Integer, String>() {
    @Override
    public String apply(Integer t) {
        return t == 42 ? "The Universe" : "Doesn't matter!";
    }
});
```
However, since Java 8, this method can be called with a statement lambda like so:
```java
process(t -> {
    return t == 42 ? "The Universe" : "Doesn't matter!"
});
```
The `t` is the input parameter, same as the `t` in the previous example. Since the `Function` defined in our example allows for a `String` return, we just simply make sure our statement return a String. Since in our current example our lambda only contains 1 statement, it can be shortened to an expression lambda:
```java
process(t -> t == 42 ? "The Universe" : "Doesn't matter!");
```
If our `process` method required multiple parameters, we'd add to the `t` like `(t, anotherParameter) ->` and if there were no parameters, we'd use `() ->` instead.

Now, assume we had another method defined somewhere that looked like this:
```java
String processInteger(Integer t) {
    return t == 42 ? "The Universe" : "Doesn't matter!";
}
```
We can now call our `process` method with a "method reference" like so:
```java
process(this::processInteger);
```
## Functional Programming Lite
As mentioned earlier, lambdas can be seen as a way to store functions in variables. For example, the following is valid:
```java
Runnable runnable = () -> System.out.println("Hello World");
```
The `runnable` variable can now be passed and then called anywhere. The variable itself doesn't *do* anything, instead it defines *how* to do something. This idea of storing functions and passing them around is the starting block for [*function*al programming](https://en.wikipedia.org/wiki/Functional_programming) which will be utilized a lot in moving forward with Discord4J.
