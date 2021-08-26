package de.tudarmstadt.smartcitystudyapp.services

import de.tudarmstadt.smartcitystudyapp.model.User

public enum class PointType(val points: Int) {
    USER(320),
    TEAM(300)
}

interface UserService {
    suspend fun getCurrentUser(): User?
    suspend fun getUserId() : String?
    suspend fun setUser(user: User)
    suspend fun addPoints(user: User, points: Int, addReportCounter: Boolean)
}