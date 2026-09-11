package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ProxyDao {
    @Query("SELECT * FROM proxies ORDER BY isSelected DESC, id DESC")
    fun getAllProxies(): Flow<List<ProxyEntity>>

    @Query("SELECT * FROM proxies WHERE id = :id LIMIT 1")
    suspend fun getProxyById(id: Long): ProxyEntity?

    @Query("SELECT * FROM proxies WHERE isSelected = 1 LIMIT 1")
    suspend fun getSelectedProxy(): ProxyEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProxy(proxy: ProxyEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(proxies: List<ProxyEntity>)

    @Update
    suspend fun updateProxy(proxy: ProxyEntity)

    @Delete
    suspend fun deleteProxy(proxy: ProxyEntity)

    @Query("DELETE FROM proxies WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("UPDATE proxies SET isSelected = 0")
    suspend fun clearSelected()

    @Query("UPDATE proxies SET isSelected = 1 WHERE id = :id")
    suspend fun setSelected(id: Long)

    @Query("UPDATE proxies SET latencyMs = :latency, isOnline = :isOnline WHERE id = :id")
    suspend fun updateLatency(id: Long, latency: Long?, isOnline: Boolean)

    @Query("DELETE FROM proxies WHERE isVProxiesManaged = 1")
    suspend fun deleteManagedProxies()

    @Query("DELETE FROM proxies")
    suspend fun deleteAll()

    @Query("UPDATE proxies SET protocol = :protocol WHERE id = :id")
    suspend fun updateProtocol(id: Long, protocol: String)

    @Query("SELECT COUNT(*) FROM proxies")
    suspend fun getCount(): Int

    @Query("SELECT * FROM proxies LIMIT 1")
    suspend fun getAnyProxy(): ProxyEntity?
}
