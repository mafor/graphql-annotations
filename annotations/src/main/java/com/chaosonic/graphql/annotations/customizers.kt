package com.chaosonic.graphql.annotations

import graphql.schema.idl.RuntimeWiring
import graphql.schema.idl.TypeDefinitionRegistry

interface RuntimeWiringCustomizer {
    fun customize(builder: RuntimeWiring.Builder)
}

interface TypeDefinitionRegistryCustomizer {
    fun customize(registry: TypeDefinitionRegistry)
}