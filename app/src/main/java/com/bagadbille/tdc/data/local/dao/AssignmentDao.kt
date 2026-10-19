package com.bagadbille.tdc.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.bagadbille.tdc.data.local.entity.AssignmentEntity

@Dao
interface AssignmentDao {

    @Query("SELECT * FROM cached_assignments ORDER BY createdAt DESC")
    suspend fun getAllAssignments(): List<AssignmentEntity>

    @Query("SELECT * FROM cached_assignments WHERE id = :id LIMIT 1")
    suspend fun getAssignmentById(id: String): AssignmentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAssignments(assignments: List<AssignmentEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAssignment(assignment: AssignmentEntity)

    @Query("DELETE FROM cached_assignments WHERE id = :id")
    suspend fun deleteAssignment(id: String)

    @Query("DELETE FROM cached_assignments")
    suspend fun clearAll()

    @Transaction
    suspend fun replaceAssignments(assignments: List<AssignmentEntity>) {
        clearAll()
        insertAssignments(assignments)
    }
}
