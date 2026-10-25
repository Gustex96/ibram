package com.example.data.model

/**
 * Modelos para o ANEXO D – PROTOCOLO DE PERÍCIA EM BEM-ESTAR ANIMAL (PPBEA)
 * Conforme Resolução CFMV nº 1.236/2018 e Manual de Verificação Inicial de Maus-Tratos (CRMV-MS).
 */

enum class PpbeaDomain(val title: String, val icon: String, val description: String) {
    NUTRICIONAL("Nutricionais", "🥣", "Alimentação, hidratação e escore corporal"),
    CONFORTO("De conforto", "🏠", "Abrigo, espaço, superfícies e higiene ambiental"),
    SAUDE("De Saúde", "🩺", "Ausência de dor, doenças, lesões e medidas preventivas"),
    COMPORTAMENTAL("Comportamentais", "🧠", "Expressão de comportamentos naturais e estado mental")
}

enum class ParecerDomain(val label: String, val colorHex: Long) {
    ADEQUADO("Adequado", 0xFF2E7D32),
    REGULAR("Regular", 0xFFF57C00),
    INADEQUADO("Inadequado", 0xFFC62828)
}

enum class GrauBemEstar(val label: String, val colorHex: Long) {
    MUITO_ALTO("Muito alto", 0xFF1B5E20),
    ALTO("Alto", 0xFF2E7D32),
    REGULAR("Regular", 0xFFF57C00),
    BAIXO("Baixo", 0xFFD32F2F),
    MUITO_BAIXO("Muito baixo", 0xFFB71C1C)
}

enum class EnquadramentoMausTratos(val label: String, val colorHex: Long) {
    CONFIGURA_MAUS_TRATOS("Configura maus-tratos (baixo e muito baixo)", 0xFFB71C1C),
    PRECISA_RECOMENDACOES_RETORNO("Não configura maus-tratos, mas precisa de recomendações e retorno (regular)", 0xFFF57C00),
    NAO_CONFIGURA_MAUS_TRATOS("Não configura maus-tratos e não precisa de retorno (alto e muito alto)", 0xFF2E7D32)
}

/**
 * Critério de avaliação do Protocolo PPBEA com perguntas Sim/Não.
 */
data class PpbeaCriterion(
    val id: String,
    val domain: PpbeaDomain,
    val question: String,
    val goodStateExplanation: String,
    val badStateExplanation: String,
    // Se "SIM" representa a situação de alerta/ruim (ex: animal magro) ou se "NÃO" é ruim (ex: sem água)
    val simIsRuim: Boolean,
    val isSevereInadequacy: Boolean
)

data class PpbeaDiagnosis(
    val nutricionalParecer: ParecerDomain = ParecerDomain.ADEQUADO,
    val confortoParecer: ParecerDomain = ParecerDomain.ADEQUADO,
    val saudeParecer: ParecerDomain = ParecerDomain.ADEQUADO,
    val comportamentalParecer: ParecerDomain = ParecerDomain.ADEQUADO,
    val grauBemEstar: GrauBemEstar = GrauBemEstar.MUITO_ALTO,
    val ocorrenciaMausTratos: EnquadramentoMausTratos = EnquadramentoMausTratos.NAO_CONFIGURA_MAUS_TRATOS,
    val gerouBoletimOcorrencia: Boolean = false,
    val observacoes: String = "",
    val isEvaluated: Boolean = false,
    val answersMap: Map<String, Boolean> = emptyMap()
) {
    val hasNutricionalInadequacy: Boolean
        get() = nutricionalParecer == ParecerDomain.INADEQUADO

    val hasConfortoInadequacy: Boolean
        get() = confortoParecer == ParecerDomain.INADEQUADO

    val hasSaudeInadequacy: Boolean
        get() = saudeParecer == ParecerDomain.INADEQUADO

    val hasComportamentalInadequacy: Boolean
        get() = comportamentalParecer == ParecerDomain.INADEQUADO

    val totalInadequaciesCount: Int
        get() = listOf(hasNutricionalInadequacy, hasConfortoInadequacy, hasSaudeInadequacy, hasComportamentalInadequacy).count { it }

    val totalRegularCount: Int
        get() = listOf(nutricionalParecer, confortoParecer, saudeParecer, comportamentalParecer).count { it == ParecerDomain.REGULAR }
}

/**
 * Repositório de Critérios Oficiais do ANEXO D do Manual CRMV-MS
 */
object PpbeaCriteriaRepository {

