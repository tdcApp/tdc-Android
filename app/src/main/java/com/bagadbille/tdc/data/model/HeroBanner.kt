package com.bagadbille.tdc.data.model

data class HeroBanner(
    val id: String? = null,
    val title: String = "TDC Coding Quest Season 3",
    val subtitle: String? = "Innovate • Build • Collaborate",
    val tag: String = "COMING SOON",
    val badgeText: String? = null,
    val buttonText: String = "Register Now",
    val actionUrl: String? = "https://forms.gle/nkSmmE9EU31os5UU8",
    val actionType: String = "URL",
    val isActive: Boolean = true
)
