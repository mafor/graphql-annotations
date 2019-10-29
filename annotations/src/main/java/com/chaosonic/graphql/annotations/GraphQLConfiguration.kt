package com.chaosonic.graphql.annotations

import graphql.GraphQL
import graphql.schema.idl.RuntimeWiring
import graphql.schema.idl.SchemaGenerator
import graphql.schema.idl.TypeDefinitionRegistry
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.core.io.support.ResourcePatternResolver
import java.util.*

class GraphQLConfigurationError(message: String) : RuntimeException(message)

@Configuration
@ConditionalOnMissingBean(value = [GraphQL::class])
@Import(value = [RuntimeWiringProvider::class, TypeDefinitionRegistryProvider::class])
class GraphQLConfiguration {

    @Bean
    fun classpathTypeDefinitionRegistryCustomizer(resourceResolver: ResourcePatternResolver) =
        ClasspathTypeDefinitionRegistryCustomizer(resourceResolver)

    @Bean(name = ["defaultGraphQLMappingValidator"])
    fun graphQLMappingValidator(typeDefinitionRegistry: TypeDefinitionRegistry) =
        GraphQLMappingValidator(typeDefinitionRegistry)

    @Bean(name = ["defaultMethodHandlerFactory"])
    fun methodHandlerFactory() = MethodHandlerFactory()

    @Bean
    fun annotationRuntimeWiringCustomizer(
        applicationContext: ApplicationContext,
        validator: GraphQLMappingValidator,
        methodHandlerFactory: MethodHandlerFactory
    ) = AnnotationRuntimeWiringCustomizer(applicationContext, validator, methodHandlerFactory)

    @Bean(name = ["defaultGraphQL"])
    fun graphQl(typeDefinitionRegistry: TypeDefinitionRegistry, runtimeWiring: RuntimeWiring): GraphQL {

        val schema = SchemaGenerator().makeExecutableSchema(typeDefinitionRegistry, runtimeWiring)
        return GraphQL.newGraphQL(schema).build()
    }
}

class RuntimeWiringProvider {

    @Autowired(required = false)
    val runtimeWiringCustomizers: List<RuntimeWiringCustomizer> = LinkedList()

    @Bean(name = ["defaultRuntimeWiring"])
    fun runtimeWiring(): RuntimeWiring {

        val runtimeWiringBuilder = RuntimeWiring.newRuntimeWiring()
        runtimeWiringCustomizers.forEach { it.customize(runtimeWiringBuilder) }

        return runtimeWiringBuilder.build()
    }
}

class TypeDefinitionRegistryProvider {

    @Autowired(required = false)
    val typeDefinitionRegistryCustomizers: List<TypeDefinitionRegistryCustomizer> = LinkedList()

    @Bean(name = ["defaultTypeDefinitionRegistry"])
    fun typeDefinitionRegistry(): TypeDefinitionRegistry {

        val typeDefinitionRegistry = TypeDefinitionRegistry()
        typeDefinitionRegistryCustomizers.forEach { it.customize(typeDefinitionRegistry) }

        return typeDefinitionRegistry
    }
}