    val criteria: List<PpbeaCriterion> = listOf(
        // 1. DOMÍNIO NUTRICIONAL (Página 26)
        PpbeaCriterion(
            id = "nut_magro",
            domain = PpbeaDomain.NUTRICIONAL,
            question = "Animal apresenta escore corporal baixo (Muito magro ou Magro)?",
            goodStateExplanation = "Escore corporal ideal ou adequado (costelas palpáveis sem ossos proeminentes)",
            badStateExplanation = "Caquexia, emaciação severa ou costelas/vértebras salientes",
            simIsRuim = true,
            isSevereInadequacy = true
        ),
        PpbeaCriterion(
            id = "nut_sem_agua",
            domain = PpbeaDomain.NUTRICIONAL,
            question = "Há ausência, privação ou inacessibilidade de água fresca e potável?",
            goodStateExplanation = "Água limpa, fresca e potável disponível à vontade",
            badStateExplanation = "Vasilhame vazio, virado, seco, imundo ou inacessível ao animal",
            simIsRuim = true,
            isSevereInadequacy = true
        ),
        PpbeaCriterion(
            id = "nut_comida_impropria",
            domain = PpbeaDomain.NUTRICIONAL,
            question = "Alimentação inadequada, insuficiente ou imprópria (apenas polenta, restos azedos ou 1x/dia com fome)?",
            goodStateExplanation = "Alimento balanceado (ração) em quantidade e frequência satisfatórias",
            badStateExplanation = "Dieta desbalanceada, restos fermentados, oferta escassa de comida",
            simIsRuim = true,
            isSevereInadequacy = false
        ),
        PpbeaCriterion(
            id = "nut_vasilhas_sujas",
            domain = PpbeaDomain.NUTRICIONAL,
            question = "Comedouros ou bebedouros em condições insalubres (sujos, com limo, lodo ou fezes)?",
            goodStateExplanation = "Comedouros e bebedouros limpos e higienizados",
            badStateExplanation = "Recipientes lodosos, com detritos, mofo ou dejetos",
            simIsRuim = true,
            isSevereInadequacy = false
        ),
        PpbeaCriterion(
            id = "nut_obeso",
            domain = PpbeaDomain.NUTRICIONAL,
            question = "Animal com sobrepeso excessivo ou obesidade mórbida?",
            goodStateExplanation = "Peso corporal compatível com o porte e idade da espécie",
            badStateExplanation = "Depósito massivo de gordura dificultando locomoção e respiração",
            simIsRuim = true,
            isSevereInadequacy = false
        ),

        // 2. DOMÍNIO DE CONFORTO / AMBIENTE (Página 27)
        PpbeaCriterion(
            id = "conf_sem_abrigo",
            domain = PpbeaDomain.CONFORTO,
            question = "Ausência de abrigo fixo que forneça proteção contra sol intenso e chuva?",
            goodStateExplanation = "Abrigo fixo, coberto e protegido (casinha, garagem, baia, laje)",
            badStateExplanation = "Exposição direta a intempéries sem qualquer abrigo coberto",
            simIsRuim = true,
            isSevereInadequacy = true
        ),
        PpbeaCriterion(
            id = "conf_sem_descanso",
            domain = PpbeaDomain.CONFORTO,
            question = "Ausência de local seco, limpo e confortável para o animal descansar?",
            goodStateExplanation = "Presença de estrado, cama, gramado seco ou piso adequado para deitar",
            badStateExplanation = "Animal deita em chão encharcado, barro ou pedregulhos pontiagudos",
            simIsRuim = true,
            isSevereInadequacy = true
        ),
        PpbeaCriterion(
            id = "conf_preso_direto",
            domain = PpbeaDomain.CONFORTO,
            question = "Animal mantido preso continuamente em canil exíguo ou corrente/corda curta sem poder correr?",
            goodStateExplanation = "Liberdade de movimentação no quintal ou espaço compatível com o porte",
            badStateExplanation = "Contenção abusiva contínua em espaço minúsculo (< 3 metros) sem soltura",
            simIsRuim = true,
            isSevereInadequacy = true
        ),
        PpbeaCriterion(
            id = "conf_termico",
            domain = PpbeaDomain.CONFORTO,
            question = "Ambiente com desconforto térmico evidente (asfalto escaldante, telha de zinco baixa sem ventilação)?",
            goodStateExplanation = "Ambiente arejado, sombreado e com temperatura amena",
            badStateExplanation = "Calor sufocante sob sol direto ou umidade extrema",
            simIsRuim = true,
            isSevereInadequacy = false
        ),
        PpbeaCriterion(
            id = "conf_sujeira",
            domain = PpbeaDomain.CONFORTO,
            question = "Ambiente com condições ruins de limpeza a ponto de comprometer a saúde (acúmulo de dejetos e fezes antigas)?",
            goodStateExplanation = "Ambiente limpo, higienizado regularmente e sem fezes acumuladas",
            badStateExplanation = "Lamaçal misturado a fezes, odores fétidos intensos e lixo cortante",
            simIsRuim = true,
            isSevereInadequacy = false
        ),
        PpbeaCriterion(
            id = "conf_sem_terra",
            domain = PpbeaDomain.CONFORTO,
            question = "Animal totalmente sem contato com terra ou grama (apenas piso abrasivo ou grade)?",
            goodStateExplanation = "Acesso periódico a área com grama, terra ou piso confortável",
            badStateExplanation = "Confinamento exclusivo sobre piso áspero, grade de ferro ou cimento abrasivo",
            simIsRuim = true,
            isSevereInadequacy = false
        ),

        // 3. DOMÍNIO DE SAÚDE & INTEGRIDADE (Página 27)
        PpbeaCriterion(
            id = "saude_dor_manco",
            domain = PpbeaDomain.SAUDE,
            question = "Animal demonstrando dor à manipulação, arqueamento de dorso ou claudicação grave (mancando) sem tratamento?",
            goodStateExplanation = "Locomoção simétrica e firme, sem sinais de dor ou manqueira",
            badStateExplanation = "Dor aguda evidente, incapacidade de apoio das patas ou fratura não tratada",
            simIsRuim = true,
            isSevereInadequacy = true
        ),
        PpbeaCriterion(
            id = "saude_purulenta",
            domain = PpbeaDomain.SAUDE,
            question = "Presença de secreções purulentas em olhos, nariz ou genitais sem assistência veterinária comprovada?",
            goodStateExplanation = "Olhos, narinas e mucosas límpidos, integrados e livres de secreção purulenta",
            badStateExplanation = "Secreção catarral espessa, pus, crostas ou inflamação ocular aguda",
            simIsRuim = true,
            isSevereInadequacy = true
        ),
        PpbeaCriterion(
            id = "saude_vomito_diarreia",
            domain = PpbeaDomain.SAUDE,
            question = "Animal prostrado com vômito frequente ou diarreia severa sem tratamento veterinário?",
            goodStateExplanation = "Fezes formadas, ausência de vômitos e funções digestivas normais",
            badStateExplanation = "Diarreia profusa, vômitos contínuos e desidratação clínica",
            simIsRuim = true,
            isSevereInadequacy = true
        ),
        PpbeaCriterion(
            id = "saude_ferido_doente",
            domain = PpbeaDomain.SAUDE,
            question = "Presença de feridas abertas, miíase (bicheiras), sarna extensa ou patologia grave sem tratamento?",
            goodStateExplanation = "Pele hígida, sem ferimentos abertos ou sob tratamento clínico atestado",
            badStateExplanation = "Feridas extensas expostas, larvas de moscas, sarna crostosa sangrante",
            simIsRuim = true,
            isSevereInadequacy = true
        ),
        PpbeaCriterion(
            id = "saude_agressao",
            domain = PpbeaDomain.SAUDE,
            question = "Evidências de agressão física intencional (marcas de espancamento, queimaduras, cortes com gume)?",
            goodStateExplanation = "Ausência de qualquer sinal de trauma infligido por violência",
            badStateExplanation = "Lesões contusas, equimoses, cortes lineares por lâmina ou marcas de chicote",
            simIsRuim = true,
            isSevereInadequacy = true
        ),
        PpbeaCriterion(
            id = "saude_parasitas_pelagem",
            domain = PpbeaDomain.SAUDE,
            question = "Infestação massiva de pulgas/carrapatos ou pelagem em estado degradado (opaca, com falhas e nós)?",
            goodStateExplanation = "Pelagem limpa, bem cuidada e controle profilático de ectoparasitas",
            badStateExplanation = "Carrapatos em grande número, dermatite alérgica e alopecia por prurido",
            simIsRuim = true,
            isSevereInadequacy = false
        ),
        PpbeaCriterion(
            id = "saude_sem_vacina_rua",
            domain = PpbeaDomain.SAUDE,
            question = "Falta de vacinação e desverminação em dia ou animal com acesso à rua sem qualquer supervisão?",
            goodStateExplanation = "Vacinas e vermífugos em dia com contenção residencial segura",
            badStateExplanation = "Sem vacinas contra zoonoses (raiva) ou perambulando livre em vias públicas",
            simIsRuim = true,
            isSevereInadequacy = false
        ),

        // 4. DOMÍNIO COMPORTAMENTAL (Página 28)
        PpbeaCriterion(
            id = "comp_bloqueio_natural",
            domain = PpbeaDomain.COMPORTAMENTAL,
            question = "Animal impedido de expressar comportamentos próprios de sua espécie (roer, cavar, correr, explorar)?",
            goodStateExplanation = "Oportunidade diária para correr, explorar e interagir",
            badStateExplanation = "Imobilização física ou privação sensorial e motora crônica",
            simIsRuim = true,
            isSevereInadequacy = true
        ),
        PpbeaCriterion(
            id = "comp_isolamento_severo",
            domain = PpbeaDomain.COMPORTAMENTAL,
            question = "Animal mantido em isolamento social severo (sozinho o dia todo, sem contato humano ou animal)?",
            goodStateExplanation = "Contato social positivo diário com familiares ou outros animais",
            badStateExplanation = "Confinamento em quarto escuro, fosso ou isolamento total contínuo",
            simIsRuim = true,
            isSevereInadequacy = true
        ),
        PpbeaCriterion(
            id = "comp_medo_tutor",
            domain = PpbeaDomain.COMPORTAMENTAL,
            question = "Animal foge com postura de medo intenso ou evita o cuidador (cauda entre pernas, cabeça baixa)?",
            goodStateExplanation = "Animal dócil, receptivo, cauda relaxada e interação confiante com o tutor",
            badStateExplanation = "Pânico visível, tremores, recuo defensivo ou agressividade por medo do tutor",
            simIsRuim = true,
            isSevereInadequacy = false
        ),
        PpbeaCriterion(
            id = "comp_estereotipia_apatia",
            domain = PpbeaDomain.COMPORTAMENTAL,
            question = "Presença de comportamentos estranhos (andar em círculos, ingerir fezes) ou prostração e apatia profunda?",
            goodStateExplanation = "Animal ativo, responsivo a estímulos e com comportamento equilibrado",
            badStateExplanation = "Movimentos repetitivos estereotipados, automutilação ou apatia comatosa",
            simIsRuim = true,
            isSevereInadequacy = false
        )
    )

