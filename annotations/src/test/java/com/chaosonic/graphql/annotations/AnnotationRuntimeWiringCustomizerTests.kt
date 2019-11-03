package com.chaosonic.graphql.annotations

import graphql.schema.DataFetchingEnvironment
import graphql.schema.idl.RuntimeWiring
import graphql.schema.idl.SchemaParser
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
import org.junit.runner.RunWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnitRunner
import org.springframework.context.ApplicationContext

@RunWith(MockitoJUnitRunner::class)
class AnnotationRuntimeWiringCustomizerTest {

    @Rule
    @JvmField
    var thrown: ExpectedException = ExpectedException.none()

    @Mock
    lateinit var appContext: ApplicationContext

    @Mock
    lateinit var graphQLMappingValidator: GraphQLMappingValidator

    @Mock
    lateinit var methodHandlerFactory: MethodHandlerFactory

    @Mock
    lateinit var runtimeWiringBuilder: RuntimeWiring.Builder

    @InjectMocks
    lateinit var customizer: AnnotationRuntimeWiringCustomizer

    @GraphQLHandler
    private class TestGraphGLHandler {

        @GraphQLMapping(type = "Type A", field = "field A")
        fun test1() = "OK"

        @GraphQLMapping(type = "Type B", field = "field B")
        fun test2() = "OK"
    }

    @Before
    fun setUp() {

        `when`(appContext.getBeansWithAnnotation(GraphQLHandler::class.java))
            .thenReturn(mapOf(Pair("any",
                TestGraphGLHandler()
            )))
    }

    @Test
    fun shouldCallRuntimeWiringBuilderOnceForEachHandlerMethod() {
        // when
        customizer.customize(runtimeWiringBuilder)
        // then
        verify(runtimeWiringBuilder, times(1)).type(eq("Type A"), any())
        verify(runtimeWiringBuilder, times(1)).type(eq("Type B"), any())
    }

    @Test
    fun shouldBreakOnInvalidMappingDeclaration() {

        // given
        `when`(graphQLMappingValidator.validate(
            any(),
            any()
        )).thenThrow(GraphQLConfigurationError::class.java)
        // then
        thrown.expect(GraphQLConfigurationError::class.java)
        // when
        customizer.customize(runtimeWiringBuilder)
        // and
        verify(runtimeWiringBuilder, times(0)).type(
            any(),
            any()
        )
    }
}

@RunWith(MockitoJUnitRunner::class)
class MethodHandlerFactoryTest {

    @Rule
    @JvmField
    var thrown: ExpectedException = ExpectedException.none()

    @GraphQLHandler
    private class TestGraphGLHandler {

        @GraphQLMapping(type = "Type A", field = "field A")
        fun test1() = "OK"

        @GraphQLMapping(type = "Type A", field = "field B")
        fun test2(env : DataFetchingEnvironment) = "OK"

        @GraphQLMapping(type = "Type A", field = "field B")
        fun test3(@GraphQLArgument("parameter 1") param : String) = "OK"

        @GraphQLMapping(type = "Type A", field = "field B")
        fun test4(@GraphQLSource param : Any) = "OK"

        @GraphQLMapping(type = "Type A", field = "field B")
        fun test5(param : String) = "OK"

        @GraphQLMapping(type = "Type A", field = "field B")
        fun test6(params : Map<String, String>) = "OK"
    }

    @Mock
    private lateinit var bean: TestGraphGLHandler

    @Mock
    lateinit var dataFetchingEnvironment: DataFetchingEnvironment

    private lateinit var methodHandlerFactory: MethodHandlerFactory

    @Before
    fun init() {
        methodHandlerFactory = MethodHandlerFactory()
    }

    @Test
    fun shouldHandleNoArgMethod() {
        // given
        val method = TestGraphGLHandler::class.java.getMethod("test1")
        // when
        val methodHandler = methodHandlerFactory.create(bean, method)
        methodHandler.invoke(dataFetchingEnvironment)
        // then
        verify(bean, times(1)).test1()
    }

