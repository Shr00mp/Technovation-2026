package com.example.technovation.ui

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.webkit.WebView
import android.webkit.WebViewClient
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
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

enum class Type {ARTICLE, VIDEO}

enum class Category {
    EXERCISE, MEDITATION
}

//Creating content of an item as a class for the database
@Entity(tableName = "Resources_Database")
data class Content(
    @PrimaryKey val contentId: Int,
    val title: String,
    val type: Type,
    val category: Category,
    //"?" makes the field optional
    val htmlContent: String? = null,
    val videoUrl: String? = null,
    val saved: Boolean = false,
    val recommended: Boolean = false
    )

//the Data Access Object is an interface defining SQL queries to database
@Dao
interface ContentDao {
    @Query("SELECT * FROM Resources_Database")
    fun displayAll(): Flow<List<Content>>

    @Query("SELECT * FROM Resources_Database WHERE recommended = 1")
    fun displayRecommended(): Flow<List<Content>>

    @Query("SELECT * FROM Resources_Database WHERE saved = 1")
    fun getSaved(): Flow<List<Content>>

    //Selects articles where the title contains some of the query, and applying filters
    @Query("""SELECT * FROM Resources_Database
        WHERE (:query = '' OR title LIKE '%' ||:query|| '%')
        AND (:typeFilter = '' OR type = :typeFilter)
        AND (:categoryFilter = '' OR category = :categoryFilter)
    """)
    //Defines the parameters for the search function
    fun search(
        query: String,
        typeFilter: String,
        categoryFilter: String
    ): Flow<List<Content>>

   @Query("SELECT * FROM Resources_Database WHERE category = :category")
   fun filterByCategory(category: Category): Flow<List<Content>>

   @Query("SELECT * FROM Resources_Database WHERE type = :type")
   fun filterByType(type: Type): Flow<List<Content>>

   @Query("UPDATE Resources_Database SET saved = :saved WHERE contentId = :id")
   suspend fun updateSaved(id: Int, saved: Boolean)

   @Query("SELECT * FROM Resources_Database WHERE contentId = :id")
   fun getById(id: Int): Flow<Content?>
}

//These are needed to convert from the self-defined enum classes to actual data types
class Converters {
    @TypeConverter
    fun fromType(value: Type): String = value.name

    @TypeConverter
    fun toType(value: String): Type = Type.valueOf(value)

    @TypeConverter
    fun fromCategory(value: Category): String = value.name

    @TypeConverter
    fun toCategory(value: String): Category = Category.valueOf(value)
}

@SuppressLint("RestrictedApi")
@Database(entities = [Content::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class ResourcesDatabase : RoomDatabase(){
    abstract fun contentDao(): ContentDao

    companion object {
        @Volatile
        private var INSTANCE: ResourcesDatabase? = null

        //this means the database is only created once avoiding conflicts
        fun getDatabase(context: Context): ResourcesDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    ResourcesDatabase::class.java,
                    "Resources_Database"
                ).createFromAsset("Resources_Database.db").build().also { INSTANCE = it }
            }
        }
    }
}

data class FilterState(
    val selectedKeywords: Set<Category> = emptySet(),
    val selectedTypes: Set<Type> = emptySet()
)

class ResourcesViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = ResourcesDatabase.getDatabase(application).contentDao()

    val searchQuery = MutableStateFlow("")
    val filterState = MutableStateFlow(FilterState())

    val saved: StateFlow<List<Content>> = dao.getSaved()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val articles: StateFlow<List<Content>> = dao.filterByType(Type.ARTICLE)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val videos: StateFlow<List<Content>> = dao.filterByType(Type.VIDEO)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredContent: StateFlow<List<Content>> = combine(
        searchQuery, filterState
    ) { query, filter -> Pair(query, filter) }
        .flatMapLatest { (query, filter) ->
            dao.displayAll().map { list ->
                list.filter { content ->
                    val matchesQuery = query.isBlank() ||
                            content.title.contains(query, ignoreCase = true)
                    val matchesType = filter.selectedTypes.isEmpty() ||
                            content.type in filter.selectedTypes
                    val matchesKeyword = filter.selectedKeywords.isEmpty() ||
                            content.category in filter.selectedKeywords
                    matchesQuery && matchesType && matchesKeyword
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun updateSearchQuery(query: String) {
        searchQuery.value = query
    }

    fun updateFilter(filter: FilterState) {
        filterState.value = filter
    }

    fun toggleSaved(content: Content) {
        viewModelScope.launch {
            dao.updateSaved(content.contentId, !content.saved)
        }
    }

    fun getContentById(id: Int): Flow<Content?> = dao.getById(id)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResourcesPage(
    navController: NavController,
    viewModel: ResourcesViewModel
){
    val articles by viewModel.articles.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier.fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ){
        Box(modifier = Modifier.fillMaxWidth().semantics {isTraversalGroup = true})
        {
            //search bar here
        }

        Spacer(modifier = Modifier.height(10.dp))

        data class CategoryTile(val label: String, val icon: ImageVector, val route: String)

        //placeholder symbols for now
        val tiles = listOf(
            CategoryTile("Articles", Icons.Default.Menu, "articles"),
            CategoryTile("Healthy Recipes", Icons.Default.ShoppingCart, "recipes"),
            CategoryTile("Exercise & Meditation Videos", Icons.Default.Person, "videos"),
            CategoryTile("Saved", Icons.Default.Favorite, "saved")
        )

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            tiles.chunked(2).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    row.forEach { tile ->
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .height(100.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = tile.icon,
                                    contentDescription = tile.label,
                                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = tile.label,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    }
                    if (row.size == 1) Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Articles",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(10.dp))

        articles.forEach { article ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
                    .clickable { navController.navigate("detail/${article.contentId}") },
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = article.title,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
//                    article.caption?.let {
//                        Spacer(modifier = Modifier.height(4.dp))
//                        Text(
//                            text = it,
//                            style = MaterialTheme.typography.bodySmall,
//                            color = MaterialTheme.colorScheme.onSurfaceVariant
//                        )
//                    }
                }
            }
        }
    }
}
