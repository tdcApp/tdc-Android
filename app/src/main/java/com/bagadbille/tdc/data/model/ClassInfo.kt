package com.bagadbille.tdc.data.model

data class ClassInfo(
    val id: String,
    val name: String,
    val subject: String,
    val teacher: String,
    val schedule: String? = null,
    val room: String? = null,
    val description: String? = null
)

data class ClassDetail(
    val classInfo: ClassInfo,
    val materials: List<ClassMaterial>
)

data class ClassMaterial(
    val id: String,
    val title: String,
    val type: String,
    val url: String,
    val uploadedAt: String
)
