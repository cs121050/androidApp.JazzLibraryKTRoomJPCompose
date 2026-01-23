package com.example.jazzlibraryktroomjpcompose.data.local.db.entities


import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "types")
data class TypeRoomEntity(
    @PrimaryKey
    @ColumnInfo(name = "type_id")
    val id: Int,

    @ColumnInfo(name = "type_name")
    val name: String
)