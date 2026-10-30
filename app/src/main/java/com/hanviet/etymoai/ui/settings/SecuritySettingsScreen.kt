package com.hanviet.etymoai.ui.settings

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import com.hanviet.etymoai.ai.provider.AiProviderType
import com.hanviet.etymoai.ai.security.SecureApiKeyStorage
import com.hanviet.etymoai.data.translation.TranslationEngine
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hanviet.etymoai.ai.provider.AiProviderType
import com.hanviet.etymoai.ai.security.SecureApiKeyStorage
import java.util.Locale

/**
 * 1. Màn hình Tài khoản & Bảo mật (Security & BYOK Settings Screen)
 * Hiện thực hóa Mục 13.7 (Bảo mật API Key bằng Android Keystore) và Mục 18 (Quyền riêng tư) của Idea 2.2.
 * Hỗ trợ cấu hình toàn diện BYOK: Provider, Endpoint URL, Model selection (Gemini 2.5/2.0, DeepSeek V3/R1, GPT-4o, Claude 3.7/3.5, Ollama)
 * và Trình quản lý bộ nhớ đệm (Cache Manager) đo lường & dọn dẹp dung lượng thực tế.
 */
@Composable
fun SecuritySettingsScreen(
    onBackClick: () -> Unit = {},
    onApiKeySaved: (provider: String, key: String) -> Unit = { _, _ -> },
    onClearDataRequested: () -> Unit = {},
    onExportData: () -> Unit = {},
    onImportData: () -> Unit = {}
) {
    val context = LocalContext.current
    val storage = remember { SecureApiKeyStorage.create(context) }

    var showAiConfigDialog by remember { mutableStateOf(false) }
    var showTranslationEngineDialog by remember { mutableStateOf(false) }
    var showClearCacheDialog by remember { mutableStateOf(false) }
    var showClearDataDialog by remember { mutableStateOf(false) }

    var selectedProvider by remember { mutableStateOf(AiProviderType.GEMINI) }
    var inputApiKey by remember { mutableStateOf(storage.getApiKey(selectedProvider) ?: "") }
    var inputEndpoint by remember { mutableStateOf(storage.getEndpoint(selectedProvider)) }
    var inputModel by remember { mutableStateOf(storage.getModel(selectedProvider)) }
    var isKeyVisible by remember { mutableStateOf(false) }

    var selectedEngine by remember { mutableStateOf(TranslationEngine.fromId(storage.getTranslationEngine())) }
    var deepLApiKey by remember { mutableStateOf(storage.getDeepLKey() ?: "") }
    var isDeepLKeyVisible by remember { mutableStateOf(false) }

    // Tính toán dung lượng cache thực tế trên đĩa
    var cacheSizeBytes by remember {
        mutableLongStateOf(calculateDiskCacheBytes(context))
    }

    // Cập nhật lại form khi đổi provider
    fun switchProvider(newProvider: AiProviderType) {
        selectedProvider = newProvider
        inputApiKey = storage.getApiKey(newProvider) ?: ""
        inputEndpoint = storage.getEndpoint(newProvider)
        inputModel = storage.getModel(newProvider)
    }

    Scaffold(
        topBar = {
            SecuritySettingsTopBar(onBackClick = onBackClick)
        },
        containerColor = Color(0xFFF4F7FB)
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                // Nhóm 1: Quản lý API Key & Cấu hình Model AI (BYOK)
                val hasKey = storage.hasApiKey(selectedProvider)
                val keyStatusText = if (hasKey) "Đã lưu khóa" else "Chưa cấu hình"
                val currentModel = storage.getModel(selectedProvider)
                val endpointDisplay = storage.getEndpoint(selectedProvider).substringAfter("://").take(28)

                SettingMenuRow(
                    icon = Icons.Default.Key,
                    iconBg = Color(0xFFEFF6FF),
                    iconTint = Color(0xFF2563EB),
                    title = "Cấu hình AI & API Key (BYOK)",
                    subtitle = "${selectedProvider.name} • Model: $currentModel • $endpointDisplay ($keyStatusText)",
                    onClick = {
                        switchProvider(selectedProvider)
                        showAiConfigDialog = true
                    }
                )
            }

            item {
                // Banner hỗ trợ đa model + Nút cấu hình nhanh
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.Top) {
                            Text(text = "🛡️", fontSize = 18.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "BYOK Đa Nền Tảng: Gemini 2.5, DeepSeek R1, GPT-4o, Claude 3.7",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF0F172A)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Tự do thiết lập API Key, tùy chỉnh Endpoint URL và chỉ định Model mới nhất để phân tích từ nguyên với chi phí tối ưu.",
                                    fontSize = 13.sp,
                                    color = Color(0xFF64748B),
                                    lineHeight = 18.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Nút [ ⚙️ Cấu hình AI & Endpoint ]
                        Button(
                            onClick = {
                                switchProvider(selectedProvider)
                                showAiConfigDialog = true
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF2563EB)
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Thiết lập AI, Endpoint & Model",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            item {
                // Nhóm 2: Cấu hình Bộ máy Dịch thuật (Machine Translation Engine)
                val engineStatus = when (selectedEngine) {
                    TranslationEngine.GOOGLE_TRANSLATE -> "Google Dịch (Miễn phí)"
                    TranslationEngine.DEEPL -> if (storage.hasDeepLKey()) "DeepL (Đã lưu API Key)" else "DeepL (Chưa nhập khóa)"
                    TranslationEngine.AI_GROUNDED -> "AI BYOK (${selectedProvider.name})"
                }

                SettingMenuRow(
                    icon = Icons.Default.Translate,
                    iconBg = Color(0xFFECFDF5),
                    iconTint = Color(0xFF059669),
                    title = "Bộ máy Dịch thuật (Translation Engine)",
                    subtitle = "$engineStatus • Chọn giữa Google Dịch, DeepL và AI trợ lý",
                    onClick = {
                        deepLApiKey = storage.getDeepLKey() ?: ""
                        showTranslationEngineDialog = true
                    }
                )
            }

            item {
                // Nhóm 3: Trình quản lý bộ nhớ đệm (Cache Manager - Real Data)
                SettingMenuRow(
                    icon = Icons.Default.DeleteSweep,
                    iconBg = Color(0xFFFEF3C7),
                    iconTint = Color(0xFFD97706),
                    title = "Bộ nhớ đệm (Cache Manager)",
                    subtitle = "Dung lượng hiện tại: ${formatByteSize(cacheSizeBytes)} • Bấm để dọn dẹp các tệp tạm",
                    onClick = { showClearCacheDialog = true }
                )
            }

            item {
                // Bảo mật Android Keystore
                SettingMenuRow(
                    icon = Icons.Default.Lock,
                    iconBg = Color(0xFFF0FDF4),
                    iconTint = Color(0xFF16A34A),
                    title = "Bảo mật phần cứng Keystore",
                    subtitle = "Mã hóa AES-256-GCM không thể trích xuất khóa ra khỏi thiết bị",
                    onClick = {
                        Toast.makeText(
                            context,
                            "Khóa API được mã hóa bằng AES-256-GCM trong Android Keystore.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                )
            }

            item {
                // Quyền riêng tư
                SettingMenuRow(
                    icon = Icons.Default.Shield,
                    iconBg = Color(0xFFFAF5FF),
                    iconTint = Color(0xFF9333EA),
                    title = "Quyền riêng tư & Cục bộ",
                    subtitle = "Dữ liệu cá nhân, lịch sử tra cứu lưu 100% cục bộ trên máy",
                    onClick = {
                        Toast.makeText(
                            context,
                            "Ứng dụng hoạt động theo triết lý Offline-first, không gửi lịch sử học tập lên mây.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                )
            }

            item {
                // Sao lưu & Khôi phục
                SettingMenuRow(
                    icon = Icons.Default.CloudUpload,
                    iconBg = Color(0xFFEFF6FF),
                    iconTint = Color(0xFF3B82F6),
                    title = "Sao lưu & Khôi phục",
                    subtitle = "Xuất/nhập file dữ liệu người dùng (user_workspace.db)",
                    onClick = onExportData
                )
            }

            item {
                // Xóa toàn bộ dữ liệu người dùng
                SettingMenuRow(
                    icon = Icons.Default.DeleteOutline,
                    iconBg = Color(0xFFFEE2E2),
                    iconTint = Color(0xFFDC2626),
                    title = "Xóa toàn bộ dữ liệu cá nhân",
                    subtitle = "Xóa lịch sử, từ vựng lưu trữ, tiến độ thẻ học SRS",
                    onClick = { showClearDataDialog = true }
                )
            }

            item {
                Spacer(modifier = Modifier.height(6.dp))
                // Khối cam kết quyền riêng tư cuối màn hình
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFFF1F5F9),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "🛡️", fontSize = 22.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Chúng tôi cam kết bảo vệ quyền riêng tư của bạn. API Key và dữ liệu tra cứu được mã hóa và lưu trữ an toàn trên thiết bị của bạn.",
                            fontSize = 12.sp,
                            color = Color(0xFF475569),
                            lineHeight = 17.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Dialog Cấu hình AI & BYOK (Provider, Endpoint, Model, Key)
    if (showAiConfigDialog) {
        val scrollState = rememberScrollState()
        val recommendedModels = remember(selectedProvider) {
            AiProviderType.getRecommendedModels(selectedProvider)
        }

        AlertDialog(
            onDismissRequest = { showAiConfigDialog = false },
            title = {
                Text(
                    text = "Cấu hình AI & BYOK",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Chọn nhà cung cấp, cấu hình Endpoint URL và chỉ định Model để phân tích từ nguyên:",
                        fontSize = 12.sp,
                        color = Color(0xFF64748B)
                    )

                    // 1. Bộ chọn Provider
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        AiProviderType.entries.forEach { provider ->
                            val isSelected = selectedProvider == provider
                            val label = when (provider) {
                                AiProviderType.GEMINI -> "Gemini"
                                AiProviderType.DEEPSEEK -> "DeepSeek"
                                AiProviderType.OPENAI -> "OpenAI"
                                AiProviderType.CLAUDE -> "Claude"
                                AiProviderType.LOCAL_GGUF -> "Ollama/Proxy"
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) Color(0xFF2563EB) else Color(0xFFF1F5F9),
                                modifier = Modifier.clickable { switchProvider(provider) }
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else Color(0xFF334155),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
                                )
                            }
                        }
                    }

                    // 2. Nhập API Key
                    OutlinedTextField(
                        value = inputApiKey,
                        onValueChange = { inputApiKey = it },
                        label = { Text("API Key (${selectedProvider.name})") },
                        placeholder = {
                            Text(
                                if (selectedProvider == AiProviderType.LOCAL_GGUF) "Tùy chọn (để trống nếu dùng Ollama)"
                                else "sk-... hoặc AIzaSy..."
                            )
                        },
                        singleLine = true,
                        visualTransformation = if (isKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { isKeyVisible = !isKeyVisible }) {
                                Icon(
                                    imageVector = if (isKeyVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = null
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // 3. Endpoint URL (Base URL)
                    OutlinedTextField(
                        value = inputEndpoint,
                        onValueChange = { inputEndpoint = it },
                        label = { Text("Endpoint URL") },
                        placeholder = { Text(selectedProvider.defaultBaseUrl) },
                        singleLine = true,
                        trailingIcon = {
                            IconButton(onClick = { inputEndpoint = selectedProvider.defaultBaseUrl }) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Đặt lại URL mặc định",
                                    tint = Color(0xFF64748B)
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // 4. Lựa chọn Model gợi ý (2025/2026 models)
                    Column {
                        Text(
                            text = "Model đề xuất mới nhất:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF475569)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            recommendedModels.forEach { modelName ->
                                val isChosen = inputModel == modelName
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (isChosen) Color(0xFFEFF6FF) else Color(0xFFF8FAFC),
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (isChosen) Color(0xFF2563EB) else Color(0xFFE2E8F0)
                                    ),
                                    modifier = Modifier.clickable { inputModel = modelName }
                                ) {
                                    Text(
                                        text = modelName,
                                        fontSize = 11.sp,
                                        fontWeight = if (isChosen) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isChosen) Color(0xFF2563EB) else Color(0xFF334155),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }

                    // 5. Tùy chỉnh tên Model
                    OutlinedTextField(
                        value = inputModel,
                        onValueChange = { inputModel = it },
                        label = { Text("Mã nhận diện Model (Model ID)") },
                        placeholder = { Text(selectedProvider.defaultModel) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        storage.saveApiKey(selectedProvider, inputApiKey)
                        storage.saveEndpoint(selectedProvider, inputEndpoint)
                        storage.saveModel(selectedProvider, inputModel)
                        onApiKeySaved(selectedProvider.name, inputApiKey)
                        Toast.makeText(
                            context,
                            "Đã lưu cấu hình AI (${selectedProvider.name} • $inputModel) an toàn!",
                            Toast.LENGTH_SHORT
                        ).show()
                        showAiConfigDialog = false
                    }
                ) {
                    Text("Lưu cấu hình")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAiConfigDialog = false }) {
                    Text("Hủy")
                }
            }
        )
    }

    // Dialog Cấu hình Bộ máy Dịch thuật (Machine Translation Engine & DeepL BYOK)
    if (showTranslationEngineDialog) {
        AlertDialog(
            onDismissRequest = { showTranslationEngineDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Translate,
                        contentDescription = null,
                        tint = Color(0xFF059669),
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Bộ máy Dịch thuật",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Chọn nguồn dịch thuật dùng cho tính năng Dịch câu & Phân tích ngữ cảnh:",
                        fontSize = 13.sp,
                        color = Color(0xFF64748B)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    TranslationEngine.entries.forEach { engine ->
                        val isSelected = selectedEngine == engine
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) Color(0xFFECFDF5) else Color(0xFFF8FAFC),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) Color(0xFF10B981) else Color(0xFFE2E8F0)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { selectedEngine = engine }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = { selectedEngine = engine },
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = Color(0xFF059669)
                                    )
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text(
                                        text = engine.displayName,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color(0xFF065F46) else Color(0xFF0F172A)
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = engine.description,
                                        fontSize = 11.sp,
                                        color = Color(0xFF64748B),
                                        lineHeight = 15.sp
                                    )
                                }
                            }
                        }
                    }

                    if (selectedEngine == TranslationEngine.DEEPL) {
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = deepLApiKey,
                            onValueChange = { deepLApiKey = it },
                            label = { Text("DeepL API Key (Free hoặc Pro)") },
                            placeholder = { Text("vd: xxxxxxxx-xxxx:fx") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            visualTransformation = if (isDeepLKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { isDeepLKeyVisible = !isDeepLKeyVisible }) {
                                    Icon(
                                        imageVector = if (isDeepLKeyVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = null,
                                        tint = Color(0xFF64748B)
                                    )
                                }
                            }
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "💡 Lưu ý: Key kết thúc bằng :fx là DeepL API Free (500k ký tự/tháng miễn phí). Khóa được mã hóa AES-256 trong Android Keystore.",
                            fontSize = 11.sp,
                            color = Color(0xFF059669),
                            lineHeight = 15.sp
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        storage.saveTranslationEngine(selectedEngine.id)
                        if (selectedEngine == TranslationEngine.DEEPL) {
                            storage.saveDeepLKey(deepLApiKey)
                        }
                        Toast.makeText(
                            context,
                            "Đã chọn bộ máy dịch: ${selectedEngine.displayName}",
                            Toast.LENGTH_SHORT
                        ).show()
                        showTranslationEngineDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669))
                ) {
                    Text("Lưu thiết lập")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTranslationEngineDialog = false }) {
                    Text("Hủy")
                }
            }
        )
    }

    // Dialog Xóa sạch Cache (Cache Manager Action)
    if (showClearCacheDialog) {
        AlertDialog(
            onDismissRequest = { showClearCacheDialog = false },
            title = {
                Text(
                    text = "Dọn dẹp bộ nhớ đệm?",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = Color(0xFF0F172A)
                )
            },
            text = {
                Text(
                    text = "Dung lượng hiện tại đang chiếm: ${formatByteSize(cacheSizeBytes)}. Hành động này sẽ xóa các tệp tạm, bộ nhớ đệm ảnh và phản hồi cache AI mà không làm mất từ vựng đã lưu.",
                    fontSize = 13.sp,
                    color = Color(0xFF475569),
                    lineHeight = 18.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val freedBytes = cacheSizeBytes
                        clearDiskCache(context)
                        cacheSizeBytes = calculateDiskCacheBytes(context)
                        Toast.makeText(
                            context,
                            "Đã giải phóng ${formatByteSize(freedBytes)} bộ nhớ đệm!",
                            Toast.LENGTH_SHORT
                        ).show()
                        showClearCacheDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706))
                ) {
                    Text("Dọn sạch")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearCacheDialog = false }) {
                    Text("Hủy")
                }
            }
        )
    }

    // Dialog Xác nhận xóa dữ liệu
    if (showClearDataDialog) {
        AlertDialog(
            onDismissRequest = { showClearDataDialog = false },
            title = {
                Text(
                    text = "Xác nhận xóa dữ liệu?",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFDC2626)
                )
            },
            text = {
                Text(
                    text = "Toàn bộ lịch sử tra cứu, từ vựng lưu trữ cá nhân và bộ nhớ đệm AI sẽ bị xóa vĩnh viễn khỏi thiết bị này.",
                    fontSize = 14.sp,
                    color = Color(0xFF334155)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onClearDataRequested()
                        showClearDataDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) {
                    Text("Xóa toàn bộ")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDataDialog = false }) {
                    Text("Hủy")
                }
            }
        )
    }
}

