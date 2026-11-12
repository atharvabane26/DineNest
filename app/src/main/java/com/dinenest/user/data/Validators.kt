package com.dinenest.user.data

import android.util.Patterns

object Validators {
    fun isValidName(name: String) = name.trim().length >= 2

    fun isValidUsername(username: String) =
        Regex("^[a-z0-9_]{3,20}$").matches(username)

    fun isValidEmail(email: String) =
        Patterns.EMAIL_ADDRESS.matcher(email).matches()

    fun hasMinLength(p: String) = p.length >= 6
    fun hasLetter(p: String) = p.any { it.isLetter() }
    fun hasDigit(p: String) = p.any { it.isDigit() }
    fun hasSpecial(p: String) = p.any { !it.isLetterOrDigit() && !it.isWhitespace() }

    fun isValidPassword(p: String) =
        hasMinLength(p) && hasLetter(p) && hasDigit(p) && hasSpecial(p)
}