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
 *  PreferenciaGridDTO.kt
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
 * Data class for PreferenciaGrid. This DTO is for Grid filters and
 * hide columns. Note that this DTO exploits the value JSON from Preferencia and PreferenciaNombre
 * nodes in Neo4j.
 *
 * This DTO convert from an to NEo4j entities.
 *
 * This DTO is for use between the Client that utilizes preferences in the
 * Grid and the IAM-server repo.
 *
 * @author rlh
 * @project : pref-service
 * @date October 2023
 */
data class PreferenciaGridDTO @JvmOverloads constructor(var id: Long? = null,
                                                        val gridName: String,
                                                        val preferencias: Collection<PrefenciaNombreDTO> = ArrayList()) {

    data class PrefenciaNombreDTO @JvmOverloads constructor(var id: Long? = null,
                                                            val prefName: String,
                                                            val gridName: String,
                                                            val publica: Boolean,
                                                            val owner: String = "",
                                                            val description: String,
        // from now these attributes are store in value for Neo4j
                                                            val orderColumns: Collection<String> = ArrayList(),
                                                            val hideColumns: Collection<String> = ArrayList(),
                                                            val freezeColumns: Collection<String> = ArrayList(),
                                                            val udfColumns: Collection<String> = ArrayList(),
                                                            val filters: Collection<FilterDTO> = ArrayList()) {
        companion object : IdDTOMapper<ObjectMapper, PreferenciaNombre, PrefenciaNombreDTO> {

            override fun fromEntity(mapper: ObjectMapper, entity: PreferenciaNombre): PrefenciaNombreDTO {
                val user = if (entity.usuario != null) entity.usuario!!.nombreUsuario else entity.owner
                val (orderColumns, hideColumns, freezeColumns, udfColumns, filters) =
                    Assign5(
                        columns(entity.valor, mapper, "orderColumns"),
                        columns(entity.valor, mapper, "hideColumns"),
                        columns(entity.valor, mapper, "freezeColumns"),
                        columns(entity.valor, mapper, "udfColumns"),
                        filters(entity.valor, mapper)
                    )

                val preferenciaNombreDTO = PrefenciaNombreDTO(id = entity.id,
                    prefName = entity.nombre,
                    gridName = entity.nombrePantalla,
                    publica = entity.publica,
                    owner = user,
                    description = entity.descripcion,
                    orderColumns = orderColumns,
                    hideColumns = hideColumns,
                    freezeColumns = freezeColumns,
                    udfColumns = udfColumns,
                    filters = filters)

                return preferenciaNombreDTO
            }

            fun columns(valor: String, mapper: ObjectMapper, columnType: String): Collection<String> {
                try {
                    val json = mapper.readValue(valor, JsonNode::class.java)
                    val jsonString = mapper.writeValueAsString(json.get(columnType))

                    if (jsonString == "null") return emptyList()
                    return mapper.readValue(jsonString, mapper.typeFactory.constructCollectionType(MutableList::class.java, String::class.java))
                } catch (e: IOException) {
                    throw PreferenciaException("Mal-formed Grid preferences ($columnType)")
                }
            }

            fun filters(valor: String, mapper: ObjectMapper): Collection<FilterDTO> {
                try {
                    val json = mapper.readValue(valor, JsonNode::class.java)
                    val jsonString = mapper.writeValueAsString(json.get("filters"))

                    if (jsonString == "null") return emptyList()
                    return mapper.readValue(jsonString, mapper.typeFactory.constructCollectionType(MutableList::class.java, FilterDTO::class.java))
                } catch (e: IOException) {
                    throw PreferenciaException("Mal-formed Grid preferences (filters)")
                }
            }

            class Assign5<A, B, C, D, E>(val a: A, val b: B, val c: C, val d: D, val e: E) {
                operator fun component1(): A = a
                operator fun component2(): B = b
                operator fun component3(): C = c
                operator fun component4(): D = d
                operator fun component5(): E = e
            }
        }

    }

    data class FilterDTO constructor(val columnName: String, val value: Any)

    companion object : IdDTOMapper<ObjectMapper, Preferencia, PreferenciaGridDTO> {

        override fun fromEntity(mapper: ObjectMapper, entity: Preferencia): PreferenciaGridDTO {

            val preferenciaDTO = PreferenciaGridDTO(id = entity.id,
                gridName = entity.nombre,
                preferencias = PrefenciaNombreDTO.mapFromEntities(mapper, entity.preferencias))

            return preferenciaDTO
        }
    }

}
