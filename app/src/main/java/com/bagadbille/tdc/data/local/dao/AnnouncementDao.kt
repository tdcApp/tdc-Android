package com.bagadbille.tdc.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.bagadbille.tdc.data.local.entity.AnnouncementEntity

@Dao
interface AnnouncementDao {

    @Query("SELECT * FROM cached_announcements ORDER BY createdAt DESC")
    suspend fun getAllAnnouncements(): List<AnnouncementEntity>

    @Query("SELECT * FROM cached_announcements WHERE id = :id LIMIT 1")
    suspend fun getAnnouncementById(id: String): AnnouncementEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnnouncements(announcements: List<AnnouncementEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnnouncement(announcement: AnnouncementEntity)

    @Query("DELETE FROM cached_announcements WHERE id = :id")
    suspend fun deleteAnnouncement(id: String)

    @Query("DELETE FROM cached_announcements")
    suspend fun clearAll()

    @Transaction
    suspend fun replaceAnnouncements(announcements: List<AnnouncementEntity>) {
        clearAll()
        insertAnnouncements(announcements)
    }
}
