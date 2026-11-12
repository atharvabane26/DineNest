package com.dinenest.user.data

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore

object AuthRepository {
    private val auth get() = FirebaseAuth.getInstance()
    private val db get() = FirebaseFirestore.getInstance()

    private fun friendly(e: Exception): String = when (e) {
        is FirebaseAuthUserCollisionException ->
            "This email is already registered. Try logging in."
        is FirebaseAuthWeakPasswordException -> "Password is too weak."
        is FirebaseAuthInvalidCredentialsException,
        is FirebaseAuthInvalidUserException -> "Incorrect email/username or password."
        is FirebaseNetworkException -> "No internet connection. Please try again."
        else -> e.message ?: "Something went wrong. Please try again."
    }

    // onResult(null) = success, otherwise an error message
    fun register(
        name: String,
        username: String,
        email: String,
        password: String,
        onResult: (String?) -> Unit
    ) {
        val uname = username.trim().lowercase()
        val mail = email.trim()

        db.collection("usernames").document(uname).get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    onResult("This username is already taken.")
                    return@addOnSuccessListener
                }
                auth.createUserWithEmailAndPassword(mail, password)
                    .addOnSuccessListener { result ->
                        val user = result.user
                        if (user == null) {
                            onResult("Something went wrong. Please try again.")
                            return@addOnSuccessListener
                        }
                        user.updateProfile(
                            UserProfileChangeRequest.Builder()
                                .setDisplayName(name.trim())
                                .build()
                        )
                        val batch = db.batch()
                        batch.set(
                            db.collection("users").document(user.uid),
                            mapOf("name" to name.trim(), "username" to uname, "email" to mail)
                        )
                        batch.set(
                            db.collection("usernames").document(uname),
                            mapOf("email" to mail, "uid" to user.uid)
                        )
                        batch.commit().addOnCompleteListener {
                            user.sendEmailVerification()
                                .addOnCompleteListener { onResult(null) }
                        }
                    }
                    .addOnFailureListener { onResult(friendly(it)) }
            }
            .addOnFailureListener { onResult(friendly(it)) }
    }

    // onResult(error, isVerified)
    fun login(
        identifier: String,
        password: String,
        onResult: (String?, Boolean) -> Unit
    ) {
        val id = identifier.trim()
        if (id.contains("@")) {
            signIn(id, password, onResult)
        } else {
            db.collection("usernames").document(id.lowercase()).get()
                .addOnSuccessListener { doc ->
                    val email = doc.getString("email")
                    if (email == null) {
                        onResult("No account found with this username.", false)
                    } else {
                        signIn(email, password, onResult)
                    }
                }
                .addOnFailureListener { onResult(friendly(it), false) }
        }
    }

    private fun signIn(email: String, password: String, onResult: (String?, Boolean) -> Unit) {
        auth.signInWithEmailAndPassword(email.trim(), password)
            .addOnSuccessListener {
                onResult(null, it.user?.isEmailVerified == true)
                syncEmail()
            }
            .addOnFailureListener { onResult(friendly(it), false) }
    }

    fun checkVerified(onResult: (Boolean) -> Unit) {
        val user = auth.currentUser
        if (user == null) {
            onResult(false)
            return
        }
        user.reload().addOnCompleteListener {
            onResult(auth.currentUser?.isEmailVerified == true)
        }
    }

    fun resendVerification(onResult: (String?) -> Unit) {
        val user = auth.currentUser
        if (user == null) {
            onResult("Please log in again.")
            return
        }
        user.sendEmailVerification()
            .addOnSuccessListener { onResult(null) }
            .addOnFailureListener { onResult(friendly(it)) }
    }

    fun sendPasswordReset(email: String, onResult: (String?) -> Unit) {
        auth.sendPasswordResetEmail(email.trim())
            .addOnSuccessListener { onResult(null) }
            .addOnFailureListener { onResult(friendly(it)) }
    }

    // Asks Firebase to confirm the user's current password again
    private fun reauthenticate(currentPassword: String, onDone: (String?) -> Unit) {
        val user = auth.currentUser
        val email = user?.email
        if (user == null || email == null) {
            onDone("Please log in again.")
            return
        }
        user.reauthenticate(EmailAuthProvider.getCredential(email, currentPassword))
            .addOnSuccessListener { onDone(null) }
            .addOnFailureListener { e ->
                onDone(
                    if (e is FirebaseAuthInvalidCredentialsException) {
                        "Current password is incorrect."
                    } else {
                        friendly(e)
                    }
                )
            }
    }

    fun changePassword(current: String, newPassword: String, onResult: (String?) -> Unit) {
        reauthenticate(current) { err ->
            if (err != null) {
                onResult(err)
                return@reauthenticate
            }
            val user = auth.currentUser
            if (user == null) {
                onResult("Please log in again.")
                return@reauthenticate
            }
            user.updatePassword(newPassword)
                .addOnSuccessListener { onResult(null) }
                .addOnFailureListener { onResult(friendly(it)) }
        }
    }

    fun changeEmail(currentPassword: String, newEmail: String, onResult: (String?) -> Unit) {
        reauthenticate(currentPassword) { err ->
            if (err != null) {
                onResult(err)
                return@reauthenticate
            }
            val user = auth.currentUser
            if (user == null) {
                onResult("Please log in again.")
                return@reauthenticate
            }
            user.verifyBeforeUpdateEmail(newEmail.trim())
                .addOnSuccessListener { onResult(null) }
                .addOnFailureListener { onResult(friendly(it)) }
        }
    }

    // After an email change is verified, copy the new email into Firestore
    // so that logging in with a username keeps working.
    fun syncEmail() {
        val user = auth.currentUser ?: return
        val email = user.email ?: return
        val userRef = db.collection("users").document(user.uid)
        userRef.get().addOnSuccessListener { doc ->
            val oldEmail = doc.getString("email")
            val uname = doc.getString("username")
            if (oldEmail != null && oldEmail != email && uname != null) {
                val batch = db.batch()
                batch.update(userRef, "email", email)
                batch.update(db.collection("usernames").document(uname), "email", email)
                batch.commit()
            }
        }
    }

    fun refreshAndSync() {
        val user = auth.currentUser ?: return
        user.reload().addOnSuccessListener { syncEmail() }
    }

    fun signOut() = auth.signOut()
}