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
 *  AbstractServiceTest.kt
 *
 *  Developed 2023 by LegoSoftSoluciones, S.C. www.legosoft.com.mx
 */
package com.ailegorreta.prefservice.service

import com.ailegorreta.resourceserver.utils.HasLogger
import org.junit.jupiter.api.BeforeEach
import org.neo4j.driver.Driver
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder
import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * Abstract class to test any type of service
 *
 * @author rlh
 * @project: pref-service
 * @date October 2023
 *
 */
abstract class AbstractServiceTest: HasLogger {

    @MockBean
    private var reactiveJwtDecoder: ReactiveJwtDecoder? = null			// Mocked the security JWT

    @BeforeEach
    fun setUp(@Autowired driver: Driver) {
        processCypher(driver, "/start.cypher", false)
    }

    fun processCypher(driver: Driver, fileName: String, commandByLine: Boolean) {
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
}