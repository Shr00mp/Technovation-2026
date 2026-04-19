package com.example.technovation.ui

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.os.Build
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import androidx.room3.Dao
import androidx.room3.Database
import androidx.room3.Entity
import androidx.room3.PrimaryKey
import androidx.room3.Query
import androidx.room3.Room
import androidx.room3.RoomDatabase
import androidx.room3.TypeConverter
import androidx.room3.TypeConverters
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.compose.material.icons.filled.SpatialAudioOff
import androidx.compose.material.icons.filled.Stop
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.core.text.HtmlCompat
import kotlinx.coroutines.flow.first
import java.util.Locale

//defining what categories of articles there are (for filtering)
enum class Category {
    MENTAL,
    PHYSICAL,
    GENERAL
}

//defining the resources database table, matching exactly with the schema in DB browser
@Entity(tableName = "Resources_Database")
data class Content(
    @PrimaryKey val contentId: Int,
    val title: String,
    val category: Category,
    val htmlContent: String? = null,
    val saved: Boolean = false,
    val recommended: Boolean = false,
    val imageRes: String? = null
)

//data access object: where I define database interactions and queries
//defined using Flow, which is a type used for continuous live updation and emission to/from database
@Dao
interface ContentDao {
    //Select all articles
    @Query("SELECT * FROM Resources_Database")
    fun displayAll(): Flow<List<Content>>

    //Select recommended and saved articles
    // 'recommended' and 'saved' fields are set to 1 in the database when recommended and 0 otherwise
    @Query("SELECT * FROM Resources_Database WHERE recommended = 1")
    fun displayRecommended(): Flow<List<Content>>

    @Query("SELECT * FROM Resources_Database WHERE saved = 1")
    fun getSaved(): Flow<List<Content>>

    //select a certain article by specific ID
    @Query("SELECT * FROM Resources_Database WHERE contentId = :id")
    fun getById(id: Int): Flow<Content?>

    //toggles the saved status depending on if saved is 0 or 1
    @Query("UPDATE Resources_Database SET saved = :saved WHERE contentId = :id")
    suspend fun updateSaved(id: Int, saved: Boolean)

    //clears all recommendations so they can be updated
    @Query("UPDATE Resources_Database SET recommended = 0")
    suspend fun clearAllRecommended()

    //flags articles recommended by broad category
    @Query("UPDATE Resources_Database SET recommended = 1 WHERE category IN (:categories)")
    suspend fun setRecommendedForCategories(categories: List<String>)

    //flags a specific article recommended by keyword matching
    @Query("UPDATE Resources_Database SET recommended = 1 WHERE contentId IN (:ids)")
    suspend fun setRecommendedByIds(ids: List<Int>)
}

//converts to and from a plain string (as stored in the database) to my defined enum class
class Converters {
    @TypeConverter fun fromCategory(value: Category): String = value.name
    @TypeConverter fun toCategory(value: String): Category = Category.valueOf(value)
}

//defining and initialising my Room database - the version number was updated whenever the schema changed
@SuppressLint("RestrictedApi")
@Database(entities = [Content::class], version = 3, exportSchema = false)
@TypeConverters(Converters::class)
abstract class ResourcesDatabase : RoomDatabase() {
    abstract fun contentDao(): ContentDao

    companion object {
        @Volatile private var INSTANCE: ResourcesDatabase? = null

        //ensures only one instance of the database is created avoiding conflicts
        fun getDatabase(context: Context): ResourcesDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    ResourcesDatabase::class.java,
                    "Resources_Database"
                    //building the database in Room from the predefined database in db browser
                ).createFromAsset("Resources_Database.db")
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}

//which filters are currently selected
data class FilterState(
    val selectedCategories: Set<Category> = emptySet()
)

