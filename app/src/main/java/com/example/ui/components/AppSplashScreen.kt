package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R

/**
 * Tela de Entrada e Início Operacional que exibe a arte vetorial estilizada do Cerrado
 * com o cavalo ao fundo, o ícone do aplicativo e o botão Iniciar sem encerramento automático.
 */
@Composable
fun AppSplashScreen(
    onStart: () -> Unit
) {
    var isVisible by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    // Animação de entrada suave dos elementos visuais
    LaunchedEffect(Unit) {
        isVisible = true
    }

    val alphaAnim by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "splashAlpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .testTag("app_splash_screen")
    ) {
        // 1. Imagem de Fundo Vetorizada em Estilo Ilustrativo do Cerrado com Cavalo
        Image(
            painter = painterResource(id = R.drawable.bg_splash_vector),
            contentDescription = "Paisagem Vetorial do Cerrado com Cavalo em Destaque",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // 2. Sobreposição com Gradiente Sutil para Contraste e Legibilidade
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0x77071B11), // Topo suave para status bar
                            Color(0x33000000), // Meio sutil (revela a arte e o cavalo)
                            Color(0xBB05170F), // Transição elegante
                            Color(0xF2030E09), // Base sólida e com alto contraste
                            Color(0xFA020805)
                        )
                    )
                )
        )

        // 3. Conteúdo Central Rolável com Tratamento de Teclado
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Topo: Distintivo Institucional Oficial
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(tween(500))
            ) {
                Surface(
                    color = Color(0xCC0D2A1B),
                    shape = RoundedCornerShape(20.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x6681C784)),
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = Color(0xFFA5D6A7),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "DISTRITO FEDERAL • FISCALIZAÇÃO AMBIENTAL",
                            color = Color(0xFFE8F5E9),
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Centro: Ícone do App em Destaque junto com o Título
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(tween(600)) + scaleIn(tween(600, easing = FastOutSlowInEasing))
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    // Contêiner Elegante do Ícone do Aplicativo
                    Surface(
                        modifier = Modifier
                            .size(94.dp)
                            .shadow(elevation = 16.dp, shape = CircleShape)
                            .border(3.dp, Color(0xFFC8E6C9), CircleShape),
                        shape = CircleShape,
                        color = Color(0xFF1B4D36)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.app_protection_fg),
                                contentDescription = "Ícone do Aplicativo de Fiscalização",
                                modifier = Modifier
                                    .size(70.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Fit
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Nome do Aplicativo
                    Text(
                        text = "LEVO",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Levantamento Operacional contra maus-tratos aos animais",
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFFC8E6C9),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Surface(
                        color = Color(0x33FFFFFF),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "Apoio a Auditores Fiscais • Bem-Estar Animal",
                            color = Color(0xFFE8F5E9),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Normal,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Botão Iniciar Permanente
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(tween(750))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Button(
                        onClick = onStart,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .testTag("splash_start_button"),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2E7D32),
                            contentColor = Color.White
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Iniciar",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Toque em Iniciar para acessar o painel operacional",
                        fontSize = 12.sp,
                        color = Color(0xDDE8F5E9),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
