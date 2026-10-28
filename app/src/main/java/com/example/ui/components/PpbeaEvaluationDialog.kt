package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.EnquadramentoMausTratos
import com.example.data.model.GrauBemEstar
import com.example.data.model.ParecerDomain
import com.example.data.model.PpbeaCriteriaRepository
import com.example.data.model.PpbeaCriterion
import com.example.data.model.PpbeaDiagnosis
import com.example.data.model.PpbeaDomain

/**
 * Caixa de Diálogo do Campo de Avaliação (ANEXO D – PPBEA)
 * Com botões Switch Sim / Não para cada critério (situação boa vs ruim),
 * botão "Resultado" para processamento lógico conforme o manual,
 * e tela do Diagnóstico Geral idêntica à imagem image.png.
 */
@Composable
fun SamplingCountCard(
    count: Int,
    onCountChange: (Int) -> Unit,
    isDog: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        color = Color(0xFFF1F8F4),
        modifier = modifier
            .fillMaxWidth()
            .testTag("ppbea_sampling_count_bar"),
        border = BorderStroke(1.2.dp, Color(0xFFA5D6A7)),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isDog) "Quantidade de cães periciados (geral):" else "Quantidade de cavalos periciados (geral):",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color(0xFF1B5E20)
                    )
                    Text(
                        text = "Amostragem geral avaliada sob o Protocolo PPBEA",
                        fontSize = 10.5.sp,
                        color = Color(0xFF2E7D32)
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    IconButton(
                        onClick = { if (count > 1) onCountChange(count - 1) },
                        enabled = count > 1,
                        modifier = Modifier
                            .size(36.dp)
                            .background(if (count > 1) Color(0xFFC8E6C9) else Color(0xFFE0E0E0), CircleShape)
                            .testTag("ppbea_decrease_sampling_count")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Remove,
                            contentDescription = "Diminuir quantidade",
                            tint = if (count > 1) Color(0xFF1B5E20) else Color.Gray,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF1B5E20),
                        modifier = Modifier
                            .width(46.dp)
                            .height(36.dp)
                            .testTag("ppbea_sampling_count_display")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = count.toString(),
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color.White
                            )
                        }
                    }

                    IconButton(
                        onClick = { onCountChange(count + 1) },
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color(0xFFC8E6C9), CircleShape)
                            .testTag("ppbea_increase_sampling_count")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Aumentar quantidade",
                            tint = Color(0xFF1B5E20),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Presets rápidos (1, 2, 3, 5, 10+)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(1, 2, 3, 5, 10).forEach { pCount ->
                    val isSel = count == pCount
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { onCountChange(pCount) }
                            .border(
                                width = if (isSel) 1.5.dp else 0.8.dp,
                                color = if (isSel) Color(0xFF1B5E20) else Color(0xFFA5D6A7),
                                shape = RoundedCornerShape(6.dp)
                            ),
                        color = if (isSel) Color(0xFF2E7D32) else Color.White
                    ) {
                        Text(
                            text = if (pCount == 10) "10+" else "$pCount",
                            textAlign = TextAlign.Center,
                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 11.sp,
                            color = if (isSel) Color.White else Color(0xFF1B5E20),
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PpbeaEvaluationDialog(
    initialDiagnosis: PpbeaDiagnosis?,
    initialAnimalCount: Int = 1,
    isDog: Boolean = false,
    isSamplingMode: Boolean = true,
    onDismiss: () -> Unit,
    onSaveDiagnosis: (PpbeaDiagnosis, Int) -> Unit
) {
    var samplingCount by remember(initialAnimalCount) { mutableIntStateOf(maxOf(1, initialAnimalCount)) }

    // Armazena as respostas dos switches: criterionId -> isYes (true = SIM, false = NÃO)
    val answersState = remember(initialDiagnosis) {
        mutableStateMapOf<String, Boolean>().apply {
            if (initialDiagnosis != null && initialDiagnosis.answersMap.isNotEmpty()) {
                putAll(initialDiagnosis.answersMap)
            }
        }
    }

    var selectedDomainIndex by remember { mutableIntStateOf(0) }
    var currentDiagnosis by remember(initialDiagnosis) { mutableStateOf(initialDiagnosis ?: PpbeaDiagnosis()) }
    var showResultScreen by remember(initialDiagnosis) { mutableStateOf(initialDiagnosis?.isEvaluated == true) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.94f)
                .clip(RoundedCornerShape(20.dp))
                .testTag("dialog_ppbea_evaluation"),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            shadowElevation = 16.dp,
            border = BorderStroke(1.5.dp, Color(0xFF1B5E20).copy(alpha = 0.6f))
        ) {
            if (showResultScreen) {
                // TELA DO DIAGNÓSTICO GERAL (IDÊNTICA À IMAGE.PNG ANEXADA)
                DiagnosticoGeralView(
                    diagnosis = currentDiagnosis,
                    samplingCount = samplingCount,
                    onSamplingCountChange = { samplingCount = it },
                    isSamplingMode = isSamplingMode,
                    isDog = isDog,
                    onUpdateDiagnosis = { currentDiagnosis = it },
                    onBackToSwitches = { showResultScreen = false },
                    onConfirmDiagnosis = {
                        onSaveDiagnosis(currentDiagnosis, samplingCount)
                        onDismiss()
                    }
                )
            } else {
                // TELA DOS INDICADORES COM SWITCHES SIM / NÃO
                Column(modifier = Modifier.fillMaxSize()) {
                    // Header Oficial
                    Surface(
                        color = Color(0xFF1B5E20),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color.White.copy(alpha = 0.2f),
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Assignment,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Column {
                                    Text(
                                        text = "PROTOCOLO PPBEA",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 14.sp,
                                        color = Color.White,
                                        letterSpacing = 0.5.sp
                                    )
                                    if (isSamplingMode) {
                                        Text(
                                            text = "Amostragem Geral",
                                            fontSize = 10.sp,
                                            color = Color.White.copy(alpha = 0.85f),
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }

                            IconButton(
                                onClick = onDismiss,
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(Color.White.copy(alpha = 0.2f), CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Fechar",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    // Seletor de Animais por Amostragem (se estiver em modo amostragem)
                    if (isSamplingMode) {
                        SamplingCountCard(
                            count = samplingCount,
                            onCountChange = { samplingCount = it },
                            isDog = isDog,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }

                    // Abas dos 4 Domínios do Manual
                    val domains = PpbeaDomain.entries
                    TabRow(
                        selectedTabIndex = selectedDomainIndex,
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        contentColor = Color(0xFF1B5E20),
                        indicator = { tabPositions ->
                            TabRowDefaults.SecondaryIndicator(
                                Modifier.tabIndicatorOffset(tabPositions[selectedDomainIndex]),
                                color = Color(0xFF1B5E20),
                                height = 3.dp
                            )
                        }
                    ) {
                        domains.forEachIndexed { index, dom ->
                            val isSelected = selectedDomainIndex == index
                            Tab(
                                selected = isSelected,
                                onClick = { selectedDomainIndex = index },
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(text = dom.icon, fontSize = 12.sp)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = dom.title,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            fontSize = 11.sp,
                                            color = if (isSelected) Color(0xFF1B5E20) else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            )
                        }
                    }

                    // Sub-cabeçalho com descrição do domínio atual
                    val activeDomain = domains[selectedDomainIndex]
                    Surface(
                        color = Color(0xFFE8F5E9),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = activeDomain.icon, fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${activeDomain.title}: ${activeDomain.description}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF2E7D32)
                            )
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    // Lista de Critérios com Switches Sim / Não
                    val criteriaInDomain = remember(selectedDomainIndex) {
                        PpbeaCriteriaRepository.criteria.filter { it.domain == activeDomain }
                    }

                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(criteriaInDomain, key = { it.id }) { criterion ->
                            val isYes = answersState[criterion.id] == true

                            PpbeaSwitchCriterionCard(
                                criterion = criterion,
                                isYes = isYes,
                                onToggle = { newYesState ->
                                    answersState[criterion.id] = newYesState
                                }
                            )
                        }

                        item {
                            Spacer(modifier = Modifier.height(14.dp))
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    // Barra Inferior com Botão "Resultado" Conforme Solicitado
                    Surface(
                        color = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedButton(
                                onClick = onDismiss,
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Cancelar", fontSize = 12.5.sp)
                            }

                            // BOTÃO "RESULTADO" (Calcula laudo lógico conforme manual CRMV-MS)
                            Button(
                                onClick = {
                                    val diagnosis = PpbeaCriteriaRepository.evaluate(answersState.toMap())
                                    currentDiagnosis = diagnosis
                                    showResultScreen = true
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF1B5E20),
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("btn_ppbea_resultado")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Gavel,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Resultado & Diagnóstico",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.5.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Card individual com a pergunta do ANEXO D e o BOTÃO SWITCH para SIM ou NÃO.
 * Mostra claramente a situação Boa vs Ruim.
 */
@Composable
private fun PpbeaSwitchCriterionCard(
    criterion: PpbeaCriterion,
    isYes: Boolean,
    onToggle: (Boolean) -> Unit
) {
    // Determina se a escolha atual indica uma situação Ruim (Alerta/Inadequação)
    val isRuim = if (criterion.simIsRuim) isYes else !isYes

    val cardBg = if (isRuim) Color(0xFFFFF8F8) else Color(0xFFF7FBF8)
    val cardBorder = if (isRuim) Color(0xFFEF9A9A) else Color(0xFFA5D6A7)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onToggle(!isYes) }
            .testTag("switch_criterion_${criterion.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = BorderStroke(1.2.dp, cardBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Pergunta Principal
            Text(
                text = criterion.question,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 17.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Detalhes da situação boa vs ruim
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isRuim) "⚠️ Situação de Risco: ${criterion.badStateExplanation}"
                    else "✅ Situação Regular/Boa: ${criterion.goodStateExplanation}",
                    fontSize = 11.sp,
                    color = if (isRuim) Color(0xFFC62828) else Color(0xFF2E7D32),
                    lineHeight = 14.sp,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
            Spacer(modifier = Modifier.height(8.dp))

            // Controle do SWITCH com rótulos explícitos de "SIM" e "NÃO"
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Badge de status da resposta
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (isRuim) Color(0xFFFFEBEE) else Color(0xFFE8F5E9)
                ) {
                    Text(
                        text = if (isRuim) {
                            if (criterion.isSevereInadequacy) "⚠️ INADEQUAÇÃO" else "⚠️ REGULAR / ALERTA"
                        } else {
                            "✅ ADEQUADO"
                        },
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isRuim) Color(0xFFC62828) else Color(0xFF2E7D32),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "NÃO",
                        fontSize = 12.sp,
                        fontWeight = if (!isYes) FontWeight.Bold else FontWeight.Normal,
                        color = if (!isYes) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    // BOTÃO SWITCH
                    Switch(
                        checked = isYes,
                        onCheckedChange = { onToggle(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = if (criterion.simIsRuim) Color(0xFFC62828) else Color(0xFF2E7D32),
                            uncheckedThumbColor = Color.White,
                            uncheckedTrackColor = if (!criterion.simIsRuim) Color(0xFFC62828) else Color(0xFF757575)
                        ),
                        modifier = Modifier.testTag("switch_toggle_${criterion.id}")
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        text = "SIM",
                        fontSize = 12.sp,
                        fontWeight = if (isYes) FontWeight.Bold else FontWeight.Normal,
                        color = if (isYes) {
                            if (criterion.simIsRuim) Color(0xFFC62828) else Color(0xFF2E7D32)
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }
        }
    }
}

/**
 * Visualização do DIAGNÓSTICO GERAL baseada exatamente na image.png anexada pelo usuário:
 * - Inadequação de quais conjuntos de indicadores? (Nutricionais, De conforto, De Saúde, Comportamentais)
 * - Grau de bem estar: (Muito alto, Alto, Regular, Baixo, Muito baixo)
 * - Ocorrência de maus-tratos:
 *     * Configura maus-tratos (baixo e muito baixo)
 *     * Não configura maus-tratos, mas precisa de recomendações e retorno (regular)
 *     * Não configura maus-tratos e não precisa de retorno (alto e muito alto)
 * - Observações
 */
@Composable
fun PpbeaEvaluationDialog(
    initialDiagnosis: PpbeaDiagnosis?,
    onDismiss: () -> Unit,
    onSaveDiagnosis: (PpbeaDiagnosis) -> Unit
) {
    PpbeaEvaluationDialog(
        initialDiagnosis = initialDiagnosis,
        initialAnimalCount = 1,
        isDog = false,
        isSamplingMode = false,
        onDismiss = onDismiss,
        onSaveDiagnosis = { diag, _ -> onSaveDiagnosis(diag) }
    )
}

/**
 * Visualização do DIAGNÓSTICO GERAL baseada exatamente na image.png anexada pelo usuário:
 * - Inadequação de quais conjuntos de indicadores? (Nutricionais, De conforto, De Saúde, Comportamentais)
 * - Grau de bem estar: (Muito alto, Alto, Regular, Baixo, Muito baixo)
 * - Ocorrência de maus-tratos:
 *     * Configura maus-tratos (baixo e muito baixo)
 *     * Não configura maus-tratos, mas precisa de recomendações e retorno (regular)
 *     * Não configura maus-tratos e não precisa de retorno (alto e muito alto)
 * - Observações
 */
@Composable
fun DiagnosticoGeralView(
    diagnosis: PpbeaDiagnosis,
    samplingCount: Int = 1,
    onSamplingCountChange: (Int) -> Unit = {},
    isSamplingMode: Boolean = false,
    isDog: Boolean = false,
    onUpdateDiagnosis: (PpbeaDiagnosis) -> Unit,
    onBackToSwitches: () -> Unit,
    onConfirmDiagnosis: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
            .testTag("view_diagnostico_geral")
    ) {
        // Título Exato da Caixa Conforme image.png
        Surface(
            color = Color(0xFF1B5E20),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Diagnóstico Geral",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = Color.White
                    )
                    if (isSamplingMode) {
                        Text(
                            text = "Amostragem Geral ($samplingCount ${if (isDog) "cães" else "cavalos"})",
                            fontSize = 10.5.sp,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                }

                Surface(
                    color = Color(diagnosis.grauBemEstar.colorHex),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = diagnosis.grauBemEstar.label.uppercase(),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Seletor de animais por amostragem dentro do Diagnóstico Geral
        if (isSamplingMode) {
            SamplingCountCard(
                count = samplingCount,
                onCountChange = onSamplingCountChange,
                isDog = isDog,
                modifier = Modifier.padding(bottom = 10.dp)
            )
        }

        // SEÇÃO 1: Inadequação de quais conjuntos de indicadores?
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "Inadequação de quais conjuntos de indicadores?",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.5.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Checkboxes com os 4 domínios da image.png
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Nutricionais
                    DiagnosisCheckboxItem(
                        label = "Nutricionais",
                        checked = diagnosis.hasNutricionalInadequacy,
                        parecer = diagnosis.nutricionalParecer,
                        modifier = Modifier.weight(1f)
                    )

                    // De conforto
                    DiagnosisCheckboxItem(
                        label = "De conforto",
                        checked = diagnosis.hasConfortoInadequacy,
                        parecer = diagnosis.confortoParecer,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // De Saúde
                    DiagnosisCheckboxItem(
                        label = "De Saúde",
                        checked = diagnosis.hasSaudeInadequacy,
                        parecer = diagnosis.saudeParecer,
                        modifier = Modifier.weight(1f)
                    )

                    // Comportamentais
                    DiagnosisCheckboxItem(
                        label = "Comportamentais",
                        checked = diagnosis.hasComportamentalInadequacy,
                        parecer = diagnosis.comportamentalParecer,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // SEÇÃO 2: Grau de bem estar:
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "Grau de bem estar:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.5.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Opções da image.png: Muito alto, Alto, Regular, Baixo, Muito baixo
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    GrauBemEstar.entries.forEach { grau ->
                        val isSelected = diagnosis.grauBemEstar == grau
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable {
                                onUpdateDiagnosis(diagnosis.copy(grauBemEstar = grau))
                            }
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { onUpdateDiagnosis(diagnosis.copy(grauBemEstar = grau)) },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = Color(grau.colorHex)
                                ),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = grau.label,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color(grau.colorHex) else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // SEÇÃO 3: Ocorrência de maus-tratos
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(
                containerColor = when (diagnosis.ocorrenciaMausTratos) {
                    EnquadramentoMausTratos.CONFIGURA_MAUS_TRATOS -> Color(0xFFFFF5F5)
                    EnquadramentoMausTratos.PRECISA_RECOMENDACOES_RETORNO -> Color(0xFFFFFBF2)
                    EnquadramentoMausTratos.NAO_CONFIGURA_MAUS_TRATOS -> Color(0xFFF1F8F4)
                }
            ),
            border = BorderStroke(
                1.dp,
                Color(diagnosis.ocorrenciaMausTratos.colorHex).copy(alpha = 0.5f)
            )
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "Ocorrência de maus-tratos",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.5.sp,
                    color = Color(diagnosis.ocorrenciaMausTratos.colorHex)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Três opções formais da image.png
                EnquadramentoMausTratos.entries.forEach { enq ->
                    val isSelected = diagnosis.ocorrenciaMausTratos == enq
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp)
                            .clickable {
                                onUpdateDiagnosis(
                                    diagnosis.copy(
                                        ocorrenciaMausTratos = enq
                                    )
                                )
                            }
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = {
                                onUpdateDiagnosis(
                                    diagnosis.copy(
                                        ocorrenciaMausTratos = enq
                                    )
                                )
                            },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = Color(enq.colorHex)
                            ),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = enq.label,
                            fontSize = 11.5.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) Color(enq.colorHex) else MaterialTheme.colorScheme.onSurface,
                            lineHeight = 15.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // SEÇÃO: Observações:
        Text(
            text = "Observações:",
            fontWeight = FontWeight.Bold,
            fontSize = 13.5.sp,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(4.dp))

        OutlinedTextField(
            value = diagnosis.observacoes,
            onValueChange = { onUpdateDiagnosis(diagnosis.copy(observacoes = it)) },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("input_ppbea_observacoes"),
            minLines = 4,
            maxLines = 8,
            shape = RoundedCornerShape(10.dp),
            placeholder = { Text("Laudo técnico pericial fundamentado conforme CRMV-MS...") }
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Botões Finais de Ação
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = onBackToSwitches,
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Editar critérios", fontSize = 12.sp)
            }

            Button(
                onClick = onConfirmDiagnosis,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1B5E20),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("btn_confirm_diagnostico_geral")
            ) {
                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Aplicar Laudo", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
    }
}

@Composable
private fun DiagnosisCheckboxItem(
    label: String,
    checked: Boolean,
    parecer: ParecerDomain,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = if (checked) Color(0xFFFFEBEE) else Color(0xFFF1F8F4),
        border = BorderStroke(
            1.dp,
            if (checked) Color(0xFFEF9A9A) else Color(0xFFA5D6A7)
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = checked,
                onCheckedChange = null,
                colors = CheckboxDefaults.colors(
                    checkedColor = Color(0xFFC62828),
                    uncheckedColor = Color(0xFF2E7D32)
                ),
                modifier = Modifier.size(20.dp)
            )

            Spacer(modifier = Modifier.width(6.dp))

            Column {
                Text(
                    text = label,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (checked) Color(0xFFC62828) else Color(0xFF2E7D32)
                )
                Text(
                    text = parecer.label,
                    fontSize = 9.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
