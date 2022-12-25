package com.example.demo.database

import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository : JpaRepository<TempUserEntity, Long> {

}