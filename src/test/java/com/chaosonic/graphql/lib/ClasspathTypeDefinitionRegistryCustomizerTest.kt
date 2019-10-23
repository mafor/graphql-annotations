package com.chaosonic.graphql.lib

import com.chaosonic.graphql.any
import graphql.schema.idl.TypeDefinitionRegistry
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit4.SpringRunner


@RunWith(SpringRunner::class)
@SpringBootTest(classes = [ClasspathTypeDefinitionRegistryCustomizer::class])
@TestPropertySource(properties = ["graphql.schema.location=classpath:test-schema.graphqls"])
class ClasspathTypeDefinitionRegistryCustomizerTest {

    @Autowired
    lateinit var customizer: ClasspathTypeDefinitionRegistryCustomizer

    @MockBean
    lateinit var registry: TypeDefinitionRegistry

    @Test
    fun shouldLocateUnParseSchemaFromTheClasspath() {
        // when
        customizer.customize(registry)
        // then
        verify(registry, times(1)).merge(any())
    }

}