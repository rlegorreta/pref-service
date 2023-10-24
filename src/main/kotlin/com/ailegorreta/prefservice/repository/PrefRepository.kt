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
 *  PrefRepository.kt
 *
 *  Developed 2023 by LegoSoftSoluciones, S.C. www.legosoft.com.mx
 */
package com.ailegorreta.prefservice.repository

import com.ailegorreta.prefservice.model.entity.Preferencia
import org.springframework.data.neo4j.repository.Neo4jRepository
import org.springframework.data.neo4j.repository.query.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*


/**
 * Interface de Neo4j repository for Entity Prerefencia and
 * PreferenciaNombre
 *
 * @see Spring Data Neo4j⚡️RX for more information
 *
 * @author rlh
 * @project : pref-service
 * @date October 2023
 *
 */
@Repository
interface PrefRepository : Neo4jRepository<Preferencia, Long> {

    fun findByNombre(@Param("nombre")nombre: String): Preferencia?

    @Query("""
        MATCH (p:Preferencia {nombre:${'$'}nombre})
        OPTIONAL MATCH (p)-[m:PREFERENCIA]->(pn:PreferenciaNombre)-[n:CREADOR]->(u:Usuario)
         RETURN p, collect(m), collect(pn), collect(n), collect(u)
    """)
    fun findDepthByNombre(@Param("nombre")nombre: String): Optional<Preferencia>

    fun save(preferencia: Preferencia): Preferencia

    @Query("""
        MATCH (p:Preferencia)-[pr:PREFERENCIA]->(pn:PreferenciaNombre)-[c:CREADOR]->(u:Usuario)
            where p.nombre = ${'$'}nombre AND
                  pn.nombre = ${'$'}prefNombre AND
                  u.nombreUsuario <> ${'$'}owner
        RETURN p
    """)
    fun getByNombreAndPrefNombreAndNotOwner(@Param("nombre")nombre: String,
                                            @Param("prefNombre") prefNombre: String,
                                            @Param("owner")owner: String) : List<Preferencia>

    @Query("""
        MATCH (p:Preferencia), (pn:PreferenciaNombre)
            where (ID(p) = ${'$'}id) AND (ID(pn) = ${'$'}idpn)
        CREATE (p)-[r:PREFERENCIA]->(pn)
        RETURN count(r)
    """)
    fun addPreferenciaNombre(@Param("id") id:Long, @Param("idpn")idpn: Long): Long

}
