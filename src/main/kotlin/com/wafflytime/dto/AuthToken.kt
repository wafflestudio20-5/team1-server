package com.wafflytime.dto

// TODO: email verification 이 안되어 있으면 resource access 할 때 해당 에러를 return 해주고, 이를 front 가 인식해서 verification 요청 페에지로 redirect 해주면 되지 않을까?
data class AuthToken(val accessToken: String, val refreshToken: String)