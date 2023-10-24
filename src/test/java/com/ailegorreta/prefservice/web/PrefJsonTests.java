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
 *  PrefJsonTests.kt
 *
 *  Developed 2023 by LegoSoftSoluciones, S.C. www.legosoft.com.mx
 */
package com.ailegorreta.prefservice.web;

import com.ailegorreta.prefservice.service.preference.dto.PreferenciaGridDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Example for a JsonTest for some Neo4j DTOs, many others can be added
 *
 * In this case we check that PreferenciaGridDTO is serializable correctly
 *
 * @project pref-service
 * @author rlh
 * @date October 2023
 */
@JsonTest
@ContextConfiguration(classes = com.ailegorreta.prefservice.web.PrefJsonTests.class)
@ActiveProfiles("integration-tests")
public class PrefJsonTests {
    @Autowired
    public JacksonTester<PreferenciaGridDTO> json;

    @Test
    void testSerialize() throws Exception {
        final var prefName = new PreferenciaGridDTO.PrefenciaNombreDTO(0L,
                "TestResumen", "TestGrid", true, "TEST", "",
                new ArrayList<>(), new ArrayList<>(),new ArrayList<>());
        var preferences = new ArrayList<PreferenciaGridDTO.PrefenciaNombreDTO>() {{
                                add(prefName);
                            }};
        var prefGridDTO = new PreferenciaGridDTO(0L, "TESTGrid", preferences);
        var jsonContent = json.write(prefGridDTO);

        assertThat(jsonContent).extractingJsonPathNumberValue("@.id")
                .isEqualTo(prefGridDTO.getId().intValue());
        assertThat(jsonContent).extractingJsonPathStringValue("@.gridName")
                .isEqualTo(prefGridDTO.getGridName().toString());
    }

    @Test
    void testDeserialize() throws Exception {
        final var prefName = new PreferenciaGridDTO.PrefenciaNombreDTO(0L,
                "TestResumen", "TestGrid", true, "TEST", "",
                new ArrayList<>(), new ArrayList<>(),new ArrayList<>());
        var preferences = new ArrayList<PreferenciaGridDTO.PrefenciaNombreDTO>() {{
                                                                add(prefName);
                                }};
        var prefGridDTO = new PreferenciaGridDTO(0L, "TESTGrid", preferences);
        var content = """
                {
                    "id": 
                    """ + "\"" + prefGridDTO.getId() + "\"," + """
                    "gridName":
                    """ + "\"" + prefGridDTO.getGridName() + "\"," + """              
                "preferencias":[
                  {
                  "id":0,
                  "prefName":
                   """ + "\"" + prefName.getPrefName() + "\"," + """
                  "gridName":
                   """ + "\"" + prefName.getGridName() + "\"," + """
                  "publica": true,
                  "owner":
                   """ + "\"" + prefName.getOwner() + "\"," + """
                  "description": "",
                  "orderColumns": [],
                  "hideColumns": [],
                  "freezeColumns": [],
                  "udfColumns": [],
                  "filters":[]
                  }
                ]
                }               
                """;
        assertThat(json.parse(content))
                .usingRecursiveComparison()
                .isEqualTo(prefGridDTO);
    }
}

