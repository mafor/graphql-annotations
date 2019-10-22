package com.chaosonic.graphql

import com.chaosonic.graphql.lib.TypeDefinitionRegistryCustomizer
import com.chaosonic.graphql.lib.TypeDefinitionRegistryProvider
import graphql.schema.idl.TypeDefinitionRegistry
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.junit4.SpringRunner


@RunWith(SpringRunner::class)
@SpringBootTest(classes = [TypeDefinitionRegistryProvider::class])
class TypeDefinitionRegistryProviderTest {

    @Autowired
    val typeDefinitionRegistry: TypeDefinitionRegistry? = null

    @MockBean
    @Qualifier("customizer1")
    val customizer1: TypeDefinitionRegistryCustomizer? = null

    @MockBean
    @Qualifier("customizer2")
    val customizer2: TypeDefinitionRegistryCustomizer? = null

    @Test
    fun shouldCallCustomizeOnAllCustomizerBeans() {
        verify(customizer1!!, times(1)).customize(any())
        verify(customizer2!!, times(1)).customize(any())
    }

}