    /**
     * Calcula o diagnóstico pericial com base nas regras estritas das páginas 10, 26, 27 e 28 do Manual CRMV-MS.
     */
    fun evaluate(answers: Map<String, Boolean>): PpbeaDiagnosis {
        // 1. Nutricional
        val nutInad = (answers["nut_magro"] == true) || (answers["nut_sem_agua"] == true)
        val nutReg = (answers["nut_comida_impropria"] == true) || 
                     (answers["nut_vasilhas_sujas"] == true) || 
                     (answers["nut_obeso"] == true)
        val nutParecer = when {
            nutInad -> ParecerDomain.INADEQUADO
            nutReg -> ParecerDomain.REGULAR
            else -> ParecerDomain.ADEQUADO
        }

        // 2. Conforto
        val confInad = (answers["conf_sem_abrigo"] == true) || 
                       (answers["conf_sem_descanso"] == true) || 
                       (answers["conf_preso_direto"] == true)
        val confReg = (answers["conf_termico"] == true) || 
                      (answers["conf_sujeira"] == true) || 
                      (answers["conf_sem_terra"] == true)
        val confParecer = when {
            confInad -> ParecerDomain.INADEQUADO
            confReg -> ParecerDomain.REGULAR
            else -> ParecerDomain.ADEQUADO
        }

        // 3. Saúde
        val saudeInad = (answers["saude_dor_manco"] == true) || 
                        (answers["saude_purulenta"] == true) || 
                        (answers["saude_vomito_diarreia"] == true) || 
                        (answers["saude_ferido_doente"] == true) || 
                        (answers["saude_agressao"] == true)
        val saudeReg = (answers["saude_parasitas_pelagem"] == true) || 
                       (answers["saude_sem_vacina_rua"] == true)
        val saudeParecer = when {
            saudeInad -> ParecerDomain.INADEQUADO
            saudeReg -> ParecerDomain.REGULAR
            else -> ParecerDomain.ADEQUADO
        }

        // 4. Comportamental
        val compInad = (answers["comp_bloqueio_natural"] == true) || 
                       (answers["comp_isolamento_severo"] == true)
        val compReg = (answers["comp_medo_tutor"] == true) || 
                      (answers["comp_estereotipia_apatia"] == true)
        val compParecer = when {
            compInad -> ParecerDomain.INADEQUADO
            compReg -> ParecerDomain.REGULAR
            else -> ParecerDomain.ADEQUADO
        }

        // Contagem de Inadequações e Regulares para cálculo do Grau de Bem-Estar
        val pareces = listOf(nutParecer, confParecer, saudeParecer, compParecer)
        val numInadequados = pareces.count { it == ParecerDomain.INADEQUADO }
        val numRegulares = pareces.count { it == ParecerDomain.REGULAR }

        // Regra Oficial (Página 10 do Manual CRMV-MS):
        // Muito Alto: todos adequados
        // Alto: 1 regular, 0 inadequado
        // Regular: 2 ou mais regulares, 0 inadequado
        // Baixo: 1 ou 2 inadequados
        // Muito Baixo: 3 ou 4 inadequados
        val grau = when {
            numInadequados >= 3 -> GrauBemEstar.MUITO_BAIXO
            numInadequados in 1..2 -> GrauBemEstar.BAIXO
            numRegulares >= 2 -> GrauBemEstar.REGULAR
            numRegulares == 1 -> GrauBemEstar.ALTO
            else -> GrauBemEstar.MUITO_ALTO
        }

        // Ocorrência de Maus-Tratos (Página 10 e 28 do Manual CRMV-MS):
        val ocorrencia = when (grau) {
            GrauBemEstar.BAIXO, GrauBemEstar.MUITO_BAIXO -> EnquadramentoMausTratos.CONFIGURA_MAUS_TRATOS
            GrauBemEstar.REGULAR -> EnquadramentoMausTratos.PRECISA_RECOMENDACOES_RETORNO
            GrauBemEstar.ALTO, GrauBemEstar.MUITO_ALTO -> EnquadramentoMausTratos.NAO_CONFIGURA_MAUS_TRATOS
        }

        // Geração do Laudo Pericial de Observações
        val obs = buildString {
            append("Protocolo de Perícia em Bem-Estar Animal (PPBEA - Resolução CFMV nº 1.236/2018). ")
            when (ocorrencia) {
                EnquadramentoMausTratos.CONFIGURA_MAUS_TRATOS -> {
                    append("Constatado Grau de Bem-Estar ")
                    append(grau.label.uppercase())
                    append(". Pareceres setoriais: Nutrição [${nutParecer.label}], Conforto [${confParecer.label}], Saúde [${saudeParecer.label}], Comportamento [${compParecer.label}]. ")
                    append("CONFIGURA CRIME DE MAUS-TRATOS (Art. 32 da Lei Federal 9.605/1998 c/c Lei 14.064/2020). Requer lavratura de Auto de Infração, BO e medidas de apreensão/recolhimento.")
                }
                EnquadramentoMausTratos.PRECISA_RECOMENDACOES_RETORNO -> {
                    append("Constatado Grau de Bem-Estar ")
                    append(grau.label.uppercase())
                    append(". Pareceres setoriais: Nutrição [${nutParecer.label}], Conforto [${confParecer.label}], Saúde [${saudeParecer.label}], Comportamento [${compParecer.label}]. ")
                    append("NÃO CONFIGURA CRIME IMEDIATO DE MAUS-TRATOS, mas exige emissão de Termo de Orientação com recomendações de melhoria e agendamento de retorno obrigatório para reavaliação.")
                }
                EnquadramentoMausTratos.NAO_CONFIGURA_MAUS_TRATOS -> {
                    append("Constatado Grau de Bem-Estar ")
                    append(grau.label.uppercase())
                    append(". Todos os parâmetros satisfatórios ou com desvios mínimos irrelevantes. ")
                    append("NÃO CONFIGURA MAUS-TRATOS. Sugere-se arquivamento da denúncia com orientações preventivas gerais de posse responsável.")
                }
            }
        }

        return PpbeaDiagnosis(
            nutricionalParecer = nutParecer,
            confortoParecer = confParecer,
            saudeParecer = saudeParecer,
            comportamentalParecer = compParecer,
            grauBemEstar = grau,
            ocorrenciaMausTratos = ocorrencia,
            gerouBoletimOcorrencia = (ocorrencia == EnquadramentoMausTratos.CONFIGURA_MAUS_TRATOS),
            observacoes = obs,
            isEvaluated = true,
            answersMap = answers
        )
    }

