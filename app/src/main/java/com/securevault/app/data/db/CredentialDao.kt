package com.securevault.app.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CredentialDao {

    @Query("SELECT * FROM credentials ORDER BY isFavorite DESC, updatedAt DESC")
    fun getAllCredentials(): Flow<List<CredentialEntity>>

    @Query("SELECT * FROM credentials ORDER BY isFavorite DESC, updatedAt DESC")
    suspend fun getAllCredentialsSync(): List<CredentialEntity>

    @Query("SELECT * FROM credentials WHERE id = :id LIMIT 1")
    suspend fun getCredentialById(id: Long): CredentialEntity?

    @Query("SELECT * FROM credentials WHERE category = :category ORDER BY isFavorite DESC, updatedAt DESC")
    fun getCredentialsByCategory(category: String): Flow<List<CredentialEntity>>

    @Query("SELECT * FROM credentials WHERE title LIKE '%' || :query || '%' OR domainOrPackage LIKE '%' || :query || '%' ORDER BY isFavorite DESC, updatedAt DESC")
    fun searchCredentials(query: String): Flow<List<CredentialEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCredential(entity: CredentialEntity): Long

    @Update
    suspend fun updateCredential(entity: CredentialEntity)

    @Delete
    suspend fun deleteCredential(entity: CredentialEntity)

    @Query("DELETE FROM credentials WHERE id = :id")
    suspend fun deleteCredentialById(id: Long)

    @Query("SELECT COUNT(*) FROM credentials")
    suspend fun getCount(): Int
}
