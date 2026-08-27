package oblitusnumen.bondcalculator.data.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import oblitusnumen.bondcalculator.data.database.entity.SecurityEntity

@Dao
interface SecurityDao {

    @Query("""
        SELECT *
        FROM securities
        WHERE secId = :secId
          AND boardId = :boardId
        LIMIT 1
    """)
    suspend fun get(
        secId: String,
        boardId: String
    ): SecurityEntity?

    @Query("""
        SELECT *
        FROM securities
        WHERE secId = :secId
        ORDER BY boardId
    """)
    suspend fun getBySecId(
        secId: String
    ): List<SecurityEntity>

    @Query("""
        SELECT *
        FROM securities
        WHERE secId = :secId
          AND boardId = :boardId
        LIMIT 1
    """)
    fun observe(
        secId: String,
        boardId: String
    ): Flow<SecurityEntity?>

    @Query("""
        SELECT *
        FROM securities
        WHERE secId = :secId
        ORDER BY boardId
    """)
    fun observeBySecId(
        secId: String
    ): Flow<List<SecurityEntity>>

    @Query("""
        SELECT *
        FROM securities
        ORDER BY secId, boardId
    """)
    fun observeAll(): Flow<List<SecurityEntity>>

    @Upsert
    suspend fun upsert(
        security: SecurityEntity
    )

    @Upsert
    suspend fun upsertAll(
        securities: List<SecurityEntity>
    )

    @Query("""
        DELETE FROM securities
        WHERE secId = :secId
          AND boardId = :boardId
    """)
    suspend fun delete(
        secId: String,
        boardId: String
    )

    @Query("""
        DELETE FROM securities
    """)
    suspend fun deleteAll()
}