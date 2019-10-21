package com.chaosonic.graphql.lib

import graphql.GraphQL
import graphql.schema.idl.RuntimeWiring
import graphql.schema.idl.SchemaGenerator
import graphql.schema.idl.SchemaParser
import graphql.schema.idl.TypeDefinitionRegistry
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Lazy
import org.springframework.core.io.support.ResourcePatternResolver
import org.springframework.stereotype.Component
import java.io.InputStreamReader

@Component
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class DataFetcher(
    val type: String,
    val field: String
)

@Configuration
class GraphQlConfig(val resourceResolver: ResourcePatternResolver, val applicationContext: ApplicationContext) {

    @Suppress("JAVA_CLASS_ON_COMPANION")
    companion object {
        @JvmStatic
        private val log = LoggerFactory.getLogger(javaClass.enclosingClass)
    }

    @Autowired(required = false)
    val dataFetchers: List<graphql.schema.DataFetcher<*>> = ArrayList()

    @Value("\${graphql.schama.location:classpath:/graphql/**/*.graphqls}")
    val schemaLocation: String = "classpath:/graphql/**/*.graphqls"

    @Bean
    @Lazy
    fun graphQl(): GraphQL {

        val typeDefinitionRegistry = typeDefinitionRegistry()
        val build = runtimeWiring()
        val schema = SchemaGenerator().makeExecutableSchema(typeDefinitionRegistry, build)

        return GraphQL.newGraphQL(schema).build()
    }

    private fun typeDefinitionRegistry(): TypeDefinitionRegistry {

        val typeDefinitionRegistry = TypeDefinitionRegistry()
        val schemaParser = SchemaParser()

        resourceResolver.getResources(schemaLocation)
            .map {
                log.info("Registering schema ${it.description}")
                schemaParser.parse(InputStreamReader(it.inputStream)) }
            .forEach {
                typeDefinitionRegistry.merge(it)
            }

        return typeDefinitionRegistry
    }

    private fun runtimeWiring(): RuntimeWiring {

        val runtimeWiring = RuntimeWiring.newRuntimeWiring()

        dataFetchers.forEach { registerDataFetcher(runtimeWiring, it) }

        return runtimeWiring.build()
    }

    private fun registerDataFetcher(runtimeWiring: RuntimeWiring.Builder, bean: graphql.schema.DataFetcher<*>) {

        val dataFetcherAnnotation = bean.javaClass.getAnnotation(DataFetcher::class.java)

        if(dataFetcherAnnotation != null) {
            with(dataFetcherAnnotation) {
                log.info("Registering data fetcher ${bean.javaClass.name} for ${type}.${field}")
                runtimeWiring.type(type) { it.dataFetcher(field, bean) }
            }
        } else {
            log.warn("DataFetcher without DataFetcher annotation")
        }

    }

}