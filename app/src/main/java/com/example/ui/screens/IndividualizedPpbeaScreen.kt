package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material.icons.filled.Tune
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.util.WatermarkUtils
import java.io.File
import com.example.data.model.EnquadramentoMausTratos
import com.example.data.model.IndividualAnimalEvaluation
import com.example.data.model.PpbeaCriteriaRepository
import com.example.data.model.PpbeaDiagnosis
import com.example.data.model.TutorEntry
import com.example.ui.components.AnimalSpecies
import com.example.ui.components.PpbeaEvaluationDialog
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

/**
 * Tela de Avaliação Individualizada do Protocolo PPBEA conforme o design do mockup:
 * caixa_avaliaçaoindividualizada.png
 *
 * Apresenta:
 * - Botão "+ Adicionar Espécies" no topo para acrescentar outros animais/espécies.
 * - Campo estilo "MENU LIST" para recuperar nome e CPF dos tutores/responsáveis cadastrados.
 * - Campo "TAG/Proprietário" com o placeholder "Identificação do animal".
 * - Botão "Iniciar PPBEA" com ícone de reprodução (Play) que abre a avaliação individual.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IndividualizedPpbeaScreen(
    initialEvaluations: List<IndividualAnimalEvaluation>,
    defaultSpecies: String,
    availableTutors: List<TutorEntry> = emptyList(),
    availablePhotoTags: List<String> = emptyList(),
    availablePhotoPaths: List<String> = emptyList(),
    onSaveAndClose: (List<IndividualAnimalEvaluation>) -> Unit,
    onCancel: () -> Unit
) {
    // Lista mutável de animais individualizados
    val evaluationsList = remember(initialEvaluations, defaultSpecies) {
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
    var activePhotoTagDialogIndex by remember { mutableStateOf<Int?>(null) }
    var showAddSpeciesDialog by remember { mutableStateOf(false) }

    // Alertas de validação e confirmação
    var showIncompleteFormsAlert by remember { mutableStateOf(false) }
    var showMissingTagAlert by remember { mutableStateOf(false) }
    var showConfirmCloseDialog by remember { mutableStateOf(false) }

    fun handleAttemptClose() {
        val emptyPpbeaIndices = evaluationsList.mapIndexedNotNull { idx, it -> if (!it.isEvaluated) idx + 1 else null }
        val emptyTagIndices = evaluationsList.mapIndexedNotNull { idx, it -> if (it.tagOrCpf.isBlank()) idx + 1 else null }

        if (emptyPpbeaIndices.isNotEmpty()) {
            showIncompleteFormsAlert = true
        } else if (emptyTagIndices.isNotEmpty()) {
            showMissingTagAlert = true
        } else {
            showConfirmCloseDialog = true
        }
    }

    Dialog(
        onDismissRequest = onCancel,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        BackHandler {
            handleAttemptClose()
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
                        onClick = { handleAttemptClose() },
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
                        onClick = { handleAttemptClose() },
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
                    availableTutors = availableTutors,
                    availablePhotoTags = availablePhotoTags,
                    onOpenPhotoTags = {
                        activePhotoTagDialogIndex = index
                    },
                    onUpdateTagOrCpf = { newTag ->
                        evaluationsList[index] = item.copy(tagOrCpf = newTag)
                    },
                    onUpdateTutorQualification = { newTutor ->
                        evaluationsList[index] = item.copy(tutorQualification = newTutor)
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
            species = activeAnimal.species,
            isDog = com.example.data.model.DfConstants.isDogSpecies(activeAnimal.species),
            onDismiss = { activeEvaluatingAnimalIndex = null },
            onSaveDiagnosis = { savedDiagnosis ->
                val answersString = PpbeaCriteriaRepository.mapToAnswersString(savedDiagnosis.answersMap)
                evaluationsList[currentActiveIndex] = activeAnimal.copy(
                    ppbeaAnswersData = answersString,
                    notes = savedDiagnosis.observacoes
                )
                activeEvaluatingAnimalIndex = null
            }
        )
    }

    // Diálogo Pop-up para seleção de Fotos e múltiplas Tags com switches (mesmo sem TAG relaciona ao responsável)
    if (activePhotoTagDialogIndex != null && activePhotoTagDialogIndex in evaluationsList.indices) {
        val animalIdx = activePhotoTagDialogIndex!!
        val targetAnimal = evaluationsList[animalIdx]

        PhotoTagSelectionDialog(
            animalIndex = animalIdx,
            animal = targetAnimal,
            availablePhotoPaths = availablePhotoPaths,
            availablePhotoTags = availablePhotoTags,
            availableTutors = availableTutors,
            onDismiss = { activePhotoTagDialogIndex = null },
            onConfirm = { newTagOrCpf, newTutor, newPhotoPaths ->
                evaluationsList[animalIdx] = targetAnimal.copy(
                    tagOrCpf = newTagOrCpf,
                    tutorQualification = newTutor,
                    photoPaths = newPhotoPaths
                )
                activePhotoTagDialogIndex = null
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

    // ALERTA 1: Formulários da Fiscalização Vazios ou Incompletos
    if (showIncompleteFormsAlert) {
        val emptyPpbeaIndices = evaluationsList.mapIndexedNotNull { idx, it -> if (!it.isEvaluated) idx + 1 else null }
        val emptyTagIndices = evaluationsList.mapIndexedNotNull { idx, it -> if (it.tagOrCpf.isBlank()) idx + 1 else null }

        AlertDialog(
            onDismissRequest = { showIncompleteFormsAlert = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = Color(0xFFD32F2F),
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = "Formulários de Avaliação Pendentes",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Os seguintes formulários da avaliação ainda estão vazios ou pendentes de preenchimento:",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Surface(
                        color = Color(0xFFFFEBEE),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "• Checklist PPBEA pendente no(s) exemplar(es): #${emptyPpbeaIndices.joinToString(", #")}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFC62828)
                            )
                            if (emptyTagIndices.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "• Fiscal não colocou TAG no(s) exemplar(es): #${emptyTagIndices.joinToString(", #")}",
                                    fontSize = 12.sp,
                                    color = Color(0xFFE65100),
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                    Text(
                        text = "Por favor, confirme as informações antes de prosseguir. Deseja concluir a avaliação assim mesmo ou revisar os formulários?",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showIncompleteFormsAlert = false
                        onSaveAndClose(evaluationsList.toList())
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF65558F))
                ) {
                    Text("Prosseguir Mesmo Assim")
                }
            },
            dismissButton = {
                TextButton(onClick = { showIncompleteFormsAlert = false }) {
                    Text("Revisar Formulários")
                }
            }
        )
    }

    // ALERTA 2: Fiscal não colocou a TAG no exemplar
    if (showMissingTagAlert) {
        val emptyTagIndices = evaluationsList.mapIndexedNotNull { idx, it -> if (it.tagOrCpf.isBlank()) idx + 1 else null }

        AlertDialog(
            onDismissRequest = { showMissingTagAlert = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = Color(0xFFE65100),
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = "Alerta: Exemplar(es) sem TAG",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "O fiscal não colocou TAG identificadora no(s) exemplar(es): #${emptyTagIndices.joinToString(", #")}.",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Estes animais e suas fotos ficarão associados diretamente ao responsável cadastrado. Por favor, confirme as informações antes de prosseguir.",
                        fontSize = 12.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showMissingTagAlert = false
                        onSaveAndClose(evaluationsList.toList())
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF65558F))
                ) {
                    Text("Confirmar e Prosseguir")
                }
            },
            dismissButton = {
                TextButton(onClick = { showMissingTagAlert = false }) {
                    Text("Inserir TAG")
                }
            }
        )
    }

    // ALERTA 3: Confirmação geral de dados preenchidos antes de prosseguir
    if (showConfirmCloseDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmCloseDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFF2E7D32),
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = "Confirmar Avaliação Individualizada",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
            },
            text = {
                Text(
                    text = "Deseja confirmar as avaliações individualizadas de ${evaluationsList.size} exemplar(es) periciado(s) e salvar os dados na fiscalização?",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirmCloseDialog = false
                        onSaveAndClose(evaluationsList.toList())
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                ) {
                    Text("Confirmar e Salvar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmCloseDialog = false }) {
                    Text("Voltar")
                }
            }
        )
    }
    }
}

/**
 * Card individual de cada animal conforme a imagem:
 * caixa_avaliaçaoindividualizada.png e o campo MENU LIST de Responsável (image_4d649560.png)
 */
