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

    val RISK_LEVELS = listOf(
        RiskLevelInfo(
            level = 1,
            title = "Muito Baixo",
            summary = "Ambiente rural seguro, animais devidamente confinados e protegidos.",
            safetyParameters = listOf(
                "Propriedade rural com cercamento íntegro e pasto delimitado",
                "Distância segura (> 5 km) de rodovias expressas do DF",
                "Ausência de risco de evasão para vias públicas",
                "Água e alimentação disponíveis no local"
            ),
            colorHex = 0xFF2E7D32 // Forest Green
        ),
        RiskLevelInfo(
            level = 2,
            title = "Baixo",
            summary = "Área semiurbana ou chácara com contenção estável e vigilância.",
            safetyParameters = listOf(
                "Área de chácara ou setor habitacional com porteiras",
                "Vias próximas de velocidade máxima reduzida (até 40 km/h)",
                "Contenção adequada mas com movimentação periódica de veículos",
                "Tutor ou caseiro presente na propriedade"
            ),
            colorHex = 0xFF689F38 // Light Green / Lime
        ),
        RiskLevelInfo(
            level = 3,
            title = "Moderado",
            summary = "Proximidade de vias urbanas coletoras ou cercamento precário.",
            safetyParameters = listOf(
                "Animais pastando próximos a avenidas internas de RA (ex: Ceilândia, Samambaia)",
                "Cerca de arame farpado com avarias ou amarração provisória com corda",
                "Fluxo contínuo de veículos locais e pedestres",
                "Risco de fuga para a via em caso de susto ou tempestade"
            ),
            colorHex = 0xFFF57C00 // Orange
        ),
        RiskLevelInfo(
            level = 4,
            title = "Alto",
            summary = "Animais às margens de rodovias de alta velocidade do DF (EPTG, EPIA, DF-001).",
            safetyParameters = listOf(
                "Pastoreio irregular no canteiro lateral ou acostamento de rodovias expressas",
                "Velocidade regulamentada da via superior a 60 km/h",
                "Risco iminente de invasão da pista de rolamento",
                "Horário noturno ou baixa visibilidade sem sinalização",
                "Ausência de responsável ou tutor no local"
            ),
            colorHex = 0xFFE65100 // Deep Orange
        ),
        RiskLevelInfo(
            level = 5,
            title = "Crítico / Iminente",
            summary = "Emergência de fiscalização ambiental: animais soltos na pista ou risco fatal de colisão.",
            safetyParameters = listOf(
                "Equinos sobre as faixas de tráfego de rodovia distrital ou federal",
                "Risco imediato de colisão com vítimas fatais ou capotamento",
                "Animal em pânico, ferido ou desidratado causando frenagens bruscas",
                "Necessidade imediata de acionamento do Batalhão de Trânsito (BPTran/PMDF) ou DER-DF"
            ),
            colorHex = 0xFFC62828 // Red / Emergency
        )
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
                "Caquexia / Emaciação (Costelas, vértebras e ossos pélvicos proeminentes - ECC 1 a 2)",
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
                "Escore corporal adequado/ideal (ECC 4 a 6 - boa musculatura e cobertura adiposa)",
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
}
