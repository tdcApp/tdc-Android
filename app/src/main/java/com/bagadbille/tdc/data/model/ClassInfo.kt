package com.bagadbille.tdc.data.model

data class ClassInfo(
    val id: String,
    val name: String,
    val type: String,
    val createdAt: String? = null,
    val updatedAt: String? = null
)
