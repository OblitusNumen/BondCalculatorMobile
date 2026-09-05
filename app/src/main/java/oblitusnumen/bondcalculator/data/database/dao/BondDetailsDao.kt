package oblitusnumen.bondcalculator.data.database.dao

import BondDetailsEntity
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface BondDetailsDao {
    @Query(
        """
        SELECT * 
        FROM bond_details 
        WHERE secid = :secid
    """
    )
    suspend fun get(secid: String): BondDetailsEntity?

    @Query(
        """
        SELECT * 
        FROM bond_details 
        WHERE secid = :secid
    """
    )
    fun observe(secid: String): Flow<BondDetailsEntity?>

    @Upsert
    suspend fun upsert(entity: BondDetailsEntity)

    @Upsert
    suspend fun upsertAll(entities: List<BondDetailsEntity>)

    @Query("DELETE FROM bond_details WHERE secid = :secid")
    suspend fun delete(secid: String)

    @Query("DELETE FROM bond_details")
    suspend fun deleteAll()

    @Query("SELECT * FROM bond_details WHERE shortName LIKE '%' || :query || '%'")
    fun search(query: String): List<BondDetailsEntity>
}