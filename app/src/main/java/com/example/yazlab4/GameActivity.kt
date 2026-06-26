package com.example.yazlab4

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.GridLayout
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import java.util.Calendar
import kotlin.math.abs
import kotlin.random.Random

enum class ActiveJoker {
    NONE, FISH, WHEEL, LOLLIPOP, SWAP_FREE, SHUFFLE, PARTY
}

class GameActivity : AppCompatActivity() {

    private lateinit var letterGrid: GridLayout
    private lateinit var tvSelectedWord: TextView
    private lateinit var tvRemainingMoves: TextView
    private lateinit var tvCurrentScore: TextView
    private lateinit var tvPossibleWordsCount: TextView

    // Joker Sayıları
    private lateinit var tvCountFish: TextView
    private lateinit var tvCountWheel: TextView
    private lateinit var tvCountLollipop: TextView
    private lateinit var tvCountSwap: TextView
    private lateinit var tvCountShuffle: TextView
    private lateinit var tvCountParty: TextView

    private var gridSize: Int = 8
    private var remainingMoves: Int = 20
    private var currentScore: Int = 0
    private var wordCount: Int = 0
    private var longestWord: String = ""
    private var startTime: Long = 0

    private lateinit var letterGenerator: LetterGenerator
    private lateinit var dictionaryManager: DictionaryManager
    private lateinit var scoreCalculator: ScoreCalculator
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var goldManager: GoldManager

    private val selectedLetters = mutableListOf<LetterView>()
    private var currentWord = ""
    private var activeJoker = ActiveJoker.NONE

    // Joker Çift Tıklama ve Güvenlik
    private var lastJokerClickTime: Long = 0
    private var lastJokerTypeClicked = ActiveJoker.NONE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        MusicManager.startMusic(this)

        gridSize = intent.getIntExtra("GRID_SIZE", 8)
        remainingMoves = intent.getIntExtra("MOVES", 20)
        startTime = System.currentTimeMillis()

        letterGenerator = LetterGenerator()
        dictionaryManager = DictionaryManager(this)
        scoreCalculator = ScoreCalculator()
        dbHelper = DatabaseHelper(this)
        goldManager = GoldManager(this)

        initViews()
        setupGrid()
        setupJokerListeners()
        updateJokerCountsUI()

        findViewById<ImageButton>(R.id.btnCheatMenu).setOnClickListener {
            showCheatDialog()
        }

