package com.example.data.model

data class AnexoFGuideCategory(
    val id: String,
    val title: String,
    val subtitle: String,
    val items: List<AnexoFGuideItem>
)

data class AnexoFGuideItem(
    val title: String,
    val description: String,
    val tag: String
)

object AnexoFData {
    const val DOCUMENT_TITLE = "ANEXO F - GUIA PRÁTICO"
    const val DOCUMENT_SUBTITLE = "BEM-ESTAR E CUIDADOS BÁSICOS COM CÃES E GATOS"
    const val SOURCE_CREDIT = "Manual de Verificação Inicial de Maus-Tratos – CRMV/MS"

    val categories = listOf(
        AnexoFGuideCategory(
            id = "normal",
            title = "Animal Saudável",
            subtitle = "O que é normal observar em uma inspeção",
            items = listOf(
                AnexoFGuideItem(
                    title = "Olhos",
                    description = "Os olhos dos animais sempre devem estar brilhantes, límpidos e abertos. Sem presença de secreções, inchaço, vermelhidão e lacrimejamento excessivo.",
                    tag = "Saúde Ocular"
                ),
                AnexoFGuideItem(
                    title = "Ouvidos",
                    description = "Os ouvidos devem estar limpos e íntegros, sem presença de secreção, pus, odores fortes, vermelhidão e ectoparasitas.",
                    tag = "Condição Auditiva"
                ),
                AnexoFGuideItem(
                    title = "Focinho & Respiração",
                    description = "O focinho deve estar limpo, sem secreção, ressecamento, ferida ou sangramento. Não deve apresentar dificuldade respiratória, respirar com a boca aberta, estar ofegante quando estiver em repouso. Observar se há tosse e espirros.",
                    tag = "Sistema Respiratório"
                ),
                AnexoFGuideItem(
                    title = "Pelagem",
                    description = "O pelo deve ser brilhante e limpo e cobrir toda parte do corpo, sem presença de cascas, falhas, crostas, oleosidade e nós (emaranhado).",
                    tag = "Pelagem & Higiene"
                ),
                AnexoFGuideItem(
                    title = "Pele",
                    description = "A pele deve estar limpa, sem presença de edemas, lesões, feridas e sem presença de pulgas, carrapatos e larvas de insetos.",
                    tag = "Dermatologia"
                ),
                AnexoFGuideItem(
                    title = "Locomoção & Coordenação",
                    description = "O animal deve andar e correr coordenado e sem dificuldades, sem claudicar, sem andar cambaleante, fraqueza, tremores, inclinação da cabeça e falta de coordenação.",
                    tag = "Aparelho Locomotor"
                ),
                AnexoFGuideItem(
                    title = "Atividade e Alerta",
                    description = "Os animais devem estar ativos, alertas e brincalhões, considerando as diferenças entre espécie, raça e temperamento.",
                    tag = "Estado de Alerta"
                ),
                AnexoFGuideItem(
                    title = "Gatos Relaxados e Seguros",
                    description = "Apresentam uma marcha relaxada, comportamento de exploração do ambiente, cauda elevada e ereta, comportamento de auto-higiene e pupilas em estado normal.",
                    tag = "Comportamento Felino"
                ),
                AnexoFGuideItem(
                    title = "Cães Relaxados",
                    description = "Mantêm as orelhas levemente postas para trás ou de lado, cabeça elevada e cauda relaxada, abanando de lado a lado ou em movimentos circulares.",
                    tag = "Comportamento Canino"
                )
            )
        ),
        AnexoFGuideCategory(
            id = "caes",
            title = "Cuidados Básicos para Cães",
            subtitle = "Requisitos essenciais de alojamento, manejo e saúde canina",
            items = listOf(
                AnexoFGuideItem(
                    title = "Abrigo Adequado",
                    description = "Os cães precisam de abrigo (casinha, cama, coberta) em local limpo, livre de sol e chuva, com boa ventilação do ambiente.",
                    tag = "Conforto Térmico"
                ),
                AnexoFGuideItem(
                    title = "Acesso a Água Fresca",
                    description = "Deve ter acesso a água fresca durante todo o dia em recipiente limpo. Se necessário, fazer trocas durante o dia.",
                    tag = "Hidratação"
                ),
                AnexoFGuideItem(
                    title = "Alimentação Apropriada",
                    description = "A alimentação deve ser ofertada ao menos uma vez ao dia, com alimento específico e de boa qualidade.",
                    tag = "Nutrição"
                ),
                AnexoFGuideItem(
                    title = "Espaço e Acomodação",
                    description = "Os animais precisam ficar soltos no quintal ou presos em canil com espaço adequado para o tamanho do animal.",
                    tag = "Espaço Físico"
                ),
                AnexoFGuideItem(
                    title = "Ajuste de Coleiras",
                    description = "As coleiras devem ter folga de um dedo entre ela e o pescoço. Ajustar tamanho da coleira conforme o crescimento do animal.",
                    tag = "Contenção Segura"
                ),
                AnexoFGuideItem(
                    title = "Sociabilidade",
                    description = "Cães são animais sociáveis e gostam de estar perto dos donos.",
                    tag = "Bem-Estar Social"
                ),
                AnexoFGuideItem(
                    title = "Passeios Diários",
                    description = "Realizar passeios com os cães ao menos uma vez ao dia. Atenção ao horário do passeio – no início da manhã ou final de tarde para não causar lesões devido a calor excessivo do solo.",
                    tag = "Exercício & Passeio"
                ),
                AnexoFGuideItem(
                    title = "Brincadeiras e Estímulos",
                    description = "Realizar brincadeiras (pega bolinha, graveto, mordedores).",
                    tag = "Enriquecimento"
                ),
                AnexoFGuideItem(
                    title = "Ações de Prevenção Sanitária",
                    description = "Ações de prevenção (vacinação, desverminação, remédios contra carrapato e pulgas).",
                    tag = "Sanidade Preventiva"
                ),
                AnexoFGuideItem(
                    title = "Castração",
                    description = "Procedimento indicado para o bem-estar animal, controle reprodutivo e prevenção de doenças.",
                    tag = "Controle Populacional"
                )
            )
        ),
        AnexoFGuideCategory(
            id = "gatos",
            title = "Cuidados Básicos para Gatos",
            subtitle = "Requisitos essenciais de enriquecimento, manejo e saúde felina",
            items = listOf(
                AnexoFGuideItem(
                    title = "Água Fresca e Hidratação",
                    description = "Ofertar água fresca em bebedouros limpos durante todo o dia. Dependendo do tamanho do ambiente, é necessário mais de um bebedouro distribuídos em pontos específicos.",
                    tag = "Hidratação Felina"
                ),
                AnexoFGuideItem(
                    title = "Alimentação Específica",
                    description = "A alimentação deve ser realizada ao menos uma vez ao dia, com alimentos específicos e de boa qualidade.",
                    tag = "Nutrição"
                ),
                AnexoFGuideItem(
                    title = "Enriquecimento Ambiental",
                    description = "Enriquecer o ambiente. Gatos são caçadores natos, eles precisam de estímulos físicos e mentais. Brincadeiras, joguinhos, arranhadores, nichos em variadas alturas.",
                    tag = "Estímulo Mental"
                ),
                AnexoFGuideItem(
                    title = "Caixas de Areia",
                    description = "As caixas de areia devem ser limpas todos os dias. Colocadas em lugar de fácil acesso para que o gato tenha facilidade de entrar e sair. Devem ser posicionadas em lugar distante de onde é fornecido água e alimentação.",
                    tag = "Manejo Sanitário"
                ),
                AnexoFGuideItem(
                    title = "Telas em Janelas e Sacadas",
                    description = "Gatos em apartamentos – as janelas e sacadas devem conter telas para prevenção de quedas.",
                    tag = "Segurança Doméstica"
                ),
                AnexoFGuideItem(
                    title = "Higiene Natural",
                    description = "Gatos não têm necessidade de tomar banho. Eles realizam comportamento de autolimpeza.",
                    tag = "Autocuidado"
                ),
                AnexoFGuideItem(
                    title = "Ações Preventivas de Doenças",
                    description = "Ações de prevenção de doenças (vacinas, desverminação e medicamentos contra pulga).",
                    tag = "Sanidade Preventiva"
                ),
                AnexoFGuideItem(
                    title = "Não Permitir Acesso à Rua",
                    description = "Não permitir acesso à rua. Animais com acesso à rua podem correr risco de atropelamento, brigas com outros animais e contrair doenças.",
                    tag = "Guarda Responsável"
                ),
                AnexoFGuideItem(
                    title = "Castração",
                    description = "Castração recomendada para evitar fugas, brigas, procriação indesejada e patologias.",
                    tag = "Controle Populacional"
                )
            )
        )
    )
}
