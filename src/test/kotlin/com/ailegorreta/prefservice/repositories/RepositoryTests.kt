/* Copyright (c) 2023, LegoSoft Soluciones, S.C.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are not permitted.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 *  RepositoryTests.kt
 *
 *  Developed 2023 by LegoSoftSoluciones, S.C. www.legosoft.com.mx
 */
package com.ailegorreta.prefservice.repositories

import com.ailegorreta.commons.utils.HasLogger
import com.ailegorreta.prefservice.EnableTestContainers
import com.ailegorreta.prefservice.model.entity.Preferencia
import com.ailegorreta.prefservice.model.entity.PreferenciaNombre
import com.ailegorreta.prefservice.repository.PrefNombreRepository
import com.ailegorreta.prefservice.repository.PrefRepository
import com.ailegorreta.prefservice.repository.UsuarioRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import org.neo4j.driver.Driver
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.neo4j.DataNeo4jTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder
import org.springframework.test.context.ActiveProfiles
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.function.Consumer

@DataNeo4jTest
@EnableTestContainers
/* ^ This is a custom annotation to load the containers */
@ExtendWith(MockitoExtension::class)
@ActiveProfiles("integration-tests")
class RepositoryTests: HasLogger {
    @MockBean
    private var reactiveJwtDecoder: ReactiveJwtDecoder? = null            // Mocked the security JWT

    /**
     * Adding what the database has been created in the Data Initialization, we add another cypher for testing purpose
     * only.
     */
    @BeforeEach
    fun setup(@Autowired driver: Driver) {
        processCypher(driver, "/start.cypher", false)
    }

    private fun processCypher(driver: Driver, fileName: String, commandByLine: Boolean) {
        BufferedReader(InputStreamReader(this.javaClass.getResourceAsStream(fileName))).use { testReader ->
            logger.info("Start process $fileName")
            driver.session().use { session ->
                do {
                    val cypher: String? = if (commandByLine) testReader.readLine()
                                            else testReader.readText()

                    if (!cypher.isNullOrBlank())
                        session.run(cypher)
                               .consume()
                } while (commandByLine && !cypher.isNullOrBlank())
            }
            logger.info("Finish process $fileName")
        }
    }

    @Test
    fun `Check all where created in prefDBStart`(@Autowired prefRepository: PrefRepository,
                                                 @Autowired prefNombreRepository: PrefNombreRepository) {
        assertThat(prefRepository.findAll().count()).isEqualTo(4L)
        assertThat(prefNombreRepository.findAll().count()).isEqualTo(4L)
        prefNombreRepository.findAll().forEach {
            assertThat(it).satisfies(Consumer { prefNombre -> assertThat(prefNombre.owner).isEqualTo("TEST") })
        }
    }

    @Test
    fun `Check where created a Preference`(@Autowired driver: Driver,
                                           @Autowired prefRepository: PrefRepository,
                                           @Autowired prefNombreRepository: PrefNombreRepository) {
        val newPreference = prefRepository.save(Preferencia(nombre = "Preferencia TEST"))

        assertThat(prefRepository.findById(newPreference.id!!).get()).isNotNull()

        val newPreferenceNombre = prefNombreRepository.save(PreferenciaNombre(
                                                                nombrePantalla = "Posicion_Derivados",
                                                                publica = true,
                                                                owner = "TEST",
                                                                nombre = "Preferencia TEST",
                                                                descripcion = "",
                                                                valor = "{}",
                                                                usuario = null))

        assertThat(prefNombreRepository.findById(newPreferenceNombre.id!!).get()).isNotNull()
    }

    @Test
    fun `Check Preference name by owner`(@Autowired prefNombreRepository: PrefNombreRepository) {
        val prefs = prefNombreRepository.findByOwner("adminTEST")

        assertThat(prefs.size).isEqualTo(4L)
    }
    @Test
    fun `Check Preference name by owner creator`(@Autowired prefNombreRepository: PrefNombreRepository) {
        val prefs = prefNombreRepository.findByCreator("adminTEST")

        assertThat(prefs.size).isEqualTo(4L)
    }

    @Test
    fun `Check User by its name`(@Autowired usuarioRepository: UsuarioRepository) {
        val user = usuarioRepository.findByNombreUsuario("adminTEST")

        assertThat(user).isNotNull
    }

}
