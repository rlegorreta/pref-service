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
 *  PreferenciaAnyDTO.kt
 *
 *  Developed 2023 by LegoSoftSoluciones, S.C. www.legosoft.com.mx
 */
package com.ailegorreta.prefservice.service.preference.dto

import com.ailegorreta.commons.dtomappers.IdDTOMapper
import com.fasterxml.jackson.databind.ObjectMapper
import com.ailegorreta.prefservice.model.entity.Preferencia
import com.ailegorreta.prefservice.model.entity.PreferenciaNombre

/**
 * Data class for Preferencia Generic. This DTO is for Form UDFs
 * Note that this DTO does NOT use the value JSON from Preferencia and PreferenciaNombre
 * nodes in Neo4j.
 *
 * This DTO convert from and to NEO4j entities.
 *
 * This DTO is for use between the Client that utilizes preferences in the
 * Form and the IAM-server repo.
 *
 * @author rlh
 * @project : pref-service
 * @date October 2023
 */
data class PreferenciaAnyDTO @JvmOverloads constructor(var id: Long? = null,
                                                       val name: String,
                                                       val preferencias: Collection<PreferenciaNombreDTO> = ArrayList()) {

    data class PreferenciaNombreDTO @JvmOverloads constructor(
        var id: Long? = null,
        val prefName: String,
        val name: String,
        val publica: Boolean,
        val owner: String = "",
        val description: String,
        val value: String
    ) {
        companion object : IdDTOMapper<ObjectMapper, PreferenciaNombre, PreferenciaNombreDTO> {

            override fun fromEntity(mapper: ObjectMapper, entity: PreferenciaNombre): PreferenciaNombreDTO {
                val user = if (entity.usuario != null) entity.usuario!!.nombreUsuario else entity.owner

                val preferenciaNombreDTO = PreferenciaNombreDTO(
                    id = entity.id,
                    prefName = entity.nombre,
                    name = entity.nombrePantalla,
                    publica = entity.publica,
                    owner = user,
                    description = entity.descripcion,
                    value = entity.valor
                )

                return preferenciaNombreDTO
            }
        }

    }

    data class FilterDTO constructor(val columnName: String, val value: Any)

    companion object : IdDTOMapper<ObjectMapper, Preferencia, PreferenciaAnyDTO> {

        override fun fromEntity(mapper: ObjectMapper, entity: Preferencia): PreferenciaAnyDTO {

            val preferenciaAnyDTO = PreferenciaAnyDTO(
                id = entity.id,
                name = entity.nombre,
                preferencias = PreferenciaNombreDTO.mapFromEntities(mapper, entity.preferencias)
            )

            return preferenciaAnyDTO
        }
    }
}

