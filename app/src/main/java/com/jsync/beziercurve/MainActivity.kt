package com.jsync.beziercurve

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.textview.MaterialTextView

class MainActivity : AppCompatActivity(), View.OnClickListener, BezierCanvas.ActionListener {

    private lateinit var buttonAdd: MaterialButton
    private lateinit var buttonRemove: MaterialButton

    private lateinit var buttonAnim: AppCompatImageView
    private lateinit var buttonIncreaseDuration: AppCompatImageView
    private lateinit var buttonDecreaseDuration: AppCompatImageView

    private lateinit var buttonShowLerp: MaterialCheckBox

    private lateinit var editDuration: AppCompatEditText

    private lateinit var textCount: MaterialTextView

    private lateinit var bezierCanvas: BezierCanvas

    private var MINIMUM_DURATION: Long = 500L

    override fun onCreate(savedInstanceState: Bundle?) {
        window.navigationBarColor = ContextCompat.getColor(this, R.color.greyCard)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        buttonAdd = findViewById(R.id.button_add)
        buttonRemove = findViewById(R.id.button_remove)
        buttonAnim = findViewById(R.id.button_start)
        buttonIncreaseDuration = findViewById(R.id.button_increase_duration)
        buttonDecreaseDuration = findViewById(R.id.button_decrease_duration)

        buttonShowLerp = findViewById(R.id.checkbox_show_lerp)

        editDuration = findViewById(R.id.edit_duration)

        textCount = findViewById(R.id.text_count)

        bezierCanvas = findViewById(R.id.bezier_canvas)

        buttonAdd.setOnClickListener(this)
        buttonRemove.setOnClickListener(this)
        buttonAnim.setOnClickListener(this)
        buttonIncreaseDuration.setOnClickListener(this)
        buttonDecreaseDuration.setOnClickListener(this)

        bezierCanvas.setActionListener(this)

        buttonShowLerp.setOnCheckedChangeListener { _, isChecked ->
            if (bezierCanvas.isAnimating()) {
                buttonShowLerp.isChecked = true
            } else {
                bezierCanvas.showLerp = isChecked
            }
        }

        textCount.text = "${bezierCanvas.controlPointCount}"
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.button_start -> {
                if (bezierCanvas.isAnimating()) {
                    buttonAnim.setImageResource(R.drawable.ic_play_circle)
                    bezierCanvas.stopAnimation()
                } else {
                    buttonAnim.setImageResource(R.drawable.ic_pause_circle)
                    bezierCanvas.startAnimation()
                }
            }

            R.id.button_add -> {
                if (!bezierCanvas.isAnimating()) {
                    bezierCanvas.add()
                    textCount.text = "${bezierCanvas.controlPointCount}"
                }
            }

            R.id.button_remove -> {
                if (!bezierCanvas.isAnimating()) {
                    bezierCanvas.remove()
                    textCount.text = "${bezierCanvas.controlPointCount}"
                }
            }

            R.id.button_increase_duration -> {
                bezierCanvas.duration = bezierCanvas.duration + MINIMUM_DURATION
                editDuration.setText("${bezierCanvas.duration}ms")
            }

            R.id.button_decrease_duration -> {
                if (bezierCanvas.duration > MINIMUM_DURATION) {
                    bezierCanvas.duration = bezierCanvas.duration - MINIMUM_DURATION
                    editDuration.setText("${bezierCanvas.duration}ms")
                }
            }

            else -> {
                // no op
            }
        }
    }

    override fun onAnimationEnd() {
        buttonAnim.setImageResource(R.drawable.ic_play_circle)
    }
}