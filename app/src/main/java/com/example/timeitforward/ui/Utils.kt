package com.example.timeitforward.ui

import android.content.pm.PackageManager
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.core.graphics.drawable.toBitmap
import com.example.timeitforward.R

// アプリの名前からアイコンイメージを取得
@Composable
fun AppIcon(
    appName: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val iconImage = try {
        context.packageManager.getApplicationIcon(appName).toBitmap().asImageBitmap()
    } catch(e: PackageManager.NameNotFoundException) { // getApplicationIconでappNameからIconを呼び出せなかったとき
        context.resources.getDrawable(R.drawable.null_app_icon).toBitmap().asImageBitmap() // 代替の白い円
    }
    Image(bitmap = iconImage,
        contentDescription = appName,
        modifier = modifier)
}