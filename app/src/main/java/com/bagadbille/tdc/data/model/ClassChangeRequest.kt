package com.bagadbille.tdc.data.model

data class ClassChangeRequest(
    val id: String,
    val studentUid: String,
    val studentName: String? = null,
    val studentEmail: String? = null,
    val studentEnrollmentNumber: String? = null,
    val studentYear: String? = null,
    val currentLanguageClassId: String? = null,
    val currentLanguageClassName: String? = null,
    val currentTechnologyClassId: String? = null,
    val currentTechnologyClassName: String? = null,
    val requestedLanguageClassId: String? = null,
    val requestedLanguageClassName: String? = null,
    val requestedTechnologyClassId: String? = null,
    val requestedTechnologyClassName: String? = null,
    val reason: String? = null,
    val status: String = "PENDING", // PENDING, APPROVED, REJECTED
    val reviewedBy: String? = null,
    val reviewerName: String? = null,
    val reviewedAt: String? = null,
    val createdAt: String = "",
    val updatedAt: String = ""
) {
    val isPending: Boolean get() = status.equals("PENDING", ignoreCase = true)
    val isApproved: Boolean get() = status.equals("APPROVED", ignoreCase = true)
    val isRejected: Boolean get() = status.equals("REJECTED", ignoreCase = true)
}
