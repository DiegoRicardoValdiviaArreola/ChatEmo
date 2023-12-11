package com.example.chatemo

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import com.android.volley.DefaultRetryPolicy
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.lang.Exception

class TransformTextActivity : AppCompatActivity() {
    private val TAG = javaClass.simpleName
    private val CHAT_GPT_API = "https://api.openai.com/v1/chat/completions"
    private val CHAT_GPT_API_TOKEN = BuildConfig.CHAT_GPT_API_KEY
    private val CHAT_GPT_MODEL = "gpt-3.5-turbo"
    private val CHAT_GPT_MAX_TOKENS = 30
    private val PROMPT_START = "Mejora este texto para que denote"
    private val EMOTIONS_LIST = listOf<String>("felicidad", "tristeza", "enojo", "comicidad")
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
        val editTextResult : EditText = view.findViewById(R.id.editText_result)
        val progressBar : ProgressBar = view.findViewById(R.id.progressBar)
        val buttonCopy : Button = view.findViewById(R.id.button_copy)

        view.findViewById<Button>(R.id.button_emotion_1).setOnClickListener { onEmotionButtonPressed(0, selectedText, editTextResult, progressBar) }
        view.findViewById<Button>(R.id.button_emotion_2).setOnClickListener { onEmotionButtonPressed(1, selectedText, editTextResult, progressBar) }
        view.findViewById<Button>(R.id.button_emotion_3).setOnClickListener { onEmotionButtonPressed(2, selectedText, editTextResult, progressBar) }
        view.findViewById<Button>(R.id.button_emotion_4).setOnClickListener { onEmotionButtonPressed(3, selectedText, editTextResult, progressBar) }

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
            val resultText = editTextResult.text.toString()

            val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clipData = ClipData.newPlainText(getString(R.string.txt_clipboard_label), resultText)
            clipboardManager.setPrimaryClip(clipData)

            Toast.makeText(this, getString(R.string.txt_copied_to_clipboard), Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }
    }

    private fun onEmotionButtonPressed(index: Int, originalText: String, editTextResult: EditText, progressBar: ProgressBar) {
        val chosenEmotion = EMOTIONS_LIST.getOrNull(index) ?: EMOTIONS_LIST[0]

        val completePrompt = "${PROMPT_START} ${chosenEmotion}: ${originalText}"

        CoroutineScope(Dispatchers.IO).launch {
            try {
                withContext(Dispatchers.Main) {
                    editTextResult.visibility = View.INVISIBLE
                    progressBar.visibility = View.VISIBLE
                }

                getChatGPTTransform(completePrompt, object: VolleyCallback {
                    override fun onSuccess(result: String) {
                            progressBar.visibility = View.INVISIBLE
                            editTextResult.visibility = View.VISIBLE
                            editTextResult.setText(result)
                    }

                    override fun onError(error: VolleyError) {
                        editTextResult.setText(getString(R.string.txt_api_error_msg))
                    }
                })
            } catch (e: Exception) {

            }
        }
    }

    private fun getChatGPTTransform(query: String, callback: VolleyCallback) {
        val queue: RequestQueue = Volley.newRequestQueue(applicationContext)
        val jsonObject = JSONObject()
        val jsonMessageArray = JSONArray()
        val jsonMessageObject = JSONObject()

        jsonMessageObject.put("role", "user")
        jsonMessageObject.put("content", query)
        jsonMessageArray.put(jsonMessageObject)

        // adding params to json object.
        jsonObject.put("model", CHAT_GPT_MODEL)
        jsonObject.put("messages", jsonMessageArray)
        jsonObject.put("temperature", 1)
        jsonObject.put("max_tokens", CHAT_GPT_MAX_TOKENS)

        val postRequest: JsonObjectRequest =
            object : JsonObjectRequest(Method.POST, CHAT_GPT_API, jsonObject,
                Response.Listener { response ->
                    val responseMsg: String =
                        response.getJSONArray("choices").getJSONObject(0)
                            .getJSONObject("message")
                            .getString("content")
                    callback.onSuccess(responseMsg)
                },
                Response.ErrorListener { error ->
                    callback.onError(error)
                    Log.e(TAG, "Error is : " + error.message + "\n" + error)
                }) {
                override fun getHeaders(): kotlin.collections.MutableMap<kotlin.String, kotlin.String> {
                    val params: MutableMap<String, String> = HashMap()
                    params["Content-Type"] = "application/json"
                    params["Authorization"] = "Bearer $CHAT_GPT_API_TOKEN"
                    return params;
                }
            }

        postRequest.retryPolicy = object : DefaultRetryPolicy(
            5000,
            0,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ) {
            override fun retry(error: VolleyError) {
            }
        }
        queue.add(postRequest)
    }

    interface VolleyCallback {
        fun onSuccess(result: String)
        fun onError(error: VolleyError)
    }
}