package com.example.data.model

object DfConstants {
    val REGIOES_ADMINISTRATIVAS = listOf(
        "Plano Piloto (RA I)",
        "Gama (RA II)",
        "Taguatinga (RA III)",
        "Brazlândia (RA IV)",
        "Sobradinho (RA V)",
        "Planaltina (RA VI)",
        "Paranoá (RA VII)",
        "Núcleo Bandeirante (RA VIII)",
        "Ceilândia (RA IX)",
        "Guará (RA X)",
        "Cruzeiro (RA XI)",
        "Samambaia (RA XII)",
        "Santa Maria (RA XIII)",
        "São Sebastião (RA XIV)",
        "Recanto das Emas (RA XV)",
        "Lago Sul (RA XVI)",
        "Riacho Fundo (RA XVII)",
        "Lago Norte (RA XVIII)",
        "Candangolândia (RA XIX)",
        "Águas Claras (RA XX)",
        "Riacho Fundo II (RA XXI)",
        "Sudoeste/Octogonal (RA XXII)",
        "Varjão (RA XXIII)",
        "Park Way (RA XXIV)",
        "SCIA / Estrutural (RA XXV)",
        "Sobradinho II (RA XXVI)",
        "Jardim Botânico (RA XXVII)",
        "Itapoã (RA XXVIII)",
        "SIA (RA XXIX)",
        "Vicente Pires (RA XXX)",
        "Fercal (RA XXXI)",
        "Sol Nascente / Pôr do Sol (RA XXXII)",
        "Arniqueira (RA XXXIII)",
        "Arapoanga (RA XXXIV)",
        "Água Quente (RA XXXV)"
    )

    data class RaCentroid(
        val name: String,
        val latitude: Double,
        val longitude: Double
    )

    /**
     * Coordenadas aproximadas dos centros urbanos/poligonais de cada uma das 35 Regiões Administrativas do DF.
     * Permite identificar dinamicamente a RA em tempo real conforme o GPS do auditor se movimenta.
     */
    val RA_CENTROIDS = listOf(
        RaCentroid("Plano Piloto (RA I)", -15.7975, -47.8919),
        RaCentroid("Gama (RA II)", -16.0177, -48.0645),
        RaCentroid("Taguatinga (RA III)", -15.8335, -48.0567),
        RaCentroid("Brazlândia (RA IV)", -15.6738, -48.2003),
        RaCentroid("Sobradinho (RA V)", -15.6534, -47.7946),
        RaCentroid("Planaltina (RA VI)", -15.6200, -47.6500),
        RaCentroid("Paranoá (RA VII)", -15.7725, -47.7808),
        RaCentroid("Núcleo Bandeirante (RA VIII)", -15.8697, -47.9678),
        RaCentroid("Ceilândia (RA IX)", -15.8175, -48.1108),
        RaCentroid("Guará (RA X)", -15.8239, -47.9806),
        RaCentroid("Cruzeiro (RA XI)", -15.7933, -47.9378),
        RaCentroid("Samambaia (RA XII)", -15.8744, -48.0878),
        RaCentroid("Santa Maria (RA XIII)", -16.0219, -47.9781),
        RaCentroid("São Sebastião (RA XIV)", -15.9083, -47.7694),
        RaCentroid("Recanto das Emas (RA XV)", -15.9078, -48.0694),
        RaCentroid("Lago Sul (RA XVI)", -15.8561, -47.8683),
        RaCentroid("Riacho Fundo (RA XVII)", -15.8825, -48.0169),
        RaCentroid("Lago Norte (RA XVIII)", -15.7336, -47.8542),
        RaCentroid("Candangolândia (RA XIX)", -15.8569, -47.9506),
        RaCentroid("Águas Claras (RA XX)", -15.8394, -48.0289),
        RaCentroid("Riacho Fundo II (RA XXI)", -15.9039, -48.0336),
        RaCentroid("Sudoeste/Octogonal (RA XXII)", -15.7981, -47.9258),
        RaCentroid("Varjão (RA XXIII)", -15.7139, -47.8778),
        RaCentroid("Park Way (RA XXIV)", -15.9000, -47.9500),
        RaCentroid("SCIA / Estrutural (RA XXV)", -15.7836, -47.9944),
        RaCentroid("Sobradinho II (RA XXVI)", -15.6361, -47.8222),
        RaCentroid("Jardim Botânico (RA XXVII)", -15.8950, -47.8183),
        RaCentroid("Itapoã (RA XXVIII)", -15.7508, -47.7656),
        RaCentroid("SIA (RA XXIX)", -15.8117, -47.9556),
        RaCentroid("Vicente Pires (RA XXX)", -15.8056, -48.0278),
        RaCentroid("Fercal (RA XXXI)", -15.5944, -47.8722),
        RaCentroid("Sol Nascente / Pôr do Sol (RA XXXII)", -15.8306, -48.1472),
        RaCentroid("Arniqueira (RA XXXIII)", -15.8583, -48.0167),
        RaCentroid("Arapoanga (RA XXXIV)", -15.6389, -47.6833),
        RaCentroid("Água Quente (RA XXXV)", -15.9861, -48.1889)
    )

