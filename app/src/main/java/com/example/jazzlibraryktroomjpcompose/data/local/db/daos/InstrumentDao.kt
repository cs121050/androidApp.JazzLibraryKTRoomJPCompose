package com.example.jazzlibraryktroomjpcompose.data.local.db.daos

import androidx.room.*
import com.example.jazzlibraryktroomjpcompose.data.local.db.entities.InstrumentRoomEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface InstrumentDao {

    @Query("SELECT * FROM instruments ORDER BY instrument_name ASC")
    fun getAllInstruments(): Flow<List<InstrumentRoomEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInstrument(instrument: InstrumentRoomEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllInstruments(instruments: List<InstrumentRoomEntity>)

    @Delete
    suspend fun deleteInstrument(instrument: InstrumentRoomEntity)

    @Query("DELETE FROM instruments")
    suspend fun deleteAllInstruments()


    @Query("SELECT * FROM instruments WHERE instrument_id = :id")
    fun getInstrumentById(id: Int): Flow<InstrumentRoomEntity>

    @Query("SELECT * FROM instruments WHERE instrument_name LIKE '%' || :query || '%'")
    fun getInstrumentByName(query: String): Flow<List<InstrumentRoomEntity>>}