package com.example.model

import com.example.R

data class AnatomicalPoint(
    val id: String,
    val name: String,
    val region: String,
    val description: String,
    val clinicalImportance: String,
    val originalAtlasPage: Int = 2
)

data class AnatomyRegionCrop(
    val id: String,
    val name: String,
    val filterChipLabel: String,
    val drawableResId: Int,
    val title: String,
    val subtitle: String,
    val description: String,
    val aspectRatio: Float
)

object HorseAnatomyData {
    const val ATLAS_TITLE = "Atlas de Anatomia de Equinos"
    const val ATLAS_SOURCE = "Biblioteca AGPTEA • Vetarq (Página 2 - Anatomia Externa)"
    const val ATLAS_DOCUMENT_URL = "https://www.bibliotecaagptea.org.br/zootecnia/equinocultura/livros/ATLAS%20DE%20ANATOMIA%20DO%20CAVALO.pdf"

    // Filtros de região anatômica organizados de acordo com os cortes do atlas
    val regions = listOf(
        "Todas as Regiões",
        "Cabeça",
        "Casco",
        "Membros Inferiores",
        "Membros Anteriores",
        "Membros Posteriores",
        "Tronco & Dorso",
        "Pescoço"
    )

    // Definição dos cortes anatômicos de alta resolução extraídos diretamente da página 2
    val regionCrops: List<AnatomyRegionCrop> = listOf(
        AnatomyRegionCrop(
            id = "todas",
            name = "Todas as Regiões",
            filterChipLabel = "Todas as Regiões",
            drawableResId = R.drawable.anatomia_cavalo_atlas,
            title = "Atlas Completo do Cavalo (Visão Geral)",
            subtitle = "Página 2 • Anatomia Externa Original (Biblioteca AGPTEA)",
            description = "Vista lateral completa preservando todas as referências anatômicas, linhas de chamada e proporções do atlas original.",
            aspectRatio = 2396f / 1650f
        ),
        AnatomyRegionCrop(
            id = "cabeca",
            name = "Cabeça",
            filterChipLabel = "Cabeça",
            drawableResId = R.drawable.anatomia_corte_cabeca,
            title = "Corte Anatômico: Cabeça",
            subtitle = "Fronte, Chanfro, Narinas, Lábios, Ganacha, Olhos e Orelhas",
            description = "Recorte nítido focado nas estruturas da cabeça, com linhas de chamada preservadas para avaliação de embocaduras, cabeçadas e traumas.",
            aspectRatio = 1196f / 750f
        ),
        AnatomyRegionCrop(
            id = "casco",
            name = "Casco",
            filterChipLabel = "Casco",
            drawableResId = R.drawable.anatomia_corte_casco,
            title = "Corte Anatômico: Casco e Dígitos",
            subtitle = "Banda Coronária, Muralha, Talões, Quartela Distal e Apoio Solar",
            description = "Recorte ampliado com alta visibilidade para verificação de rachaduras, anéis de laminite, desgaste e estado de ferrageamento.",
            aspectRatio = 2000f / 600f
        ),
        AnatomyRegionCrop(
            id = "membros_inf",
            name = "Membros Inferiores",
            filterChipLabel = "Membros Inferiores",
            drawableResId = R.drawable.anatomia_corte_membros_inferiores,
            title = "Corte Anatômico: Membros Inferiores",
            subtitle = "Canelas, Boletos, Tendões Flexores, Quartelas e Cascos (Ant. e Post.)",
            description = "Recorte simultâneo das extremidades distais dos membros anteriores e posteriores para análise de aprumos, tendinites e cascos.",
            aspectRatio = 2100f / 830f
        ),
        AnatomyRegionCrop(
            id = "membros_ant",
            name = "Membros Anteriores",
            filterChipLabel = "Membros Anteriores",
            drawableResId = R.drawable.anatomia_corte_membros_anteriores,
            title = "Corte Anatômico: Membros Anteriores",
            subtitle = "Espádua, Braço, Carpo (Joelho), Canela, Boleto e Quartela",
            description = "Recorte completo do membro torácico preservando as referências articulares e aprumos desde a escápula até a extremidade.",
            aspectRatio = 950f / 1230f
        ),
        AnatomyRegionCrop(
            id = "membros_post",
            name = "Membros Posteriores",
            filterChipLabel = "Membros Posteriores",
            drawableResId = R.drawable.anatomia_corte_membros_posteriores,
            title = "Corte Anatômico: Membros Posteriores",
            subtitle = "Coxa, Soldra (Patela), Perna, Jarrete (Tarso), Canela e Boleto",
            description = "Recorte detalhado do membro pelvino com alavanca propulsora do jarrete, soldra e aprumos posteriores.",
            aspectRatio = 1000f / 1300f
        ),
        AnatomyRegionCrop(
            id = "tronco",
            name = "Tronco & Dorso",
            filterChipLabel = "Tronco & Dorso",
            drawableResId = R.drawable.anatomia_corte_tronco,
            title = "Corte Anatômico: Tronco e Dorso",
            subtitle = "Cernelha, Dorso, Lombo, Garupa, Peito, Costado e Flanco",
            description = "Recorte focado na linha dorsal, tórax e abdômen, essencial para conferência de escore de condição corporal e lesões por arreios.",
            aspectRatio = 1600f / 820f
        ),
        AnatomyRegionCrop(
            id = "pescoco",
            name = "Pescoço",
            filterChipLabel = "Pescoço",
            drawableResId = R.drawable.anatomia_corte_pescoco,
            title = "Corte Anatômico: Pescoço",
            subtitle = "Bordo Superior, Crina, Sulco Jugular e Tábuas do Pescoço",
            description = "Recorte nítido do segmento cervical com o trajeto vascular venoso e transição para a cernelha e espádua.",
            aspectRatio = 900f / 760f
        )
    )

