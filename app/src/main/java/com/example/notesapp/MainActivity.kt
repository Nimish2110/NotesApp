package com.example.notesapp

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.notesapp.adapter.NoteAdapter
import com.example.notesapp.data.NoteDatabase
import com.example.notesapp.data.NoteRepository
import com.example.notesapp.databinding.ActivityMainBinding
import com.example.notesapp.data.Note
import com.example.notesapp.viewmodel.NoteViewModel
import com.example.notesapp.viewmodel.NoteViewModelFactory
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: NoteAdapter

    private val viewModel: NoteViewModel by viewModels {
        NoteViewModelFactory(
            NoteRepository(NoteDatabase.getDatabase(application).noteDao())
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = NoteAdapter(
            onItemClick = { note -> showEditNoteDialog(note) },
            onDeleteClick = { note ->
                viewModel.deleteNote(note)
                Toast.makeText(this, "Note deleted", Toast.LENGTH_SHORT).show()
            }
        )

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        viewModel.allNotes.observe(this, Observer { notes ->
            adapter.submitList(notes)
        })

        binding.fab.setOnClickListener {
            showAddNoteDialog()
        }
    }

    private fun showAddNoteDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_note, null)
        val titleEditText = dialogView.findViewById<EditText>(R.id.edit_title)
        val contentEditText = dialogView.findViewById<EditText>(R.id.edit_note)

        MaterialAlertDialogBuilder(this)
            .setTitle("Add Note")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val title = titleEditText.text.toString().trim()
                val content = contentEditText.text.toString().trim()

                if (title.isNotEmpty() && content.isNotEmpty()) {
                    viewModel.insertNote(Note(title = title, content = content))
                    Toast.makeText(this, "Note added", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Title and content cannot be empty", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }


    private fun showEditNoteDialog(note: Note) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_note, null)
        val editText = dialogView.findViewById<EditText>(R.id.edit_note)
        editText.setText(note.content)

        MaterialAlertDialogBuilder(this)
            .setTitle("Edit Note")
            .setView(dialogView)
            .setPositiveButton("Update") { _, _ ->
                val updatedContent = editText.text.toString()
                if (updatedContent.isNotBlank()) {
                    val updatedNote = note.copy(content = updatedContent)
                    viewModel.updateNote(updatedNote)
                } else {
                    Toast.makeText(this, "Note cannot be empty", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
