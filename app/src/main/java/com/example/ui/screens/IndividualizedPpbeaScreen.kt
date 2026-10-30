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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Sell
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
import androidx.compose.material3.OutlinedTextField
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
    var showAddSpeciesDialog by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onCancel,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
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
                    availableTutors = availableTutors,
                    availablePhotoTags = availablePhotoTags,
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
    onUpdateTagOrCpf: (String) -> Unit,
    onUpdateTutorQualification: (String) -> Unit,
    onUpdateSpecies: (String) -> Unit,
    onStartPpbea: () -> Unit,
    onDelete: () -> Unit
) {
    var showTutorMenu by remember { mutableStateOf(false) }
    var showTagMenu by remember { mutableStateOf(false) }
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
                    val validTutors = availableTutors.filter { it.name.isNotBlank() || it.cpf.isNotBlank() }
                    if (validTutors.isNotEmpty()) {
                        validTutors.forEach { tutor ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(
                                            text = tutor.name.ifBlank { "Tutor sem nome" },
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            fontSize = 13.5.sp
                                        )
                                        if (tutor.cpf.isNotBlank()) {
                                            Text(
                                                text = "CPF: ${tutor.cpf}",
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
                                    text = "Nenhum tutor cadastrado no início do levantamento",
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
            // Lado Esquerdo: Campo "TAG/Proprietário" | "Identificação do animal"
            // Lado Direito: Botão "Iniciar PPBEA"
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 1. Campo Estilizado em container lilás claro conforme caixa_avaliaçaoindividualizada.png
                // Inclui menu suspenso com função GET das tags das fotos georreferenciadas
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFE8DEF8), // Tom lavanda/lilás claro do campo no mockup
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 64.dp)
                        .testTag("input_tag_owner_$index")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "TAG/Proprietário",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF49454F) // Label cinza escuro conforme mockup
                            )

                            // Menu Suspenso (GET das tags das fotos georreferenciadas)
                            Box {
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .clickable { showTagMenu = true }
                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                        .testTag("btn_dropdown_photo_tags_$index"),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PhotoCamera,
                                        contentDescription = "Tags das fotos georreferenciadas",
                                        tint = Color(0xFF65558F),
                                        modifier = Modifier.size(11.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = if (availablePhotoTags.isNotEmpty()) "Tags (${availablePhotoTags.size})" else "Tags",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF65558F)
                                    )
                                    Icon(
                                        imageVector = Icons.Default.ArrowDropDown,
                                        contentDescription = null,
                                        tint = Color(0xFF65558F),
                                        modifier = Modifier.size(14.dp)
                                    )
                                }

                                DropdownMenu(
                                    expanded = showTagMenu,
                                    onDismissRequest = { showTagMenu = false },
                                    modifier = Modifier
                                        .background(Color(0xFF282828))
                                        .testTag("dropdown_photo_tags_menu_$index")
                                ) {
                                    if (availablePhotoTags.isNotEmpty()) {
                                        Text(
                                            text = "Tags das Fotos Georreferenciadas",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFD0BCFF),
                                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                                        )
                                        availablePhotoTags.forEach { photoTag ->
                                            DropdownMenuItem(
                                                text = {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Icon(
                                                            imageVector = Icons.Default.Sell,
                                                            contentDescription = null,
                                                            tint = Color(0xFFD0BCFF),
                                                            modifier = Modifier.size(15.dp)
                                                        )
                                                        Spacer(modifier = Modifier.width(8.dp))
                                                        Text(
                                                            text = photoTag,
                                                            fontWeight = FontWeight.Bold,
                                                            color = Color.White,
                                                            fontSize = 13.sp
                                                        )
                                                    }
                                                },
                                                onClick = {
                                                    onUpdateTagOrCpf(photoTag)
                                                    showTagMenu = false
                                                }
                                            )
                                        }
                                    } else {
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    text = "Nenhuma TAG digitada nas fotos georreferenciadas",
                                                    color = Color(0xFFB0B0B0),
                                                    fontSize = 12.sp
                                                )
                                            },
                                            onClick = { showTagMenu = false }
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(modifier = Modifier.weight(1f)) {
                                BasicTextField(
                                    value = item.tagOrCpf,
                                    onValueChange = onUpdateTagOrCpf,
                                    singleLine = true,
                                    textStyle = TextStyle(
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF1D1B20)
                                    ),
                                    cursorBrush = SolidColor(Color(0xFF65558F)),
                                    decorationBox = { innerTextField ->
                                        if (item.tagOrCpf.isEmpty()) {
                                            Text(
                                                text = "Identificação do animal",
                                                fontSize = 13.5.sp,
                                                fontWeight = FontWeight.Normal,
                                                color = Color(0xFF79747E)
                                            )
                                        }
                                        innerTextField()
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            if (availablePhotoTags.isNotEmpty()) {
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "Selecionar Tag",
                                    tint = Color(0xFF65558F),
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clickable { showTagMenu = true }
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
