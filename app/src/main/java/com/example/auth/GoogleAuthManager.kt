package com.example.auth

import android.app.Activity
import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

data class GoogleUserProfile(
    val email: String,
    val displayName: String,
    val photoUrl: String? = null,
    val idToken: String
)

sealed class GoogleAuthState {
    object Idle : GoogleAuthState()
    object Loading : GoogleAuthState()
    data class Authenticated(val profile: GoogleUserProfile) : GoogleAuthState()
    data class Error(val message: String) : GoogleAuthState()
}

class GoogleAuthManager(private val context: Context) {

    // Web Client ID provisioned for this project
    val oAuthClientId = "753037065832-1scqh1ltl63v04fdh01h7ivq4e36o23i.apps.googleusercontent.com"
    val defaultApiKey = "AIzaSyD9v4RT1yWF1hoHTup8om_MO_2t5VJdVYc"

    private val credentialManager = CredentialManager.create(context)
    private val httpClient = OkHttpClient()

    private val _authState = MutableStateFlow<GoogleAuthState>(GoogleAuthState.Idle)
    val authState: StateFlow<GoogleAuthState> = _authState.asStateFlow()

    suspend fun signInWithGoogle(activity: Activity): Result<GoogleUserProfile> {
        _authState.value = GoogleAuthState.Loading

        return try {
            val signInOption = GetSignInWithGoogleOption.Builder(oAuthClientId)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(signInOption)
                .build()

            val result: GetCredentialResponse = credentialManager.getCredential(
                request = request,
                context = activity
            )

            val credential = result.credential
            if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)

                val profile = GoogleUserProfile(
                    email = googleIdTokenCredential.id,
                    displayName = googleIdTokenCredential.displayName ?: googleIdTokenCredential.id.substringBefore("@"),
                    photoUrl = googleIdTokenCredential.profilePictureUri?.toString(),
                    idToken = googleIdTokenCredential.idToken
                )

                _authState.value = GoogleAuthState.Authenticated(profile)
                Result.success(profile)
            } else {
                val errorMsg = "Unexpected credential format returned"
                _authState.value = GoogleAuthState.Error(errorMsg)
                Result.failure(Exception(errorMsg))
            }
        } catch (e: GetCredentialCancellationException) {
            _authState.value = GoogleAuthState.Idle
            Result.failure(e)
        } catch (e: GetCredentialException) {
            val errorMsg = "Google Sign-In failed: ${e.localizedMessage ?: "Unknown authentication error"}"
            _authState.value = GoogleAuthState.Error(errorMsg)
            Result.failure(e)
        } catch (e: Exception) {
            val errorMsg = "Sign-in error: ${e.localizedMessage ?: "Unknown error"}"
            _authState.value = GoogleAuthState.Error(errorMsg)
            Result.failure(e)
        }
    }

    fun signOut() {
        _authState.value = GoogleAuthState.Idle
    }

    /**
     * Retrieves YouTube Live broadcast and its activeLiveChatId for a given video ID or channel
     */
    suspend fun retrieveLiveChatStreamId(
        videoId: String,
        apiKey: String = defaultApiKey
    ): Result<Pair<String, String>> = withContext(Dispatchers.IO) {
        try {
            val url = "https://www.googleapis.com/youtube/v3/videos?part=snippet,liveStreamingDetails&id=$videoId&key=$apiKey"
            val request = Request.Builder().url(url).build()

            httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext Result.failure(Exception("HTTP ${response.code}: ${response.message}"))
                }

                val body = response.body?.string() ?: ""
                val json = JSONObject(body)
                val items = json.optJSONArray("items")

                if (items == null || items.length() == 0) {
                    return@withContext Result.failure(Exception("No live video found for ID $videoId"))
                }

                val item = items.getJSONObject(0)
                val snippet = item.optJSONObject("snippet")
                val title = snippet?.optString("title", "Live Stream") ?: "Live Stream"

                val liveStreamingDetails = item.optJSONObject("liveStreamingDetails")
                val activeLiveChatId = liveStreamingDetails?.optString("activeLiveChatId", "") ?: ""

                if (activeLiveChatId.isBlank()) {
                    return@withContext Result.failure(Exception("Video is not currently live or live chat is disabled."))
                }

                Result.success(Pair(activeLiveChatId, title))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
