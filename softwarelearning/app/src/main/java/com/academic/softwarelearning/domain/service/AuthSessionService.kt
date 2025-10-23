package com.academic.softwarelearning.domain.service

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import com.academic.softwarelearning.domain.model.UserClaims

object AuthSessionService {
    private const val PREFS = "auth_session"
    private const val KEY_ROLE = "role"

    fun saveClaims(ctx: Context, claims: UserClaims) {
        ctx.getSharedPreferences(PREFS, AppCompatActivity.MODE_PRIVATE).edit()
            .putString(KEY_ROLE, claims.role)
            .apply()
    }

    fun getClaims(ctx: Context): UserClaims {
        val r = ctx.getSharedPreferences(PREFS, AppCompatActivity.MODE_PRIVATE)
            .getString(KEY_ROLE, null)
        return UserClaims(role = r)
    }

    fun hasRole(ctx: Context, vararg allowed: String): Boolean {
        val r = getClaims(ctx).role ?: return false
        return allowed.contains(r)
    }

}