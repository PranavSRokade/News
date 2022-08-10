package com.example.news

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import java.util.*
import kotlin.collections.ArrayList

open class News : AppCompatActivity() {
    var layoutManager: RecyclerView.LayoutManager? = null
    var adapter: Adapter? = null
    lateinit var recyclerView: RecyclerView

    var newsList = ArrayList<Article>()
    var tempNewsList = ArrayList<Article>()

    lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_news)


        auth = FirebaseAuth.getInstance()

        recyclerView = findViewById(R.id.recyclerView)

        newsList = intent.getSerializableExtra("News") as ArrayList<Article>
        tempNewsList.addAll(newsList)

        layoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = layoutManager

        adapter = Adapter(tempNewsList)
        recyclerView.adapter = adapter
    }



    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.search, menu)
        val item = menu.findItem(R.id.search)
        val searchView = item.actionView as SearchView

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                tempNewsList.clear()
                val searchText = newText
                if(searchText.isNotEmpty()){
                    newsList.forEach{
                        if(it.title.toLowerCase(Locale.getDefault()).contains(searchText) || it.description.toLowerCase(Locale.getDefault()).contains(searchText) || it.source.toLowerCase(Locale.getDefault()).contains(searchText)){
                            tempNewsList.add(it)
                        }
                    }
                    adapter?.notifyDataSetChanged()
                }
                else{
                    tempNewsList.clear()
                    tempNewsList.addAll(newsList)
                    adapter?.notifyDataSetChanged()
                }
                return true
            }
        })
        return super.onCreateOptionsMenu(menu)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == R.id.logout){
            auth.signOut()
            startActivity(Intent(this, LoginSignup::class.java))
        }
        return true
    }

}