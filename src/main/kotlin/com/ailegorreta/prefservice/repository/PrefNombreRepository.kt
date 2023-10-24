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
 *  PrefNombreRepository.kt
 *
 *  Developed 2023 by LegoSoftSoluciones, S.C. www.legosoft.com.mx
 */
package com.ailegorreta.prefservice.repository

import com.ailegorreta.prefservice.model.entity.PreferenciaNombre
import org.springframework.data.neo4j.repository.Neo4jRepository
import org.springframework.data.neo4j.repository.query.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

/**
 * Interface de Neo4j repository for Entity PreferenciaNombreRepository
 *
 * @see Spring Data Neo4j⚡️RX for more information
 *
 * @author rlh
 * @project : pref-service
 * @date October 2023
 *
 */
@Repository
interface PrefNombreRepository : Neo4jRepository<PreferenciaNombre, Long> {

    fun findByOwner(owner: String): Collection<PreferenciaNombre>
    @Query("""
           MATCH (u:Usuario)<-[c:CREADOR]->(pn:PreferenciaNombre)
             where u.nombreUsuario = ${'$'}owner
           RETURN pn
    """)
    fun findByCreator(@Param("owner")owner: String) : Collection<PreferenciaNombre>

    fun save(preferenciaNombre: PreferenciaNombre): PreferenciaNombre

    @Query("""
        MATCH(pn:PreferenciaNombre)
          where (ID(pn) = ${'$'}id)
        SET pn.publica = ${'$'}publica, pn.descripcion = ${'$'}descripcion, pn.valor = ${'$'}valor 
        RETURN ID(pn)
    """)
    fun update(@Param("id") id: Long,
               @Param("publica")publica: Boolean,
               @Param("descripcion")descripcion: String,
               @Param("valor")valor: String): Long

    @Query("""
        MATCH (pn:PreferenciaNombre), (u:Usuario)
          where (ID(pn) = ${'$'}id) AND (ID(u) = ${'$'}idu)
        CREATE (pn)-[r:CREADOR]->(u)
        RETURN count(r)
    """)
    fun addUsuario(@Param("id") id:Long, @Param("idu")idu: Long): Long

}
