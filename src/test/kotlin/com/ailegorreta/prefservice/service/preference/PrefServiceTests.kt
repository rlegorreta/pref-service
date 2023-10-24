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
 *  PrefServiceTests.kt
 *
 *  Developed 2023 by LegoSoftSoluciones, S.C. www.legosoft.com.mx
 */

package com.ailegorreta.prefservice.service.preference

import com.ailegorreta.prefservice.service.AbstractServiceTest
import com.ailegorreta.prefservice.EnableTestContainers
import com.ailegorreta.prefservice.service.preference.dto.PreferenciaAnyDTO
import com.ailegorreta.prefservice.service.preference.dto.PreferenciaFormDTO
import com.ailegorreta.prefservice.service.preference.dto.PreferenciaGridDTO
import org.junit.jupiter.api.*
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.assertj.core.api.Assertions.assertThat

/**
 * Test for the PrefService.
 *
 * @author rlh
 * @project: pref-service
 * @date October 2023
 *
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@EnableTestContainers
/* ^ This is a custom annotation to load the containers */
@ExtendWith(MockitoExtension::class)
@ActiveProfiles("integration-tests")
class PrefServiceTests: AbstractServiceTest() {

    @Test
    fun `Get Preference by User`(@Autowired prefService: PrefService) {
        val prefs = prefService.getPreferenceAnyByUsuario("adminTEST")

        assertThat(prefs.size).isEqualTo(4L)
    }
    @Test
    fun `Delete preferences name by User`(@Autowired prefService: PrefService) {
        val prefs = prefService.getPreferenceAnyByUsuario("adminTEST")
        val prefAny = PreferenciaAnyDTO(name = "Posicion_Directo", preferencias = prefs)

        prefService.deletePreferenciaAny(prefAny)
        assertThat(prefService.getPreferenceAnyByUsuario("adminTEST").size).isEqualTo(3L)
        // ^ One less. Just one preference deleted
    }

    @Test
    fun `Get Preference by Grid`(@Autowired prefService: PrefService) {
        val prefGrids = prefService.getPreferenceGrid("Posicion_Directo", "adminTEST", "TEST")

        assertThat(prefGrids.preferencias.size).isEqualTo(1L)
    }

    @Test
    fun `Save preferences for a Grid component`(@Autowired prefService: PrefService) {
        val prefGrid = PreferenciaGridDTO(gridName = "TESTGrid",
            preferencias = arrayListOf(PreferenciaGridDTO.PrefenciaNombreDTO(
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

        prefService.savePreferenciaGrid(prefGrid)
        assertThat(prefService.getPreferenceGrid("TestGrid","adminTEST", "adminTEST")).isNotNull
    }

    @Test
    fun `Has preference Grid with Other owner`(@Autowired prefService: PrefService) {
        assertThat(prefService.hasPreferenceGridWithOtherOwner("Posicion_Reporto","Resumen", "userTEST")).isTrue()
    }
    @Test
    fun `Get Preference by Form`(@Autowired prefService: PrefService) {
        val prefForm = prefService.getPreferenceForm("Mov_efectivo", "adminTEST", "userTEST")

        assertThat(prefForm.preferencias).isEmpty()
    }
    @Test
    fun `Save preferences for a Form component`(@Autowired prefService: PrefService) {
        val prefForm = PreferenciaFormDTO(formName = "TestForm",
            preferencias = arrayListOf(PreferenciaFormDTO.PrefenciaNombreDTO(
                prefName= "TestResumen",
                formName = "TestGrid",
                publica = true,
                owner = "adminTEST",
                description = "",
                udfs = ArrayList())))

        prefService.savePreferenciaForm(prefForm)
        assertThat(prefService.getPreferenceForm("TestForm","userTEST", "userTEST")).isNotNull
    }

}
