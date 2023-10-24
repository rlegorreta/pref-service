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
 *  UsuarioDTO.kt
 *
 *  Developed 2023 by LegoSoftSoluciones, S.C. www.legosoft.com.mx
 */
package com.ailegorreta.prefservice.service.preference.dto

import com.ailegorreta.commons.dtomappers.EntityDTOMapper
import java.time.*
import com.fasterxml.jackson.annotation.*

import com.ailegorreta.prefservice.model.entity.Usuario

/**
 * Data class for UsuarioDTO for Facultad Controller.
 *
 * @author rlh
 * @project : pref-service
 * @date October 2023
 */
@JsonIgnoreProperties(value = ["grupos", "companias", "supervisor", "perfil", "sinFacultades", "extraFacultades"])
data class UsuarioDTO @JvmOverloads constructor (val id : Long? = null,
					 val idUsuario: Long,
					 val nombre: String,
					 val nombreUsuario: String,
				 	 val apellido: String,
					 val telefono: String,
					 val mail: String,
					 @JsonProperty("activo")
					 val activo: Boolean,
					 @JsonProperty("administrador")
					 val administrador: Boolean,
					 @JsonProperty("interno")
					 val interno: Boolean,
					 val fechaIngreso: LocalDate,
					 val usuarioModificacion: String,
					 val zonaHoraria: String?,
					 val fechaModificacion: LocalDateTime = LocalDateTime.now()) {
												 
    companion object : EntityDTOMapper<Usuario, UsuarioDTO> {
		override var dtos = HashMap<Int, Any>()

        override fun fromEntityRecursive(entity: Usuario): UsuarioDTO {
        	val a = dtos.get(entity.hashCode())
			
			if (a != null)
				return a as UsuarioDTO

			val usuarioDTO = UsuarioDTO(id = entity.id,
										idUsuario = entity.idUsuario,
										nombreUsuario = entity.nombreUsuario,
										nombre = entity.nombre,
										apellido = entity.apellido,
										telefono = entity.telefono,
										mail = entity.mail,
										activo = entity.activo,
										interno = entity.interno,
										administrador = entity.administrador,
										fechaIngreso = entity.fechaIngreso,
										zonaHoraria = entity.zonaHoraria,
										usuarioModificacion = entity.usuarioModificacion,
										fechaModificacion = entity.fechaModificacion)

			dtos.put(entity.hashCode(), usuarioDTO)
			
			return usuarioDTO
		}
    }
}
