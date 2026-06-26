package com.example.yazlab4

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {

    private lateinit var loginScreenLayout: LinearLayout
    private lateinit var mainMenuLayout: LinearLayout
    private lateinit var gridSelectionLayout: LinearLayout
    private lateinit var moveSelectionLayout: LinearLayout
    
    private lateinit var tvUsernameDisplayTop: TextView
    private lateinit var etUsernameInput: EditText
    
    private var selectedGridSize: Int = 8
    private var selectedMoves: Int = 20

    private val PREFS_NAME = "WordCrushPrefs"
    private val KEY_USERNAME = "username"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        MusicManager.startMusic(this)

        initViews()
        setupListeners()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        checkUserStatus()

        // MODERN GERİ TUŞU YÖNETİMİ
        onBackPressedDispatcher.addCallback(this, object : androidx.activity.OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                when {
                    gridSelectionLayout.visibility == View.VISIBLE || moveSelectionLayout.visibility == View.VISIBLE -> {
                        val sharedPref = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                        val savedName = sharedPref.getString(KEY_USERNAME, "") ?: ""
                        showMainMenu(savedName)
                    }
                    mainMenuLayout.visibility == View.VISIBLE -> {
                        androidx.appcompat.app.AlertDialog.Builder(this@MainActivity)
                            .setTitle("Çıkış")
                            .setMessage("Uygulamadan çıkmak istiyor musunuz?")
                            .setPositiveButton("Evet") { _, _ -> 
                                isEnabled = false
                                onBackPressedDispatcher.onBackPressed() 
                            }
                            .setNegativeButton("Hayır", null)
                            .show()
                    }
                    else -> {
                        isEnabled = false
                        onBackPressedDispatcher.onBackPressed()
                    }
                }
            }
        })
    }

    private fun initViews() {
        loginScreenLayout = findViewById(R.id.loginScreenLayout)
        mainMenuLayout = findViewById(R.id.mainMenuLayout)
        gridSelectionLayout = findViewById(R.id.gridSelectionLayout)
        moveSelectionLayout = findViewById(R.id.moveSelectionLayout)
        
        tvUsernameDisplayTop = findViewById(R.id.tvUsernameDisplayTop)
        etUsernameInput = findViewById(R.id.etUsernameInput)
    }

    private fun setupListeners() {
        // Login
        findViewById<Button>(R.id.btnSaveUsername).setOnClickListener {
            val name = etUsernameInput.text.toString().trim()
            if (name.isNotEmpty()) {
                saveUsername(name)
                showMainMenu(name)
            } else {
                Toast.makeText(this, getString(R.string.empty_name_error), Toast.LENGTH_SHORT).show()
            }
        }

        // Ana Menü
        findViewById<Button>(R.id.btnNewGame).setOnClickListener { showGridSelection() }
        findViewById<Button>(R.id.btnHighScores).setOnClickListener {
            startActivity(Intent(this, ScoreActivity::class.java))
        }
        findViewById<Button>(R.id.btnMarket).setOnClickListener {
            startActivity(Intent(this, MarketActivity::class.java))
        }

        // Grid Seçimi
        findViewById<Button>(R.id.btnGrid6x6).setOnClickListener { 
            selectedGridSize = 6
            showMoveSelection() 
        }
        findViewById<Button>(R.id.btnGrid8x8).setOnClickListener { 
            selectedGridSize = 8
            showMoveSelection() 
        }
        findViewById<Button>(R.id.btnGrid10x10).setOnClickListener { 
            selectedGridSize = 10
            showMoveSelection() 
        }

        // Hamle Seçimi
        findViewById<Button>(R.id.btnMovesEasy).setOnClickListener { 
            selectedMoves = 25
            startGame()
        }
        findViewById<Button>(R.id.btnMovesMedium).setOnClickListener { 
            selectedMoves = 20
            startGame()
        }
        findViewById<Button>(R.id.btnMovesHard).setOnClickListener { 
            selectedMoves = 15
            startGame()
        }

        // İsim Değiştirme
        tvUsernameDisplayTop.setOnClickListener { showLoginScreen() }
    }

    private fun checkUserStatus() {
        val sharedPref = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val savedName = sharedPref.getString(KEY_USERNAME, null)

        if (savedName != null) {
            showMainMenu(savedName)
        } else {
            showLoginScreen()
        }
    }

    private fun saveUsername(name: String) {
        val sharedPref = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString(KEY_USERNAME, name)
            apply()
        }
    }

    private fun hideAllScreens() {
        loginScreenLayout.visibility = View.GONE
        mainMenuLayout.visibility = View.GONE
        gridSelectionLayout.visibility = View.GONE
        moveSelectionLayout.visibility = View.GONE
        tvUsernameDisplayTop.visibility = View.VISIBLE
    }

    private fun showMainMenu(name: String) {
        hideAllScreens()
        tvUsernameDisplayTop.text = name
        mainMenuLayout.visibility = View.VISIBLE
    }

    private fun showLoginScreen() {
        hideAllScreens()
        tvUsernameDisplayTop.visibility = View.GONE
        val sharedPref = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val savedName = sharedPref.getString(KEY_USERNAME, "")
        etUsernameInput.setText(savedName)
        loginScreenLayout.visibility = View.VISIBLE
    }

    private fun showGridSelection() {
        hideAllScreens()
        gridSelectionLayout.visibility = View.VISIBLE
    }

    private fun showMoveSelection() {
        hideAllScreens()
        moveSelectionLayout.visibility = View.VISIBLE
    }

    private fun startGame() {
        val intent = Intent(this, GameActivity::class.java).apply {
            putExtra("GRID_SIZE", selectedGridSize)
            putExtra("MOVES", selectedMoves)
        }
        startActivity(intent)

        // Oyun başladığında arka plandaki menüyü ana menüye sıfırlıyoruz.
        // Böylece oyundan dönüldüğünde seçim ekranı değil ana menü görünür.
        val sharedPref = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val savedName = sharedPref.getString(KEY_USERNAME, "") ?: ""
        showMainMenu(savedName)
    }
}