    /**
     * Calcula e retorna dinamicamente a Região Administrativa mais próxima com base nas coordenadas de GPS atuais.
     */
    fun detectClosestRa(latitude: Double, longitude: Double): String {
        // Se estiver fora dos limites plausíveis do DF/Entorno, retorna Plano Piloto por padrão
        if (latitude > -15.0 || latitude < -16.5 || longitude < -49.0 || longitude > -47.0) {
            return "Plano Piloto (RA I)"
        }

        var closestRa = "Plano Piloto (RA I)"
        var minDistanceSq = Double.MAX_VALUE

        for (centroid in RA_CENTROIDS) {
            val dLat = latitude - centroid.latitude
            val dLon = longitude - centroid.longitude
            val distSq = (dLat * dLat) + (dLon * dLon)
            if (distSq < minDistanceSq) {
                minDistanceSq = distSq
                closestRa = centroid.name
            }
        }
        return closestRa
    }

    data class RiskLevelInfo(
        val level: Int,
        val title: String,
        val summary: String,
        val safetyParameters: List<String>,
        val colorHex: Long
    )


    const val DEFAULT_DF_LATITUDE = -15.7975
    const val DEFAULT_DF_LONGITUDE = -47.8919

    data class MistreatmentCategory(
        val domain: String,
        val icon: String,
        val items: List<String>
    )

    val MISTREATMENT_CATEGORIES = listOf(
        MistreatmentCategory(
            domain = "1. Estado Nutricional & Hidratação",
            icon = "🌾",
            items = listOf(
                "Caquexia / Emaciação (Costelas, vértebras e ossos pélvicos proeminentes)",
                "Privação ou ausência de água potável / Recipiente inacessível ou imundo",
                "Ausência de alimento adequado ou pasto completamente degradado",
                "Desidratação severa constatada (olhos encovados / turgor diminuído)"
            )
        ),
        MistreatmentCategory(
            domain = "2. Ambiente & Conforto (Abrigo e Contenção)",
            icon = "🌦️",
            items = listOf(
                "Ausência de abrigo contra sol intenso, tempestades ou intempéries",
                "Contenção abusiva: corda/corrente curta ou atado em asfalto quente",
                "Local insalubre: acúmulo de lamaçal, dejetos e fezes sem higienização",
                "Ambiente perigoso com arame solto, entulho cortante ou lixo"
            )
        ),
        MistreatmentCategory(
            domain = "3. Saúde & Integridade Física",
            icon = "🩺",
            items = listOf(
                "Ferimentos abertos, escoriações extensas ou cortes sem tratamento",
                "Presença de miíase (bicheira) ou infestações massivas de ectoparasitas",
                "Claudicação grave (manqueira) ou fratura/incapacidade de locomoção",
                "Cascos excessivamente compridos, rachados, podres ou desferrados lesivos",
                "Doença aparente sem assistência veterinária ou animal agonizante"
            )
        ),
        MistreatmentCategory(
            domain = "4. Tração, Arreios & Manejo",
            icon = "🪢",
            items = listOf(
                "Arreios lesivos: pisaduras na cernelha, dorso ou cortes por freio/embocadura",
                "Sinais evidentes de sobrecarga de peso em veículo de tração animal (carroça/VTA)",
                "Exaustão física extrema induzida por esforço contínuo sem descanso",
                "Marcas de agressão física, chicotadas, espancamento ou violência deliberada"
            )
        )
    )

    data class AdequateCategory(
        val domain: String,
        val icon: String,
        val items: List<String>
    )

