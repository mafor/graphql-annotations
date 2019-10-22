package com.chaosonic.graphql.lib

import graphql.schema.idl.SchemaParser
import graphql.schema.idl.TypeDefinitionRegistry
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.support.ResourcePatternResolver
import org.springframework.stereotype.Component
import java.io.InputStreamReader

@Component
class ClasspathTypeDefinitionRegistryCustomizer(
    private val resourceResolver: ResourcePatternResolver
) : TypeDefinitionRegistryCustomizer {

    @Suppress("JAVA_CLASS_ON_COMPANION")
    private companion object {
        @JvmStatic
        private val log = LoggerFactory.getLogger(javaClass.enclosingClass)
    }

    @Value("\${graphql.schema.location:classpath:/graphql/**/*.graphqls}")
    private val schemaLocation: String = "classpath:/graphql/**/*.graphqls"

    override fun customize(registry: TypeDefinitionRegistry) {

        val schemaParser = SchemaParser()

        resourceResolver.getResources(schemaLocation)
            .map {
                log.info("Registering schema ${it.description}")
                schemaParser.parse(InputStreamReader(it.inputStream))
            }
            .forEach { registry.merge(it) }
    }

}