package com.anwesh.uiprojects.linkedaltplusminusview

/**
 * Created by anweshmishra on 17/06/18.
 */

import android.view.View
import android.view.MotionEvent
import android.graphics.*
import android.content.Context

val LAPM_NODES : Int = 5

class LinkedAltPlusMinusView(ctx : Context) : View(ctx) {

    private val paint : Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    override fun onDraw(canvas : Canvas) {

    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {

            }
        }
        return true
    }

    data class State(var j : Int = 0, var prevScale : Float = 0f, var dir : Float = 0f) {

        val scales : Array<Float> = arrayOf(0f, 0f)

        fun update(stopcb : (Float) -> Unit) {
            scales[j] += dir * 0.1f
            if (Math.abs(scales[j] - prevScale) > 1) {
                scales[j] = prevScale + dir
                j += dir.toInt()
                if (j == scales.size || j == -1) {
                    j -= dir.toInt()
                    dir = 0f
                    prevScale = scales[j]
                    stopcb(prevScale)
                }
            }
        }

        fun startUpdating(startcb : () -> Unit) {
            if (dir == 0f) {
                dir = 1 - 2 * prevScale
                startcb()
            }
        }
    }

    data class Animator (var view : View, var animated : Boolean = false) {

        fun animate(cb : () -> Unit) {
            if (animated) {
                cb()
                try {
                    Thread.sleep(50)
                    view.invalidate()
                } catch(ex : Exception) {

                }
            }
        }

        fun start() {
            if (!animated) {
                animated = true
                view.postInvalidate()
            }
        }

        fun stop() {
            if (animated) {
                animated = false
            }
        }
    }

    data class LAPMNode(var i : Int) {

        private val state : State = State()

        private var curr : LAPMNode? = null

        private var next : LAPMNode? = null

        private var prev : LAPMNode? = null

        init {
            addNeighbor()
        }

        fun addNeighbor() {
            if (i < LAPM_NODES - 1) {
                next = LAPMNode(i + 1)
                next?.prev = this
            }
        }

        fun draw(canvas : Canvas, paint : Paint) {
            val w : Float = canvas.width.toFloat()
            val h : Float = canvas.height.toFloat()
            val gap : Float = w / LAPM_NODES
            prev?.draw(canvas, paint)
            paint.color = Color.WHITE
            paint.strokeWidth = Math.min(w, h) / 60
            paint.strokeCap = Paint.Cap.ROUND
            canvas.save()
            canvas.translate((i-1) * gap - gap/6 + gap * state.scales[0], h/2)
            for (j in 0..1) {
                canvas.save()
                canvas.rotate(90f * j * (i + ((1 - 2 * i) * state.scales[1])))
                canvas.drawLine(-gap/6, 0f, gap/6, 0f, paint)
                canvas.restore()
            }
            canvas.restore()

        }

        fun update(stopcb : (Float) -> Unit) {
            state.update(stopcb)
        }

        fun startUpdating(startcb : () -> Unit) {
            state.startUpdating(startcb)
        }

        fun getNeighbor(dir : Int, cb : () -> Unit) : LAPMNode {
            var curr : LAPMNode? = prev
            if (dir == 1) {
                curr = next
            }
            if (curr != null) {
                return curr
            }
            cb()
            return this
        }
    }

    data class LinkedAltPlusMinus(var i : Int) {

        private var curr : LAPMNode = LAPMNode(0)

        private var dir : Int = 1

        fun draw(canvas : Canvas, paint : Paint) {
            curr.draw(canvas, paint)
        }

        fun update(stopcb : (Float) -> Unit) {
            curr.update {
                curr = curr.getNeighbor(dir) {
                    dir *= -1
                }
                stopcb(it)
            }
        }

        fun startUpdating(startcb : () -> Unit) {
            curr.startUpdating(startcb)
        }
    }

    data class Renderer(var view : LinkedAltPlusMinusView) {

        private val animator : Animator = Animator(view)

        private val linkedAltPlusMinus : LinkedAltPlusMinus = LinkedAltPlusMinus(0)

        fun render(canvas : Canvas, paint : Paint) {
            canvas.drawColor(Color.parseColor("#212121"))
            linkedAltPlusMinus.draw(canvas, paint)
            animator.animate {
                linkedAltPlusMinus.update {
                    animator.stop()
                }
            }
        }

        fun handleTap() {
            linkedAltPlusMinus.startUpdating {
                animator.start()
            }
        }
    }
}