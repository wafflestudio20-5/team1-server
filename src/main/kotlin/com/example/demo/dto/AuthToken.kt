package com.example.demo.dto


// TODO(정민) : AuthToken 넘겨줄 때 email verification 여부도 같이 넘겨줘야 할 것 같아서 이렇게 설정해두었습니다. 다른 좋은 아이디어가 떠오르면 수정 부탁드립니다
data class AuthToken(val accessToken: String, val mailVerified: Boolean = false)