package com.chaosonic.graphql.lib

import graphql.schema.DataFetchingEnvironment
import graphql.schema.idl.RuntimeWiring
import graphql.schema.idl.TypeDefinitionRegistry
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Component
import java.lang.reflect.Method
import java.lang.reflect.Parameter

@Component
class AnnotationRuntimeWiringCustomizer(
    private val typeDefinitionRegistry: TypeDefinitionRegistry,
    private val applicationContext: ApplicationContext
) : RuntimeWiringCustomizer {

    @Suppress("JAVA_CLASS_ON_COMPANION")
    private companion object {

        @JvmStatic
        private val log = LoggerFactory.getLogger(javaClass.enclosingClass)

        private fun <T> DataFetchingEnvironment.getArgument(name: String, clazz: Class<T>): T =
            this.getArgument<T>(name)

        private fun <T> DataFetchingEnvironment.getSource(clazz: Class<T>): T = this.getSource<T>()
    }

    override fun customize(builder: RuntimeWiring.Builder) {

        val graphQLHandlers = applicationContext.getBeansWithAnnotation(GraphQLHandler::class.java)

        graphQLHandlers.forEach { (_, handler) ->
            handler.javaClass.declaredMethods.forEach { registerMethodHandler(builder, handler, it) }
        }
    }

    private fun registerMethodHandler(runtimeWiring: RuntimeWiring.Builder, bean: Any, method: Method) {

        val annotation = method.getDeclaredAnnotation(GraphQLMapping::class.java) ?: return
        val methodName = "${bean.javaClass.name}.${method.name}"

        log.info("Registering GraphQL mapping for ${annotation.type}.${annotation.field} [$methodName]")

        if (typeDefinitionRegistry.getType(annotation.type).isEmpty) {
            throw GraphQLConfigurationError("Type '${annotation.type}' not found in the schema [$methodName]")
        }

        val parameterHandlers = method.parameters
            .map { param ->
                createParamHandler(param)
                    ?: throw GraphQLConfigurationError("Unknown parameter '${param.name}' [$methodName]")
            }

        val methodHandler =
            { env: DataFetchingEnvironment ->
                method.invoke(
                    bean,
                    *(parameterHandlers.map { h -> h(env) }.toTypedArray())
                )
            }

        runtimeWiring.type(annotation.type) { it.dataFetcher(annotation.field, methodHandler) }
    }

    private fun createParamHandler(param: Parameter): ((DataFetchingEnvironment) -> Any?)? {

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

        return null
    }
}