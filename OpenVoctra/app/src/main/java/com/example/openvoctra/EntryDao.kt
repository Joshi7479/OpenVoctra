package com.example.openvoctra

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
abstract class EntryDao {

    @Insert
    abstract suspend fun insert(entry: Entry)

    @Query("SELECT COALESCE(MAX(id), 0) + 1 FROM entries")
    abstract suspend fun nextId(): Long

    @Transaction
    open suspend fun insertNext(title: String, content: String): Long {
        val id = nextId()
        insert(Entry(id, title, content))
        return id
    }

    @Query("SELECT * FROM entries WHERE id = :id")
    abstract suspend fun getById(id: Long): Entry?

    @Query("SELECT * FROM entries ORDER BY id ASC")
    abstract suspend fun getAllOnce(): List<Entry>

    @Query("DELETE FROM entries WHERE id = :id")
    abstract suspend fun deleteById(id: Long)

    @Query("UPDATE entries SET id = id - 1 WHERE id > :id")
    abstract suspend fun shiftDown(id: Long)

    @Query("SELECT COUNT(*) FROM entries")
    abstract suspend fun getCount(): Int

    @Transaction
    open suspend fun deleteAndCompact(id: Long) {
        deleteById(id)
        shiftDown(id)
    }
}