@Composable
private fun IndividualAnimalCard(
    index: Int,
    item: IndividualAnimalEvaluation,
    canDelete: Boolean,
    availableTutors: List<TutorEntry>,
    availablePhotoTags: List<String> = emptyList(),
    onOpenPhotoTags: () -> Unit,
    onUpdateTagOrCpf: (String) -> Unit,
    onUpdateTutorQualification: (String) -> Unit,
    onUpdateSpecies: (String) -> Unit,
    onStartPpbea: () -> Unit,
    onDelete: () -> Unit
) {
    var showTutorMenu by remember { mutableStateOf(false) }
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

            // Campo no estilo "MENU LIST" (conforme imagem modelo):
            // Recupera os nomes e CPFs dos tutores/responsáveis preenchidos no início do levantamento
            Box(modifier = Modifier.fillMaxWidth()) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, Color(0xFFF0E0E6)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showTutorMenu = true }
                        .testTag("menu_responsavel_$index")
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 11.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(14.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Responsável",
                                    tint = Color(0xFF49454F),
                                    modifier = Modifier.size(24.dp)
                                )

                                Column {
                                    Text(
                                        text = "Responsável",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF351F28)
                                    )

                                    Spacer(modifier = Modifier.height(2.dp))

                                    Text(
                                        text = item.tutorQualification.ifBlank { "Qualificação" },
                                        fontSize = 12.5.sp,
                                        fontWeight = if (item.tutorQualification.isNotBlank()) FontWeight.SemiBold else FontWeight.Normal,
                                        color = if (item.tutorQualification.isNotBlank()) Color(0xFF4A378B) else Color(0xFF79747E),
                                        maxLines = 1
                                    )
                                }
                            }

                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Selecionar responsável",
                                tint = Color(0xFF49454F),
                                modifier = Modifier.size(13.dp)
                            )
                        }

                        // Linha divisória inferior conforme o modelo da imagem anexada
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp)
                                .height(1.dp)
                                .background(Color(0xFFE6D6DC))
                        )
                    }
                }

                DropdownMenu(
                    expanded = showTutorMenu,
                    onDismissRequest = { showTutorMenu = false },
                    modifier = Modifier
                        .background(Color(0xFF282828))
                        .testTag("dropdown_tutors_$index")
                ) {
                    val validTutors = availableTutors.filter { it.name.isNotBlank() || it.cpf.isNotBlank() || it.phone.isNotBlank() }
                    if (validTutors.isNotEmpty()) {
                        validTutors.forEach { tutor ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(
                                            text = tutor.name.ifBlank { "Responsável sem nome" },
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            fontSize = 13.5.sp
                                        )
                                        val details = buildList {
                                            if (tutor.cpf.isNotBlank()) add("Doc: ${tutor.cpf}")
                                            if (tutor.phone.isNotBlank()) add("Tel: ${tutor.phone}")
                                        }.joinToString(" • ")
                                        if (details.isNotBlank()) {
                                            Text(
                                                text = details,
                                                fontSize = 11.5.sp,
                                                color = Color(0xFFD0BCFF)
                                            )
                                        }
                                    }
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = Color(0xFFD0BCFF),
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                onClick = {
                                    val qual = if (tutor.cpf.isNotBlank()) "${tutor.name} (${tutor.cpf})" else tutor.name
                                    onUpdateTutorQualification(qual)
                                    if (item.tagOrCpf.isBlank()) {
                                        onUpdateTagOrCpf(tutor.name)
                                    }
                                    showTutorMenu = false
                                }
                            )
                        }

                        if (item.tutorQualification.isNotBlank()) {
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = "Desvincular responsável",
                                        color = Color(0xFFEF9A9A),
                                        fontSize = 12.5.sp
                                    )
                                },
                                onClick = {
                                    onUpdateTutorQualification("")
                                    showTutorMenu = false
                                }
                            )
                        }
                    } else {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = "Nenhum responsável cadastrado no início do levantamento",
                                    color = Color(0xFFCCCCCC),
                                    fontSize = 12.sp
                                )
                            },
                            onClick = { showTutorMenu = false }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // CORPO DO CARD CONFORME O DESIGN DO MOCKUP:
            // Lado Esquerdo: Botão com função de abrir pop-up com thumbnails e switches das tags
            // Lado Direito: Botão "Iniciar PPBEA"
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 1. Botão substituindo o campo "TAG/Proprietário" para abrir pop-up com thumbnails e switches de tags
                Surface(
                    onClick = onOpenPhotoTags,
                    shape = RoundedCornerShape(10.dp),
                    color = if (item.tagOrCpf.isNotBlank()) Color(0xFFE8DEF8) else Color(0xFFF3EDF7),
                    border = BorderStroke(
                        width = 1.2.dp,
                        color = if (item.tagOrCpf.isNotBlank()) Color(0xFF65558F) else Color(0xFFCAC4D0)
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 64.dp)
                        .testTag("btn_select_tags_$index")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.PhotoLibrary,
                                    contentDescription = null,
                                    tint = Color(0xFF65558F),
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = "Fotos & Tags do Exemplar",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF49454F)
                                )
                            }
                            Spacer(modifier = Modifier.height(3.dp))
                            if (item.tagOrCpf.isNotBlank()) {
                                Text(
                                    text = item.tagOrCpf,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF1D1B20),
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            } else {
                                Text(
                                    text = "Vincular fotos / tags...",
                                    fontSize = 12.sp,
                                    color = Color(0xFF79747E),
                                    fontStyle = FontStyle.Italic
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF65558F).copy(alpha = 0.15f),
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Tune,
                                    contentDescription = "Selecionar fotos e tags",
                                    tint = Color(0xFF65558F),
                                    modifier = Modifier.size(17.dp)
                                )
                            }
                        }
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
        "Cavalo" to "🐎 Cavalo (Equino)",
        "Cachorro" to "🐕 Cachorro (Canino)",
        "Suíno" to "🐖 Suíno (Porco)",
        "Bovinos" to "🐂 Bovino (Gado)",
        "Caprinos / Ovinos" to "🐐 Caprino / Ovino",
        "Gato" to "🐈 Gato (Felino)",
        "Galináceos" to "🐓 Galináceos (Aves)",
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

