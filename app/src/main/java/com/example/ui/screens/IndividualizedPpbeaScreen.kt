package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.EnquadramentoMausTratos
import com.example.data.model.IndividualAnimalEvaluation
import com.example.data.model.PpbeaCriteriaRepository
import com.example.data.model.PpbeaDiagnosis
import com.example.ui.components.AnimalSpecies
import com.example.ui.components.PpbeaEvaluationDialog

/**
 * Tela de Avaliação Individualizada do Protocolo PPBEA conforme o design do mockup:
 * caixa_avaliaçaoindividualizada.png
 *
 * Apresenta:
 * - Botão "+ Adicionar Espécies" no topo para acrescentar outros animais/espécies.
 * - Campo "TAG/Proprietário" com o placeholder "Identificação do animal".
 * - Botão "Iniciar PPBEA" com ícone de reprodução (Play) que abre a avaliação individual.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IndividualizedPpbeaScreen(
    initialEvaluations: List<IndividualAnimalEvaluation>,
    defaultSpecies: String,
    onSaveAndClose: (List<IndividualAnimalEvaluation>) -> Unit,
    onCancel: () -> Unit
) {
    // Lista mutável de animais individualizados
    val evaluationsList = remember {
        mutableStateListOf<IndividualAnimalEvaluation>().apply {
            if (initialEvaluations.isNotEmpty()) {
                addAll(initialEvaluations)
            } else {
                // Inicia com um animal por padrão com a espécie atual do levantamento
                add(
                    IndividualAnimalEvaluation(
                        species = defaultSpecies.ifBlank { "Cavalo" },
                        tagOrCpf = ""
                    )
                )
            }
        }
    }

    // Controle de qual animal está sendo avaliado no diálogo PPBEA
    var activeEvaluatingAnimalIndex by remember { mutableStateOf<Int?>(null) }
    var showAddSpeciesDialog by remember { mutableStateOf(false) }

    BackHandler {
        onSaveAndClose(evaluationsList.toList())
    }

    Scaffold(
        containerColor = Color(0xFF1E1E1E), // Fundo escuro do mockup caixa_avaliaçaoindividualizada.png
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Avaliação Individualizada",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "${evaluationsList.size} animal(is) registrado(s)",
                            fontSize = 12.sp,
                            color = Color(0xFFD0BCFF)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = { onSaveAndClose(evaluationsList.toList()) },
                        modifier = Modifier.testTag("btn_back_individualized_ppbea")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    // Botão "+ Adicionar Espécies" conforme imagem caixa_avaliaçaoindividualizada.png
                    Surface(
                        onClick = { showAddSpeciesDialog = true },
                        shape = RoundedCornerShape(24.dp),
                        color = Color(0xFF65558F), // Púrpura exato do botão no mockup
                        shadowElevation = 4.dp,
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .testTag("btn_add_species_top")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Adicionar Espécies",
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF141414)
                )
            )
        },
        bottomBar = {
            Surface(
                color = Color(0xFF141414),
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onCancel,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Cancelar",
                            color = Color(0xFFE2E8F0),
                            fontSize = 14.sp
                        )
                    }

                    Button(
                        onClick = { onSaveAndClose(evaluationsList.toList()) },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF65558F),
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .weight(2f)
                            .testTag("btn_confirm_individualized_ppbea")
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Concluir Avaliação (${evaluationsList.size})",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp)
        ) {
            itemsIndexed(evaluationsList, key = { index, item -> item.id }) { index, item ->
                IndividualAnimalCard(
                    index = index,
                    item = item,
                    canDelete = evaluationsList.size > 1,
                    onUpdateTagOrCpf = { newTag ->
                        evaluationsList[index] = item.copy(tagOrCpf = newTag)
                    },
                    onUpdateSpecies = { newSpecies ->
                        evaluationsList[index] = item.copy(species = newSpecies)
                    },
                    onStartPpbea = {
                        activeEvaluatingAnimalIndex = index
                    },
                    onDelete = {
                        if (evaluationsList.size > 1) {
                            evaluationsList.removeAt(index)
                        }
                    }
                )
            }
        }
    }

    // Diálogo do Checklist PPBEA para o animal ativo selecionado
    if (activeEvaluatingAnimalIndex != null && activeEvaluatingAnimalIndex in evaluationsList.indices) {
        val currentActiveIndex = activeEvaluatingAnimalIndex!!
        val activeAnimal = evaluationsList[currentActiveIndex]
        val activeDiagnosis = activeAnimal.ppbeaDiagnosis

        PpbeaEvaluationDialog(
            initialDiagnosis = activeDiagnosis,
            onDismiss = { activeEvaluatingAnimalIndex = null },
            onSaveDiagnosis = { savedDiagnosis ->
                val answersString = PpbeaCriteriaRepository.mapToAnswersString(savedDiagnosis.answersMap)
                evaluationsList[currentActiveIndex] = activeAnimal.copy(
                    ppbeaAnswersData = answersString
                )
                activeEvaluatingAnimalIndex = null
            }
        )
    }

    // Diálogo para escolha da nova espécie a ser adicionada
    if (showAddSpeciesDialog) {
        AddSpeciesSelectionDialog(
            onDismiss = { showAddSpeciesDialog = false },
            onSpeciesSelected = { chosenSpecies ->
                evaluationsList.add(
                    IndividualAnimalEvaluation(
                        species = chosenSpecies,
                        tagOrCpf = ""
                    )
                )
                showAddSpeciesDialog = false
            }
        )
    }
}

/**
 * Card individual de cada animal conforme a imagem:
 * caixa_avaliaçaoindividualizada.png
 */
