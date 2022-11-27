package com.joecollins.models.general.social.generic

import java.net.URL

interface User {
    val screenName: String
    val name: String
    val profileImageURL: URL
    val isVerified: Boolean
    val isProtected: Boolean
}
