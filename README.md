# Simple kotlin callbacks

[![Maven Central](https://img.shields.io/maven-central/v/io.github.edmondantes/simple-kotlin-callbacks?color=green&style=flat-square)](https://search.maven.org/search?q=g:io.github.edmondantes%20a:simple-kotlin-callbacks)
![GitHub](https://img.shields.io/github/license/edmondantes/simple-kotlin-callbacks?style=flat-square)
![Lines of code](https://img.shields.io/tokei/lines/github/edmondantes/simple-kotlin-callbacks?style=flat-square)

---

#### Lightweight callback implementation, written in [![Pure Kotlin](https://img.shields.io/badge/100%25-kotlin-blue.svg)](https://kotlinlang.org/).

# Table of Contents

1. [How to add this library to your project](#how-add-this-library-to-your-project)
2. [Getting started](#getting-started)
3. [Features](#features)
    1. [Priority](#priority)
    2. [Timeout for waiting result](#timeout-for-waiting-result)
    3. [Reusing callback](#reusing-callback)
    4. [Stage managing](#stage-managing)
4. [Key-association callbacks](#key-association-callbacks)
5. [Integrations](#integrations)
    1. [Future from java.util.concurrent](#future-from-javautilconcurrent)
    2. [Coroutines](#coroutines)

## How add this library to your project

### Maven

```xml
<dependency>
    <groupId>io.github.edmondantes</groupId>
    <artifactId>simple-kotlin-callbacks</artifactId>
    <version>0.1.1</version>
</dependency>
```

### Gradle (kotlin)

```kotlin
implementation("io.github.edmondantes:simple-kotlin-callbacks:0.1.1")
```

### Gradle (groove)

```groovy
implementation "io.github.edmondantes:simple-kotlin-callbacks:0.1.1"
```

## Getting started

```kotlin
val callbackManager = DefaultCallbackManager<String>()
callbackManager.add(SimpleCallback { context ->
    if (context == null) {
        println("Callback was cancelled")
    } else {
        println(context.data)
    }
})

// ... \\

callbackManager.invoke("Hello world")
```

## Features

### Priority

You can set priority for callbacks. Example:

```kotlin
callbackManager.add(callback1, priority = 1)
callbackManager.add(callback2, priority = 2)
```

In example before callback `callback1` will be called before `callback2`

### Timeout for waiting result

For enable this future, you should create `DefaultCallbackManager` with first parameter `SchedulerExecutorService`.
Example:

```kotlin
val callbackManager = DefaultCallbackManager<String>(
    Executors.newScheduledThreadPool(1)
)
```

After that you can call method `add` with additional parameter `timeout`. Example:

```kotlin
callbackManager.add(SimpleCallback {
    // ... \\
}, timeout = 1000)
```

After timeout callback will be cancelled.

### Reusing callback

For default all callback will not be deleted after call. For removing callback after call, please call method `remove`
in callback context. Please pay attention if you're using class `SimpleCallback` before removing callback will be called
and callback context will be `null`.

```kotlin
SimpleCallback { context ->
    // ... \\
    contex.remove()
}
```

### Stage managing

If a callback want to ignore all another callback which have less priority, you can use method `ignoreNextStage` in
callback context. Example:

```kotlin
val callback1 = SimpleCallback { context ->
    // ... \\
    context.ignoreNextStage()
}

callbackManager.add(callback1, 1)
callbackManager.add(callback2, 2)
```

In example before manager will call only callback `callback1`, because this callback requires ignoring callbacks which
have less priority

## Key-association callbacks

If you want to get callback only for special key, you can use class `DefaultKeyCallbackManager`. Example:

```kotlin
val callbackManager = DefaultKeyCallbackManager<String, String>()
callbackManager.add("key", callback1)
callbackManager.add("anotherKey", callback2)

// ... \\

callbackManager.invoke("key", "data")
```

In example before manager will call only callback `callback1`, because `callback2` associate with another key.

This type of managers support all features described before

## Integrations

In integrations you can use only these futures: [priority](#priority),
[timeout for waiting result](#timeout-for-waiting-result)

### Future from java.util.concurrent

If you want to get `Future` from callback manager you can call method `future`. Returned future supports canceling
feature. Example:

```kotlin
val future = callbackManager.future(priority = -1)
try {
    future.get(5, TimeUnit.MILLISECONDS)
} catch (e: TimeoutException) {
    future.cancel(true)
}
```

### Coroutines

You can use `wait` for wait callback's result in coroutine. If callback will be cancelled this method
throw `CancellationException`. Example:

```kotlin
coroutineScope {
    val result = callbackManager.wait(priority = 1, timeout = 2000)
}
```