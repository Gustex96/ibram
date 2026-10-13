package com.example.data.model

/**
 * Representa um dos 5 Domínios de Bem-Estar Animal estabelecidos no modelo científico
 * de David Mellor e adotados pela Resolução CFMV nº 1.236/2018 para verificação
 * e fiscalização de maus-tratos a animais.
 */
data class AnimalWelfareDomain(
    val number: Int,
    val title: String,
    val shortName: String,
    val subtitle: String,
    val whatMustBeGuaranteed: String,
    val auditIndicators: List<String>,
    val positiveIndicators: List<String>,
    val violationIndicators: List<String>,
    val tag: String
)

object FiveDomainsData {
    const val FRAMEWORK_TITLE = "Os 5 Domínios do Bem-Estar Animal"
    const val FRAMEWORK_SUBTITLE = "Garantias Fundamentais para a Dignidade e Proteção dos Animais"
    const val FRAMEWORK_SOURCE = "Resolução CFMV nº 1.236/2018 • Modelo David Mellor • Padrão DIFIS-IV"

    val domains = listOf(
        AnimalWelfareDomain(
            number = 1,
            title = "1. Nutrição (Alimentação e Hidratação)",
            shortName = "Nutrição",
            subtitle = "Garantia de água potável irrestrita e dieta balanceada em quantidade e qualidade",
            whatMustBeGuaranteed = "Deve ser garantido a todo animal o acesso contínuo e irrestrito a água limpa, fresca e potável em recipientes ou bebedouros higienizados. Alimentação de qualidade, oferecida regularmente em quantidade suficiente para suprir as exigências nutricionais da espécie, porte e fase de desenvolvimento, prevenindo a fome, desidratação, desnutrição e caquexia.",
            auditIndicators = listOf(
                "Disponibilidade contínua de água límpida, fresca e acessível sem esforço",
                "Alimento específico e de boa qualidade, livre de mofo, azedamento ou fezes",
                "Escore de Condição Corporal (ECC) adequado ao porte e padrão da espécie"
            ),
            positiveIndicators = listOf(
                "Bebedouros limpos, com água renovada e protegida do sol escaldante",
                "Escore corporal ideal (ECC 4 a 6 para cavalos / ECC 4 a 5 para cães)",
                "Alimentação específica ofertada diariamente em vasilhas limpas"
            ),
            violationIndicators = listOf(
                "Recipiente de água seco, vazio, imundo, com água verde ou infestada de larvas",
                "Caquexia e emaciação severa (costelas, vértebras e ossos da bacia proeminentes)",
                "Restos de comida azeda, lixo exposto ou privação deliberada de alimento"
            ),
            tag = "Domínio Físico-Nutricional"
        ),
        AnimalWelfareDomain(
            number = 2,
            title = "2. Ambiente (Conforto Ambiental e Físico)",
            shortName = "Ambiente",
            subtitle = "Garantia de abrigo adequado, espaço físico digno e proteção térmica",
            whatMustBeGuaranteed = "Deve ser garantido aos animais um ambiente seguro, salubre e protegido contra intempéries climáticas (radiação solar direta, chuva torrencial, frio intenso e ventos fortes). Área de repouso seca, limpa, bem ventilada, com piso não abrasivo e espaço suficiente para permitir que o animal se deite, levante e estique o corpo livremente.",
            auditIndicators = listOf(
                "Sombra natural farta ou cobertura artificial com proteção térmica efetiva",
                "Ambiente limpo, livre de acúmulo de esterco, lama profunda, esgoto ou entulhos perfurocortantes",
                "Espaço proporcional ao porte que permita repouso confortável e movimentação livre"
            ),
            positiveIndicators = listOf(
                "Abrigo coberto, arejado e com piso drenado e seco",
                "Espaço amplo que permite ao animal deitar confortavelmente com membros estendidos",
                "Local sem riscos de acidentes, pregos expostos ou fiação perigosa"
            ),
            violationIndicators = listOf(
                "Exposição contínua e desprotegida ao sol quente sem nenhuma sombra",
                "Animal alojado sobre lamaçal fétido, fezes acumuladas de semanas ou esgoto",
                "Pisos em brasa, grades pontiagudas ou espaço onde o animal mal consegue se virar"
            ),
            tag = "Domínio Ambiental"
        ),
        AnimalWelfareDomain(
            number = 3,
            title = "3. Saúde (Sanidade e Integridade Física)",
            shortName = "Saúde",
            subtitle = "Garantia de prevenção sanitária, ausência de dor e assistência veterinária rápida",
            whatMustBeGuaranteed = "Deve ser garantida a preservação da integridade física e o controle preventivo de doenças (vacinação e desverminação periódica). Assistência veterinária imediata em casos de feridas, fraturas, cólicas ou debilidades. Proibição absoluta de arreios cortantes, correntes lesivas na carne viva ou mutilações estéticas proibidas por lei.",
            auditIndicators = listOf(
                "Ausência de feridas abertas, pisaduras na cernelha/dorso ou miíases (bicheiras)",
                "Marcha coordenada e sem claudicação grave ou fraturas não tratadas",
                "Cascos e unhas íntegros, bem aparados ou ferrageamento correto em dia",
                "Olhos límpidos, respiração livre e pelagem sem infestação maciça de parasitas"
            ),
            positiveIndicators = listOf(
                "Animal vacinado, desverminado e com carteira ou histórico sanitário",
                "Cascos íntegros, aprumos corretos e ausência de rachaduras profundas",
                "Pelagem brilhante, olhos límpidos e mucosas normais"
            ),
            violationIndicators = listOf(
                "Feridas purulentas, bicheiras (miíases) e cortes extensos sem qualquer curativo",
                "Uso de freios/embocaduras lacerantes, chicotadas ou arreios sobre feridas vivas",
                "Animal em decúbito (deitado sem conseguir levantar), febril e sem socorro médico"
            ),
            tag = "Domínio Sanitário"
        ),
        AnimalWelfareDomain(
            number = 4,
            title = "4. Comportamento (Interações e Expressão Natural)",
            shortName = "Comportamento",
            subtitle = "Garantia de liberdade para expressar os comportamentos naturais da espécie",
            whatMustBeGuaranteed = "Deve ser garantida a possibilidade de expressar comportamentos naturais essenciais da espécie (caminhar, trotar, forragear, farejar, rolar e interagir positivamente com seus semelhantes e seres humanos). É proibido o isolamento social crônico, o acorrentamento permanente e o confinamento contínuo em cubículos.",
            auditIndicators = listOf(
                "Espaço e tempo para locomoção e exercícios compatíveis com a espécie",
                "Corrente com folga suficiente quando for estritamente necessária contenção temporária",
                "Enriquecimento do ambiente e convívio social sem agressões ou privação sensorial"
            ),
            positiveIndicators = listOf(
                "Animal curioso, ativo, explorando o ambiente e expressando vitalidade",
                "Possibilidade de soltura periódica em piquete, quintal ou passeios diários",
                "Relação de confiança e ausência de medo na presença de pessoas"
            ),
            violationIndicators = listOf(
                "Acorrentamento contínuo em corrente curta (inferior a 2 metros) por meses ou anos",
                "Estereotipias graves causadas por sofrimento (tique do urso, balanço contínuo, automutilação)",
                "Animal enclausurado em escuridão constante sem estímulo algum"
            ),
            tag = "Domínio Comportamental"
        ),
        AnimalWelfareDomain(
            number = 5,
            title = "5. Estado Mental (Bem-Estar Psicológico e Afetivo)",
            shortName = "Estado Mental",
            subtitle = "Garantia de experiências emocionais positivas e ausência de sofrimento crônico",
            whatMustBeGuaranteed = "Representa o estado resultante da integração dos quatro domínios biológicos. Deve ser garantida a mitigação constante de estados mentais negativos severos (como terror, pânico, medo crônico, dor persistente, frustração, fome e desamparo aprendido) e a promoção de estados positivos de conforto, segurança, saciedade, tranquilidade e prazer de viver.",
            auditIndicators = listOf(
                "Postura corporal relaxada (olhar calmo, cauda e orelhas em posição natural)",
                "Ausência de pânico, submissão aterrorizada ou agressividade por medo extremo",
                "Animal receptivo e sem terror histérico perante a aproximação humana"
            ),
            positiveIndicators = listOf(
                "Tranquilidade, segurança e curiosidade amistosa com o ambiente",
                "Postura confiante sem se encolher ou fugir em desespero ao ver o tutor",
                "Animal descontraído, descansando com tranquilidade"
            ),
            violationIndicators = listOf(
                "Animal tremendo convulsivamente de pânico ao ver pessoas ou objetos (sinal de espancamento)",
                "Desamparo aprendido: animal que não tenta mais escapar ou expressar dor por resignação ao abuso",
                "Prostração profunda e sofrimento visível sem alívio da dor"
            ),
            tag = "Domínio Mental / Afetivo"
        )
    )
}
