package com.ailegorreta.prefservice

import com.ailegorreta.prefservice.repository.*
import com.ailegorreta.prefservice.service.preference.PrefService
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Bean
import org.springframework.test.context.ActiveProfiles
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder

/**
 * For a good test slices for testing @SpringBootTest, see:
 * https://reflectoring.io/spring-boot-test/
 * https://www.diffblue.com/blog/java/software%20development/testing/spring-boot-test-slices-overview-and-usage/
 *
 * This class test all context with @SpringBootTest annotation and checks that everything is loaded correctly.
 * Also creates the classes needed for all slices in @TestConfiguration annotation
 *
 * Testcontainers:
 *
 * Use for test containers Neo4j & Kafka following the next's ticks:
 *
 * - As little overhead as possible:
 * - Containers are started only once for all tests
 * - Containers are started in parallel
 * - No requirements for test inheritance
 * - Declarative usage.
 *
 * see article: https://maciejwalkowiak.com/blog/testcontainers-spring-boot-setup/
 *
 * Also for a problem with bootstrapServerProperty
 * see: https://blog.mimacom.com/embeddedkafka-kafka-auto-configure-springboottest-bootstrapserversproperty/
 *
 * @project pref-service
 * @autho: rlh
 * @date: October 2023
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@EnableTestContainers
/* ^ This is a custom annotation to load the containers */
@ExtendWith(MockitoExtension::class)
@ActiveProfiles("integration-tests")
class PrefServiceApplicationTests {

	@MockBean
	private var reactiveJwtDecoder: ReactiveJwtDecoder? = null            // Mocked the security JWT

	@Autowired
	private val prefRepository: PrefRepository? = null

	@Autowired
	private val prefNombreRepository: PrefNombreRepository? = null

	@Autowired
	private val usuarioRepository: UsuarioRepository? = null

	@Autowired
	private val prefService: PrefService? = null

	@Test
	fun contextLoads() {
		println("Preference repository:$prefRepository")
		println("Preference name repository:$prefNombreRepository")
		println("Users repository:$usuarioRepository")
		println("Pref service:$prefService")
		println("JwtDecoder:$reactiveJwtDecoder")
	}

	/**
	 * This TestConfiguration is for ALL file testers, so do not delete this class.
	 *
	 * This is to configure the ObjectMapper with JSR310Module and Java 8 JavaTime()
	 * module that it is not initialized for test mode. i.e., ObjectMapper @Autowired does not exist
	 */
	@TestConfiguration
	class ObjectMapperConfiguration {
		@Bean
		fun objectMapper(): ObjectMapper = ObjectMapper().findAndRegisterModules()

	}
}