    fun getCropForRegion(region: String): AnatomyRegionCrop {
        return regionCrops.firstOrNull { it.name.equals(region, ignoreCase = true) }
            ?: regionCrops.firstOrNull { it.filterChipLabel.equals(region, ignoreCase = true) }
            ?: regionCrops.first()
    }

    fun getPointsForRegion(region: String): List<AnatomicalPoint> {
        return when (region) {
            "Todas as Regiões" -> points
            "Membros Inferiores" -> points.filter { point ->
                point.id in listOf(
                    "canela_ant", "canela_post", "boleto_ant", "quartela_ant",
                    "coroa_casco", "muralha_casco", "sola_ranilha", "taloes_bulbos"
                ) || point.region == "Casco & Dígito"
            }
            "Casco", "Casco & Dígito" -> points.filter { point ->
                point.region == "Casco & Dígito" ||
                        point.id in listOf("coroa_casco", "muralha_casco", "sola_ranilha", "taloes_bulbos")
            }
            "Cabeça" -> points.filter { it.region == "Cabeça" }
            "Pescoço" -> points.filter { it.region == "Pescoço" }
            "Tronco & Dorso" -> points.filter { it.region == "Tronco & Dorso" }
            "Membros Anteriores" -> points.filter { it.region == "Membros Anteriores" }
            "Membros Posteriores" -> points.filter { it.region == "Membros Posteriores" }
            else -> points.filter { it.region.equals(region, ignoreCase = true) }
        }
    }

