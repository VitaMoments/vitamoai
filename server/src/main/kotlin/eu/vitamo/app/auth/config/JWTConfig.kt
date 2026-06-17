package eu.vitamo.app.auth.config

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm

data class JWTConfig(
    val issuer: String,
    val audience: String,
    val realm: String = "access",
    val secret: String,
    val accessTokenExpirationSeconds: Long = 3600,
    val refreshTokenExpirationSeconds: Long = 604800,
) {
    val algorithm: Algorithm = Algorithm.HMAC256(secret)

    val verifier: JWTVerifier = JWT.require(algorithm)
        .withIssuer(issuer)
        .withAudience(audience)
        .withSubject(JWT_SUBJECT)
        .build()

    companion object {
        const val JWT_SUBJECT: String = "Authentication"
        const val USER_ID_CLAIM: String = "userId"
    }
}
