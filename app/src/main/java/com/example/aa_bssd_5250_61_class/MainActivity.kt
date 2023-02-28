package com.example.aa_bssd_5250_61_class

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.ContactsContract.CommonDataKinds.Website.URL
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.button.MaterialButton
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStream
import java.net.URL
import javax.net.ssl.HttpsURLConnection
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {
    private val API_KEY = "a6ced572e0288192872a7ed83283261c"
    private val service = "https://api.themoviedb.org/3/search/movie"
    private val imageBasePath = "https://image.tmdb.org/t/p/w500"
    private lateinit var posterImage: ImageView
    private lateinit var titleField: TextView
    private lateinit var yearField: TextView

    private  var searchResponse:String = ""
    //to hold the fetched movie year for further comparison
    private var fetchedYear:String = ""
    private var prevTitle:String = ""
    private  var yearGuess:String = ""
    private  var movieTitle:String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        posterImage = ImageView(this).apply {
            layoutParams = ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.MATCH_PARENT,
                ConstraintLayout.LayoutParams.MATCH_PARENT,
            )
        }

        titleField = EditText(this).apply {
            hint = "Enter Movie Title"
        }
        yearField = EditText(this).apply {
            hint = "Enter Guess for year"
        }

        val submitButton = MaterialButton(this).apply {
            text = "Submit"
            id = View.generateViewId()
            setOnClickListener{
                movieTitle = titleField.text.toString()
                yearGuess = yearField.text.toString()
                if(yearGuess == "" || movieTitle == ""){
                    this@MainActivity.runOnUiThread(java.lang.Runnable {
                        Toast.makeText(applicationContext, "Enter all fields", Toast.LENGTH_SHORT).show()
                    })
                }else {
                    showResult(movieTitle, yearGuess)
                }

            }
        }

        val pageWrapper = LinearLayoutCompat(this).apply {
            orientation = LinearLayoutCompat.VERTICAL
            setBackgroundColor(Color.WHITE)
            layoutParams = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
            )
            addView(titleField)
            addView(yearField)
            addView(submitButton)
            addView(posterImage)
        }

        setContentView(pageWrapper)

    }

    private fun showResult(title:String, guessYear:String){
        thread(true){
            var query = title
            val requestURL = "$service?api_key=$API_KEY&query=$query"
//            Log.d( "MACT", getRequest(requestURL).toString())
            if(prevTitle != movieTitle){
                Log.d("Submit", "Title Changed")
                searchResponse = getRequest(requestURL).toString()
                var res = ""
                res = JSONObject(searchResponse).getString("total_results")
                Log.d("SearchRes", res)
                if(res.toInt() < 1){
                    this@MainActivity.runOnUiThread(java.lang.Runnable {
                        Toast.makeText(applicationContext, "Try Again", Toast.LENGTH_SHORT).show()
                    })
                }else{
                    parseJSON(getRequest(requestURL).toString(), guessYear)
                    prevTitle = movieTitle
                }

            }else{
                parseJSON(searchResponse, guessYear)
                Log.d("Submit", "Title equals prev title")
            }
        }
    }

    private fun parseJSON (jsonString: String, guess:String) {
        //tmdb always returns an object
        val jsonData = JSONObject(jsonString)
        val jsonArray = jsonData.getJSONArray("results")
        val film = jsonArray.getJSONObject(0)
        val posterPath = film.getString("poster_path")
        val fullPath = imageBasePath + posterPath
//        Log.d("MACTPoster", fullPath)
        var year = film.getString("release_date")
        fetchedYear = year.substring(0,4)
        var answer = "Incorrect"
        if(fetchedYear == guess){
            answer = "Correct"
        }
        this@MainActivity.runOnUiThread(java.lang.Runnable {
            Toast.makeText(applicationContext, answer, Toast.LENGTH_SHORT).show()
        })
        thread (true){
            val bmp = loadBitmapData(fullPath)
            this@MainActivity.runOnUiThread(java.lang.Runnable{
                posterImage.setImageBitmap(bmp)
            })

        }
    }

    private fun loadBitmapData(path:String): Bitmap?{
        val inputStream: InputStream
        var result: Bitmap? = null

        try {
            val url = URL(path)
            val conn: HttpsURLConnection = url.openConnection() as HttpsURLConnection
            conn.connect()
            inputStream = conn.inputStream
            result = BitmapFactory.decodeStream(inputStream)
        }catch (err: Error){
            print("Error when executing get request: " + err.localizedMessage)
        }
        return result
    }

    private fun getRequest (sUrl: String): String? {
        val inputStream: InputStream
        var result: String? = null
        try {
            // Create URL
            val url = URL(sUrl)
            // Create HttpURLConnection
            val conn: HttpsURLConnection = url.openConnection() as HttpsURLConnection
            // Launch GET request
            conn.connect()
            // Receive response as inputStream
            inputStream = conn.inputStream

            result = if (inputStream != null)
            // Convert input stream to string
                inputStream.bufferedReader().use(BufferedReader::readText)
            else
                "error: inputStream is nULl"
        } catch (err: Error) {
            print("Error when executing get request:" + err.localizedMessage)

        }
        return result
    }
}