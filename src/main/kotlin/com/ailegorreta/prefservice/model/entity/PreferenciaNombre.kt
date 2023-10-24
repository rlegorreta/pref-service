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
 *  PreferenciaNombre.kt
 *
 *  Developed 2023 by LegoSoftSoluciones, S.C. www.legosoft.com.mx
 */
package com.ailegorreta.prefservice.model.entity

import org.springframework.data.neo4j.core.schema.*

/**
 * Preference instance by name entity. This is the detail of all
 * preference instance defined by the user
 *
 * @author rlh
 * @project : pref-service
 * @date October 2023
 *
 */
@Node("PreferenciaNombre")
data class PreferenciaNombre(@Id @GeneratedValue var id: Long? = null,
                             @Property(name = "nombre") 	var nombre: String,
                             @Property(name = "nombrePantalla") var nombrePantalla:String,
                             @Property(name = "publica") var publica: Boolean,
                             @Property(name = "owner") var owner: String,
                             @Property(name = "descripcion") var descripcion: String,
                             @Property(name = "valor") var valor: String = "sin valor", // normally is a JSON value
                             @Relationship(type = "CREADOR", direction = Relationship.Direction.OUTGOING) var usuario: Usuario?)
