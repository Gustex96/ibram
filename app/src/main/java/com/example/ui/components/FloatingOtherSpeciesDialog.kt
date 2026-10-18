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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

/**
 * Caixa de Diálogo Flutuante com Campo de Digitação e Sugestões Rápidas.
 * Exibida quando o auditor escolhe a espécie "Outros", permitindo digitar o nome
 * da espécie (ex: Bovino, Caprino, Suíno) e confirmar para levá-lo ao perfil criado.
 */
@Composable
fun FloatingOtherSpeciesDialog(
    initialText: String = "",
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var speciesInput by remember { mutableStateOf(initialText) }
    val focusManager = LocalFocusManager.current

    val suggestions = listOf(
        "Bovino (Gado)",
        "Caprino (Bode/Cabra)",
        "Ovino (Carneiro/Ovelha)",
        "Suíno (Porco)",
        "Ave Silvestre",
        "Coelho",
        "Muares / Asininos (Burro/Jumento)"
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = Color(0xFF0A2216),
            border = BorderStroke(1.5.dp, Color(0xFF69F0AE)),
            shadowElevation = 24.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .testTag("floating_other_species_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
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
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Pets,
                                    contentDescription = null,
                                    tint = Color(0xFF69F0AE),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Cadastrar Nova Espécie",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Campo flutuante de especificação",
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

                Text(
                    text = "Digite o nome da espécie a ser incluída no levantamento pericial do auditor:",
                    fontSize = 12.sp,
                    color = Color(0xFFE8F5E9),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Campo de Digitação Flutuante
                OutlinedTextField(
                    value = speciesInput,
                    onValueChange = { speciesInput = it },
                    placeholder = {
                        Text(
                            text = "Ex: Caprino, Bovino, Suíno, Ave...",
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

                // Sugestões Rápidas de Espécies Frequentes
                Text(
                    text = "Ou selecione uma sugestão rápida:",
                    fontSize = 11.sp,
                    color = Color(0xFF81C784),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(6.dp))

                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    suggestions.chunked(2).forEach { rowItems ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            rowItems.forEach { suggestion ->
                                Surface(
                                    color = if (speciesInput.equals(suggestion, ignoreCase = true))
                                        Color(0xFF2E7D32)
                                    else
                                        Color(0x33FFFFFF),
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(
                                        1.dp,
                                        if (speciesInput.equals(suggestion, ignoreCase = true))
                                            Color(0xFF69F0AE)
                                        else
                                            Color(0x33FFFFFF)
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

                // Botões de Ação: Cancelar e Confirmar & Criar Perfil
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
                            text = "Confirmar e Criar Perfil",
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
