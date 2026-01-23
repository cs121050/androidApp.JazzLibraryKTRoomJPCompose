package com.example.jazzlibraryktroomjpcompose.data.local.db.daos


import androidx.room.*
import com.example.jazzlibraryktroomjpcompose.data.local.db.entities.TypeRoomEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TypeDao {

    @Query("SELECT * FROM types ORDER BY type_name ASC")
    fun getAllTypes(): Flow<List<TypeRoomEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertType(type: TypeRoomEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllTypes(types: List<TypeRoomEntity>)

    @Update
    suspend fun updateType(type: TypeRoomEntity)

    @Delete
    suspend fun deleteType(type: TypeRoomEntity)

    @Query("DELETE FROM types")
    suspend fun deleteAllTypes()

    @Query("SELECT * FROM types WHERE type_id = :id")
    fun getTypeById(id: Int): Flow<TypeRoomEntity>

    @Query("SELECT * FROM types WHERE type_name LIKE '%' || :query || '%'")
    fun searchTypes(query: String): Flow<List<TypeRoomEntity>>
}