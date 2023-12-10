package com.example.chatemo

import android.app.ProgressDialog.show
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class TransformTextActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        hasIntent()
    }

    private fun hasIntent() {
        if (intent  != null) {
            var text: String? = null

            text = intent.getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT)?.toString()

            showAlertDialog(text ?: "Floating window like alert dialog...")
        } else {
            showAlertDialog("Floating window like alert dialog...")
        }
    }

    private fun showAlertDialog(selectedText: String) {
        val view : View = LayoutInflater.from(this).inflate(R.layout.floating_window_layout, null)
        val textViewText : TextView = view.findViewById(R.id.textView_original)
        val buttonCopy : Button = view.findViewById(R.id.button_copy)
        textViewText.setText(selectedText)

        val dialog = MaterialAlertDialogBuilder(this)
            .setIcon(R.drawable.ic_launcher_foreground)
            .setView(view)
            .setTitle("ChatEmo")
            .setOnDismissListener {
                onBackPressed()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
                onBackPressed()
            }
            .create()

        dialog.apply {
            window?.setGravity(Gravity.TOP)
            show()
        }

        buttonCopy.setOnClickListener{
            dialog.dismiss()
        }
    }
}