@Composable
private fun IndividualAnimalCard(
    index: Int,
    item: IndividualAnimalEvaluation,
    canDelete: Boolean,
    onUpdateTagOrCpf: (String) -> Unit,
    onUpdateSpecies: (String) -> Unit,
    onStartPpbea: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF282828)),
        border = BorderStroke(
            1.dp,
            if (item.isEvaluated) {
                if (item.ppbeaDiagnosis?.ocorrenciaMausTratos == EnquadramentoMausTratos.CONFIGURA_MAUS_TRATOS)
                    Color(0xFFEF5350)
                else
                    Color(0xFF81C784)
            } else {
                Color(0xFF3E3E3E)
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("card_individual_animal_$index")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Cabeçalho com número do exemplar, espécie e botão de remover
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFF65558F),
                        modifier = Modifier.size(24.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "${index + 1}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    Text(
                        text = "Exemplar #${index + 1} • ${item.species}",
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                if (canDelete) {
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .size(28.dp)
                            .testTag("btn_delete_animal_$index")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Remover",
                            tint = Color(0xFFEF9A9A),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // CORPO DO CARD CONFORME O DESIGN DO MOCKUP:
            // Lado Esquerdo: Campo "TAG/Proprietário" | "Identificação do animal"
            // Lado Direito: Botão "Iniciar PPBEA"
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 1. Campo Estilizado em container lilás claro conforme caixa_avaliaçaoindividualizada.png
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFE8DEF8), // Tom lavanda/lilás claro do campo no mockup
                    modifier = Modifier
                        .weight(1f)
                        .height(64.dp)
                        .testTag("input_tag_owner_$index")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "TAG/Proprietário",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF49454F) // Label cinza escuro conforme mockup
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        BasicTextField(
                            value = item.tagOrCpf,
                            onValueChange = onUpdateTagOrCpf,
                            singleLine = true,
                            textStyle = TextStyle(
                                fontSize = 14.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF1D1B20) // Texto escuro legível
                            ),
                            cursorBrush = SolidColor(Color(0xFF65558F)),
                            decorationBox = { innerTextField ->
                                if (item.tagOrCpf.isEmpty()) {
                                    Text(
                                        text = "Identificação do animal",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Normal,
                                        color = Color(0xFF79747E) // Placeholder conforme mockup
                                    )
                                }
                                innerTextField()
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // 2. Botão Pílula "Iniciar PPBEA" com ícone de reprodução conforme mockup
                Surface(
                    onClick = onStartPpbea,
                    shape = RoundedCornerShape(24.dp),
                    color = if (item.isEvaluated) Color(0xFF4A378B) else Color(0xFF65558F), // Púrpura sólido conforme mockup
                    shadowElevation = 4.dp,
                    modifier = Modifier.testTag("btn_start_ppbea_$index")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = if (item.isEvaluated) "Editar PPBEA" else "Iniciar PPBEA",
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            // Exibição do Diagnóstico Pericial se este exemplar já foi avaliado
            if (item.isEvaluated && item.ppbeaDiagnosis != null) {
                val diag = item.ppbeaDiagnosis!!
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (diag.ocorrenciaMausTratos == EnquadramentoMausTratos.CONFIGURA_MAUS_TRATOS)
                        Color(0xFF3E1D1D)
                    else
                        Color(0xFF1D3520),
                    border = BorderStroke(
                        1.dp,
                        if (diag.ocorrenciaMausTratos == EnquadramentoMausTratos.CONFIGURA_MAUS_TRATOS)
                            Color(0xFFEF5350)
                        else
                            Color(0xFF81C784)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = if (diag.ocorrenciaMausTratos == EnquadramentoMausTratos.CONFIGURA_MAUS_TRATOS)
                                Icons.Default.Warning
                            else
                                Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = if (diag.ocorrenciaMausTratos == EnquadramentoMausTratos.CONFIGURA_MAUS_TRATOS)
                                Color(0xFFEF5350)
                            else
                                Color(0xFF81C784),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "PPBEA Concluído: Grau ${diag.grauBemEstar.label} • ${diag.ocorrenciaMausTratos.label}",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

/**
 * Diálogo de seleção rápida de espécie para o botão "+ Adicionar Espécies"
 */
@Composable
private fun AddSpeciesSelectionDialog(
    onDismiss: () -> Unit,
    onSpeciesSelected: (String) -> Unit
) {
    val speciesOptions = listOf(
        "Cavalo" to "🐎 Equino",
        "Cachorro" to "🐕 Canino",
        "Gato" to "🐈 Felino",
        "Galináceos" to "🐓 Aves",
        "Bovinos" to "🐂 Bovino",
        "Caprinos / Ovinos" to "🐐 Caprino/Ovino",
        "Outros" to "🐾 Outros"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Adicionar Animal / Espécie",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Escolha a espécie do novo exemplar periciado:",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(speciesOptions) { (key, label) ->
                        Surface(
                            onClick = { onSpeciesSelected(key) },
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
