package com.yxc.sudoku

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.yxc.newsbykotlin.GameMap
import com.yxc.newsbykotlin.Step
import com.zhy.autolayout.AutoLayoutActivity
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AutoLayoutActivity() {
    val game: GameMap
    val gameMap: Array<Array<Int>>
    val success: StringBuilder
    val positions = ArrayList<String>()
    val allViews = ArrayList<TextView>()
    val emptyViews = ArrayList<String>()
    val steps = LinkedList<Step>()

    init {
        game = GameMap()
        gameMap = game.generate()
        success = StringBuilder()
        for (i in 0..gameMap.size - 1) {
            val row = gameMap[i]
            for (j in 0..row.size - 1) {
                positions.add("$i$j")
                success.append(gameMap[i][j])
            }
        }
        println("${success.toString()}")
    }

    var focusedTextView: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val r = Random()
        for (i in 0..39) {
            val position = r.nextInt(positions.size)
            val index = positions[position]
            gameMap[index.substring(0, 1).toInt()][index.substring(1, 2).toInt()] = 0
            positions.remove(index)
        }
        for (i in 0..gameMap.size - 1) {
            val row = gameMap[i]
            for (j in 0..row.size - 1) {
                val textView: TextView? = layout_numbers.findViewWithTag("$i$j") as TextView
                if (gameMap[i][j] > 0) {
                    textView?.text = gameMap[i][j].toString()
                } else {
                    textView?.text = ""
                    emptyViews.add(textView?.tag.toString())
//                    textView?.setBackgroundColor(resources.getColor(R.color.white))
                }
            }
        }

        setListener(layout_numbers)
        tvDelete.setOnClickListener {
            it ->
            if (focusedTextView==null || TextUtils.isEmpty(focusedTextView?.text)){
                return@setOnClickListener
            }
            focusedTextView?.text = ""
            val tag: String = focusedTextView?.tag as String
            val row = tag.substring(0, 1).toInt()
            val col = tag.substring(1, 2).toInt()
            val preValue = gameMap[row][col]
            gameMap[row][col] = 0
            steps.add(Step(Pair(row, col), -1, preValue))
            refreshUI("")
        }
        tvMoveBack.setOnClickListener {
            it ->
            if (steps.size==0){
                return@setOnClickListener
            }
            val step = steps.last
            for (tv in allViews){
                if (tv.tag.toString().equals("${step.position.first}${step.position.second}")){
                    focusedTextView = tv
                    break
                }
            }
            focusedTextView?.text = if(step.value==0) "" else step.value.toString()
            gameMap[step.position.first][step.position.second] = step.value
            steps.removeLast()
            refreshUI("")
        }
        for (i in 0..layout_operation.childCount - 1) {
            val child = layout_operation.getChildAt(i)
            child.setOnClickListener {
                it ->
                if (focusedTextView==null){
                    return@setOnClickListener
                }
                focusedTextView?.text = (it as TextView).text
                val tag: String = focusedTextView?.tag as String
                val row = tag.substring(0, 1).toInt()
                val col = tag.substring(1, 2).toInt()
                val preValue = gameMap[row][col]
                gameMap[row][col] = it.text.toString().toInt()
                steps.add(Step(Pair(row, col), 0, preValue))
                refreshUI(it.text.toString())
                check()
            }
        }
    }

    fun setListener(view: View) {
        if (view is ViewGroup) {
            for (i in 0..view.childCount - 1) {
                val child = view.getChildAt(i)
                setListener(child)
            }
        } else if (view is TextView) {
            allViews.add(view)
            view.setOnClickListener {
                it ->
                if (emptyViews.contains(it.tag.toString())) {
                    focusedTextView = it as TextView?
                } else {
                    focusedTextView = null
                }
                refreshUI((it as TextView).text.toString())
            }
        }
    }

    fun refreshUI(value: String) {
        for (tv in allViews) {
            if (tv == focusedTextView) {
                tv.setTextColor(resources.getColor(R.color.text_highlight))
                tv.setBackgroundResource(R.drawable.bg_focus)
                continue
            }
            if (checkEffected(tv)) {
                if (!TextUtils.isEmpty(focusedTextView?.text.toString()) && tv.text.toString().equals(focusedTextView?.text.toString())) {
                    tv.setBackgroundColor(resources.getColor(R.color.warning))
                } else {
                    tv.setBackgroundColor(resources.getColor(R.color.effected))
                }
                if (emptyViews.contains(tv.tag.toString())) {
                    tv.setTextColor(resources.getColor(R.color.text_editable))
                } else {
                    tv.setTextColor(resources.getColor(R.color.text_light))
                }
            } else {
                if (!TextUtils.isEmpty(tv.text) && tv.text.toString().equals(value)) {
                    tv.setTextColor(resources.getColor(R.color.text_highlight))
                    tv.setBackgroundColor(resources.getColor(R.color.highlight))
                } else {
                    val tag: String = tv.tag as String
                    val row = tag.substring(0, 1).toInt()
                    val col = tag.substring(1, 2).toInt()
                    if ((row < 3 && col < 3) || (row < 3 && col > 5) || (row in 3..5 && col in 3..5) || (row > 5 && col < 3) || (row > 5 && col > 5)) {
                        tv.setBackgroundColor(resources.getColor(R.color.dark))
                        if (emptyViews.contains(tv.tag.toString())) {
                            tv.setTextColor(resources.getColor(R.color.text_editable))
                        } else {
                            tv.setTextColor(resources.getColor(R.color.text_light))
                        }
                    } else {
                        tv.setBackgroundColor(resources.getColor(R.color.light))
                        if (emptyViews.contains(tv.tag.toString())) {
                            tv.setTextColor(resources.getColor(R.color.text_editable))
                        } else {
                            tv.setTextColor(resources.getColor(R.color.text_dark))
                        }
                    }
                }
            }
        }
    }

    fun checkEffected(view: TextView): Boolean {
        if (focusedTextView == null) return false
        val tag: String = view.tag as String
        val row = tag.substring(0, 1).toInt()
        val col = tag.substring(1, 2).toInt()
        val focusedTag: String = focusedTextView?.tag as String
        val focusedRow = focusedTag.substring(0, 1).toInt()
        val focusedCol = focusedTag.substring(1, 2).toInt()
        if (row == focusedRow || col == focusedCol || getAreaIndex(row, col) == getAreaIndex(focusedRow, focusedCol)) {
            return true
        }
        return false
    }

    fun getAreaIndex(row: Int, col: Int): Int {
        return when {
            row in 0..2 && col in 0..2 -> 0
            row in 0..2 && col in 3..5 -> 1
            row in 0..2 && col in 6..8 -> 2
            row in 3..5 && col in 0..2 -> 3
            row in 3..5 && col in 3..5 -> 4
            row in 3..5 && col in 6..8 -> 5
            row in 6..8 && col in 0..2 -> 6
            row in 6..8 && col in 3..5 -> 7
            row in 6..8 && col in 6..8 -> 8
            else -> -1
        }
    }

    fun check() {
        val result = StringBuilder()
        var isSuccess = true
        for (i in 0..8) {
            var sumRow = 0
            var sumCol = 0
            for (j in 0..8) {
                result.append(gameMap[i][j])
                sumCol += gameMap[j][i]
                sumRow += gameMap[i][j]
            }
            if (sumRow != 45 || sumCol != 45) {
                println("行$i 或 列$i 不合格")
                isSuccess = false
                break
            }
        }

        if (!isSuccess) {
            println("行列 还不行哦, 加油")
            return
        } else {
            for (areaRow in 0..2) {
                for (areaCol in 0..2) {
                    var sumArea = 0
                    for (row in 0..2) {
                        for (col in 0..2) {
                            sumArea += gameMap[row + areaRow * 3][col + areaCol * 3]
                        }
                    }
                    if (sumArea != 45) {
                        println("区域 $areaRow$areaCol 不合格")
                        isSuccess = false
                        break
                    }
                }
            }
        }

        if (!isSuccess) {
            println("区域 还不行哦, 加油")
            return
        } else {
            println("通过")
        }

        if (result.toString().equals(success.toString())) {
            println(success.toString())
            println(result.toString())
            println("通过")
        } else {
            println(success.toString())
            println(result.toString())
            println("还不行哦, 加油")
        }
    }

}
