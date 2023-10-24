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
 *  PrefIntegrationTests.kt
 *
 *  Developed 2023 by LegoSoftSoluciones, S.C. www.legosoft.com.mx
 */
package com.ailegorreta.prefservice.endpoints

import com.ailegorreta.commons.utils.HasLogger
import com.ailegorreta.prefservice.EnableTestContainers
import com.ailegorreta.prefservice.config.ResourceServerConfig
import com.ailegorreta.prefservice.config.ServiceConfig
import com.ailegorreta.prefservice.controller.PrefController
import com.ailegorreta.prefservice.service.preference.PrefService
import com.ailegorreta.prefservice.service.preference.dto.PreferenciaAnyDTO
import com.ailegorreta.prefservice.service.preference.dto.PreferenciaFormDTO
import com.ailegorreta.prefservice.service.preference.dto.PreferenciaGridDTO
import com.fasterxml.jackson.databind.ObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import org.neo4j.driver.Driver
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.util.UriComponentsBuilder
import reactor.core.publisher.Mono
import java.io.BufferedReader
import java.io.InputStreamReader
import java.time.*
import java.util.*

/**
 *  This test class uses Spring WebFlux testing REST so the UserContext cannot be utilized
 *  because it runs in a different thread.
 *
 *  When we want to send events to the event logger, therefore need to use the UserContext
 *  we cannot use the WebTestClient class
 *
 * @author rlh
 * @project pref-service
 * @date October 2023
 */

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@EnableTestContainers
@ExtendWith(MockitoExtension::class)
@Import(ServiceConfig::class, ResourceServerConfig::class, PrefController::class)
@ActiveProfiles("integration-tests-webflux")            // This is to permit duplicate singleton beans
class PrefIntegrationTests(@Autowired val applicationContext: ApplicationContext): HasLogger {


    var webTestClient: WebTestClient? = null

    /**
     * note : To simplify the REST testing we disabled the token checking. This is done in the SecurityConfiguration
     *        class and the method ResourceServerConfiguration.
     *        Before deployment the token has to be enabled again
     */

