package com.chaosonic.graphql

import org.mockito.Mockito

/**
 * Fix for Mockito.any() returning null which collides with Kotlin's null checks
 * @see <a href="https://discuss.kotlinlang.org/t/how-to-use-mockito-with-kotlin/324">How to use Mockito with Kotlin?</a>
 */
fun <T> any(): T = Mockito.any<T>()
