package com.example.jazzlibraryktroomjpcompose.data.local.db.daos

import androidx.room.*
import com.example.jazzlibraryktroomjpcompose.data.local.db.entities.FilterPathRoomEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FilterPathDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFilterPath(filterPath: FilterPathRoomEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllFilterPaths(filterPaths: List<FilterPathRoomEntity>)

    @Update
    suspend fun updateFilterPath(filterPath: FilterPathRoomEntity)

    @Delete
    suspend fun deleteFilterPath(filterPath: FilterPathRoomEntity)

    @Query("DELETE FROM filter_path")
    suspend fun deleteAllFilterPaths()

    @Query("DELETE FROM filter_path WHERE category_id = :categoryId")
    suspend fun deleteByCategory(categoryId: Int)

    @Query("SELECT * FROM filter_path")
    fun getAllFilterPaths(): Flow<List<FilterPathRoomEntity>>

    @Query("SELECT * FROM filter_path WHERE category_id = :categoryId")
    fun getFilterPathByCategory(categoryId: Int): Flow<List<FilterPathRoomEntity>>

    @Query("SELECT * FROM filter_path WHERE entity_id = :entityId AND category_id = :categoryId")
    fun getFilterPath(entityId: Int, categoryId: Int): Flow<FilterPathRoomEntity?>
}