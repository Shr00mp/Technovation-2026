package com.example.technovation.ui

import android.annotation.SuppressLint
import android.content.Context
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
    @Volatile private var INSTANCE: ResourcesDatabase? = null

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