/**
 * Đo lường dung lượng tệp cache thực tế trên đĩa
 */
private fun calculateDiskCacheBytes(context: Context): Long {
    return try {
        val cacheFiles = context.cacheDir?.walkTopDown()?.filter { it.isFile }?.sumOf { it.length() } ?: 0L
        val codeCacheFiles = context.codeCacheDir?.walkTopDown()?.filter { it.isFile }?.sumOf { it.length() } ?: 0L
        cacheFiles + codeCacheFiles
    } catch (_: Exception) {
        0L
    }
}

/**
 * Xóa đĩa cache
 */
private fun clearDiskCache(context: Context) {
    try {
        context.cacheDir?.listFiles()?.forEach { file ->
            file.deleteRecursively()
        }
    } catch (_: Exception) {
    }
}

/**
 * Định dạng số byte thành chuỗi dễ đọc
 */
private fun formatByteSize(bytes: Long): String {
    return when {
        bytes <= 0L -> "0 B"
        bytes < 1024L -> "$bytes B"
        bytes < 1024L * 1024L -> String.format(Locale.US, "%.1f KB", bytes / 1024.0)
        else -> String.format(Locale.US, "%.2f MB", bytes / (1024.0 * 1024.0))
    }
}

/**
 * TopBar
 */
@Composable
private fun SecuritySettingsTopBar(
    onBackClick: () -> Unit
) {
    Surface(
        color = Color(0xFFF4F7FB),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Quay lại",
                    tint = Color(0xFF0F172A)
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Tài khoản & Bảo mật",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A)
            )
        }
    }
}

/**
 * Mục cài đặt menu dạng hàng (Row card với Icon, Title, Subtitle, Chevron)
 */
@Composable
private fun SettingMenuRow(
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
        shadowElevation = 0.5.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = Color(0xFF64748B),
                    maxLines = 2,
                    lineHeight = 16.sp
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = Color(0xFF94A3B8),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