/**
 * Item selecionável de foto georreferenciada e sua respectiva TAG.
 * Cada foto possui identidade própria (itemKey única) para que fotos com a mesma TAG
 * não sejam tratadas como a mesma foto nem desmarcadas em conjunto.
 */
private data class SelectablePhotoTagItem(
    val photoPath: String,
    val photoIndex: Int,
    val tag: String
) {
    val itemKey: String
        get() = if (photoPath.isNotBlank()) photoPath else "foto_idx_$photoIndex"
}

/**
 * Pop-up com miniaturas das fotos georreferenciadas e suas respectivas TAGs,
 * permitindo ao fiscal relacionar a foto ao responsável pelo animal mesmo sem TAG,
 * além de selecionar múltiplas fotos e TAGs por exemplar através de botões switch independentes.
 */
@Composable
private fun PhotoTagSelectionDialog(
    animalIndex: Int,
    animal: IndividualAnimalEvaluation,
    availablePhotoPaths: List<String>,
    availablePhotoTags: List<String>,
    availableTutors: List<TutorEntry> = emptyList(),
    onDismiss: () -> Unit,
    onConfirm: (newTagOrCpf: String, newTutor: String, newPhotoPaths: String) -> Unit
) {
    var selectedTutor by remember { mutableStateOf(animal.tutorQualification) }
    var showTutorMenuInDialog by remember { mutableStateOf(false) }
    var showNoTagAlertInDialog by remember { mutableStateOf(false) }
    var customTagInput by remember { mutableStateOf("") }

    val photoItems = remember(availablePhotoPaths, availablePhotoTags) {
        if (availablePhotoPaths.isNotEmpty()) {
            availablePhotoPaths.mapIndexed { idx, path ->
                val extracted = WatermarkUtils.extractTagFromPhotoPath(path)?.trim() ?: ""
                val fallback = availablePhotoTags.getOrNull(idx)?.trim() ?: ""
                val effectiveTag = extracted.ifBlank { fallback }
                SelectablePhotoTagItem(
                    photoPath = path,
                    photoIndex = idx + 1,
                    tag = effectiveTag
                )
            }
        } else if (availablePhotoTags.isNotEmpty()) {
            availablePhotoTags.mapIndexed { idx, tag ->
                SelectablePhotoTagItem(
                    photoPath = "",
                    photoIndex = idx + 1,
                    tag = tag
                )
            }
        } else {
            emptyList()
        }
    }

    // Inicializa o estado dos switches com base nas fotos já vinculadas ao exemplar (ou tags)
    val selectedTagsMap = remember(animal.photoPaths, animal.tagOrCpf, photoItems) {
        val initialSelected = mutableStateMapOf<String, Boolean>()
        val currentPhotoPaths = animal.photoPathsList
        val currentTags = animal.tagOrCpf.split(Regex("[,;•]"))
            .map { it.trim().lowercase() }
            .filter { it.isNotBlank() }

        photoItems.forEach { item ->
            // 1. Vínculo prioritário e exato por caminho de arquivo ou chave única
            val hasExactPath = currentPhotoPaths.isNotEmpty() && (
                currentPhotoPaths.contains(item.itemKey) ||
                (item.photoPath.isNotBlank() && currentPhotoPaths.contains(item.photoPath))
            )

            // 2. Vínculo por número de foto ou TAG textual se ainda não houver caminhos explícitos
            val tagNorm = item.tag.lowercase()
            val photoKeyNorm = "foto #${item.photoIndex}"
            val hasTagMatch = currentPhotoPaths.isEmpty() && currentTags.any { cur ->
                cur == photoKeyNorm || cur.contains(photoKeyNorm) ||
                (tagNorm.isNotBlank() && (tagNorm == cur || cur.contains(tagNorm) || tagNorm.contains(cur)))
            }

            if (hasExactPath || hasTagMatch) {
                initialSelected[item.itemKey] = true
            }
        }
        initialSelected
    }

    fun executeConfirmation() {
        val checkedItems = photoItems.filter { selectedTagsMap[it.itemKey] == true }
        val activePhotoPaths = checkedItems.map { it.itemKey }

        val activeTags = mutableListOf<String>()
        checkedItems.forEach { item ->
            val baseTag = if (item.tag.isNotBlank()) {
                item.tag
            } else {
                // Foto sem TAG: relaciona com o responsável pelo animal
                val respShort = selectedTutor.split("(").firstOrNull()?.trim() ?: selectedTutor
                if (respShort.isNotBlank()) "Foto #${item.photoIndex} (Resp: $respShort)" else "Foto #${item.photoIndex}"
            }
            // Se houver mais de uma foto selecionada com a MESMA tag, adiciona o número da foto para desvinculá-las no relatório
            val sameTagCount = checkedItems.count { it.tag.isNotBlank() && it.tag.equals(item.tag, ignoreCase = true) }
            val tagEntry = if (sameTagCount > 1 && item.tag.isNotBlank()) {
                "$baseTag [Foto #${item.photoIndex}]"
            } else {
                baseTag
            }
            activeTags.add(tagEntry)
        }
        if (customTagInput.isNotBlank()) {
            activeTags.add(customTagInput.trim())
        }
        val resultTags = activeTags.joinToString(", ")
        val joinedPaths = activePhotoPaths.joinToString(";;")
        onConfirm(resultTags, selectedTutor, joinedPaths)
    }

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
                .fillMaxWidth(0.95f)
                .heightIn(max = 660.dp)
                .clip(RoundedCornerShape(20.dp))
                .testTag("dialog_photo_tag_selection"),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            shadowElevation = 16.dp,
            border = BorderStroke(1.2.dp, Color(0xFF65558F).copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Header do Pop-up
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF65558F).copy(alpha = 0.15f),
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.PhotoLibrary,
                                    contentDescription = null,
                                    tint = Color(0xFF65558F),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Text(
                                text = "Fotos & Identificação do Exemplar",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Exemplar #${animalIndex + 1} • ${animal.species}",
                                fontSize = 11.5.sp,
                                color = Color(0xFF65558F),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(32.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Fechar",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Campo para relacionar as fotos com o Responsável pelo animal (mesmo sem TAG)
                Box(modifier = Modifier.fillMaxWidth()) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFF3EDF7),
                        border = BorderStroke(1.dp, Color(0xFFD0BCFF)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showTutorMenuInDialog = true }
                            .testTag("btn_select_dialog_tutor")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 9.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = Color(0xFF65558F),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Responsável Relacionado ao Animal / Fotos:",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF49454F)
                                    )
                                    Text(
                                        text = selectedTutor.ifBlank { "Nenhum responsável selecionado (toque para escolher)" },
                                        fontSize = 12.5.sp,
                                        fontWeight = if (selectedTutor.isNotBlank()) FontWeight.SemiBold else FontWeight.Normal,
                                        color = if (selectedTutor.isNotBlank()) Color(0xFF4F378B) else Color(0xFF79747E),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                tint = Color(0xFF65558F)
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = showTutorMenuInDialog,
                        onDismissRequest = { showTutorMenuInDialog = false },
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.surface)
                            .testTag("dropdown_dialog_tutors")
                    ) {
                        val validTutors = availableTutors.filter { it.name.isNotBlank() || it.cpf.isNotBlank() || it.phone.isNotBlank() }
                        if (validTutors.isNotEmpty()) {
                            validTutors.forEach { tutor ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(
                                                text = tutor.name.ifBlank { "Responsável sem nome" },
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp
                                            )
                                            val details = buildList {
                                                if (tutor.cpf.isNotBlank()) add("Doc: ${tutor.cpf}")
                                                if (tutor.phone.isNotBlank()) add("Tel: ${tutor.phone}")
                                            }.joinToString(" • ")
                                            if (details.isNotBlank()) {
                                                Text(
                                                    text = details,
                                                    fontSize = 11.sp,
                                                    color = Color(0xFF65558F)
                                                )
                                            }
                                        }
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Person,
                                            contentDescription = null,
                                            tint = Color(0xFF65558F),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    },
                                    onClick = {
                                        val qual = if (tutor.cpf.isNotBlank()) "${tutor.name} (${tutor.cpf})" else tutor.name
                                        selectedTutor = qual
                                        showTutorMenuInDialog = false
                                    }
                                )
                            }
                        } else {
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = "Nenhum responsável cadastrado no levantamento",
                                        fontSize = 12.sp,
                                        color = Color.Gray
                                    )
                                },
                                onClick = { showTutorMenuInDialog = false }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Selecione as fotos tiradas para este exemplar. Mesmo que a foto não tenha TAG, ela será relacionada ao responsável acima indicado:",
                    fontSize = 11.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 15.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Lista com os thumbnails das fotos, respectivas tags e botões switchs
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (photoItems.isEmpty()) {
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(20.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PhotoCamera,
                                        contentDescription = null,
                                        tint = Color(0xFF9E9E9E),
                                        modifier = Modifier.size(40.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Nenhuma foto georreferenciada tirada até o momento.",
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color(0xFF757575),
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Tire fotos na câmera do app ou digite uma identificação/TAG manual abaixo.",
                                        fontSize = 11.sp,
                                        color = Color(0xFF9E9E9E),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    } else {
                        itemsIndexed(photoItems) { idx, photoItem ->
                            val isChecked = selectedTagsMap[photoItem.itemKey] == true

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .border(
                                        width = if (isChecked) 1.6.dp else 0.8.dp,
                                        color = if (isChecked) Color(0xFF65558F) else MaterialTheme.colorScheme.outlineVariant,
                                        shape = RoundedCornerShape(12.dp)
                                    ),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isChecked) Color(0xFFF3EDF7) else MaterialTheme.colorScheme.surface
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        // Miniatura / Thumbnail da Foto
                                        if (photoItem.photoPath.isNotBlank() && File(photoItem.photoPath).exists()) {
                                            AsyncImage(
                                                model = File(photoItem.photoPath),
                                                contentDescription = "Miniatura da foto ${photoItem.photoIndex}",
                                                modifier = Modifier
                                                    .size(64.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(Color(0xFFE0E0E0)),
                                                contentScale = ContentScale.Crop
                                            )
                                        } else {
                                            Surface(
                                                modifier = Modifier
                                                    .size(64.dp)
                                                    .clip(RoundedCornerShape(8.dp)),
                                                color = Color(0xFFE8DEF8)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Icon(
                                                        imageVector = Icons.Default.PhotoCamera,
                                                        contentDescription = null,
                                                        tint = Color(0xFF65558F),
                                                        modifier = Modifier.size(24.dp)
                                                    )
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "Foto #${photoItem.photoIndex}",
                                                fontSize = 12.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )

                                            Spacer(modifier = Modifier.height(3.dp))

                                            if (photoItem.tag.isNotBlank()) {
                                                Surface(
                                                    color = Color(0xFF65558F).copy(alpha = 0.15f),
                                                    shape = RoundedCornerShape(6.dp)
                                                ) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Sell,
                                                            contentDescription = null,
                                                            tint = Color(0xFF65558F),
                                                            modifier = Modifier.size(12.dp)
                                                        )
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text(
                                                            text = photoItem.tag,
                                                            fontSize = 11.5.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = Color(0xFF65558F)
                                                        )
                                                    }
                                                }
                                            } else {
                                                // Fotografia sem TAG informada na captura
                                                Surface(
                                                    color = Color(0xFFFFF3E0),
                                                    shape = RoundedCornerShape(6.dp),
                                                    border = BorderStroke(0.6.dp, Color(0xFFFFB74D))
                                                ) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Warning,
                                                            contentDescription = null,
                                                            tint = Color(0xFFE65100),
                                                            modifier = Modifier.size(11.dp)
                                                        )
                                                        Spacer(modifier = Modifier.width(3.dp))
                                                        Text(
                                                            text = "Sem TAG na foto",
                                                            fontSize = 10.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = Color(0xFFE65100)
                                                        )
                                                    }
                                                }

                                                Spacer(modifier = Modifier.height(2.dp))

                                                Text(
                                                    text = if (isChecked) {
                                                        if (selectedTutor.isNotBlank()) "✓ Relacionada ao resp: ${selectedTutor.split("(").firstOrNull()?.trim() ?: selectedTutor}"
                                                        else "✓ Relacionada a este animal"
                                                    } else {
                                                        "Toque no switch para relacionar ao responsável"
                                                    },
                                                    fontSize = 9.5.sp,
                                                    color = if (isChecked) Color(0xFF1B5E20) else Color(0xFF757575),
                                                    fontWeight = if (isChecked) FontWeight.SemiBold else FontWeight.Normal,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    // Botão Switch para seleção da foto e respectiva tag
                                    Switch(
                                        checked = isChecked,
                                        onCheckedChange = { checked ->
                                            selectedTagsMap[photoItem.itemKey] = checked
                                        },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = Color.White,
                                            checkedTrackColor = Color(0xFF65558F)
                                        ),
                                        modifier = Modifier.testTag("switch_photo_tag_${photoItem.photoIndex}")
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Campo para adicionar identificação / TAG manual adicional se desejado
                OutlinedTextField(
                    value = customTagInput,
                    onValueChange = { customTagInput = it },
                    label = { Text("Ou digite identificação / TAG adicional", fontSize = 11.5.sp) },
                    placeholder = { Text("Ex: Cão 1, Baia 3, Registro...", fontSize = 11.sp) },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_custom_tag_dialog"),
                    textStyle = TextStyle(fontSize = 13.sp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Botões de Ação
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Cancelar", fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
                            val checkedItems = photoItems.filter { selectedTagsMap[it.itemKey] == true }
                            val hasUntaggedChecked = checkedItems.any { it.tag.isBlank() }
                            if (hasUntaggedChecked && customTagInput.isBlank()) {
                                showNoTagAlertInDialog = true
                            } else {
                                executeConfirmation()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF65558F),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("btn_confirm_photo_tags")
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Confirmar Tags & Fotos", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }
    }

    // ALERTA POP-UP: Fiscal não colocou TAG na foto
    if (showNoTagAlertInDialog) {
        val checkedUntaggedCount = photoItems.count { selectedTagsMap[it.itemKey] == true && it.tag.isBlank() }
        AlertDialog(
            onDismissRequest = { showNoTagAlertInDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = Color(0xFFE65100),
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = "Atenção: Fotografia(s) sem TAG",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Você selecionou $checkedUntaggedCount foto(s) sem TAG identificadora.",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Surface(
                        color = Color(0xFFFFF3E0),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "A(s) foto(s) será(ão) relacionada(s) diretamente ao responsável pelo exemplar: ${selectedTutor.ifBlank { "Sem responsável selecionado" }}.",
                                fontSize = 12.sp,
                                color = Color(0xFFBF360C),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    Text(
                        text = "Deseja confirmar a vinculação sem TAG ou prefere digitar uma TAG no campo adicional?",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showNoTagAlertInDialog = false
                        executeConfirmation()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF65558F))
                ) {
                    Text("Confirmar sem TAG")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNoTagAlertInDialog = false }) {
                    Text("Digitar TAG")
                }
            }
        )
    }
}