    /**
     * Serializa o mapa de respostas em string persistível (chave:valor separadas por ;;)
     */
    fun mapToAnswersString(answers: Map<String, Boolean>): String {
        return answers.entries.joinToString(";;") { "${it.key}:${it.value}" }
    }

    /**
     * Reconstrói o mapa de respostas a partir da string persistida
     */
    fun parseAnswersString(serialized: String): Map<String, Boolean> {
        if (serialized.isBlank()) return emptyMap()
        val result = mutableMapOf<String, Boolean>()
        serialized.split(";;").forEach { item ->
            val parts = item.split(":")
            if (parts.size == 2) {
                val boolVal = parts[1].trim().toBooleanStrictOrNull()
                if (boolVal != null) {
                    result[parts[0].trim()] = boolVal
                }
            }
        }
        return result
    }

    /**
     * Reconstrói ou recupera o mapa de respostas do protocolo PPBEA
     */
    fun reconstructAnswers(
        serializedAnswers: String,
        mistreatmentIndicators: List<String>,
        adequateIndicators: List<String>
    ): Map<String, Boolean> {
        val parsed = parseAnswersString(serializedAnswers)
        if (parsed.isNotEmpty()) return parsed

        // Reconstrução inteligente a partir das listas de indicadores de campo se disponíveis
        val reconstructed = mutableMapOf<String, Boolean>()
        criteria.forEach { crit ->
            val tagKeyword = crit.question.replace("?", "").trim()
            val hasMistreatment = mistreatmentIndicators.any { 
                it.contains(tagKeyword, ignoreCase = true) || it.contains(crit.badStateExplanation, ignoreCase = true) 
            }
            val hasAdequate = adequateIndicators.any { 
                it.contains(tagKeyword, ignoreCase = true) || it.contains(crit.goodStateExplanation, ignoreCase = true) 
            }

            if (hasMistreatment) {
                reconstructed[crit.id] = crit.simIsRuim
            } else if (hasAdequate) {
                reconstructed[crit.id] = !crit.simIsRuim
            }
        }
        return reconstructed
    }
}

