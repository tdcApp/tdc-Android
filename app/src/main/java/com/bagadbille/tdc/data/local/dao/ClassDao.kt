package com.bagadbille.tdc.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.bagadbille.tdc.data.local.entity.ClassEntity

@Dao
interface ClassDao {

    @Query("SELECT * FROM cached_classes ORDER BY name ASC")
    suspend fun getAllClasses(): List<ClassEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClasses(classes: List<ClassEntity>)

    @Query("DELETE FROM cached_classes")
    suspend fun clearAll()

    @Transaction
    suspend fun replaceClasses(classes: List<ClassEntity>) {
        clearAll()
        insertClasses(classes)
    }
}
