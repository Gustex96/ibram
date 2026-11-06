package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.R

/**
 * Representa uma espécie catalogada dentro do card "Outros".
 */
data class OtherSpeciesOption(
    val name: String,
    val subtitle: String,
    val emoji: String,
    val iconRes: Int
)

/**
 * Caixa de Diálogo Flutuante do Card "Outros" no Menu Carrossel.
 * Contém os cards das espécies Suíno, Bovino, Caprino/Ovino, além do campo
 * para digitação e outras sugestões rápidas.
 */
@Composable
fun FloatingOtherSpeciesDialog(
    initialText: String = "",
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var speciesInput by remember { mutableStateOf(initialText) }
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    // Cards destacados conforme solicitação: Suíno, Bovino e Caprino/Ovino
    val featuredSpecies = remember {
        listOf(
            OtherSpeciesOption(
                name = "Suíno",
                subtitle = "Porcos, cevados e matrizes suínas",
                emoji = "🐖",
                iconRes = R.drawable.ic_species_swine
            ),
            OtherSpeciesOption(
                name = "Bovino",
                subtitle = "Gado de corte, leite e bezerros",
                emoji = "🐂",
                iconRes = R.drawable.ic_species_bovine
            ),
            OtherSpeciesOption(
                name = "Caprino / Ovino",
                subtitle = "Bodes, cabras, ovelhas e carneiros",
                emoji = "🐐",
                iconRes = R.drawable.ic_species_caprine
            )
        )
    }

    val additionalSuggestions = listOf(
        "Ave Silvestre",
        "Coelho",
        "Muares / Asininos (Burro/Jumento)",
        "Equídeos Não Convencionais"
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFF0A2216),
            border = BorderStroke(1.5.dp, Color(0xFF69F0AE)),
            shadowElevation = 24.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 12.dp)
                .testTag("floating_other_species_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 620.dp)
                    .verticalScroll(scrollState)
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header com Ícone e Botão Fechar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = Color(0xFF1B4D36),
                            shape = CircleShape,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Pets,
                                    contentDescription = null,
                                    tint = Color(0xFF69F0AE),
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Outras Espécies",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Selecione ou personalize a espécie",
                                fontSize = 11.sp,
                                color = Color(0xFFC8E6C9)
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Fechar",
                            tint = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Seção: Espécies Catalogadas no Card Outros (Suíno, Bovino, Caprino/Ovino)
                Text(
                    text = "Espécies catalogadas disponíveis:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF69F0AE),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    featuredSpecies.forEach { opt ->
                        val isSelected = speciesInput.equals(opt.name, ignoreCase = true) ||
                                (opt.name.contains("Suíno", true) && speciesInput.contains("Suíno", true)) ||
                                (opt.name.contains("Bovino", true) && speciesInput.contains("Bovino", true)) ||
                                (opt.name.contains("Caprino", true) && speciesInput.contains("Caprino", true))

                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = if (isSelected) Color(0xFF1B4D36) else Color(0xFF142E1F),
                            border = BorderStroke(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) Color(0xFF69F0AE) else Color(0x4469F0AE)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    speciesInput = opt.name
                                }
                                .testTag("card_species_option_${opt.name.lowercase().replace(" ", "_").replace("/", "_")}")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    color = if (isSelected) Color(0xFF2E7D32) else Color(0xFF0F3622),
                                    shape = CircleShape,
                                    modifier = Modifier.size(42.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            painter = painterResource(id = opt.iconRes),
                                            contentDescription = opt.name,
                                            tint = if (isSelected) Color(0xFF69F0AE) else Color.White,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "${opt.emoji} ${opt.name}",
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = opt.subtitle,
                                        fontSize = 11.5.sp,
                                        color = Color(0xFFC8E6C9)
                                    )
                                }

                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Selecionado",
                                        tint = Color(0xFF69F0AE),
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Seção: Digitação personalizada
                Text(
                    text = "Ou personalize com o nome de outra espécie:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFFC8E6C9),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = speciesInput,
                    onValueChange = { speciesInput = it },
                    placeholder = {
                        Text(
                            text = "Ex: Coelho, Muares, Ave silvestre...",
                            color = Color(0x88FFFFFF)
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF69F0AE),
                        unfocusedBorderColor = Color(0x66FFFFFF),
                        cursorColor = Color(0xFF69F0AE),
                        focusedContainerColor = Color(0x22000000),
                        unfocusedContainerColor = Color(0x22000000)
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {
                        focusManager.clearFocus()
                        if (speciesInput.isNotBlank()) {
                            onConfirm(speciesInput.trim())
                        }
                    }),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_other_species_name")
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Sugestões Complementares
                Text(
                    text = "Outras sugestões rápidas:",
                    fontSize = 11.sp,
                    color = Color(0xFF81C784),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(6.dp))

                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    additionalSuggestions.chunked(2).forEach { rowItems ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            rowItems.forEach { suggestion ->
                                val isSelected = speciesInput.equals(suggestion, ignoreCase = true)
                                Surface(
                                    color = if (isSelected) Color(0xFF2E7D32) else Color(0x33FFFFFF),
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(
                                        1.dp,
                                        if (isSelected) Color(0xFF69F0AE) else Color(0x33FFFFFF)
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable {
                                            speciesInput = suggestion
                                        }
                                ) {
                                    Text(
                                        text = suggestion,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color.White,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(vertical = 6.dp, horizontal = 4.dp),
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Botões de Ação
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(
                            text = "Cancelar",
                            color = Color(0xFFCFD8DC)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            val finalName = speciesInput.trim().ifEmpty { "Outros" }
                            onConfirm(finalName)
                        },
                        enabled = speciesInput.isNotBlank(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2E7D32),
                            contentColor = Color.White,
                            disabledContainerColor = Color(0x332E7D32),
                            disabledContentColor = Color(0x66FFFFFF)
                        ),
                        modifier = Modifier.testTag("confirm_other_species_button")
                    ) {
                        Text(
                            text = if (speciesInput.isNotBlank()) "Iniciar (${speciesInput.trim()})" else "Confirmar",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}
