package de.mwvb.blockpuzzle

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import de.mwvb.blockpuzzle.persistence.IPersistence
import de.mwvb.blockpuzzle.persistence.Persistence
import kotlinx.android.synthetic.main.activity_start_screen.*

/**
 * Start Screen activity
 */
class StartScreenActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start_screen)

        if (Build.VERSION.SDK_INT >= 21) {
            window.navigationBarColor = ContextCompat.getColor(this, R.color.colorHeadlineBackground);
        }


        oldGame.setOnClickListener { onOldGame() }
    }



    private fun onOldGame() {
        val per = per()
        per.saveOldGame(1)
        startActivity(Intent(this, MainActivity::class.java))
    }

    private fun per(): IPersistence {
        return Persistence(this)
    }
}