/**
 * Modelo para Avaliação Individualizada de cada animal/exemplar periciado
 * conforme o design de caixa_avaliaçaoindividualizada.png
 */
data class IndividualAnimalEvaluation(
    val id: String = java.util.UUID.randomUUID().toString(),
    val tagOrCpf: String = "",
    val species: String = "Cavalo",
    val ppbeaAnswersData: String = "",
    val notes: String = ""
) {
    val ppbeaAnswersMap: Map<String, Boolean>
        get() = PpbeaCriteriaRepository.reconstructAnswers(ppbeaAnswersData, emptyList(), emptyList())

    val ppbeaDiagnosis: PpbeaDiagnosis?
        get() {
            val map = ppbeaAnswersMap
            return if (map.isNotEmpty()) PpbeaCriteriaRepository.evaluate(map) else null
        }

    val isEvaluated: Boolean
        get() = ppbeaDiagnosis != null
}

object IndividualAnimalSerializer {
    fun serializeList(list: List<IndividualAnimalEvaluation>): String {
        return list.joinToString(";;;") { item ->
            val cleanId = item.id.replace(":::", "_").replace(";;;", "_")
            val cleanTag = item.tagOrCpf.replace(":::", "_").replace(";;;", "_")
            val cleanSpecies = item.species.replace(":::", "_").replace(";;;", "_")
            val cleanAnswers = item.ppbeaAnswersData.replace(":::", "_").replace(";;;", "_")
            val cleanNotes = item.notes.replace(":::", "_").replace(";;;", "_")
            "$cleanId:::$cleanTag:::$cleanSpecies:::$cleanAnswers:::$cleanNotes"
        }
    }

    fun deserializeList(raw: String): List<IndividualAnimalEvaluation> {
        if (raw.isBlank()) return emptyList()
        return raw.split(";;;").filter { it.isNotBlank() }.mapNotNull { chunk ->
            val parts = chunk.split(":::")
            if (parts.size >= 3) {
                IndividualAnimalEvaluation(
                    id = parts.getOrNull(0) ?: java.util.UUID.randomUUID().toString(),
                    tagOrCpf = parts.getOrNull(1) ?: "",
                    species = parts.getOrNull(2) ?: "Cavalo",
                    ppbeaAnswersData = parts.getOrNull(3) ?: "",
                    notes = parts.getOrNull(4) ?: ""
                )
            } else null
        }
    }
}

