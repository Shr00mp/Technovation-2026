package com.example.technovation.ui

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
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

}

//Creating content of an item as a class for the database
@Entity(tableName = "content")
data class Content(
    @PrimaryKey val contentId: Int,
    val title: String,
    val type: Type,
    val category: Category,
    //"?" makes the field optional
    val htmlContent: String? = null,
    val videoUrl: String? = null,
    val caption: String? = null,
    val saved: Boolean = false,
    val recommended: Boolean = false
    )

//the Data Access Object is an interface defining SQL queries to database
@Dao
interface ContentDao {
    @Query("SELECT * FROM Content")
    fun displayAll(): Flow<List<Content>>

    @Query("SELECT * FROM Content WHERE recommended = 1")
    fun displayRecommended(): Flow<List<Content>>

    @Query("SELECT * FROM Content WHERE saved = 1")
    fun getSaved(): Flow<List<Content>>

    //Selects articles where the title contains some of the query, and applying filters
    @Query("""SELECT * FROM Content
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

   @Query("SELECT * FROM content WHERE category = :category")
   fun filterByCategory(category: Category): Flow<List<Content>>

   @Query("SELECT * FROM content WHERE type = :type")
   fun filterByType(type: Type): Flow<List<Content>>

   @Query("UPDATE content SET saved = :saved WHERE id = :id")
   suspend fun updateSaved(id: Int, saved: Boolean)
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
                    "health_content_db"
                ).build().also { INSTANCE = it }
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
}