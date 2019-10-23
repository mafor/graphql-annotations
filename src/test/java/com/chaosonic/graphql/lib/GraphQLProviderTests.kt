package com.chaosonic.graphql.lib

import com.chaosonic.graphql.any
import graphql.GraphQL
import graphql.schema.idl.RuntimeWiring
import graphql.schema.idl.SchemaParser
import graphql.schema.idl.TypeDefinitionRegistry
import org.assertj.core.api.Assertions
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Bean
import org.springframework.test.context.junit4.SpringRunner

const val MINIMAL_VALID_SCHEMA = "type QueryType {}\nschema {query: QueryType}"

@RunWith(SpringRunner::class)
@SpringBootTest(classes = [TypeDefinitionRegistryProvider::class])
class TypeDefinitionRegistryProviderTest {

    @MockBean
    @Qualifier("customizer1")
    lateinit var customizer1: TypeDefinitionRegistryCustomizer

    @MockBean
    @Qualifier("customizer2")
    lateinit var customizer2: TypeDefinitionRegistryCustomizer

    @Test
    fun shouldCallCustomizeOnAllCustomizerBeans() {
        verify(customizer1, times(1)).customize(any())
        verify(customizer2, times(1)).customize(any())
    }

}

@RunWith(SpringRunner::class)
@SpringBootTest(classes = [RuntimeWiringProvider::class])
class RuntimeWiringProviderTest {

    @MockBean
    @Qualifier("customizer1")
    lateinit var customizer1: RuntimeWiringCustomizer

    @MockBean
    @Qualifier("customizer2")
    lateinit var customizer2: RuntimeWiringCustomizer

    @Test
    fun shouldCallCustomizeOnAllCustomizerBeans() {
        verify(customizer1, times(1)).customize(any())
        verify(customizer2, times(1)).customize(any())
    }

}

@RunWith(SpringRunner::class)
@SpringBootTest(classes = [GraphQLProvider::class, GraphQLProviderTest.Configuration::class])
class GraphQLProviderTest {

    class Configuration {

        @Bean
        fun runtimeWiring() : RuntimeWiring = RuntimeWiring.newRuntimeWiring().build()

        @Bean
        fun typeDefinitionRegistry() : TypeDefinitionRegistry =
            SchemaParser().parse(MINIMAL_VALID_SCHEMA)
    }

    @Autowired
    val graphQL : GraphQL? = null

    @Test
    fun shouldSetUpGraphQL() {
        Assertions.assertThat(graphQL).isNotNull
    }

}

