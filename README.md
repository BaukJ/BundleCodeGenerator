# BundleCodeGenerator

## Introduction

This Java annotation is used to turn resource bundles into Java code.
It uses a custom data structure in the resource files for advanced useage but works with sinple key-value lists.

## Use

### Add dependency

You can add this project as a dependecy by using JitPack.
For details, see [the JitPack website](https://jitpack.io.).

``` groovy
allprojects {
  repositories {
    jcenter()
    maven { url "https://jitpack.io" }
  }
}
dependencies {
  implementation      'com.github.BaukJ.BundleCodeGenerator:BundleCodeGenerator:0.1.0'
  annotationProcessor 'com.github.BaukJ.BundleCodeGenerator:BundleCodeGenerator:0.1.0'
}
```

### Configure project


TODO

