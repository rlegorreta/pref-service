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
 *  PreferenciaFormDTO.kt
 *
 *  Developed 2023 by LegoSoftSoluciones, S.C. www.legosoft.com.mx
 */
package com.ailegorreta.prefservice.service.preference.dto

import com.ailegorreta.commons.dtomappers.IdDTOMapper
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.ailegorreta.prefservice.exception.PreferenciaException
import com.ailegorreta.prefservice.model.entity.Preferencia
import com.ailegorreta.prefservice.model.entity.PreferenciaNombre
import java.io.IOException

/**
 * Data class for PreferenciaForm. This DTO is for Form UDFs
 * Note that this DTO exploits the value JSON from Preferencia and PreferenciaNombre
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
data class PreferenciaFormDTO @JvmOverloads constructor(var id: Long? = null,
                                                        val formName: String,
                                                        val preferencias: Collection<PrefenciaNombreDTO> = ArrayList()) {

    data class PrefenciaNombreDTO @JvmOverloads constructor(var id: Long? = null,
                                                            val prefName: String,
                                                            val formName: String,
                                                            val publica: Boolean,
                                                            val owner: String = "",
                                                            val description: String,
        // from now these attributes are store in value for Neo4j
                                                            val udfs: Collection<String> = ArrayList()) {
        companion object : IdDTOMapper<ObjectMapper, PreferenciaNombre, PrefenciaNombreDTO> {

            override fun fromEntity(mapper: ObjectMapper, entity: PreferenciaNombre): PrefenciaNombreDTO {
                val user = if (entity.usuario != null) entity.usuario!!.nombreUsuario else entity.owner
                val udfs = columns(entity.valor, mapper, "udfs")

                val preferenciaNombreDTO = PrefenciaNombreDTO(id = entity.id,
                    prefName = entity.nombre,
                    formName = entity.nombrePantalla,
                    publica = entity.publica,
                    owner = user,
                    description = entity.descripcion,
                    udfs = udfs)

                return preferenciaNombreDTO
            }

            private fun columns(valor: String, mapper: ObjectMapper, columnType: String): Collection<String> {
                try {
                    val json = mapper.readValue(valor, JsonNode::class.java)
                    val jsonString = mapper.writeValueAsString(json.get(columnType))

                    if (jsonString == "null") return emptyList()
                    return mapper.readValue(jsonString, mapper.typeFactory.constructCollectionType(MutableList::class.java, String::class.java))
                } catch (e: IOException) {
                    throw PreferenciaException("Mal-formed Form preferences ($columnType)")
                }
            }
        }

    }

    data class FilterDTO constructor(val columnName: String, val value: Any)

    companion object : IdDTOMapper<ObjectMapper, Preferencia, PreferenciaFormDTO> {

        override fun fromEntity(mapper: ObjectMapper, entity: Preferencia): PreferenciaFormDTO {

            return PreferenciaFormDTO(
                id = entity.id,
                formName = entity.nombre,
                preferencias = PrefenciaNombreDTO.mapFromEntities(mapper, entity.preferencias)
            )
        }
    }

}
