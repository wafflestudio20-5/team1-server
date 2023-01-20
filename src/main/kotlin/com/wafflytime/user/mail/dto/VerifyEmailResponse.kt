package com.wafflytime.user.mail.dto

import com.wafflytime.common.DateTimeResponse
import com.wafflytime.user.mail.database.MailVerificationEntity

data class VerifyEmailResponse(
    val validUntil: DateTimeResponse
) {
    companion object {

        fun of(entity: MailVerificationEntity) = entity.run {
            VerifyEmailResponse(
                DateTimeResponse.includeSeconds(modifiedAt!!.plusMinutes(3))
            )
        }
    }
}
