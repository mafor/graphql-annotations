package com.chaosonic.graphql.lib

import graphql.schema.DataFetchingEnvironment
import graphql.schema.idl.RuntimeWiring
import graphql.schema.idl.TypeDefinitionRegistry
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Component
import java.lang.reflect.Method
import java.lang.reflect.Parameter

private typealias ParameterHandler = (env: DataFetchingEnvironment) -> Any?
private typealias MethodHandler = (env: DataFetchingEnvironment) -> Any?

private fun location(method: Method) = "${method.declaringClass.name}.${method.name}"

@Component
class AnnotationRuntimeWiringCustomizer(
    private val applicationContext: ApplicationContext,
    private val validator: GraphQLMappingValidator,
    private val methodHandlerFactory: MethodHandlerFactory
) : RuntimeWiringCustomizer {

    @Suppress("JAVA_CLASS_ON_COMPANION")
    private companion object {
        @JvmStatic
        private val log = LoggerFactory.getLogger(javaClass.enclosingClass)
    }

    override fun customize(builder: RuntimeWiring.Builder) {

        applicationContext.getBeansWithAnnotation(GraphQLHandler::class.java).forEach { (_, bean) ->
            bean.javaClass.declaredMethods.forEach { method ->
                method.getDeclaredAnnotation(GraphQLMapping::class.java)?.let { annotation ->

                    log.info("Registering data fetcher for the ${annotation.type}.${annotation.field} [${location(method)}]")
                    validator.validate(annotation, method)

                    builder.type(annotation.type) {
                        it.dataFetcher(
                            annotation.field,
                            methodHandlerFactory.create(bean, method)
                        )
                    }
                }
            }
        }
    }
}

@Component
class GraphQLMappingValidator(private val typeDefinitionRegistry: TypeDefinitionRegistry) {

    fun validate(annotation: GraphQLMapping, method: Method) {

        if (!typeDefinitionRegistry.getType(annotation.type).isPresent) {
            throw GraphQLConfigurationError(
                "Type '${annotation.type}' not found in the schema [${location(method)}]"
            )
        }
    }
}

@Component
class MethodHandlerFactory {

    private val parameterHandlers = listOf(
        ::graphQLArgumentAnnotationHandler,
        ::sourceAnnotationHandler,
        ::dataFetchingEnvironmentParameterTypeHandler
    )

    fun create(bean: Any, method: Method): MethodHandler {

        return method.parameters
            .map { param ->
                parameterHandlers.asSequence().map { it(param) }.firstOrNull { it != null }
                    ?: throw GraphQLConfigurationError("Unknown parameter '${param.name}' [${location(method)}]")
            }
            .let { params -> { env -> method.invoke(bean, *(params.map { h -> h(env) }.toTypedArray())) } }
    }

    private fun graphQLArgumentAnnotationHandler(param: Parameter): ParameterHandler? =

        param.getAnnotation(GraphQLArgument::class.java)?.let {
            { env: DataFetchingEnvironment -> env.getArgument(it.name, param.type) }
        }

    private fun sourceAnnotationHandler(param: Parameter): ParameterHandler? =

        param.getAnnotation(GraphQLSource::class.java)?.let {
            { env: DataFetchingEnvironment -> env.getSource(param.type) }
        }

    private fun dataFetchingEnvironmentParameterTypeHandler(param: Parameter): ParameterHandler? =

        if (param.type.isAssignableFrom(DataFetchingEnvironment::class.java)) {
            { env: DataFetchingEnvironment -> env }
        } else null

    private fun <T> DataFetchingEnvironment.getArgument(name: String, clazz: Class<T>): T = getArgument<T>(name)

    private fun <T> DataFetchingEnvironment.getSource(clazz: Class<T>): T = getSource<T>()

}
