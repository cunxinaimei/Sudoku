package com.yxc.newsbykotlin

import java.util.*

/**
 * 类注释
 * Created by robin on 16/8/26.
 * @author robin
 */

fun main(args: Array<String>) {
    val game = GameMap()
    for (i in 0..1000){
        game.generate()
    }
}

data class Step(val position: Pair<Int, Int>, val type:Int, val value: Int)

class GameMap() {

    val result = arrayOf(
            arrayOf(0, 0, 0, 0, 0, 0, 0, 0, 0),
            arrayOf(0, 0, 0, 0, 0, 0, 0, 0, 0),
            arrayOf(0, 0, 0, 0, 0, 0, 0, 0, 0),
            arrayOf(0, 0, 0, 0, 0, 0, 0, 0, 0),
            arrayOf(0, 0, 0, 0, 0, 0, 0, 0, 0),
            arrayOf(0, 0, 0, 0, 0, 0, 0, 0, 0),
            arrayOf(0, 0, 0, 0, 0, 0, 0, 0, 0),
            arrayOf(0, 0, 0, 0, 0, 0, 0, 0, 0),
            arrayOf(0, 0, 0, 0, 0, 0, 0, 0, 0)
    )

    val gameMap = arrayListOf(
            Pair("00", Area(0, 0)),
            Pair("01", Area(0, 1)),
            Pair("02", Area(0, 2)),
            Pair("10", Area(1, 0)),
            Pair("11", Area(1, 1)),
            Pair("12", Area(1, 2)),
            Pair("20", Area(2, 0)),
            Pair("21", Area(2, 1)),
            Pair("22", Area(2, 2))
    )
    val r = Random()

    fun getEfectivedArea(key: String): Pair<List<Area>, List<Area>> {
        val row = key.substring(0, 1)
        val col = key.substring(1, 2)
        val resultRow = ArrayList<Area>()
        val resultCol = ArrayList<Area>()
        for ((k, area) in gameMap) {
            if (k.startsWith(row) && !k.equals(key)) {
                resultRow.add(area)
            }
            if (k.endsWith(col) && !k.equals(key)) {
                resultCol.add(area)
            }
        }
        return Pair(resultRow, resultCol)
    }

    fun generate(): Array<Array<Int>> {
        val start = System.currentTimeMillis()
        var failedCount = 0
        val existPositions = ArrayList<Pair<Int, Int>>()
        var value = 1
        all@ while (value<=9){
            var cursor = 0
            while (cursor < gameMap.size) {
                try {
                    val key = gameMap[cursor].first
                    val area = gameMap[cursor].second
                    val contentOrigin = area.getTheUnUsedContent()
                    val content = ArrayList<Pair<Int, Int>>()
                    for (p in contentOrigin) {
                        content.add(p)
                    }
                    val efectivedArea = getEfectivedArea(key)
                    for (efectArea in efectivedArea.first) {
                        val existPosition: Pair<Int, Int> = efectArea.getRowCol(value)
                        if (existPosition.first != -1) {
                            for (pair in contentOrigin) {
                                if (pair.first == existPosition.first) {
                                    content.remove(pair)
                                }
                            }
                        }
                    }
                    for (efectArea in efectivedArea.second) {
                        val existPosition: Pair<Int, Int> = efectArea.getRowCol(value)
                        if (existPosition.second != -1) {
                            for (pair in contentOrigin) {
                                if (pair.second == existPosition.second) {
                                    content.remove(pair)
                                }
                            }
                        }
                    }
                    if (content.size == 0) {
//                        println("无路可走 : $value -- $cursor")
                        failedCount++
                        if (failedCount==100){
                            value = 1
                            existPositions.clear()
                            for (arr in result){
                                for (i in 0..8){
                                    arr[i] = 0
                                }
                            }
                            for ((areaKey, areaInMap) in gameMap){
                                areaInMap.clear()
                            }
//                            println("重新开始 : $value -- $cursor")
                            failedCount = 0
                            continue@all
                        }
                        while (cursor>0){
                            cursor--
                            gameMap[cursor].second.setValueIgnoreRelativePosition(0, existPositions.last().first, existPositions.last().second)
                            result[existPositions.last().first][existPositions.last().second] = 0
                            existPositions.remove(existPositions.last())
                        }
                        continue
                    }
                    var row = -1
                    var col = -1
                    val index = r.nextInt(content.size)
                    var i = 0
                    for ((k, v) in content) {
                        if (i == index) {
                            row = k
                            col = v
                            break
                        }
                        i++
                    }
                    area.setValueIgnoreRelativePosition(value, row, col)
                    existPositions.add(Pair(row, col))
                    cursor++
                } catch (e: Exception) {
//                    println("当前数值 : $value")
//                    println("当前区域 : $cursor")
                    break@all
                }
            }
            value++
        }

        for ((k, area) in gameMap) {
            for ((position, value) in area.content) {
                val row = position.substring(0, 1).toInt()
                val col = position.substring(1, 2).toInt()
                result[row][col] = value
            }
        }
        val end = System.currentTimeMillis()
        println("用时 : ${end-start}")
        return result
    }

    class Area(val areaRow: Int, val areaCol: Int) {

        val content = mutableMapOf(
                Pair("${areaRow * 3 + 0}${areaCol * 3 + 0}", 0),
                Pair("${areaRow * 3 + 0}${areaCol * 3 + 1}", 0),
                Pair("${areaRow * 3 + 0}${areaCol * 3 + 2}", 0),
                Pair("${areaRow * 3 + 1}${areaCol * 3 + 0}", 0),
                Pair("${areaRow * 3 + 1}${areaCol * 3 + 1}", 0),
                Pair("${areaRow * 3 + 1}${areaCol * 3 + 2}", 0),
                Pair("${areaRow * 3 + 2}${areaCol * 3 + 0}", 0),
                Pair("${areaRow * 3 + 2}${areaCol * 3 + 1}", 0),
                Pair("${areaRow * 3 + 2}${areaCol * 3 + 2}", 0)
        )

        var unUsedContent = ArrayList<Pair<Int, Int>>()

        fun clear(){
            for ((key, value) in content){
                content[key] = 0
            }
        }

        fun getRowCol(value: Int): Pair<Int, Int> {
            for ((k, v) in content) {
                if (v == value) {
                    return Pair(k.substring(0, 1).toInt(), k.substring(1, 2).toInt())
                }
            }
            return Pair(-1, -1)
        }

        fun setValue(value: Int, row: Int, col: Int) {
            content["${areaRow * 3 + row}${areaCol * 3 + col}"] = value
        }

        fun setValueIgnoreRelativePosition(value: Int, row: Int, col: Int) {
            content["$row$col"] = value
        }

        fun print() {
            for ((k, v) in content) {
                println("$k value is $v")
            }
        }

        fun getTheUnUsedContent(): ArrayList<Pair<Int, Int>> {
            unUsedContent.clear()
            for ((k, v) in content) {
                if (v == 0) {
                    unUsedContent.add(Pair(k.substring(0, 1).toInt(), k.substring(1, 2).toInt()))
                }
            }
            return unUsedContent
        }
    }
}