package com.example.myLifeLog

// 時間の単位を表すオブジェクト
object Period {
    const val DAY = 0
    const val WEEK = 1
    const val MONTH = 2
    val All = listOf(DAY, WEEK, MONTH)

    // インデックスに対応する文字列リソースIDを返す
    fun getStringId(index: Int): Int {
        return when (index) {
            DAY -> R.string.day
            WEEK -> R.string.week
            MONTH -> R.string.month
            else -> throw IllegalArgumentException("unknown period")
        }
    }
}

// 睡眠状態を表すオブジェクト
object SleepState {
    const val SLEEP = 0
    const val AWAKE = 1
    const val UNKNOWN = 2
}

// コンテンツタイプを表すオブジェクト
object ContentType {
    const val APP = 0
    const val LOCATION = 1
    const val SLEEP = 2
    const val OTHERS = 3
    val All = listOf(LOCATION, APP, SLEEP, OTHERS)

    // インデックスに対応する文字列リソースIDを返す
    fun getStringId(index: Int): Int {
        return when (index) {
            APP -> R.string.app
            LOCATION -> R.string.location
            SLEEP -> R.string.sleep
            OTHERS -> R.string.others
            else -> throw IllegalArgumentException("unknown content type")
        }
    }

    // インデックスに対応するアイコンリソースIDを返す
    fun getIconId(index: Int): Int {
        return when (index) {
            APP -> R.drawable.app
            LOCATION -> R.drawable.walking
            SLEEP -> R.drawable.sleep
            OTHERS -> R.drawable.others
            else -> throw IllegalArgumentException("unknown content type")
        }
    }
}

// データタイプを表すオブジェクト
object DataType {
    const val LOCATION = 0
    const val TRANSITION = 1
    const val SLEEP = 2
    val All = listOf(LOCATION, TRANSITION, SLEEP)

    // インデックスに対応する文字列を返す
    fun getString(index: Int): String {
        return when (index) {
            LOCATION -> "location"
            TRANSITION -> "transition"
            SLEEP -> "sleep"
            else -> throw IllegalArgumentException("unknown data type")
        }
    }
}

