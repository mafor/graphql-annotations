package com.chaosonic.graphql.lib

import graphql.GraphQL
import graphql.schema.idl.RuntimeWiring
import graphql.schema.idl.SchemaGenerator
import graphql.schema.idl.TypeDefinitionRegistry
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.*

class GraphQLConfigurationError(message: String) : RuntimeException(message)

@Configuration
class GraphQLProvider {

    @Bean
    fun graphQl(typeDefinitionRegistry: TypeDefinitionRegistry, runtimeWiring: RuntimeWiring): GraphQL {

        val schema = SchemaGenerator().makeExecutableSchema(typeDefinitionRegistry, runtimeWiring)
        return GraphQL.newGraphQL(schema).build()
    }
}

@Configuration
class RuntimeWiringProvider {

    @Autowired(required = false)
    val runtimeWiringCustomizers: List<RuntimeWiringCustomizer> = LinkedList()

    @Bean
    fun runtimeWiring(): RuntimeWiring {

        val runtimeWiringBuilder = RuntimeWiring.newRuntimeWiring()
        runtimeWiringCustomizers.forEach { it.customize(runtimeWiringBuilder) }

        return runtimeWiringBuilder.build()
    }
}

@Configuration
class TypeDefinitionRegistryProvider {

    @Autowired(required = false)
    val typeDefinitionRegistryCustomizers: List<TypeDefinitionRegistryCustomizer> = LinkedList()

    @Bean
    fun typeDefinitionRegistry(): TypeDefinitionRegistry {

        val typeDefinitionRegistry = TypeDefinitionRegistry()
        typeDefinitionRegistryCustomizers.forEach { it.customize(typeDefinitionRegistry) }

        return typeDefinitionRegistry
    }
}