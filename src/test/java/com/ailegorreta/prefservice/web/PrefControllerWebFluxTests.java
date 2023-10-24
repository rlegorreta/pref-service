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
 *  ResourceServerConfig.kt
 *
 *  Developed 2023 by LegoSoftSoluciones, S.C. www.legosoft.com.mx
 */
package com.ailegorreta.prefservice.web;

import com.ailegorreta.commons.utils.HasLogger;
import com.ailegorreta.prefservice.EnableTestContainers;
import com.ailegorreta.prefservice.config.ResourceServerConfig;
import com.ailegorreta.prefservice.config.ServiceConfig;
import com.ailegorreta.prefservice.controller.PrefController;
import com.ailegorreta.prefservice.repository.PrefNombreRepository;
import com.ailegorreta.prefservice.repository.PrefRepository;
import com.ailegorreta.prefservice.repository.UsuarioRepository;
import com.ailegorreta.prefservice.service.preference.PrefService;
import com.ailegorreta.prefservice.service.preference.dto.PreferenciaAnyDTO;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockJwt;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.springSecurity;

/**
 * This class tests the REST calls received
 *
 * @proyect: pref-service
 * @author: rlh
 * @date: October 2023
 */
@WebFluxTest(PrefController.class)
@EnableTestContainers
@ExtendWith(MockitoExtension.class)
@Import({ServiceConfig.class, ResourceServerConfig.class, PrefController.class})
@ActiveProfiles("integration-tests-webflux")            // This is to permit duplicate singleton beans
class PrefControllerWebFluxTests implements HasLogger {

    @Autowired
    ApplicationContext applicationContext;
    @MockBean
    PrefService prefService;
    @MockBean
    PrefRepository prefRepository;
    @MockBean
    PrefNombreRepository prefNombreRepository;
    @MockBean
    UsuarioRepository usuarioRepository;

    // @Autowired
    WebTestClient webTestClient;

    @MockBean
    private ReactiveJwtDecoder reactiveJwtDecoder;
    /* ^ Mocks the Reactive JwtDecoder so that the application does not try to call Spring Security Server and get the
     * public keys for decoding the Access Token  */

    @BeforeEach
    void setUp() {
        webTestClient = WebTestClient.bindToApplicationContext(applicationContext)
                // ^ add Spring Security test Support
                .apply(springSecurity())
                .configureClient()
                .build();
    }

    /**
     * Test for listing all preferences for a user.
     *
     * note: This is just ONE example, many other REST calls can e added, but we did not include them because the
     * REST call is the same as call the service. If for some reason the controller has some code we need to add
     * a test in this class otherwise is the same as for service tests
     */
    @Test
    void whenGetAllPreferencesAndAuthenticatedTheShouldReturn200() {
        var uri = UriComponentsBuilder.fromUriString("/preference/preferencia/any/by/usuario?usuario=adminTEST");

        when (prefService.getPreferenceAnyByUsuario("adminTEST")).thenReturn(
                new ArrayList<>( Collections.singleton(
                        new PreferenciaAnyDTO.PreferenciaNombreDTO( 0L,
                "Resumen", "Posicion_Reporto", true, "adminTEST", "", "{}"))));

        getLogger().debug("Read preferences by usuario");
        var res = webTestClient.mutateWith(mockJwt().authorities(Arrays.asList(new SimpleGrantedAuthority("SCOPE_cartera.read"),
                                                                               new SimpleGrantedAuthority("ROLE_ADMINLEGO"))))
                                .get()
                                .uri(uri.build().toUri())
                                .exchange()
                                .expectStatus().isOk()
                                .expectBody(List.class)
                                .returnResult()
                                .getResponseBody();
        assertThat(res.size()).isEqualTo(1);
    }

    @NotNull
    @Override
    public Logger getLogger() { return HasLogger.DefaultImpls.getLogger(this); }
}