    val points: List<AnatomicalPoint> = listOf(
        // CABEÇA
        AnatomicalPoint(
            id = "chanfro",
            name = "Chanfro (Nasal)",
            region = "Cabeça",
            description = "Região dorsal e anterior da cabeça, estendendo-se da fronte até as narinas e focinho.",
            clinicalImportance = "Área de apoio de cabrestos e focinheiras; inspeção de marcas de atrito, fraturas ósseas nasais e escoriações."
        ),
        AnatomicalPoint(
            id = "fronte",
            name = "Fronte (Testa)",
            region = "Cabeça",
            description = "Parte superior e plana da cabeça entre as orelhas e os olhos, recoberta parcialmente pelo topete.",
            clinicalImportance = "Avaliação de conformação do crânio, ferimentos contusos e estado de alerta sensorial do equino."
        ),
        AnatomicalPoint(
            id = "narinas",
            name = "Narinas e Focinho",
            region = "Cabeça",
            description = "Aberturas respiratórias flexíveis e orifício nasal móvel na extremidade anterior da cabeça.",
            clinicalImportance = "Avaliação de secreções nasais (catarro, sangue, espuma), batimento de asas nasais por dispneia ou cansaço térmico."
        ),
        AnatomicalPoint(
            id = "labios",
            name = "Lábios e Comissura Labial",
            region = "Cabeça",
            description = "Lábios superior e inferior altamente táteis, com a comissura bucal lateral.",
            clinicalImportance = "Inspeção de ferimentos provocados por embocaduras (freios e bridões) e hidratação das mucosas orais."
        ),
        AnatomicalPoint(
            id = "ganache",
            name = "Ganacha e Barbelo",
            region = "Cabeça",
            description = "Bordo inferior e arredondado da mandíbula, formando a transição inferior da bochecha e espaço intermandibular.",
            clinicalImportance = "Palpação de linfonodos submandibulares (pesquisa de garrotilho/adenite equina) e pulso arterial facial."
        ),
        AnatomicalPoint(
            id = "olhos",
            name = "Órbita e Olhos",
            region = "Cabeça",
            description = "Globos oculares laterais protegidos por pálpebras superior, inferior e terceira pálpebra.",
            clinicalImportance = "Detecção de conjuntivite, úlcera de córnea, uveíte recorrente equina, secreção purulenta e reflexo fotomotor."
        ),
        AnatomicalPoint(
            id = "orelhas_topete",
            name = "Orelhas e Topete",
            region = "Cabeça",
            description = "Pavilhões auriculares móveis e inserção anterior da crina sobre a nuca e fronte.",
            clinicalImportance = "Avaliação da mobilidade auditiva, presença de ectoparasitas (carrapatos de orelha) e feridas por cabeçadas apertadas."
        ),

        // PESCOÇO
        AnatomicalPoint(
            id = "bordo_superior",
            name = "Bordo Superior e Crina",
            region = "Pescoço",
            description = "Linha dorsal do pescoço onde se insere a crina equina e o ligamento nucal.",
            clinicalImportance = "Avaliação do escore de gordura cervical ('crista'), presença de ectoparasitas (carrapatos) e dermatites pruriginosas."
        ),
        AnatomicalPoint(
            id = "tabuas_pescoco",
            name = "Tábuas do Pescoço",
            region = "Pescoço",
            description = "Superfície lateral musculosa do pescoço, delimitada entre a crina, a espádua e o sulco jugular.",
            clinicalImportance = "Região padrão para teste de turgor cutâneo (prega de pele para desidratação) e aplicação de injeções intramusculares."
        ),
        AnatomicalPoint(
            id = "sulco_jugular",
            name = "Sulco Jugular e Garganta",
            region = "Pescoço",
            description = "Depressão linear ventral onde transita a veia jugular externa bilateral e transição laringotraqueal.",
            clinicalImportance = "Ponto de acesso venoso para medicação e exames de sangue; inspeção de flebites ou compressões traqueais."
        ),

        // TRONCO & DORSO
        AnatomicalPoint(
            id = "cernelha",
            name = "Cernelha (Withers)",
            region = "Tronco & Dorso",
            description = "Região elevada formada pelos processos espinhosos das primeiras vértebras torácicas (T2 a T8).",
            clinicalImportance = "Ponto anatômico oficial para medição da altura do cavalo (hipometria); local frequente de bursites e feridas por selas desajustadas."
        ),
        AnatomicalPoint(
            id = "dorso_lombo",
            name = "Dorso e Lombo",
            region = "Tronco & Dorso",
            description = "Superfície dorsal média entre a cernelha e a garupa, formada pelas vértebras torácicas e lombares.",
            clinicalImportance = "Avaliação de seladura, dor por sobrecarga de carga/trabalho e estado de atrofia da musculatura lombar."
        ),
        AnatomicalPoint(
            id = "garupa",
            name = "Garupa e Anca",
            region = "Tronco & Dorso",
            description = "Região pélvica superior entre os ossos ílio, ísquio e sacro, com forte musculatura glútea.",
            clinicalImportance = "Determinação visual do escore de condição corporal (Henneke 1 a 9); anca pontiaguda indica desnutrição severa."
        ),
        AnatomicalPoint(
            id = "peito",
            name = "Peito e Esterno",
            region = "Tronco & Dorso",
            description = "Região cranial do tórax entre as espáduas, apoiada no osso esterno.",
            clinicalImportance = "Inspeção de ferimentos provocados por peitorais de carroças, atrito de arreios e edema de declive."
        ),
        AnatomicalPoint(
            id = "costado",
            name = "Costado (Costelas) e Tórax",
            region = "Tronco & Dorso",
            description = "Arco costal lateral formado por 18 pares de costelas equinas que protegem coração e pulmões.",
            clinicalImportance = "Exposição visual das costelas para diagnóstico de emaciação ou caquexia; auscultação respiratória e cardíaca."
        ),
        AnatomicalPoint(
            id = "flanco",
            name = "Flanco (Vazio) e Ventre",
            region = "Tronco & Dorso",
            description = "Área triangular lateral entre o último arco costal, o lombo e a coxa, estendendo-se ao abdômen inferior.",
            clinicalImportance = "Avaliação de dor abdominal cólica, ausculta de borborigmos cecais e observação de afundamento por desidratação severa."
        ),

        // MEMBROS ANTERIORES
        AnatomicalPoint(
            id = "espadua",
            name = "Espádua (Paleta) e Braço",
            region = "Membros Anteriores",
            description = "Região da escápula e úmero, articulando com o tronco e o tórax sem clavícula.",
            clinicalImportance = "Avaliação de claudicações altas (manqueira), assimetria muscular e cicatrizes por tração em carroça."
        ),
        AnatomicalPoint(
            id = "joelho_carpo",
            name = "Joelho Carpal (Carpo)",
            region = "Membros Anteriores",
            description = "Complexo articular equivalente ao punho humano, unindo rádio/ulna ao osso canela (metacarpo).",
            clinicalImportance = "Frequente ponto de higromas, efusões articulares, bursites e escoriações por quedas ou pisos abrasivos."
        ),
        AnatomicalPoint(
            id = "canela_ant",
            name = "Canela Anterior e Tendões",
            region = "Membros Anteriores",
            description = "Região do 3º metacarpo e feixe tendíneo posterior (flexores digital superficial e profundo).",
            clinicalImportance = "Palpação de tendinite ('arcos tendíneos'), calor local e dores ao esforço físico e tração."
        ),
        AnatomicalPoint(
            id = "boleto_ant",
            name = "Boleto Anterior",
            region = "Membros Anteriores",
            description = "Articulação metacarpofalangiana com os ossos sesamoides proximais.",
            clinicalImportance = "Área de alta sobrecarga biomecânica; feridas por atrito e efusões sinoviais ('gamas')."
        ),
        AnatomicalPoint(
            id = "quartela_ant",
            name = "Quartela (Falange Proximal)",
            region = "Membros Anteriores",
            description = "Segmento oblíquo entre o boleto e a coroa do casco (1ª e 2ª falanges).",
            clinicalImportance = "Avaliação do ângulo de apoio do membro; quartelas retas ou excessivamente rebaixadas predispõem a lesões."
        ),

        // MEMBROS POSTERIORES
        AnatomicalPoint(
            id = "coxa_soldra",
            name = "Coxa e Soldra (Patela / Joelho Posterior)",
            region = "Membros Posteriores",
            description = "Região do fêmur e articulação fêmoro-tíbio-patelar, motor de propulsão do cavalo.",
            clinicalImportance = "Inspeção de fixação superior de patela (travamento de membro), claudicações posteriores e atrofias."
        ),
        AnatomicalPoint(
            id = "jarrete",
            name = "Jarrete (Tarso)",
            region = "Membros Posteriores",
            description = "Articulação angulosa intermediária correspondente ao calcanhar humano, com a ponta do calcâneo.",
            clinicalImportance = "Articulação crítica de propulsão; propensa a osteoartrite (esparavão), bursites de ponta de jarrete e inchaços."
        ),
        AnatomicalPoint(
            id = "canela_post",
            name = "Canela e Boleto Posterior",
            region = "Membros Posteriores",
            description = "Segmento do metatarso e articulação metatarsofalangiana até a quartela posterior.",
            clinicalImportance = "Inspeção de escoriações por arreios traseiros e integridade do aparelho ligamentar suspensor."
        ),

        // CASCO & DÍGITO
        AnatomicalPoint(
            id = "coroa_casco",
            name = "Coroa do Casco (Banda Coronária)",
            region = "Casco & Dígito",
            description = "Junção dermoepidérmica na transição entre a pele da quartela e a muralha córnea do casco.",
            clinicalImportance = "Região germinativa que produz o estojo córneo; ferimentos na coroa provocam fendas permanentes no casco."
        ),
        AnatomicalPoint(
            id = "muralha_casco",
            name = "Muralha (Parede) do Casco",
            region = "Casco & Dígito",
            description = "Superfície córnea externa visível do casco com pinça, quartos e talões.",
            clinicalImportance = "Detecção de rachaduras (quartos rachados), ressecamento, desgaste irregular por falta de ferrageamento e anéis de laminite."
        ),
        AnatomicalPoint(
            id = "sola_ranilha",
            name = "Sola e Ranilha (Face Solar)",
            region = "Casco & Dígito",
            description = "Superfície plantar de amortecimento em formato de 'V' elástico (ranilha) e concavidade da sola.",
            clinicalImportance = "Exame fundamental de pododermatite necrótica ('broca'), podridão da ranilha, penetração de corpos estranhos (pregos) e dor solar."
        ),
        AnatomicalPoint(
            id = "taloes_bulbos",
            name = "Talões e Bulbos do Casco",
            region = "Casco & Dígito",
            description = "Extremidade posterior de suporte do casco e almofadas digitais bulbosas.",
            clinicalImportance = "Verificação de talões encastoados, contração de talões por ressecamento e lesões por atrito de ferraduras mal ajustadas."
        )
    )
}
