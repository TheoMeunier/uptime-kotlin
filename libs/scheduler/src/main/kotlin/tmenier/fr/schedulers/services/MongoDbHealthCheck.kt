package tmenier.fr.schedulers.services

import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.client.MongoClients
import jakarta.enterprise.context.ApplicationScoped
import org.bson.Document
import tmenier.fr.common.dtos.ProbeContent
import tmenier.fr.common.encryption.EncryptionService
import java.util.concurrent.TimeUnit

fun interface MongoDbHealthCheck {
    fun check(
        content: ProbeContent.MongoDb,
        timeoutSeconds: Int,
    )
}

@ApplicationScoped
class DriverMongoDbHealthCheck(
    private val encryptionService: EncryptionService,
) : MongoDbHealthCheck {
    override fun check(
        content: ProbeContent.MongoDb,
        timeoutSeconds: Int,
    ) {
        val connectionString = parseConnectionString(content.connectionString)
        val command = parseCommand(content.command)
        val timeoutMilliseconds = timeoutSeconds * 1_000L

        val settings =
            MongoClientSettings
                .builder()
                .applyConnectionString(connectionString)
                .applicationName(connectionString.applicationName ?: APPLICATION_NAME)
                .applyToClusterSettings {
                    it.serverSelectionTimeout(timeoutMilliseconds, TimeUnit.MILLISECONDS)
                }.applyToSocketSettings {
                    it.connectTimeout(timeoutMilliseconds, TimeUnit.MILLISECONDS)
                    it.readTimeout(timeoutMilliseconds, TimeUnit.MILLISECONDS)
                }.applyToConnectionPoolSettings {
                    it.minSize(0)
                    it.maxSize(1)
                    it.maxWaitTime(timeoutMilliseconds, TimeUnit.MILLISECONDS)
                }.build()

        MongoClients.create(settings).use { client ->
            val result =
                client
                    .getDatabase(connectionString.database ?: DEFAULT_DATABASE)
                    .runCommand(command)
            if (!isOk(result)) throw IllegalStateException("MongoDB command returned ok=${result["ok"]}")
        }
    }

    internal fun parseConnectionString(connectionString: String): ConnectionString {
        val plainText = encryptionService.decryptIfEncrypted(connectionString)
        require(plainText.startsWith("mongodb://") || plainText.startsWith("mongodb+srv://")) {
            "Connection string must start with mongodb:// or mongodb+srv://"
        }
        return ConnectionString(plainText)
    }

    internal fun parseCommand(command: String): Document {
        val document =
            try {
                Document.parse(command.ifBlank { ProbeContent.MongoDb.DEFAULT_MONGODB_COMMAND })
            } catch (error: Exception) {
                throw IllegalArgumentException("Invalid MongoDB command: ${error.message}", error)
            }
        require(document.isNotEmpty()) { "MongoDB command is required" }
        return document
    }

    internal fun isOk(result: Document): Boolean = (result["ok"] as? Number)?.toDouble() == 1.0

    private companion object {
        const val APPLICATION_NAME = "uptime-kotlin"
        const val DEFAULT_DATABASE = "admin"
    }
}
