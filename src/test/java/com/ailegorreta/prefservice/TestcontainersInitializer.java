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
 *  TestcontainersInitializer.java
 *
 *  Developed 2023 by LegoSoftSoluciones, S.C. www.legosoft.com.mx
 */
package com.ailegorreta.prefservice;

import com.ailegorreta.resourceserver.utils.HasLogger;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.Neo4jContainer;
import org.testcontainers.containers.Neo4jLabsPlugin;
import org.testcontainers.lifecycle.Startables;
import org.testcontainers.utility.DockerImageName;

import static org.testcontainers.shaded.org.awaitility.Awaitility.await;

/**
 * This is a class to start the containers only once for all tests
 *
 * Algo it starts the container in parallel
 *
 * @project pref-service
 * @author rlh
 * @date October 2023
 */
class TestcontainersInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext>, HasLogger {

    static Neo4jContainer<?> neo4j = new Neo4jContainer<>(DockerImageName.parse("neo4j:4.4.5"))
                        .withLabsPlugins(Neo4jLabsPlugin.APOC)
                        // .withNeo4jConfig("dbms.security.procedures.unrestricted", "gds*,apoc.*")
                        .withAdminPassword("verysecret");

    static {
        Startables.deepStart(neo4j).join();
        await().until( neo4j::isRunning);
    }

    /**
     * Sets all environment variables without the need to create a
     * @ActiveProfiles("integration-flyway")
     *
     * @param ctx the application to configure
     */
    @Override
    public void initialize(ConfigurableApplicationContext ctx) {
        TestPropertyValues.of(
                "spring.neo4j.uri=" + neo4j.getBoltUrl(),
                "spring.neo4j.authentication.username=" + "neo4j",
                "spring.neo4j.authentication.password=" + neo4j.getAdminPassword()
        ).applyTo(ctx.getEnvironment());
        getLogger().info("Neo4j url: {}", neo4j.getBoltUrl());
        getLogger().info("Neo4j password(not needed): {}", neo4j.getAdminPassword());
    }

    /**
     * Sets all environment variables without the need to create a
     * @ActiveProfiles("integration-test")
     *
     * note: Do NOT use public void initialize(ConfigurableApplicationContext ctx) because
     *       for Neo4j container it does not work (i.e. it tries to connect before)
     */
    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
    }

    @NotNull
    @Override
    public Logger getLogger() { return HasLogger.DefaultImpls.getLogger(this); }
}
