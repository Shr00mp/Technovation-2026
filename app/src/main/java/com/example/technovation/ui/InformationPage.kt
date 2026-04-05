package com.example.technovation.ui

import androidx.room3.Dao
import androidx.room3.Entity
import androidx.room3.PrimaryKey
import androidx.room3.Query
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
}