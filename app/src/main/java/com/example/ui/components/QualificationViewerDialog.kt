package com.example.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.SanadTheme
import com.example.util.QualificationAttachmentHelper

@Composable
fun QualificationViewerDialog(
    qualificationTitle: String,
    qualificationInstitution: String,
    fileName: String,
    mimeType: String,
    sizeBytes: Long,
    base64Content: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var isLoading by remember { mutableStateOf(true) }
    var bitmaps by remember { mutableStateOf<List<Bitmap>>(emptyList()) }

    LaunchedEffect(base64Content, mimeType) {
        isLoading = true
        bitmaps = QualificationAttachmentHelper.decodeBase64ToPreviewBitmaps(
            context = context,
            mimeType = mimeType,
            base64Content = base64Content
        )
        isLoading = false
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .testTag("qualification_viewer_dialog"),
            shape = RoundedCornerShape(24.dp),
            color = SanadTheme.colors.bg,
            border = BorderStroke(1.dp, SanadTheme.colors.divider)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Surface(
                    color = SanadTheme.colors.surface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = if (mimeType == "application/pdf") Icons.Default.PictureAsPdf else Icons.Default.Image,
                                contentDescription = null,
                                tint = SanadTheme.colors.accent,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "معاينة المؤهل المرفق",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = SanadTheme.colors.text
                                )
                                Text(
                                    text = "$fileName • ${QualificationAttachmentHelper.formatFileSize(sizeBytes)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = SanadTheme.colors.textSecondary
                                )
                            }
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("close_qualification_viewer")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "إغلاق المعاينة",
                                tint = SanadTheme.colors.text
                            )
                        }
                    }
                }

                // Qualification Info Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SanadTheme.colors.surface),
                    border = BorderStroke(1.dp, SanadTheme.colors.divider)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.School,
                            contentDescription = null,
                            tint = SanadTheme.colors.accent,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = qualificationTitle.ifBlank { "مؤهل أكاديمي / مهني" },
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = SanadTheme.colors.text
                            )
                            if (qualificationInstitution.isNotBlank()) {
                                Text(
                                    text = "الجهة المانحة: $qualificationInstitution",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SanadTheme.colors.textSecondary
                                )
                            }
                        }
                    }
                }

                // Document Preview Body
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    when {
                        isLoading -> {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(color = SanadTheme.colors.accent)
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "جاري تجهيز معاينة المستند...",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SanadTheme.colors.textSecondary
                                )
                            }
                        }

                        bitmaps.isEmpty() -> {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Description,
                                    contentDescription = null,
                                    tint = SanadTheme.colors.textSecondary,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "تعذر عرض معاينة هذا الملف، يُرجى التأكد من سلامة المرفق.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = SanadTheme.colors.textSecondary
                                )
                            }
                        }

                        else -> {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                itemsIndexed(bitmaps) { index, pageBitmap ->
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(SanadTheme.colors.surface)
                                            .border(1.dp, SanadTheme.colors.divider, RoundedCornerShape(16.dp))
                                            .padding(8.dp)
                                    ) {
                                        if (bitmaps.size > 1) {
                                            Text(
                                                text = "صفحة ${index + 1} من ${bitmaps.size}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = SanadTheme.colors.textSecondary,
                                                modifier = Modifier.padding(bottom = 6.dp, start = 4.dp)
                                            )
                                        }
                                        Image(
                                            bitmap = pageBitmap.asImageBitmap(),
                                            contentDescription = "صورة المؤهل صفحة ${index + 1}",
                                            contentScale = ContentScale.FillWidth,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(12.dp))
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
