package com.project.noteapp.dao

import androidx.room.*
import com.project.noteapp.entities.Notes

@Dao
interface NoteDao {

    @get: Query("SELECT * FROM notes ORDER BY id DESC")
    val allNotes: List<Notes>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertNotes(notes: Notes)

    @Delete
    fun deleteNote(note: Notes)
}