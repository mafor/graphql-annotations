package com.chaosonic.graphql.lib

import graphql.GraphQL
import graphql.schema.idl.RuntimeWiring
import graphql.schema.idl.SchemaGenerator
import graphql.schema.idl.TypeDefinitionRegistry
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Lazy
import java.util.*

class GraphQLConfigurationError(message: String) : RuntimeException(message)

@Configuration
class GraphQLProvider {

    @Autowired
    val runtimeWiringCustomizers: List<RuntimeWiringCustomizer> = LinkedList()

    @Bean
    @Lazy
    fun graphQl(typeDefinitionRegistry: TypeDefinitionRegistry): GraphQL {

        val runtimeWiringBuilder = RuntimeWiring.newRuntimeWiring()
        runtimeWiringCustomizers.forEach { it.customize(runtimeWiringBuilder) }
        val schema = SchemaGenerator().makeExecutableSchema(typeDefinitionRegistry, runtimeWiringBuilder.build())

        return GraphQL.newGraphQL(schema).build()
    }

}

@Configuration
class TypeDefinitionRegistryProvider {

    @Autowired
    val typeDefinitionRegistryCustomizers: List<TypeDefinitionRegistryCustomizer> = LinkedList()

    @Bean
    @Lazy
    fun typeDefinitionRegistry(): TypeDefinitionRegistry {

        val typeDefinitionRegistry = TypeDefinitionRegistry()
        typeDefinitionRegistryCustomizers.forEach { it.customize(typeDefinitionRegistry) }

        return typeDefinitionRegistry;
    }
}