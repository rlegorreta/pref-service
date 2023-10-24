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
 *  PrefController.kt
 *
 *  Developed 2023 by LegoSoftSoluciones, S.C. www.legosoft.com.mx
 */
package com.ailegorreta.prefservice.controller

import com.ailegorreta.commons.utils.HasLogger
import com.ailegorreta.prefservice.service.preference.PrefService
import org.springframework.web.bind.annotation.*

import com.ailegorreta.prefservice.exception.PreferenciaException
import com.ailegorreta.prefservice.service.preference.dto.PreferenciaAnyDTO
import com.ailegorreta.prefservice.service.preference.dto.PreferenciaFormDTO
import com.ailegorreta.prefservice.service.preference.dto.PreferenciaGridDTO

/**
 * Controller for all REST services for the daily operation for the user preference.
 *
 * @author rlh
 * @project : pref-service
 * @date October 2023
 * 
 */
@CrossOrigin
@RestController
@RequestMapping("/preference")
class PrefController (val prefService: PrefService): HasLogger {

	@GetMapping("/preferencia/any/by/usuario")
	fun getPreferenceAnyByUsuario(@RequestParam(required = false)usuario: String) = prefService.getPreferenceAnyByUsuario(usuario)

	@PostMapping("/preferencia/any/delete/by/nombre")
	fun deletePreferencia(@RequestBody preferenciaAnyDTO: PreferenciaAnyDTO) = prefService.deletePreferenciaAny(preferenciaAnyDTO)

	// GRID preference services
	@PostMapping("/preferencia/grid/add")
	fun savePreferenciaGrid(@RequestBody preferenciaGridDTO: PreferenciaGridDTO) =
		try {
			prefService.savePreferenciaGrid(preferenciaGridDTO)
		} catch (e: PreferenciaException) {
			logger.error("Error when saving a preference for grid ${preferenciaGridDTO.gridName}")
		}

	@GetMapping("/preferencia/grid/by/nombre")
	fun getPreferenceGrid(@RequestParam(required = true)gridName: String,
						  @RequestParam(required = false)owner: String?,
						  @RequestParam(required = true)usuario: String) = prefService.getPreferenceGrid(gridName, owner, usuario)

	@GetMapping("/preferencia/grid/by/usuario")
	fun getPreferenceGridByUsuario(@RequestParam(required = false)usuario: String) = prefService.getPreferenceGridByUsuario(usuario)

	@GetMapping("/preferencia/grid/by/nombre/exists")
	fun hasPreferenceGridWithOtherOwner(@RequestParam(required = true) nombre: String,
										@RequestParam(required = true) prefNombre: String,
										@RequestParam(required = true) usuario : String) = prefService.hasPreferenceGridWithOtherOwner(nombre, prefNombre, usuario)

	@PostMapping("/preferencia/form/add")
	fun savePreferenciaForm(@RequestBody preferenciaFormDTO: PreferenciaFormDTO) =
		try {
			prefService.savePreferenciaForm(preferenciaFormDTO)
		} catch (e: PreferenciaException) {
			logger.error("Error saving preference for ${preferenciaFormDTO.formName}")
		}

	@GetMapping("/preferencia/form/by/nombre")
	fun getPreferenceForm(@RequestParam(required = true)formName: String,
						  @RequestParam(required = false)owner: String?,
						  @RequestParam(required = true)usuario: String) =
		prefService.getPreferenceForm(formName, owner, usuario)

	@GetMapping("/preferencia/form/by/usuario")
	fun getPreferenceFormByUsuario(@RequestParam(required = false)usuario: String) = prefService.getPreferenceFormByUsuario(usuario)

	@GetMapping("/preferencia/form/by/nombre/exists")
	fun hasPreferenceFormWithOtherOwner(@RequestParam(required = true) nombre: String,
										@RequestParam(required = true) prefNombre: String,
										@RequestParam(required = true) usuario : String) = prefService.hasPreferenceFormWithOtherOwner(nombre, prefNombre, usuario)

}