//holds information being updated during the runtime of the app
class ResourcesViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = ResourcesDatabase.getDatabase(application).contentDao()

    val searchQuery = MutableStateFlow("")
    val filterState = MutableStateFlow(FilterState())

    val saved: StateFlow<List<Content>> = dao.getSaved()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recommended: StateFlow<List<Content>> = dao.displayRecommended()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val articles: StateFlow<List<Content>> = dao.displayAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    //articles are filtered by the current SQL query and the selected filters
    //reruns when either state changes
    val filteredContent: StateFlow<List<Content>> = combine(
        searchQuery, filterState
    ) { query, filter -> Pair(query, filter) }
        .flatMapLatest { (query, filter) ->
            dao.displayAll().map { list ->
                list.filter { content ->
                    val matchesQuery = query.isBlank() ||
                            content.title.contains(query, ignoreCase = true)
                    val matchesCategory = filter.selectedCategories.isEmpty() ||
                            content.category in filter.selectedCategories
                    matchesQuery && matchesCategory
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun updateSearchQuery(query: String) { searchQuery.value = query }

    fun updateFilter(filter: FilterState) { filterState.value = filter }

    fun toggleSaved(content: Content) {
        viewModelScope.launch {
            dao.updateSaved(content.contentId, !content.saved)
        }
    }

    fun getContentById(id: Int): Flow<Content?> = dao.getById(id)

    //used to hold the last time that recommendations were refreshed
    private val sharedPrefs = application.getSharedPreferences("recom_prefs", Context.MODE_PRIVATE)

    //checks against sharedPrefs, and if it's been more than 24 hours returns true
    private fun shouldRefresh(): Boolean {
        val lastRefresh = sharedPrefs.getLong("last_refresh_timestamp", 0L)
        val currentTime = System.currentTimeMillis()
        val oneDayInMillis = 24 * 60 * 60 * 1000

        return (currentTime - lastRefresh) > oneDayInMillis
    }

    //saves the current time as the last refresh timestamp when called
    private fun markRefreshed() {
        sharedPrefs.edit().putLong("last_refresh_timestamp", System.currentTimeMillis()).apply()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun refreshRecommendations(allEntriesViewModel: AllJournalEntries) {
        // only continues to refreshing if a day has passed
        if (!shouldRefresh()) return

        viewModelScope.launch {
            // gets keywords from the journal, converts all to lowercase, and removes duplicates
            val keywords = allEntriesViewModel.history.flatMap { entry ->
                entry.physicalSymptomsEntry.map { it.name.lowercase() } +
                        entry.mentalSymptomsEntry.map { it.name.lowercase() } +
                        entry.activitiesEntry.map { it.name.lowercase() } +
                        entry.textInJournal.split(",", " ", ".", "!", "?").map { it.trim().lowercase() }
            }.filter { it.isNotBlank() }.toSet()

            if (keywords.isEmpty()) return@launch

            // gets the articles from the database and matches them up with journal entries
            //'first()' added as a bug fix to ensure Room has emitted the article list before recommendations made
            val allContent = dao.displayAll().first()

            //maps article titles to the keywords
            val candidateIds = allContent.filter { content ->
                val titleWords = content.title.lowercase()
                keywords.any { keyword -> titleWords.contains(keyword) }
            }.map { it.contentId }

            //if there are enough articles recommended to refresh them, shuffle and refresh (the next day)
            if (candidateIds.isNotEmpty()) {
                val currentRecommendedIds = recommended.value.map { it.contentId }.toSet()

                //check if there are new articles that aren't currently recommended
                val newOptionsAvailable = candidateIds.any { it !in currentRecommendedIds }

                if (newOptionsAvailable || currentRecommendedIds.isEmpty()) {
                    // shuffle the candidates and take only the top 3
                    val selectedIds = candidateIds.shuffled().take(3)

                    //updation to the database
                    dao.clearAllRecommended()
                    dao.setRecommendedByIds(selectedIds)

                    //update timestamp so won't refresh until next day
                    markRefreshed()
                }
            }
        }
    }
}

//formatting of where everything goes on the page + application of the functions
@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResourcesPage(
    navController: NavController,
    viewModel: ResourcesViewModel,
    allEntriesViewModel: AllJournalEntries
) {
    val articles by viewModel.articles.collectAsStateWithLifecycle()
    val recommended by viewModel.recommended.collectAsStateWithLifecycle()
    val saved by viewModel.saved.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val filterState by viewModel.filterState.collectAsStateWithLifecycle()
    val filteredContent by viewModel.filteredContent.collectAsStateWithLifecycle()

    var expanded by rememberSaveable { mutableStateOf(false) }
    var showSaved by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.refreshRecommendations(allEntriesViewModel)
    }

    val isSearchOrFilterActive = searchQuery.isNotBlank() ||
            filterState.selectedCategories.isNotEmpty()

    Column(modifier = Modifier.fillMaxSize().padding(bottom=50.dp)) {
        SearchBar(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            inputField = {
                SearchBarDefaults.InputField(
                    query = searchQuery,
                    onQueryChange = { viewModel.updateSearchQuery(it) },
                    onSearch = { expanded = false },
                    expanded = expanded,
                    onExpandedChange = { expanded = it },
                    placeholder = { Text("Search articles", fontSize = 16.sp) },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    }
                )
            },
            expanded = expanded,
            onExpandedChange = { expanded = it }
        ) {
            filteredContent.forEach { content ->
                ListItem(
                    headlineContent = { Text(content.title) },
                    supportingContent = {
                        Text(content.category.name.lowercase().replaceFirstChar { it.uppercase() })
                    },
                    modifier = Modifier.clickable {
                        viewModel.updateSearchQuery(content.title)
                        expanded = false
                        navController.navigate("detail/${content.contentId}")
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(Category.entries) { category ->
                    FilterChip(
                        selected = category in filterState.selectedCategories,
                        onClick = {
                            val current = filterState.selectedCategories
                            viewModel.updateFilter(
                                filterState.copy(
                                    selectedCategories = if (category in current)
                                        current - category
                                    else
                                        current + category
                                )
                            )
                        },
                        label = {
                            Text(category.name.lowercase().replaceFirstChar { it.uppercase() })
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xff9CC5A1),
                            selectedLabelColor = Color.White),
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (isSearchOrFilterActive) {
                Text(
                    text = "Results (${filteredContent.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 22.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                filteredContent.forEach { article ->
                    ArticleCard(
                        article = article,
                        onClick = { navController.navigate("detail/${article.contentId}") },
                        onBookmarkClick = { viewModel.toggleSaved(article) }
                    )
                }
            } else {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showSaved = !showSaved },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xffDCE1DE)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = "Saved",
                                tint = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Saved articles",
                                fontSize = 20.sp,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                        Text(
                            text = "${saved.size}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }

                if (showSaved) {
                    Spacer(modifier = Modifier.height(10.dp))
                    if (saved.isEmpty()) {
                        Text(
                            text = "Nothing saved yet",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                    } else {
                        saved.forEach { article ->
                            ArticleCard(
                                article = article,
                                onClick = { navController.navigate("detail/${article.contentId}") },
                                onBookmarkClick = { viewModel.toggleSaved(article) }
                            )
                        }
                    }
                }

                if (recommended.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "Recommended for you",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 22.sp
                    )
                    Spacer(modifier = Modifier.height(15.dp))
                    recommended.forEach { article ->
                        ArticleCard(
                            article = article,
                            onClick = { navController.navigate("detail/${article.contentId}") },
                            onBookmarkClick = { viewModel.toggleSaved(article) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "All articles",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 22.sp
                )
                Spacer(modifier = Modifier.height(15.dp))
                articles.forEach { article ->
                    ArticleCard(
                        article = article,
                        onClick = { navController.navigate("detail/${article.contentId}") },
                        onBookmarkClick = { viewModel.toggleSaved(article) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

//defines how all articles should appear before being clicked on, with a title, image, label, and save/unsave heart
@Composable
fun ArticleCard(
    article: Content,
    onClick: () -> Unit,
    onBookmarkClick: () -> Unit
) {
    val context = LocalContext.current
    //matches the imageRes to the file in res -> drawable
    val imageResId: Int? = article.imageRes?.let { resName ->
        val id = context.resources.getIdentifier(resName, "drawable", context.packageName)
        if (id != 0) id else null
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(Color(0xffDCE1DE))
    ) {
        Column {
            // cover image shown when the article has an imageRes set in the DB
            if (imageResId != null) {
                Image(
                    painter = androidx.compose.ui.res.painterResource(id = imageResId),
                    contentDescription = article.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = article.title,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 21.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = article.category.name
                            .lowercase()
                            .replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xff216869),
                        fontSize = 14.sp
                    )
                }
                //heart icon is filled if saved and unfilled if not
                IconButton(onClick = onBookmarkClick) {
                    Icon(
                        imageVector = if (article.saved) Icons.Default.Favorite
                        else Icons.Default.FavoriteBorder,
                        contentDescription = if (article.saved) "Unsave" else "Save",
                        tint = if (article.saved) Color(0xFFBC4B51) else Color(0xFF216869),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

//renders the HTML content of the articles in a WebView once clicked on
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticleDetailScreen(
    contentId: Int,
    viewModel: ResourcesViewModel,
    navController: NavController
) {
    val content by viewModel.getContentById(contentId)
        .collectAsStateWithLifecycle(initialValue = null)

    val context = LocalContext.current
    //checks if text to speech (TTS) is currently speaking, to update the icon between play and stop
    val isSpeaking = remember { mutableStateOf(false) }
    val tts = remember { mutableStateOf<TextToSpeech?>(null) }

    //initialises TTS and stops it when the screen is left so it doesn't keep speaking
    DisposableEffect(Unit) {
        var instance: TextToSpeech? = null
        instance = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                instance?.language = Locale.getDefault()
                //updates isSpeaking if the article is finished or if there's an error
                instance?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {}
                    override fun onDone(utteranceId: String?) { isSpeaking.value = false }
                    override fun onError(utteranceId: String?) { isSpeaking.value = false }
                })
            }
            tts.value = instance
        }
        onDispose {
            instance?.stop()
            instance?.shutdown()
        }
    }

    content?.let { article ->
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(article.title) },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        //read aloud button
                        IconButton(onClick = {
                            val engine = tts.value ?: return@IconButton
                            if (isSpeaking.value) {
                                engine.stop()
                                isSpeaking.value = false
                            } else {
                                //strips the HTML tags off so it's only reading out the actual content
                                val plainText = HtmlCompat.fromHtml(
                                    article.htmlContent ?: "",
                                    HtmlCompat.FROM_HTML_MODE_LEGACY
                                ).toString()
                                engine.speak(plainText, TextToSpeech.QUEUE_FLUSH, null, "tts_utterance")
                                isSpeaking.value = true
                            }
                        }) {
                            Icon(
                                imageVector = if (isSpeaking.value) Icons.Default.Stop
                                else Icons.Default.SpatialAudioOff,
                                contentDescription = if (isSpeaking.value) "Stop reading" else "Read aloud"
                            )
                        }
                        IconButton(onClick = { viewModel.toggleSaved(article) }) {
                            Icon(
                                imageVector = if (article.saved) Icons.Default.Favorite
                                else Icons.Default.FavoriteBorder,
                                contentDescription = if (article.saved) "Unsave" else "Save",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                )
            }
        ) { padding ->
            //rendering of the stored HTML
            AndroidView(
                factory = { ctx ->
                    WebView(ctx).apply {
                        webViewClient = WebViewClient()
                        settings.useWideViewPort = true
                        settings.loadWithOverviewMode = true
                    }
                },
                update = { webView ->
                    //defines the auto settings for font and style
                    val html = """
                        <html><head>
                        <meta name="viewport" content="width=device-width, initial-scale=1.0">
                        <style>
                          body { font-family: sans-serif; font-size: 16px;
                                 line-height: 1.6; padding: 12px; }
                          img { max-width: 100%; height: auto; }
                        </style>
                        </head><body>${article.htmlContent ?: ""}</body></html>
                    """.trimIndent()
                    webView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null)
                },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            )
        }
        //shows the loading spinner whilst the article is being loaded
    } ?: Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}