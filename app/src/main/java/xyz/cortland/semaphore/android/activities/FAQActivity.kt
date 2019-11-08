package xyz.cortland.semaphore.android.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import us.feras.mdv.MarkdownView

class FAQActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val markdown = MarkdownView(this)
        setContentView(markdown)
        markdown.loadMarkdownFile("file:///android_asset/faq.md")
    }
}
