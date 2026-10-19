package com.bagadbille.tdc.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.bagadbille.tdc.data.model.ClassInfo

@Entity(tableName = "cached_classes")
data class ClassEntity(
    @PrimaryKey val id: String,
    val name: String,
    val type: String,
    val createdAt: String? = null,
    val updatedAt: String? = null
) {
    fun toDomain(): ClassInfo = ClassInfo(
        id = id,
        name = name,
        type = type,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    companion object {
        fun fromDomain(classInfo: ClassInfo): ClassEntity = ClassEntity(
            id = classInfo.id,
            name = classInfo.name,
            type = classInfo.type,
            createdAt = classInfo.createdAt,
            updatedAt = classInfo.updatedAt
        )
    }
}
