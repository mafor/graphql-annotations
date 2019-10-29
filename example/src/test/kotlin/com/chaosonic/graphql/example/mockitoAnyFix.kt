package com.chaosonic.graphql.example

import org.mockito.Mockito

/**
 * Fix for Mockito.any() returning `null` which collides with Kotlin's null checks. See
 * [How to use Mockito with Kotlin?](https://discuss.kotlinlang.org/t/how-to-use-mockito-with-kotlin/324)
 */
fun <T> any(): T = Mockito.any<T>()
