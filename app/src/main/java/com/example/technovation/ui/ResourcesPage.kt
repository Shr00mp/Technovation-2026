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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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

enum class Type { ARTICLE, VIDEO }

enum class Category {
    EXERCISE,
    MEDITATION,
    HEALTH,
    RECIPES
}

@Entity(tableName = "Resources_Database")
data class Content(
    @PrimaryKey val contentId: Int,
    val title: String,
    val type: Type,
    val category: Category,
    val htmlContent: String? = null,
    val videoUrl: String? = null,
    val saved: Boolean = false,
    val recommended: Boolean = false,
    val imageRes: String? = null
)

//data access object: where I define database interactions and queries
@Dao
interface ContentDao {
    @Query("SELECT * FROM Resources_Database")
    fun displayAll(): Flow<List<Content>>

    @Query("SELECT * FROM Resources_Database WHERE recommended = 1")
    fun displayRecommended(): Flow<List<Content>>

    @Query("SELECT * FROM Resources_Database WHERE saved = 1")
    fun getSaved(): Flow<List<Content>>

    @Query("SELECT * FROM Resources_Database WHERE type = :type")
    fun filterByType(type: Type): Flow<List<Content>>

    @Query("SELECT * FROM Resources_Database WHERE contentId = :id")
    fun getById(id: Int): Flow<Content?>

    @Query("UPDATE Resources_Database SET saved = :saved WHERE contentId = :id")
    suspend fun updateSaved(id: Int, saved: Boolean)

    @Query("UPDATE Resources_Database SET recommended = 0")
    suspend fun clearAllRecommended()

    @Query("UPDATE Resources_Database SET recommended = 1 WHERE category IN (:categories)")
    suspend fun setRecommendedForCategories(categories: List<String>)

    @Query("UPDATE Resources_Database SET recommended = 1 WHERE contentId IN (:ids)")
    suspend fun setRecommendedByIds(ids: List<Int>)
}

class Converters {
    @TypeConverter fun fromType(value: Type): String = value.name
    @TypeConverter fun toType(value: String): Type = Type.valueOf(value)
    @TypeConverter fun toCategory(value: String): Category = Category.valueOf(value)
}

@SuppressLint("RestrictedApi")
@Database(entities = [Content::class], version = 2, exportSchema = false)
@TypeConverters(Converters::class)
abstract class ResourcesDatabase : RoomDatabase() {
    abstract fun contentDao(): ContentDao

    companion object {
        @Volatile private var INSTANCE: ResourcesDatabase? = null

        fun getDatabase(context: Context): ResourcesDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    ResourcesDatabase::class.java,
                    "Resources_Database"
                    //building the database in Room from the predefined database in db browser
                ).createFromAsset("Resources_Database.db")
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}

data class FilterState(
    val selectedCategories: Set<Category> = emptySet()
)

class ResourcesViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = ResourcesDatabase.getDatabase(application).contentDao()

    val searchQuery = MutableStateFlow("")
    val filterState = MutableStateFlow(FilterState())

    val saved: StateFlow<List<Content>> = dao.getSaved()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recommended: StateFlow<List<Content>> = dao.displayRecommended()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val articles: StateFlow<List<Content>> = dao.filterByType(Type.ARTICLE)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

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

    @RequiresApi(Build.VERSION_CODES.O)
    fun refreshRecommendations(allEntriesViewModel: AllJournalEntries) {
        viewModelScope.launch {
            val symptomAndActivityNames = allEntriesViewModel.history.flatMap { entry ->
                entry.physicalSymptomsEntry.map { it.name } +
                entry.mentalSymptomsEntry.map { it.name } +   // fix field name here
                entry.activitiesEntry.map { it.name } +
                entry.textInJournal.split(",", " ", ".", "!", "?")
                    .map { it.trim().lowercase() }
            }

            dao.clearAllRecommended()

            if (symptomAndActivityNames.isNotEmpty()) {
                val allContent = dao.displayAll().map { it }.stateIn(viewModelScope).value
                val matchingIds = allContent.filter { content ->
                    val titleWords = content.title.lowercase()
                    symptomAndActivityNames.any { symptom -> titleWords.contains(symptom) }
                }.map { it.contentId }

                if (matchingIds.isNotEmpty()) {
                    dao.setRecommendedByIds(matchingIds)
                }
            }
        }
    }
}
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

    Column(modifier = Modifier.fillMaxSize()) {
        SearchBar(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            inputField = {
                SearchBarDefaults.InputField(
                    query = searchQuery,
                    onQueryChange = { viewModel.updateSearchQuery(it) },
                    onSearch = { expanded = false },
                    expanded = expanded,
                    onExpandedChange = { expanded = it },
                    placeholder = { Text("Search articles") },
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
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (isSearchOrFilterActive) {
                Text(
                    text = "Results (${filteredContent.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
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
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
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
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
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
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(10.dp))
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
                    text = "Articles",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(10.dp))
                articles.forEach { article ->
                    ArticleCard(
                        article = article,
                        onClick = { navController.navigate("detail/${article.contentId}") },
                        onBookmarkClick = { viewModel.toggleSaved(article) }
                    )
                }
            }
        }
    }
}


@Composable
fun ArticleCard(
    article: Content,
    onClick: () -> Unit,
    onBookmarkClick: () -> Unit
) {
    val context = LocalContext.current
    val imageResId: Int? = article.imageRes?.let { resName ->
        val id = context.resources.getIdentifier(resName, "drawable", context.packageName)
        if (id != 0) id else null
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp)
    ) {
        Column {
            // Cover image — only shown when the article has an imageRes set in the DB
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
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = article.category.name
                            .lowercase()
                            .replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = onBookmarkClick) {
                    Icon(
                        imageVector = if (article.saved) Icons.Default.Favorite
                        else Icons.Default.FavoriteBorder,
                        contentDescription = if (article.saved) "Unsave" else "Save",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticleDetailScreen(
    contentId: Int,
    viewModel: ResourcesViewModel,
    navController: NavController
) {
    val content by viewModel.getContentById(contentId)
        .collectAsStateWithLifecycle(initialValue = null)

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
            AndroidView(
                factory = { ctx ->
                    WebView(ctx).apply {
                        webViewClient = WebViewClient()
                        settings.useWideViewPort = true
                        settings.loadWithOverviewMode = true
                    }
                },
                update = { webView ->
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
    } ?: Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}