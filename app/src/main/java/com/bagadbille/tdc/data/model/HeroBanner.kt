package com.bagadbille.tdc.data.model

data class HeroBanner(
    val id: String? = null,
    val title: String = "TDC Hackathon & Events",
    val subtitle: String? = "Stay tuned for upcoming hackathons, workshops, and challenges",
    val tag: String = "UPCOMING",
    val badgeText: String? = null,
    val buttonText: String = "Learn More",
    val actionUrl: String? = null,
    val actionType: String = "URL",
    val isActive: Boolean = false
)