    @Test
    fun shouldHandleDataFetchingEnvironmentParameter() {
        // given
        val method = TestGraphGLHandler::class.java.getMethod("test2", DataFetchingEnvironment::class.java)
        // when
        val methodHandler = methodHandlerFactory.create(bean, method)
        methodHandler.invoke(dataFetchingEnvironment)
        // then
        verify(bean, times(1)).test2(dataFetchingEnvironment)
    }

    @Test
    fun shouldHandleGraphQLArgumentParameter() {
        // given
        val method = TestGraphGLHandler::class.java.getMethod("test3", String::class.java)
        `when`(dataFetchingEnvironment.getArgument("parameter 1") as String?).thenReturn("VALUE")
        // when
        val methodHandler = methodHandlerFactory.create(bean, method)
        methodHandler.invoke(dataFetchingEnvironment)
        // then
        verify(bean, times(1)).test3("VALUE")
    }

    @Test
    fun shouldHandleGraphQLSourceParameter() {
        // given
        val method = TestGraphGLHandler::class.java.getMethod("test4", Any::class.java)
        `when`(dataFetchingEnvironment.getSource() as String?).thenReturn("VALUE")
        // when
        val methodHandler = methodHandlerFactory.create(bean, method)
        methodHandler.invoke(dataFetchingEnvironment)
        // then
        verify(bean, times(1)).test4("VALUE")
    }

    @Test
    fun shouldUseParameterNameWhenNoAnnoatationsArePresent() {
        // given
        val method = TestGraphGLHandler::class.java.getMethod("test5", String::class.java)
        `when`(dataFetchingEnvironment.getArgument("param") as String?).thenReturn("VALUE")
        // when
        val methodHandler = methodHandlerFactory.create(bean, method)
        methodHandler.invoke(dataFetchingEnvironment)
        // then
        verify(bean, times(1)).test5("VALUE")
    }


    @Test
    fun shouldInjectArgumentsMap() {
        // given
        val method = TestGraphGLHandler::class.java.getMethod("test6", String::class.java)
        val args = emptyMap<String, String>()
        `when`(dataFetchingEnvironment.arguments).thenReturn(args)
        // when
        val methodHandler = methodHandlerFactory.create(bean, method)
        methodHandler.invoke(dataFetchingEnvironment)
        // then
        verify(bean, times(1)).test6(args)
    }
}

@RunWith(MockitoJUnitRunner::class)
class GraphQLMappingValidatorTest {

    @Rule
    @JvmField
    var thrown: ExpectedException = ExpectedException.none()

    @GraphQLHandler
    private class TestGraphGLHandler {

        @GraphQLMapping(type = "TypeA", field = "fieldA")
        fun test1() = "OK"

        @GraphQLMapping(type = "TypeB", field = "fieldB")
        fun test2() = "OK"
    }


    private lateinit var graphQLMappingValidator: GraphQLMappingValidator

    @Before
    fun init() {
        val typeDefinitionRegistry = SchemaParser().parse("type TypeA {schema {query: TypeA}}")
        graphQLMappingValidator = GraphQLMappingValidator(typeDefinitionRegistry)
    }

    @Test
    fun shouldValidateCorrectGraphQLMapping() {
        // given
        val method = TestGraphGLHandler::class.java.getMethod("test1")
        // then
        graphQLMappingValidator.validate(method.getAnnotation(GraphQLMapping::class.java), method)
    }

    @Test
    fun shouldThrowOnUnknownType() {
        // given
        val method = TestGraphGLHandler::class.java.getMethod("test2")
        // then
        thrown.expect(GraphQLConfigurationError::class.java)
        // when
        graphQLMappingValidator.validate(method.getAnnotation(GraphQLMapping::class.java), method)
    }


}