    val ADEQUATE_CATEGORIES = listOf(
        AdequateCategory(
            domain = "1. Estado Nutricional & Hidratação",
            icon = "🌾",
            items = listOf(
                "Escore corporal adequado/ideal (boa musculatura e cobertura adiposa satisfatória)",
                "Água limpa, fresca, potável e acessível à vontade em bebedouro higienizado",
                "Alimentação volumosa de boa qualidade (pasto verde farto, feno ou forragem)",
                "Animal bem hidratado (turgor cutâneo elástico e mucosas róseas)"
            )
        ),
        AdequateCategory(
            domain = "2. Ambiente & Conforto (Abrigo e Contenção)",
            icon = "🌦️",
            items = listOf(
                "Abrigo adequado contra sol, chuva e vento (baia coberta ou sombra natural farta)",
                "Espaço amplo que permite livre locomoção, repouso, deitar e rolar com segurança",
                "Contenção segura e confortável (pasto/piquete sem risco de fuga ou lesões)",
                "Ambiente limpo, seco e ventilado, sem acúmulo de dejetos ou lamaçal"
            )
        ),
        AdequateCategory(
            domain = "3. Saúde & Integridade Física",
            icon = "🩺",
            items = listOf(
                "Animal hígido, alerta, ativo, com pelagem limpa e brilhante",
                "Cascos íntegros, bem aparados ou ferrageamento correto em dia",
                "Locomoção simétrica e firme, sem qualquer claudicação (manqueira)",
                "Ausência total de ferimentos, escoriações ou ectoparasitas visíveis"
            )
        ),
        AdequateCategory(
            domain = "4. Tração, Arreios & Manejo",
            icon = "🪢",
            items = listOf(
                "Arreios acolchoados, limpos e bem ajustados sem atrito ou cortes",
                "Carga compatível com o porte do animal, respeitando limites de tração",
                "Manejo calmo e respeitoso, sem uso de violência, chicote ou esporas",
                "Pausas regulares para descanso e hidratação durante o trabalho"
            )
        )
    )

    // ==========================================
    // PARÂMETROS ESPECÍFICOS PARA CÃES / CACHORROS (RES. CFMV 1.236/2018)
    // ==========================================
    val DOG_MISTREATMENT_CATEGORIES = listOf(
        MistreatmentCategory(
            domain = "1. Estado Nutricional & Hidratação",
            icon = "🥣",
            items = listOf(
                "Caquexia / Emaciação severa (costelas e vértebras salientes, perda de massa muscular)",
                "Privação ou ausência de água potável / Recipiente vazio, virado, inacessível ou sujo",
                "Ausência de alimentação adequada (ração) / Animal faminto, inanição constatada",
                "Desidratação acentuada (turgor cutâneo diminuído, olhos encovados, mucosas ressecadas)"
            )
        ),
        MistreatmentCategory(
            domain = "2. Confinamento, Espaço & Abrigo",
            icon = "🏠",
            items = listOf(
                "Confinamento em espaço minúsculo/incompatível com o porte (sem área de circulação ou fuga)",
                "Contenção abusiva: acorrentamento curto permanente (< 3 metros) ou preso em arame sem mobilidade",
                "Ausência de abrigo coberto contra sol intenso, chuva, frio e vento (sem casinha ou laje)",
                "Ambiente insalubre: acúmulo de fezes antigas, urina, lixo, entulhos cortantes ou lamaçal"
            )
        ),
        MistreatmentCategory(
            domain = "3. Saúde, Pelagem & Integridade Física",
            icon = "🩺",
            items = listOf(
                "Ferimentos abertos, cortes, queimaduras, escoriações ou miíase (bicheiras) sem tratamento",
                "Dermatite severa, sarna generalizada, alopécia extensa ou feridas nas orelhas",
                "Infestação massiva por ectoparasitas (pulgas, carrapatos em grande quantidade no corpo)",
                "Claudicação grave, fratura óssea aparente, incapacidade de apoio ou locomoção",
                "Doença infectocontagiosa visível (cinomose, leishmaniose, parvovirose) sem assistência veterinária"
            )
        ),
        MistreatmentCategory(
            domain = "4. Manejo, Agressão & Abandono",
            icon = "🚫",
            items = listOf(
                "Sinais evidentes de violência física, espancamento, pauladas, chutes ou queimaduras",
                "Enforcador/coleira cravada na pele do pescoço ou arame causando estrangulamento e sangramento",
                "Animal em pânico extremo, defensivo ou prostrado decorrente de abusos e agressões reiteradas",
                "Abandono constatado em via pública, terreno baldio ou imóvel fechado desabitado sem vigilância"
            )
        )
    )