    @BeforeEach
    fun setup(@Autowired driver: Driver) {
        processCypher(driver, "/start.cypher", false)
        webTestClient = WebTestClient.bindToApplicationContext(applicationContext) // ^ add Spring Security test Support
                                    .apply(SecurityMockServerConfigurers.springSecurity())
                                    .configureClient()
                                    .build()
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
    fun `Read all preference by User to test the REST`()  {
        val uri = UriComponentsBuilder.fromUriString("/preference/preferencia/any/by/usuario")
        val res = webTestClient!!.mutateWith(SecurityMockServerConfigurers.mockJwt().authorities(
                                        listOf<GrantedAuthority>(
                                                SimpleGrantedAuthority("SCOPE_cartera.read"),
                                                SimpleGrantedAuthority("ROLE_ADMINLEGO")
                                        )
                                    )
                                )
                                .get()
                                .uri(uri.queryParam("usuario", "adminTEST").build().toUri())
                                .exchange()
                                .expectStatus().isOk()
                                .expectBody(MutableList::class.java)
                                .returnResult()
                                .responseBody

        assertThat(res!!.size).isEqualTo(4L)
    }

    @Test
    fun `Delete all preferences by user`(@Autowired prefService: PrefService,
                                      @Autowired mapper: ObjectMapper) {
        // read the first
        val uri = UriComponentsBuilder.fromUriString("/preference/preferencia/any/by/usuario")
        val res1 = webTestClient!!.mutateWith(SecurityMockServerConfigurers.mockJwt().authorities(
                                            listOf<GrantedAuthority>(
                                                SimpleGrantedAuthority("SCOPE_cartera.read"),
                                                SimpleGrantedAuthority("ROLE_ADMINLEGO")
                                            )
                                        )
                                    )
                                    .get()
                                    .uri(uri.queryParam("usuario", "adminTEST").build().toUri())
                                    .exchange()
                                    .expectStatus().isOk()
                                    .expectBody(MutableList::class.java)
                                    .returnResult()
                                    .responseBody
        assertThat(res1!!.size).isEqualTo(4L)

        // now delete it
        val uriDel = UriComponentsBuilder.fromUriString("/preference/preferencia/any/delete/by/nombre")
        val preferenceDTO = PreferenciaAnyDTO(null, "Posicion_Reporto",
                                                arrayListOf(PreferenciaAnyDTO.PreferenciaNombreDTO( id = null,
                                                    prefName = "Posicion_Reporto",  // don´t care data
                                                    description = "",
                                                    name = "Posicion_Reporto",
                                                    publica = true,
                                                    owner = "adminTEST",
                                                    value = "{}")))

        val res2 = webTestClient!!.mutateWith(SecurityMockServerConfigurers.mockJwt().authorities(
                                        listOf<GrantedAuthority>(
                                            SimpleGrantedAuthority("SCOPE_cartera.read"),
                                            SimpleGrantedAuthority("ROLE_ADMINLEGO")
                                        )
                                    )
                                )
                        .post()
                        .uri(uriDel.build().toUri())
                        .accept(MediaType.APPLICATION_JSON)
                        .body(Mono.just(preferenceDTO), PreferenciaAnyDTO::class.java)
                        .exchange()
                        .expectBody(PreferenciaAnyDTO::class.java)
                        .returnResult()
                        .responseBody

        assertThat(res2).isNotNull

        // re-read preferences
        val res3 = webTestClient!!.mutateWith(SecurityMockServerConfigurers.mockJwt().authorities(
                                                    listOf<GrantedAuthority>(
                                                        SimpleGrantedAuthority("SCOPE_cartera.read"),
                                                        SimpleGrantedAuthority("ROLE_ADMINLEGO")
                                                    )
                                            )
                                        )
                                        .get()
                                        .uri(uri.queryParam("usuario", "adminTEST").build().toUri())
                                        .exchange()
                                        .expectStatus().isOk()
                                        .expectBody(MutableList::class.java)
                                        .returnResult()
                                        .responseBody
        assertThat(res3!!.size).isEqualTo(0L)
    }
    @Test
    fun `Save a new grid preference`() {
        val prefGrid = PreferenciaGridDTO(gridName = "TESTGrid",
                                            preferencias = arrayListOf(
                                                PreferenciaGridDTO.PrefenciaNombreDTO(
                                                prefName= "TestResumen",
                                                gridName = "TestGrid",
                                                publica = true,
                                                owner = "adminTEST",
                                                description = "",
                                                orderColumns = ArrayList(),
                                                hideColumns = ArrayList(),
                                                freezeColumns = ArrayList(),
                                                udfColumns = ArrayList(),
                                                filters = ArrayList())))
        val uriAdd = UriComponentsBuilder.fromUriString("/preference/preferencia/grid/add")
        val res = webTestClient!!.mutateWith(SecurityMockServerConfigurers.mockJwt().authorities(
                                                                    listOf<GrantedAuthority>(
                                                                        SimpleGrantedAuthority("SCOPE_cartera.read"),
                                                                        SimpleGrantedAuthority("ROLE_ADMINLEGO")
                                                                    )
                                                                )
                                            )
                                            .post()
                                            .uri(uriAdd.build().toUri())
                                            .accept(MediaType.APPLICATION_JSON)
                                            .body(Mono.just(prefGrid), PreferenciaGridDTO::class.java)
                                            .exchange()
                                            .expectBody(PreferenciaGridDTO::class.java)
                                            .returnResult()
                                            .responseBody

        assertThat(res).isNotNull

        // now read any preference and must be + 1
        val uri = UriComponentsBuilder.fromUriString("/preference/preferencia/any/by/usuario")
        val res2 = webTestClient!!.mutateWith(SecurityMockServerConfigurers.mockJwt().authorities(
                                                                listOf<GrantedAuthority>(
                                                                    SimpleGrantedAuthority("SCOPE_cartera.read"),
                                                                    SimpleGrantedAuthority("ROLE_ADMINLEGO")
                                                                )
                                                            )
                                                        )
                                            .get()
                                            .uri(uri.queryParam("usuario", "adminTEST").build().toUri())
                                            .exchange()
                                            .expectStatus().isOk()
                                            .expectBody(MutableList::class.java)
                                            .returnResult()
                                            .responseBody

        assertThat(res2!!.size).isEqualTo(5L)
    }

    @Test
    fun `Read a grid preference by user name`() {
        val uri = UriComponentsBuilder.fromUriString("/preference/preferencia/grid/by/nombre")
        val res = webTestClient!!.mutateWith(SecurityMockServerConfigurers.mockJwt().authorities(
                                                        listOf<GrantedAuthority>(
                                                            SimpleGrantedAuthority("SCOPE_cartera.read"),
                                                            SimpleGrantedAuthority("ROLE_ADMINLEGO")
                                                        )
                                                    )
                                        )
                                        .get()
                                        .uri(uri
                                                .queryParam("gridName", "Posicion_reporto")
                                                .queryParam("owner", "adminTEST")
                                                .queryParam("usuario", "adminTEST")
                                            .build().toUri())
                                        .exchange()
                                        .expectStatus().isOk()
                                        .expectBody(PreferenciaGridDTO::class.java)
                                        .returnResult()
                                        .responseBody

        assertThat(res).isNotNull
    }

    @Test
    fun `Read a grid preference by user`() {
        val uri = UriComponentsBuilder.fromUriString("/preference/preferencia/grid/by/usuario")
        val res = webTestClient!!.mutateWith(SecurityMockServerConfigurers.mockJwt().authorities(
                                                                listOf<GrantedAuthority>(
                                                                    SimpleGrantedAuthority("SCOPE_cartera.read"),
                                                                    SimpleGrantedAuthority("ROLE_ADMINLEGO")
                                                                )
                                                            )
                                        )
                                        .get()
                                        .uri(uri.queryParam("usuario", "adminTEST")
                                            .build().toUri())
                                        .exchange()
                                        .expectStatus().isOk()
                                        .expectBody(MutableList::class.java)
                                        .returnResult()
                                        .responseBody

        assertThat(res).isNotNull
    }

    @Test
    fun `Read if exists grid preference by user`() {
        val uri = UriComponentsBuilder.fromUriString("/preference/preferencia/grid/by/nombre/exists")
        val res = webTestClient!!.mutateWith(SecurityMockServerConfigurers.mockJwt().authorities(
                                                                listOf<GrantedAuthority>(
                                                                    SimpleGrantedAuthority("SCOPE_cartera.read"),
                                                                    SimpleGrantedAuthority("ROLE_ADMINLEGO")
                                                                )
                                                            )
                                                        )
                                        .get()
                                        .uri(uri
                                            .uri(uri
                                                .queryParam("nombre", "Posicion_Reporto")
                                                .queryParam("prefNombre", "Resumen")
                                                .queryParam("usuario", "userTEST")
                                                .build().toUri())
                                            .build().toUri())
                                        .exchange()
                                        .expectStatus().isOk()
                                        .expectBody(String::class.java)
                                        .returnResult()
                                        .responseBody

        assertThat(res).isEqualTo("true")
    }
    @Test
    fun `Save a new form preference`() {
        val prefForm = PreferenciaFormDTO(formName = "TestForm",
                                preferencias = arrayListOf(
                                    PreferenciaFormDTO.PrefenciaNombreDTO(
                                    prefName= "TestResumen",
                                    formName = "TestForm",
                                    publica = true,
                                    owner = "userTEST",
                                    description = "",
                                    udfs = ArrayList())))

        val uriAdd = UriComponentsBuilder.fromUriString("/preference/preferencia/form/add")
        val res = webTestClient!!.mutateWith(SecurityMockServerConfigurers.mockJwt().authorities(
                                                                    listOf<GrantedAuthority>(
                                                                        SimpleGrantedAuthority("SCOPE_cartera.read"),
                                                                        SimpleGrantedAuthority("ROLE_ADMINLEGO")
                                                                    )
                                                            )
                                )
                                .post()
                                .uri(uriAdd.build().toUri())
                                .accept(MediaType.APPLICATION_JSON)
                                .body(Mono.just(prefForm), PreferenciaFormDTO::class.java)
                                .exchange()
                                .expectBody(PreferenciaFormDTO::class.java)
                                .returnResult()
                                .responseBody

        assertThat(res).isNotNull
    }

    @Test
    fun `Read a form preference by name`() {
        val uri = UriComponentsBuilder.fromUriString("/preference/preferencia/form/by/nombre")
        val res = webTestClient!!.mutateWith(SecurityMockServerConfigurers.mockJwt().authorities(
                                                                listOf<GrantedAuthority>(
                                                                    SimpleGrantedAuthority("SCOPE_cartera.read"),
                                                                    SimpleGrantedAuthority("ROLE_ADMINLEGO")
                                                                )
                                                            )
                                    )
                                    .get()
                                    .uri(uri
                                        .queryParam("formName", "Mov_reporto")
                                        .queryParam("owner", "adminTEST")
                                        .queryParam("usuario", "adminTEST")
                                        .build().toUri())
                                    .exchange()
                                    .expectStatus().isOk()
                                    .expectBody(PreferenciaFormDTO::class.java)
                                    .returnResult()
                                    .responseBody

        assertThat(res!!.preferencias).isEmpty()
    }

    @Test
    fun `Read a form preference by user`() {
        val uri = UriComponentsBuilder.fromUriString("/preference/preferencia/form/by/usuario")
        val res = webTestClient!!.mutateWith(SecurityMockServerConfigurers.mockJwt().authorities(
                                                                    listOf<GrantedAuthority>(
                                                                        SimpleGrantedAuthority("SCOPE_cartera.read"),
                                                                        SimpleGrantedAuthority("ROLE_ADMINLEGO")
                                                                    )
                                                                )
                                    )
                                    .get()
                                    .uri(uri
                                        .queryParam("usuario", "adminTEST")
                                        .build().toUri())
                                    .exchange()
                                    .expectStatus().isOk()
                                    .expectBody(MutableList::class.java)
                                    .returnResult()
                                    .responseBody

        assertThat(res!!.size).isEqualTo(4L)
    }
    @Test
    fun `Read if exists form preference by user`() {
        val uri = UriComponentsBuilder.fromUriString("/preference/preferencia/form/by/nombre/exists")
        val res = webTestClient!!.mutateWith(SecurityMockServerConfigurers.mockJwt().authorities(
                                                            listOf<GrantedAuthority>(
                                                                SimpleGrantedAuthority("SCOPE_cartera.read"),
                                                                SimpleGrantedAuthority("ROLE_ADMINLEGO")
                                                            )
                                                        )
                                            )
                                            .get()
                                            .uri(uri
                                                .uri(uri
                                                    .queryParam("nombre", "Posicion_Reporto")
                                                    .queryParam("prefNombre", "Resumen")
                                                    .queryParam("usuario", "userTEST")
                                                    .build().toUri())
                                                .build().toUri())
                                            .exchange()
                                            .expectStatus().isOk()
                                            .expectBody(String::class.java)
                                            .returnResult()
                                            .responseBody

        assertThat(res).isEqualTo("true")
    }

}