        // Geri tuşu kontrolü
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                showExitConfirmationDialog()
            }
        })
    }

    private fun showExitConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.exit_confirmation_title))
            .setMessage(getString(R.string.exit_confirmation_message))
            .setPositiveButton(getString(R.string.yes)) { _, _ ->
                endGame()
            }
            .setNegativeButton(getString(R.string.no), null)
            .show()
    }

    private fun initViews() {
        letterGrid = findViewById(R.id.letterGrid)
        tvSelectedWord = findViewById(R.id.tvSelectedWord)
        tvRemainingMoves = findViewById(R.id.tvRemainingMoves)
        tvCurrentScore = findViewById(R.id.tvCurrentScore)
        tvPossibleWordsCount = findViewById(R.id.tvPossibleWordsCount)

        tvCountFish = findViewById(R.id.tvCountFish)
        tvCountWheel = findViewById(R.id.tvCountWheel)
        tvCountLollipop = findViewById(R.id.tvCountLollipop)
        tvCountSwap = findViewById(R.id.tvCountSwap)
        tvCountShuffle = findViewById(R.id.tvCountShuffle)
        tvCountParty = findViewById(R.id.tvCountParty)

        updateStatsUI()
    }

    private fun updateStatsUI() {
        if (::tvRemainingMoves.isInitialized) {
            tvRemainingMoves.text = getString(R.string.game_card_duration).replace("Süre:", "Hamle:").split(":")[0] + ": $remainingMoves"
            // Let's use a simpler way since the above is hacky
            tvRemainingMoves.text = "Hamle: $remainingMoves"
        }
        if (::tvCurrentScore.isInitialized) {
            tvCurrentScore.text = "Puan: $currentScore"
        }
        if (letterGrid.childCount >= gridSize * gridSize) {
            updatePossibleWordsDisplay()
        }
    }

    private fun updateJokerCountsUI() {
        tvCountFish.text = goldManager.getJokerCount(GoldManager.KEY_JOKER_FISH).toString()
        tvCountWheel.text = goldManager.getJokerCount(GoldManager.KEY_JOKER_WHEEL).toString()
        tvCountLollipop.text = goldManager.getJokerCount(GoldManager.KEY_JOKER_LOLLIPOP).toString()
        tvCountSwap.text = goldManager.getJokerCount(GoldManager.KEY_JOKER_SWAP).toString()
        tvCountShuffle.text = goldManager.getJokerCount(GoldManager.KEY_JOKER_SHUFFLE).toString()
        tvCountParty.text = goldManager.getJokerCount(GoldManager.KEY_JOKER_PARTY).toString()
    }

    private fun setupJokerListeners() {
        setupJokerButton(R.id.btnJokerFish, ActiveJoker.FISH, R.string.joker_fish_desc)
        setupJokerButton(R.id.btnJokerWheel, ActiveJoker.WHEEL, R.string.joker_wheel_desc)
        setupJokerButton(R.id.btnJokerLollipop, ActiveJoker.LOLLIPOP, R.string.joker_lollipop_desc)
        setupJokerButton(R.id.btnJokerSwapFree, ActiveJoker.SWAP_FREE, R.string.joker_swap_free_desc)
        setupJokerButton(R.id.btnJokerShuffle, ActiveJoker.SHUFFLE, R.string.joker_shuffle_name)
        setupJokerButton(R.id.btnJokerParty, ActiveJoker.PARTY, R.string.joker_party_desc)
    }

    private fun setupJokerButton(buttonId: Int, jokerType: ActiveJoker, descResId: Int) {
        val button = findViewById<ImageButton>(buttonId)
        
        button.setOnClickListener {
            handleJokerClick(jokerType)
        }
        
        button.setOnLongClickListener {
            Toast.makeText(this, getString(descResId), Toast.LENGTH_LONG).show()
            true
        }
    }

    private fun handleJokerClick(joker: ActiveJoker) {
        val currentTime = System.currentTimeMillis()
        if (joker == lastJokerTypeClicked && currentTime - lastJokerClickTime < 1000) {
            // Çift tıklama başarılı
            activateJoker(joker)
            lastJokerTypeClicked = ActiveJoker.NONE
        } else {
            // İlk tıklama
            Toast.makeText(this, "Kullanmak için tekrar dokunun", Toast.LENGTH_SHORT).show()
            lastJokerTypeClicked = joker
            lastJokerClickTime = currentTime
        }
    }

    private fun activateJoker(joker: ActiveJoker) {
        val key = when(joker) {
            ActiveJoker.FISH -> GoldManager.KEY_JOKER_FISH
            ActiveJoker.WHEEL -> GoldManager.KEY_JOKER_WHEEL
            ActiveJoker.LOLLIPOP -> GoldManager.KEY_JOKER_LOLLIPOP
            ActiveJoker.SWAP_FREE -> GoldManager.KEY_JOKER_SWAP
            ActiveJoker.SHUFFLE -> GoldManager.KEY_JOKER_SHUFFLE
            ActiveJoker.PARTY -> GoldManager.KEY_JOKER_PARTY
            else -> ""
        }

        if (goldManager.getJokerCount(key) > 0) {
            activeJoker = joker
            
            // Hemen çalışan jokerler
            if (joker == ActiveJoker.FISH) {
                if (goldManager.useJoker(key)) {
                    applyFishJoker()
                    activeJoker = ActiveJoker.NONE
                    Toast.makeText(this, "Balık kullanıldı!", Toast.LENGTH_SHORT).show()
                }
            } else if (joker == ActiveJoker.SHUFFLE) {
                if (goldManager.useJoker(key)) {
                    applyShuffleJoker()
                    activeJoker = ActiveJoker.NONE
                    Toast.makeText(this, "Harfler karıştırıldı!", Toast.LENGTH_SHORT).show()
                }
            } else if (joker == ActiveJoker.PARTY) {
                if (goldManager.useJoker(key)) {
                    applyPartyJoker()
                    activeJoker = ActiveJoker.NONE
                    Toast.makeText(this, "Parti Güçlendiricisi aktif!", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Joker Aktif! Grid üzerinde kullanın.", Toast.LENGTH_SHORT).show()
            }
            updateJokerCountsUI()
        } else {
            Toast.makeText(this, "Bu jokerden elinizde kalmadı!", Toast.LENGTH_SHORT).show()
            activeJoker = ActiveJoker.NONE
        }
    }

    private fun updatePossibleWordsDisplay() {
        if (letterGrid.childCount < gridSize * gridSize) return
        
        val foundWords = getAllValidUniqueWords()
        if (::tvPossibleWordsCount.isInitialized) {
            tvPossibleWordsCount.text = getString(R.string.possible_words_count, foundWords.size)
        }
        
        if (foundWords.isEmpty() && remainingMoves > 0) {
            handleNoWordsLeft()
        }
    }

    private fun getAllValidUniqueWords(): List<String> {
        val paths = findAllValidPaths()
        return paths.map { path ->
            path.joinToString("") { tile ->
                val view = letterGrid.getChildAt(tile.first * gridSize + tile.second) as? LetterView
                view?.text?.toString()?.filter { it.isLetter() } ?: ""
            }
        }.filter { dictionaryManager.isWordValid(it) }
        .sorted()
    }

    private fun handleNoWordsLeft() {
        Toast.makeText(this, getString(R.string.no_words_left), Toast.LENGTH_SHORT).show()
        
        // Crash önleme: applyShuffleJoker() direkt çağrılmak yerine tabloyu kontrollü yenilemeli
        letterGrid.postDelayed({
            setupGrid() // Shuffle yerine setupGrid kullanıyoruz çünkü o daha garanti
        }, 500)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupGrid() {
        var count = 0
        var attempts = 0
        val maxAttempts = 20
        
        while (count == 0 && attempts < maxAttempts) {
            attempts++
            letterGrid.removeAllViews()
            letterGrid.columnCount = gridSize
            letterGrid.rowCount = gridSize
            val letters = letterGenerator.generateGrid(gridSize)
            for (r in 0 until gridSize) {
                for (c in 0 until gridSize) {
                    val letterView = createLetterView(r, c, letters[r][c].toString())
                    letterGrid.addView(letterView)
                }
            }
            count = getAllValidUniqueWords().size
        }
        
        if (count == 0) {
            forceInjectWord()
        }

        updatePossibleWordsDisplay()
        letterGrid.setOnTouchListener { _, event ->
            handleTouch(event)
            true
        }
    }

    private fun forceInjectWord() {
        val word = "SORU"
        for (i in word.indices) {
            if (i < gridSize) {
                val view = letterGrid.getChildAt(i) as? LetterView
                view?.setLetter(word[i].toString())
            }
        }
    }

    private fun createLetterView(r: Int, c: Int, char: String): LetterView {
        return LetterView(this, r, c).apply {
            setLetter(char)
            val params = GridLayout.LayoutParams().apply {
                width = 0
                height = 0
                columnSpec = GridLayout.spec(c, 1f)
                rowSpec = GridLayout.spec(r, 1f)
                setMargins(2, 2, 2, 2)
            }
            layoutParams = params
        }
    }

    private fun handleTouch(event: MotionEvent) {
        if (remainingMoves <= 0) return

        val x = event.x
        val y = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                val child = findChildAt(x, y)
                if (child is LetterView) {
                    if (activeJoker == ActiveJoker.NONE) {
                        trySelectLetter(child)
                    } else {
                        if (event.action == MotionEvent.ACTION_DOWN) {
                            handleJokerInteraction(child)
                        }
                    }
                }
            }
            MotionEvent.ACTION_UP -> {
                if (activeJoker == ActiveJoker.NONE) {
                    finalizeWordSelection()
                }
            }
        }
    }

    private fun handleJokerInteraction(view: LetterView) {
        val key = when(activeJoker) {
            ActiveJoker.WHEEL -> GoldManager.KEY_JOKER_WHEEL
            ActiveJoker.LOLLIPOP -> GoldManager.KEY_JOKER_LOLLIPOP
            ActiveJoker.SWAP_FREE -> GoldManager.KEY_JOKER_SWAP
            else -> ""
        }

        when(activeJoker) {
            ActiveJoker.WHEEL -> {
                if (goldManager.useJoker(key)) {
                    applyWheelJoker(view)
                    activeJoker = ActiveJoker.NONE
                }
            }
            ActiveJoker.LOLLIPOP -> {
                if (goldManager.useJoker(key)) {
                    view.text = ""
                    applyGravity()
                    activeJoker = ActiveJoker.NONE
                }
            }
            ActiveJoker.SWAP_FREE -> {
                if (selectedLetters.isEmpty()) {
                    view.isSelectedState = true
                    selectedLetters.add(view)
                } else {
                    val first = selectedLetters[0]
                    if (isNeighbor(first, view)) {
                        if (goldManager.useJoker(key)) {
                            val tempText = first.text
                            val tempPower = first.powerType
                            first.setLetter(view.text.toString().filter { it.isLetter() }, view.powerType)
                            view.setLetter(tempText.toString().filter { it.isLetter() }, tempPower)
                            resetSelection()
                            activeJoker = ActiveJoker.NONE
                            updatePossibleWordsDisplay()
                        }
                    } else {
                        resetSelection()
                        view.isSelectedState = true
                        selectedLetters.add(view)
                    }
                }
            }
            else -> {}
        }
        updateJokerCountsUI()
    }

    private fun applyFishJoker() {
        val countToDestroy = Random.nextInt(3, 7)
        val allViews = (0 until letterGrid.childCount).map { letterGrid.getChildAt(it) as LetterView }.shuffled()
        for (i in 0 until countToDestroy) {
            allViews[i].text = ""
        }
        applyGravity()
    }

    private fun applyWheelJoker(centerView: LetterView) {
        val row = centerView.row
        val col = centerView.col
        for (i in 0 until letterGrid.childCount) {
            val child = letterGrid.getChildAt(i) as LetterView
            if (child.row == row || child.col == col) {
                child.text = ""
            }
        }
        applyGravity()
    }

    private fun applyShuffleJoker() {
        if (letterGrid.childCount < gridSize * gridSize) return
        
        val allLetters = mutableListOf<Pair<String, PowerType>>()
        for (i in 0 until letterGrid.childCount) {
            val child = letterGrid.getChildAt(i) as? LetterView ?: continue
            allLetters.add(child.text.toString().filter { it.isLetter() } to child.powerType)
        }
        allLetters.shuffle()
        for (i in 0 until letterGrid.childCount) {
            val child = letterGrid.getChildAt(i) as? LetterView ?: continue
            if (i < allLetters.size) {
                child.setLetter(allLetters[i].first, allLetters[i].second)
            }
        }
        updatePossibleWordsDisplay()
    }

    private fun applyPartyJoker() {
        for (i in 0 until letterGrid.childCount) {
            val child = letterGrid.getChildAt(i) as LetterView
            child.text = ""
        }
        applyGravity()
    }

    private fun findChildAt(x: Float, y: Float): View? {
        for (i in 0 until letterGrid.childCount) {
            val child = letterGrid.getChildAt(i)
            
            // Algılama alanını daralt (Hücrenin %25'lik kenar payını görmezden gel)
            // Bu sayede çapraz geçişlerde parmak ucu yan hücreye hafifçe değse bile seçim yapılmaz.
            val horizontalPadding = child.width * 0.25f
            val verticalPadding = child.height * 0.25f
            
            if (x >= child.left + horizontalPadding && x <= child.right - horizontalPadding && 
                y >= child.top + verticalPadding && y <= child.bottom - verticalPadding) {
                return child
            }
        }
        return null
    }

    private fun trySelectLetter(view: LetterView) {
        if (selectedLetters.contains(view)) {
            if (selectedLetters.size >= 2 && selectedLetters[selectedLetters.size - 2] == view) {
                val last = selectedLetters.removeAt(selectedLetters.size - 1)
                last.isSelectedState = false
                updateWordPreview()
            }
            return
        }

        if (selectedLetters.isEmpty()) {
            select(view)
        } else {
            val last = selectedLetters.last()
            if (isNeighbor(last, view)) {
                select(view)
            }
        }
    }

    private fun select(view: LetterView) {
        view.isSelectedState = true
        selectedLetters.add(view)
        updateWordPreview()
    }

    private fun isNeighbor(v1: LetterView, v2: LetterView): Boolean {
        return abs(v1.row - v2.row) <= 1 && abs(v1.col - v2.col) <= 1
    }

    private fun updateWordPreview() {
        currentWord = selectedLetters.joinToString("") { it.text.toString().filter { char -> char.isLetter() } }
        tvSelectedWord.text = currentWord
    }

    private fun finalizeWordSelection() {
        if (currentWord.isEmpty()) {
            resetSelection()
            return
        }
        
        if (currentWord.length < 3) {
            Toast.makeText(this, "En az 3 harf seçmelisiniz!", Toast.LENGTH_SHORT).show()
            resetSelection()
            return
        }

        remainingMoves--
        
        if (dictionaryManager.isWordValid(currentWord)) {
            processValidWord()
        } else {
            Toast.makeText(this, "Geçersiz Kelime!", Toast.LENGTH_SHORT).show()
        }

        updateStatsUI()
        resetSelection()

        if (remainingMoves <= 0) {
            endGame()
        }
    }

    private fun processValidWord() {
        val explodedViews = mutableSetOf<LetterView>()
        
        for (view in selectedLetters) {
            explodedViews.add(view)
            if (view.powerType != PowerType.NONE) {
                triggerPower(view, explodedViews)
            }
        }

        // 1. Combo ve Puan Hesaplama
        val (comboScore, foundWords) = scoreCalculator.calculateComboScore(currentWord, dictionaryManager)
        
        // 2. Patlayan diğer harflerin puanlarını ekle (güçlerle patlayanlar)
        var extraScore = 0
        for (view in explodedViews) {
            if (!selectedLetters.contains(view)) {
                val baseChar = view.text.toString().filter { it.isLetter() }
                extraScore += scoreCalculator.calculateWordScore(baseChar)
            }
        }

        val totalEarned = comboScore + extraScore
        currentScore += totalEarned
        wordCount++
        if (currentWord.length > longestWord.length) {
            longestWord = currentWord
        }

        val lastLetterView = selectedLetters.last()
        val newPowerType = when {
            currentWord.length >= 7 -> PowerType.MEGA
            currentWord.length == 6 -> PowerType.COLUMN
            currentWord.length == 5 -> PowerType.AREA
            currentWord.length == 4 -> PowerType.ROW
            else -> PowerType.NONE
        }

        for (view in explodedViews) {
            if (view != lastLetterView || newPowerType == PowerType.NONE) {
                view.text = ""
                view.powerType = PowerType.NONE
            }
        }

        if (newPowerType != PowerType.NONE) {
            val baseChar = lastLetterView.text.toString().filter { it.isLetter() }
            lastLetterView.setLetter(baseChar, newPowerType)
        }

        applyGravity()
        
        val comboMsg = if (foundWords.size > 1) "${foundWords.size}x COMBO! " else ""
        Toast.makeText(this, "$comboMsg+$totalEarned Puan", Toast.LENGTH_SHORT).show()
    }

    private fun triggerPower(powerView: LetterView, explodedViews: MutableSet<LetterView>) {
        val row = powerView.row
        val col = powerView.col

        when (powerView.powerType) {
            PowerType.ROW -> {
                for (i in 0 until letterGrid.childCount) {
                    val child = letterGrid.getChildAt(i) as LetterView
                    if (child.row == row) explodedViews.add(child)
                }
            }
            PowerType.COLUMN -> {
                for (i in 0 until letterGrid.childCount) {
                    val child = letterGrid.getChildAt(i) as LetterView
                    if (child.col == col) explodedViews.add(child)
                }
            }
            PowerType.AREA -> {
                for (i in 0 until letterGrid.childCount) {
                    val child = letterGrid.getChildAt(i) as LetterView
                    if (abs(child.row - row) <= 1 && abs(child.col - col) <= 1) explodedViews.add(child)
                }
            }
            PowerType.MEGA -> {
                for (i in 0 until letterGrid.childCount) {
                    val child = letterGrid.getChildAt(i) as LetterView
                    if (abs(child.row - row) <= 2 && abs(child.col - col) <= 2) explodedViews.add(child)
                }
            }
            else -> {}
        }
    }

    private fun applyGravity() {
        val affectedCols = (0 until gridSize).toList()
        for (col in affectedCols) {
            shiftColumnDown(col)
        }
        updatePossibleWordsDisplay()
    }

    private fun shiftColumnDown(col: Int) {
        val columnViews = mutableListOf<LetterView>()
        for (i in 0 until letterGrid.childCount) {
            val child = letterGrid.getChildAt(i) as LetterView
            if (child.col == col) {
                columnViews.add(child)
            }
        }
        columnViews.sortByDescending { it.row }

        for (i in 0 until columnViews.size) {
            if (columnViews[i].text.isEmpty()) {
                var found = false
                for (j in i + 1 until columnViews.size) {
                    if (columnViews[j].text.isNotEmpty()) {
                        columnViews[i].setLetter(columnViews[j].text.toString().filter { it.isLetter() }, columnViews[j].powerType)
                        columnViews[j].text = ""
                        columnViews[j].powerType = PowerType.NONE
                        found = true
                        break
                    }
                }
                if (!found) {
                    columnViews[i].setLetter(letterGenerator.getRandomLetter().toString())
                }
            }
        }
    }

    private fun findAllValidPaths(): List<List<Pair<Int, Int>>> {
        val paths = mutableListOf<List<Pair<Int, Int>>>()
        if (letterGrid.childCount < gridSize * gridSize) return paths
        
        val grid = Array(gridSize) { r ->
            Array(gridSize) { c ->
                val view = letterGrid.getChildAt(r * gridSize + c) as? LetterView
                view?.text?.toString()?.filter { it.isLetter() } ?: ""
            }
        }

        for (r in 0 until gridSize) {
            for (c in 0 until gridSize) {
                dfs(r, c, "", mutableListOf(), HashSet(), grid, paths, 0)
            }
        }
        return paths
    }

    private fun dfs(
        r: Int, c: Int, 
        current: String, 
        path: MutableList<Pair<Int, Int>>, 
        visited: HashSet<Pair<Int, Int>>, 
        grid: Array<Array<String>>,
        paths: MutableList<List<Pair<Int, Int>>>,
        depth: Int
    ) {
        if (depth > 8) return // Daha uzun kelimeler için derinlik artırıldı

        val tile = Pair(r, c)
        if (r !in 0 until gridSize || c !in 0 until gridSize || visited.contains(tile)) return

        val letter = grid[r][c]
        if (letter.isEmpty()) return

        val newWord = current + letter
        if (!dictionaryManager.isPrefixValid(newWord)) return

        visited.add(tile)
        path.add(tile)

        if (newWord.length >= 3 && dictionaryManager.isWordValid(newWord)) {
            paths.add(ArrayList(path))
        }

        for (dr in -1..1) {
            for (dc in -1..1) {
                if (dr == 0 && dc == 0) continue
                dfs(r + dr, c + dc, newWord, path, visited, grid, paths, depth + 1)
            }
        }

        path.removeAt(path.size - 1)
        visited.remove(tile)
    }

    private fun showCheatDialog() {
        val words = getAllValidUniqueWords()

        val message = if (words.isEmpty()) {
            "Sözlükte eşleşen kelime bulunamadı!"
        } else {
            "Sözlükte bulunan ${words.size} farklı kelime bulundu:\n\n" + words.joinToString(", ")
        }

        AlertDialog.Builder(this)
            .setTitle("Cheat Menu (Sözlük Onaylı)")
            .setMessage(message)
            .setPositiveButton("Tamam", null)
            .show()
    }

    private fun resetSelection() {
        selectedLetters.forEach { it.isSelectedState = false }
        selectedLetters.clear()
        currentWord = ""
        tvSelectedWord.text = ""
    }

    private fun endGame() {
        val durationSeconds = ((System.currentTimeMillis() - startTime) / 1000).toInt()
        val date = android.text.format.DateFormat.format("dd.MM.yyyy", Calendar.getInstance().time).toString()
        
        val record = GameRecord(
            date = date,
            gridSide = gridSize,
            score = currentScore,
            wordCount = wordCount,
            longestWord = longestWord,
            durationSeconds = durationSeconds
        )
        
        dbHelper.addGameRecord(record)
        
        Toast.makeText(this, getString(R.string.game_over_score, currentScore), Toast.LENGTH_LONG).show()
        finish()
    }
}