    val DOG_ADEQUATE_CATEGORIES = listOf(
        AdequateCategory(
            domain = "1. Estado Nutricional & Hidratação",
            icon = "🥣",
            items = listOf(
                "Escore de condição corporal ideal (costelas palpáveis com leve cobertura adiposa)",
                "Água limpa, fresca e potável à vontade em vasilhame higienizado",
                "Alimentação regular de boa qualidade (ração comercial ou balanceada adequada à idade e porte)",
                "Animal bem hidratado e nutrido (pelagem brilhante, olhos vivos e mucosas róseas)"
            )
        ),
        AdequateCategory(
            domain = "2. Alojamento, Espaço & Conforto",
            icon = "🏠",
            items = listOf(
                "Casinha ou área coberta protegida contra sol, tempestades, vento e umidade",
                "Espaço amplo em quintal ou recinto que permite livre corrida, locomoção e estiramento",
                "Quintal limpo, varrido, seco e desinfetado, sem fezes acumuladas ou odores fétidos",
                "Contenção segura (muros e portões fechados que evitam evasão para a rua e atropelamentos)"
            )
        ),
        AdequateCategory(
            domain = "3. Saúde, Higiene & Cuidados Clínicos",
            icon = "🩺",
            items = listOf(
                "Animal saudável, ativo, alerta, receptivo e com pelagem bem cuidada e sem nós",
                "Vacinação antirrábica e polivalente (V8/V10) em dia, além de vermifugação periódica",
                "Ausência total de pulgas, carrapatos ou lesões cutâneas visíveis",
                "Unhas aparadas, orelhas limpas e acompanhamento médico-veterinário periódico"
            )
        ),
        AdequateCategory(
            domain = "4. Manejo, Socialização & Bem-Estar Psicológico",
            icon = "🐾",
            items = listOf(
                "Livre de correntes ou amarras curtas fixas (movimentação solta no ambiente)",
                "Passeios externos regulares para socialização com coleira/guia e peitoral seguro",
                "Interação positiva e afetiva com o tutor, sem medo excessivo ou sinais de estresse",
                "Disponibilidade de brinquedos/enriquecimento ambiental para estímulo cognitivo"
            )
        )
    )

    val DOG_RISK_LEVELS = listOf(
        RiskLevelInfo(
            level = 1,
            title = "Muito Baixo",
            summary = "Cão em residência segura, quintal cercado, sem risco de fuga para vias públicas.",
            safetyParameters = listOf(
                "Imóvel com portão fechado e muro íntegro sem frestas",
                "Animal seguro e recolhido em área protegida",
                "Água e alimentação disponíveis no local",
                "Distância segura de vias de trânsito rápido"
            ),
            colorHex = 0xFF2E7D32
        ),
        RiskLevelInfo(
            level = 2,
            title = "Baixo",
            summary = "Área residencial calma ou chácara com circulação local reduzida de veículos.",
            safetyParameters = listOf(
                "Rua residencial sem tráfego intenso (até 30 km/h)",
                "Animal contido ou com monitoramento de tutor",
                "Baixo risco de fuga para vias movimentadas",
                "Tutor presente ou responsável contatável"
            ),
            colorHex = 0xFF689F38
        ),
        RiskLevelInfo(
            level = 3,
            title = "Moderado",
            summary = "Cão perambulando solto próximo a avenidas urbanas de RA (risco de atropelamento).",
            safetyParameters = listOf(
                "Animal solto na calçada ou via coletora urbana (ex: Ceilândia, Samambaia, Taguatinga)",
                "Fluxo contínuo de veículos locais, ônibus e pedestres",
                "Risco moderado de atropelamento ou conflito com outros animais",
                "Portão aberto ou cerca residencial danificada"
            ),
            colorHex = 0xFFF57C00
        ),
        RiskLevelInfo(
            level = 4,
            title = "Alto",
            summary = "Cão sem contenção às margens de vias expressas ou rodovias do DF (EPTG, EPIA, Estrutural).",
            safetyParameters = listOf(
                "Cão perambulando no canteiro ou acostamento de rodovias distritais/federais",
                "Velocidade dos veículos na via superior a 60 km/h",
                "Alto risco de desorientação e invasão da pista de rolamento",
                "Horário de pico ou baixa luminosidade aumentando risco de impacto"
            ),
            colorHex = 0xFFE65100
        ),
        RiskLevelInfo(
            level = 5,
            title = "Crítico / Iminente",
            summary = "Emergência fiscal/resgate: cão ferido na pista ou risco iminente de colisão fatal.",
            safetyParameters = listOf(
                "Animal sobre as faixas de rolamento ou ferido em via expressa",
                "Risco imediato de atropelamento com acidentes em cadeia ou capotamento",
                "Cão agonizante, atropelado ou em sofrimento agudo exigindo socorro urgente",
                "Acionamento emergencial do BPMA/PMDF, socorro veterinário ou órgãos de trânsito"
            ),
            colorHex = 0xFFC62828
        )
    )

    fun isDogSpecies(species: String): Boolean {
        return species.equals("Cachorro", ignoreCase = true) ||
            species.equals("dog", ignoreCase = true) ||
            species.equals("Canino", ignoreCase = true) ||
            species.equals("Cão", ignoreCase = true)
    }

    fun getMistreatmentCategories(species: String): List<MistreatmentCategory> {
        return if (isDogSpecies(species)) DOG_MISTREATMENT_CATEGORIES else MISTREATMENT_CATEGORIES
    }

    fun getAdequateCategories(species: String): List<AdequateCategory> {
        return if (isDogSpecies(species)) DOG_ADEQUATE_CATEGORIES else ADEQUATE_CATEGORIES
    }

    fun getRiskLevels(species: String): List<RiskLevelInfo> {
        return if (isDogSpecies(species)) DOG_RISK_LEVELS else RISK_LEVELS
    }
}
