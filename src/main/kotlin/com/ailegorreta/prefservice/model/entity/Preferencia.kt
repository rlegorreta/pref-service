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
 *  Preferencia.kt
 *
 *  Developed 2023 by LegoSoftSoluciones, S.C. www.legosoft.com.mx
 */
package com.ailegorreta.prefservice.model.entity

import org.springframework.data.neo4j.core.schema.*

/**
 * Profile entity. This is the 'job' for the Preference entity.
 *
 * The relation to Usuarios is where the preferences values are stored
 *
 * @author rlh
 * @project : pref-service
 * @date October 2023
 *
 */
@Node("Preferencia")
data class Preferencia(@Id @GeneratedValue var id: Long? = null,
                       @Property(name = "nombre") 		var nombre: String,
                       @Relationship(type = "PREFERENCIA", direction = Relationship.Direction.OUTGOING)
                       var preferencias: LinkedHashSet<PreferenciaNombre>? = null) {

    fun addPreferencia(preferencia: PreferenciaNombre) { if (preferencias != null) preferencias!!.add(preferencia) else preferencias = linkedSetOf(preferencia) }

    fun removePreferencia(preferencia: PreferenciaNombre) { if (preferencias != null) preferencias!!.remove(preferencia) else throw Exception("found empty collection") }

}
