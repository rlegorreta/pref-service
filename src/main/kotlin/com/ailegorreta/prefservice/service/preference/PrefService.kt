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
 *  PrefService.kt
 *
 *  Developed 2023 by LegoSoftSoluciones, S.C. www.legosoft.com.mx
 */
package com.ailegorreta.prefservice.service.preference

import com.ailegorreta.commons.utils.HasLogger
import com.fasterxml.jackson.databind.ObjectMapper
import com.ailegorreta.prefservice.exception.PreferenciaException
import com.ailegorreta.prefservice.model.entity.Preferencia
import com.ailegorreta.prefservice.model.entity.PreferenciaNombre
import com.ailegorreta.prefservice.repository.PrefNombreRepository
import com.ailegorreta.prefservice.repository.PrefRepository
import com.ailegorreta.prefservice.repository.UsuarioRepository
import com.ailegorreta.prefservice.service.preference.dto.PreferenciaAnyDTO
import com.ailegorreta.prefservice.service.preference.dto.PreferenciaFormDTO
import com.ailegorreta.prefservice.service.preference.dto.PreferenciaGridDTO
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Preference service that includes all preferences services for
 * Preference controller.
 *
 * @author rlh
 * @project : pref-service
 * @date October 2023
 *
 */
@Service
class PrefService(private val prefRepository: PrefRepository,
                    private val prefNombreRepository: PrefNombreRepository,
                    private val usuarioRepository: UsuarioRepository,
                    private val mapper: ObjectMapper): HasLogger {

    /**
     * Read all preferences for a User independent on what type of preference
     *
     * note: The list of preferenciaNombre does not come with any relationship
     *       initialized
     */
    fun getPreferenceAnyByUsuario(usuario: String) = PreferenciaAnyDTO.PreferenciaNombreDTO.mapFromEntities(mapper, prefNombreRepository.findByCreator(usuario))

    /**
     * This method deletes a PrefName node and its relationships. Preferencia is left even
     * if none PrefName exist in the database.
     *
     */
    @Transactional
    fun deletePreferenciaAny(preferenciaAnyDTO: PreferenciaAnyDTO): PreferenciaAnyDTO {
        val preference = prefRepository.findDepthByNombre(preferenciaAnyDTO.name)

        if (!preference.isPresent)
            throw PreferenciaException("La preferencia ${preferenciaAnyDTO.name} no existe en la base de datos")

        preferenciaAnyDTO.preferencias.forEach { it ->
            val preferenciaNombreDTO = it

            val preferenciaNombre = preference.get().preferencias!!.asSequence()
                                                .filter { it.nombre == preferenciaNombreDTO.prefName }
                                                .toList()
            preference.get().preferencias!!.removeAll(preferenciaNombre)
            prefNombreRepository.deleteAll(preferenciaNombre)
        }
        logger.debug("Delete preference ${preferenciaAnyDTO.name}")

        return preferenciaAnyDTO
    }

    /**
     * Save or update a preference for Grid components
     */
    @Transactional
    fun savePreferenciaGrid(preferenciaGridDTO: PreferenciaGridDTO): PreferenciaGridDTO {
        // we suppose that just one preferenciaNombre is sent, so we use the first one
        if (preferenciaGridDTO.preferencias.size != 1)
            throw PreferenciaException("La preferencia ${preferenciaGridDTO.gridName} debe de tener UN solo valor")

        val preferenciaNombreDTO = preferenciaGridDTO.preferencias.first()

        logger.debug("Actualicación de  preferencia:${preferenciaGridDTO.gridName} con el nombre:${preferenciaNombreDTO.prefName}")

        // Get the user data
        val owner = usuarioRepository.findByNombreUsuario(preferenciaNombreDTO.owner)
            ?: throw PreferenciaException("El usuario ${preferenciaNombreDTO.owner} no existe en la base de datos")

        var preferencia = prefRepository.findByNombre(preferenciaGridDTO.gridName)

        if (preferencia == null) { // it is a new one, so we inserted
            preferencia = Preferencia(nombre = preferenciaGridDTO.gridName)
            preferencia = prefRepository.save(preferencia)
        }

        var preferenciaNombre: PreferenciaNombre? = null

        preferencia.preferencias?.forEach {
            if (it.nombre.equals(preferenciaNombreDTO.prefName))
                preferenciaNombre = it
        }
        if (preferenciaNombre == null) { // it is a new preferenciaNombre
            preferenciaNombre = PreferenciaNombre(nombre = preferenciaNombreDTO.prefName,
                                                    nombrePantalla = preferenciaGridDTO.gridName,
                                                    publica = preferenciaNombreDTO.publica,
                                                    descripcion = preferenciaNombreDTO.description,
                                                    owner = preferenciaNombreDTO.owner,
                                                    usuario = null)
            preferenciaNombre = prefNombreRepository.save(preferenciaNombre!!)
            prefRepository.addPreferenciaNombre(preferencia.id!!, preferenciaNombre!!.id!!)
            preferencia.addPreferencia(preferenciaNombre!!)
            prefNombreRepository.addUsuario(preferenciaNombre!!.id!!, owner.id!!)
            preferenciaNombre!!.usuario = owner
        } else {
            preferenciaNombre = prefNombreRepository.findById(preferenciaNombre!!.id!!).get()
            // ^ preferenciaNombre does not have the reletionship to usuario so we need to re-read it
            if (preferenciaNombre!!.usuario == null) {
                prefNombreRepository.addUsuario(preferenciaNombre!!.id!!, owner.id!!)
                preferenciaNombre!!.usuario = owner
            } else if (!preferenciaNombre!!.usuario!!.id!!.equals(owner.id))
                throw PreferenciaException("La preferencia ${preferenciaNombreDTO.prefName} ya esta definida por otro usuario")
        }

        // now just update de values
        preferenciaNombre!!.publica = preferenciaNombreDTO.publica
        preferenciaNombre!!.descripcion = preferenciaNombreDTO.description
        preferenciaNombre!!.valor = mapper.writeValueAsString(preferenciaNombreDTO)
        // ^ for simplicity we just write the JSON and some fields are repeated (no big deal)
        prefNombreRepository.update(preferenciaNombre!!.id!!, preferenciaNombre!!.publica, preferenciaNombre!!.descripcion, preferenciaNombre!!.valor)

        return PreferenciaGridDTO.fromEntity(mapper, preferencia)
    }

    /**
     * Read all preferences for a Grid component
     *
     * If owner is specified, just for this owner otherwise all preferences
     * are read independent of the owner
     *
     * Is the preference is private the user must be the same as the owner
     */
    fun getPreferenceGrid(gridName: String, owner: String?, usuario: String): PreferenciaGridDTO {
        val preference = prefRepository.findDepthByNombre(gridName)

        if (!preference.isPresent)
            return PreferenciaGridDTO(null, gridName)

        val filteredPrefs: LinkedHashSet<PreferenciaNombre> = linkedSetOf()

        if (preference.get().preferencias != null && preference.get().preferencias!!.isNotEmpty())
            preference.get().preferencias!!.asSequence()
                .filter { (owner == null) || it.usuario!!.nombreUsuario == owner }
                .filter { it.publica || it.usuario!!.nombreUsuario == usuario }
                .toCollection(filteredPrefs)
        preference.get().preferencias = filteredPrefs

        return PreferenciaGridDTO.fromEntity(mapper, preference.get())
    }

    /**
     * Read all preferences for a User
     *
     * note: The list of preferenciaNombre does not come with any relationship
     *       initialized
     */
    fun getPreferenceGridByUsuario(usuario: String) = PreferenciaGridDTO.PrefenciaNombreDTO.mapFromEntities(mapper, prefNombreRepository.findByCreator(usuario))

    /**
     * This method return just a true/false value and checks if the user wants to get
     * a preference name that other already have it (for the GridName or Preference)
     */
    fun hasPreferenceGridWithOtherOwner(nombre: String, prefNombre: String, owner: String): Boolean {
        val preference = prefRepository.getByNombreAndPrefNombreAndNotOwner(nombre, prefNombre, owner)

        return preference.isNotEmpty()
    }

    /**
     * Save or update a preference for Form components
     */
    @Transactional
    fun savePreferenciaForm(preferenciaFormDTO: PreferenciaFormDTO): PreferenciaFormDTO {
        // we suppose that just one preferenciaNombre is sent, so we use the first one
        if (preferenciaFormDTO.preferencias.size != 1)
            throw PreferenciaException("La preferencia ${preferenciaFormDTO.formName} debe de tener UN solo valor")

        val preferenciaNombreDTO = preferenciaFormDTO.preferencias.first()

        logger.debug("Actualicación de  preferencia:${preferenciaFormDTO.formName} con el nombre:${preferenciaNombreDTO.prefName}")

        // Get the user data
        val owner = usuarioRepository.findByNombreUsuario(preferenciaNombreDTO.owner)
            ?: throw PreferenciaException("El usuario ${preferenciaNombreDTO.owner} no existe en la base de datos")

        var preferencia = prefRepository.findByNombre(preferenciaFormDTO.formName)

        if (preferencia == null) { // it is a new one, so we inserted
            preferencia = Preferencia(nombre = preferenciaFormDTO.formName)
            preferencia = prefRepository.save(preferencia)
        }

        var preferenciaNombre: PreferenciaNombre? = null

        preferencia.preferencias?.forEach {
            if (it.nombre == preferenciaNombreDTO.prefName)
                preferenciaNombre = it
        }
        if (preferenciaNombre == null) { // it is a new preferenciaNombre
            preferenciaNombre = PreferenciaNombre(nombre = preferenciaNombreDTO.prefName,
                nombrePantalla = preferenciaFormDTO.formName,
                publica = preferenciaNombreDTO.publica,
                descripcion = preferenciaNombreDTO.description,
                owner = preferenciaNombreDTO.owner,
                usuario = null)
            preferenciaNombre = prefNombreRepository.save(preferenciaNombre!!)
            prefRepository.addPreferenciaNombre(preferencia.id!!, preferenciaNombre!!.id!!)
            preferencia.addPreferencia(preferenciaNombre!!)
            prefNombreRepository.addUsuario(preferenciaNombre!!.id!!, owner.id!!)
            preferenciaNombre!!.usuario = owner
        } else {
            preferenciaNombre = prefNombreRepository.findById(preferenciaNombre!!.id!!).get()
            // ^ preferenciaNombre does not have the reletionship to usuario so we need to re-read it
            if (preferenciaNombre!!.usuario == null) {
                prefNombreRepository.addUsuario(preferenciaNombre!!.id!!, owner.id!!)
                preferenciaNombre!!.usuario = owner
            } else if (preferenciaNombre!!.usuario!!.id!! != owner.id)
                throw PreferenciaException("La preferencia ${preferenciaNombreDTO.prefName} ya esta definida por otro usuario")
        }

        // now just update de values
        preferenciaNombre!!.publica = preferenciaNombreDTO.publica
        preferenciaNombre!!.descripcion = preferenciaNombreDTO.description
        preferenciaNombre!!.valor = mapper.writeValueAsString(preferenciaNombreDTO)
        // ^ for simplicity we just write the JSON and some fields are repeated (no big deal)
        prefNombreRepository.update(preferenciaNombre!!.id!!, preferenciaNombre!!.publica, preferenciaNombre!!.descripcion, preferenciaNombre!!.valor)

        return PreferenciaFormDTO.fromEntity(mapper, preferencia)
    }

    /**
     * Read all preferences for a Form component
     *
     * If owner is specified, just for this owner otherwise all preferences
     * are read independent from the owner
     *
     * Is the preference is private the user must be the same as the owner
     */
    fun getPreferenceForm(formName: String, owner: String?, usuario: String): PreferenciaFormDTO {
        val preference = prefRepository.findDepthByNombre(formName)

        if (!preference.isPresent)
            return PreferenciaFormDTO(null, formName)

        val filteredPrefs: LinkedHashSet<PreferenciaNombre> = linkedSetOf()

        preference.get().preferencias!!.asSequence()
            .filter { (owner == null) || it.usuario!!.nombreUsuario == owner }
            .filter { it.publica || it.usuario!!.nombreUsuario == usuario }
            .toCollection(filteredPrefs)
        preference.get().preferencias = filteredPrefs

        return PreferenciaFormDTO.fromEntity(mapper, preference.get())
    }

    /**
     * Read all preferences for a User
     *
     * note: The list of preferenciaNombre does not come with any relationship
     *       initialized
     */
    fun getPreferenceFormByUsuario(usuario: String) = PreferenciaFormDTO.PrefenciaNombreDTO.mapFromEntities(mapper, prefNombreRepository.findByCreator(usuario))

    /**
     * This method return just a true/false value and checks if the user wants to get
     * a preference name that other already have it (for the GridName or Preference)
     */
    fun hasPreferenceFormWithOtherOwner(nombre: String, prefNombre: String, owner: String): Boolean {
        val preference = prefRepository.getByNombreAndPrefNombreAndNotOwner(nombre, prefNombre, owner)

        return preference.isNotEmpty()
    }

}
