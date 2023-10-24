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
package com.ailegorreta.prefservice.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.savedrequest.NoOpServerRequestCache

/**
 * Resource server configuration.
 *
 * Three scopes are defined for this resource:
 *  -cartera.rad: all access to PrefController
 *
 * @author rlh
 * @project : pref-service
 * @date October 2023
 *
 */
@EnableWebFluxSecurity
@Configuration(proxyBeanMethods = false)
class ResourceServerConfig {
    // @formatter:off
    /**
     *  -- This code is we want for develop purpose to use all REST calls without a token --
     *  -- For example: if you want to run the REST from swagger and test the micro service
     * http.authorizeHttpRequests{ auth ->  auth
     *     .requestMatchers("/ **").permitAll()
     *     .anyRequest().authenticated()
     *
     * note: erse white space between '/ **' ) just for comment
     **/
    @Bean
    @Throws(Exception::class)
    fun reactiveSecurityFilterChain( http:ServerHttpSecurity): SecurityWebFilterChain {

        http.authorizeExchange{oauth2 ->  oauth2
                                .pathMatchers("/actuator/**").permitAll()
                                .pathMatchers("/preference/**").hasAuthority("SCOPE_cartera.read")
                                .anyExchange().authenticated()
                            }
            .oauth2ResourceServer{ server -> server.jwt { Customizer.withDefaults<Any>()}}
            .requestCache{ requestCacheSpec ->
                requestCacheSpec.requestCache(NoOpServerRequestCache.getInstance())
            }
            .csrf(ServerHttpSecurity.CsrfSpec::disable)

        return http.build()
    }
    // @formatter:on
}
