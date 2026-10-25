package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

/**
 * 1. Botão "Abrir Avaliação" estilizado exatamente conforme o mockup Abrir_avalia__o_9fb09810.png:
 * Formato pílula elevado, fundo lavanda suave com camada interna translúcida,
 * ícone circular com estrela branca e tipografia púrpura profunda "Abrir Avaliação".
 */
@Composable
fun PpbeaOpenEvaluationButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    hasDiagnosis: Boolean = false,
    individualCount: Int = 0
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(32.dp),
        color = Color(0xFFE8DEF8), // Tom lavanda suave do design M3
        border = BorderStroke(1.2.dp, Color(0xFFD0BCFF)),
        shadowElevation = 4.dp,
        modifier = modifier
            .fillMaxWidth()
            .testTag("btn_abrir_avaliacao_ppbea")
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            // Container interno arredondado com leve contraste
            Surface(
                shape = RoundedCornerShape(26.dp),
                color = Color(0xFFDCC8FA).copy(alpha = 0.55f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    // Ícone circular com estrela branca conforme mockup
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFF553C8B), // Púrpura profundo do ícone
                        shadowElevation = 2.dp,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Filled.Star,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(horizontalAlignment = Alignment.Start) {
                        Text(
                            text = if (hasDiagnosis || individualCount > 0) "Abrir Avaliação (Editar)" else "Abrir Avaliação",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF381E72), // Púrpura escuro da tipografia
                            letterSpacing = 0.3.sp
                        )
                        if (individualCount > 0) {
                            Text(
                                text = "$individualCount animais individualizados",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF65558F)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 2. Caixa de Diálogo Modal conforme o mockup image_686e7f53.png:
 * Topo em verde vibrante ("Avaliação - PPBEA" e texto),
 * e base em lavanda clara com duas opções:
 * "Avaliação Individualizada" (com padrão de grade sutil) e "Amostra" (Amostragem).
 */
@Composable
fun PpbeaSelectionModalDialog(
    onDismiss: () -> Unit,
    onSelectIndividualized: () -> Unit,
    onSelectSampling: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(26.dp))
                .testTag("dialog_ppbea_selection_modal"),
            shape = RoundedCornerShape(26.dp),
            shadowElevation = 16.dp,
            border = BorderStroke(2.dp, Color(0xFF6EC625))
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // PARTE SUPERIOR (VERDE VIBRANTE conforme imagem image_686e7f53.png)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF6FC826)) // Tom verde limão / grama vibrante do mockup
                        .padding(horizontal = 22.dp, vertical = 20.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Avaliação - PPBEA",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1B2E0B), // Texto escuro com alto contraste sobre o verde
                            letterSpacing = 0.2.sp
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Selecione a modalidade pericial de bem-estar animal:",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF22420E)
                        )
                    }
                }

                // Linha divisória sutil
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.5.dp)
                        .background(Color(0xFFC48BFF))
                )

                // PARTE INFERIOR (LAVANDA / BRANCO-AROXEADO conforme imagem image_686e7f53.png)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF3EDF7)) // Fundo lavanda suave do mockup
                        .padding(horizontal = 16.dp, vertical = 22.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // OPÇÃO 1: "Avaliação Individualizada" (com textura de grade técnica vermelha/rosa conforme mockup)
                        Card(
                            onClick = onSelectIndividualized,
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFDFBFF)),
                            border = BorderStroke(1.2.dp, Color(0xFFD0BCFF)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                            modifier = Modifier
                                .weight(1.15f)
                                .height(95.dp)
                                .testTag("btn_modal_avaliacao_individualizada")
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(95.dp)
                                    // Padrão de grade sutil em rosa/vermelho desenhado em Canvas conforme o mockup
                                    .drawBehind {
                                        val gridSize = 12.dp.toPx()
                                        val gridColor = Color(0xFFFFCDD2).copy(alpha = 0.55f)
                                        var x = 0f
                                        while (x < size.width) {
                                            drawLine(gridColor, Offset(x, 0f), Offset(x, size.height), strokeWidth = 1f)
                                            x += gridSize
                                        }
                                        var y = 0f
                                        while (y < size.height) {
                                            drawLine(gridColor, Offset(0f, y), Offset(size.width, y), strokeWidth = 1f)
                                            y += gridSize
                                        }
                                    }
                                    .padding(horizontal = 10.dp, vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.GridView,
                                        contentDescription = null,
                                        tint = Color(0xFF4F378B),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Avaliação Individualizada",
                                        fontSize = 13.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF4F378B), // Púrpura escuro conforme o mockup
                                        textAlign = TextAlign.Center,
                                        lineHeight = 16.sp
                                    )
                                }
                            }
                        }

                        // OPÇÃO 2: "Amostra" / "Amostragem"
                        Card(
                            onClick = onSelectSampling,
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFDFBFF)),
                            border = BorderStroke(1.2.dp, Color(0xFFD0BCFF)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                            modifier = Modifier
                                .weight(0.85f)
                                .height(95.dp)
                                .testTag("btn_modal_amostra")
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(95.dp)
                                    .padding(horizontal = 10.dp, vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Group,
                                        contentDescription = null,
                                        tint = Color(0xFF4F378B),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Amostra",
                                        fontSize = 14.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF4F378B), // Púrpura escuro conforme o mockup
                                        textAlign = TextAlign.Center
                                    )
                                    Text(
                                        text = "(Geral)",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Normal,
                                        color = Color(0xFF65558F)
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
