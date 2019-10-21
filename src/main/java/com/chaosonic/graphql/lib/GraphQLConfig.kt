package com.chaosonic.graphql.lib

import graphql.GraphQL
import graphql.schema.DataFetchingEnvironment
import graphql.schema.idl.RuntimeWiring
import graphql.schema.idl.SchemaGenerator
import graphql.schema.idl.SchemaParser
import graphql.schema.idl.TypeDefinitionRegistry
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Lazy
import org.springframework.core.io.support.ResourcePatternResolver
import java.io.InputStreamReader
import java.lang.reflect.Method
import java.lang.reflect.Parameter

private fun <T> DataFetchingEnvironment.getArgument(name: String, clazz: Class<T>): T = this.getArgument<T>(name)

private fun <T> DataFetchingEnvironment.getSource(clazz: Class<T>): T = this.getSource<T>()

@Configuration
class GraphQlConfig(val resourceResolver: ResourcePatternResolver, val applicationContext: ApplicationContext) {

    @Suppress("JAVA_CLASS_ON_COMPANION")
    companion object {
        @JvmStatic
        private val log = LoggerFactory.getLogger(javaClass.enclosingClass)
    }

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
                schemaParser.parse(InputStreamReader(it.inputStream))
            }
            .forEach {
                typeDefinitionRegistry.merge(it)
            }

        return typeDefinitionRegistry
    }

    private fun runtimeWiring(): RuntimeWiring {

        val runtimeWiring = RuntimeWiring.newRuntimeWiring()

        val beans = applicationContext.getBeansWithAnnotation(GraphQLController::class.java)

        beans.forEach { (_, bean) ->
            bean.javaClass.declaredMethods.forEach { registerMethodHandler(runtimeWiring, bean, it) }
        }

        return runtimeWiring.build()
    }

    fun registerMethodHandler(runtimeWiring: RuntimeWiring.Builder, bean: Any, method: Method) {

        val annotation = method.getDeclaredAnnotation(GraphQLMapping::class.java) ?: return

        log.info("Registering GraphQL mapping for ${annotation.type}.${annotation.field} [${bean.javaClass.name}.${method.name}]")

        val argHandlers = method.parameters.map { param -> createParamHandler(param) }
        val handler =
            { env: DataFetchingEnvironment -> method.invoke(bean, *(argHandlers.map { h -> h(env) }.toTypedArray())) }
        runtimeWiring.type(annotation.type) { it.dataFetcher(annotation.field, handler) }
    }

    fun createParamHandler(param: Parameter): (DataFetchingEnvironment) -> Any? {

        val paramAnnotation = param.getAnnotation(GraphQLArgument::class.java)

        if (paramAnnotation != null) {
            return { env: DataFetchingEnvironment -> env.getArgument(paramAnnotation.name, param.type) }
        }

        val sourceAnnotation = param.getAnnotation(GraphQLSource::class.java)

        if (sourceAnnotation != null) {
            return { env: DataFetchingEnvironment -> env.getSource(param.type) }
        }

        if (param.type.isAssignableFrom(DataFetchingEnvironment::class.java)) {
            return { env: DataFetchingEnvironment -> env }
        }

        throw RuntimeException("Couldn't handle the ${param.name}")
    }
}