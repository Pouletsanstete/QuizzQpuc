// ============================================================================
// QUIZZ QPUC - Catégorie « États américains »
// Données : assets/etats.json, carte : assets/carte_usa.json, drapeaux : assets/drapeaux/us-xx.png
// ============================================================================

package com.example.quizzqpuc

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.json.JSONArray

private const val CARTE_USA = "carte_usa.json"

// ----------------------------------------------------------------------------
// DONNÉES
// ----------------------------------------------------------------------------

data class Equipe(val nom: String, val variantes: List<String>)

data class Etat(
    val code: String,
    val nom: String,
    val nomsAcceptes: List<String>,
    val capitale: String,
    val capitalesAcceptees: List<String>,
    val surnoms: List<String>,
    val nfl: List<Equipe>,
    val mlb: List<Equipe>,
    val nba: List<Equipe>,
    val nhl: List<Equipe>
) {
    val iso2 get() = "us-" + code.lowercase()
}

private fun JSONArray?.textes(): List<String> =
    if (this == null) emptyList() else List(length()) { getString(it) }

private fun JSONArray?.equipes(): List<Equipe> =
    if (this == null) emptyList() else List(length()) {
        val o = getJSONObject(it)
        Equipe(o.getString("n"), o.getJSONArray("v").textes())
    }

fun chargerEtats(ctx: Context): List<Etat> {
    val texte = ctx.assets.open("etats.json").bufferedReader().use { it.readText() }
    val j = JSONArray(texte)
    return List(j.length()) { i ->
        val o = j.getJSONObject(i)
        Etat(
            code = o.getString("a"),
            nom = o.getString("n"),
            nomsAcceptes = o.optJSONArray("na").textes(),
            capitale = o.getString("c"),
            capitalesAcceptees = o.optJSONArray("ca").textes(),
            surnoms = o.getJSONArray("s").textes(),
            nfl = o.optJSONArray("nfl").equipes(),
            mlb = o.optJSONArray("mlb").equipes(),
            nba = o.optJSONArray("nba").equipes(),
            nhl = o.optJSONArray("nhl").equipes()
        )
    }.sortedBy { normaliser(it.nom) }
}

object EtatsCache {
    private var liste: List<Etat>? = null
    fun obtenir(ctx: Context): List<Etat> = liste ?: chargerEtats(ctx.applicationContext).also { liste = it }
}

// ----------------------------------------------------------------------------
// MODES
// ----------------------------------------------------------------------------

enum class ModeEtat(val label: String, val libelleChamp: String, val nom: String, val texte: Boolean) {
    NOM("Nom de l'État", "Nom de l'État", "Nom", true),
    CAPITALE("Capitale", "Capitale", "Capitale", true),
    ABREVIATION("Abréviation (ex : TX)", "Abréviation\n(2 lettres, ex : TX)", "Abréviation", true),
    SURNOM("Surnom (ex : Lone Star State)", "Surnom\n(un seul, en anglais)", "Surnom", true),
    NFL("Équipes NFL (football américain)", "Équipes NFL", "Équipes NFL", true),
    MLB("Équipes MLB (baseball)", "Équipes MLB", "Équipes MLB", true),
    NBA("Équipes NBA (basket)", "Équipes NBA", "Équipes NBA", true),
    NHL("Équipes NHL (hockey)", "Équipes NHL", "Équipes NHL", true),
    LOCALISATION("Emplacement sur la carte", "", "Localisation", false),
    DRAPEAU("Drapeau (QCM)", "", "Drapeau", false)
}

fun equipesDe(e: Etat, m: ModeEtat): List<Equipe> = when (m) {
    ModeEtat.NFL -> e.nfl
    ModeEtat.MLB -> e.mlb
    ModeEtat.NBA -> e.nba
    ModeEtat.NHL -> e.nhl
    else -> emptyList()
}

private val SPORTS = listOf(ModeEtat.NFL, ModeEtat.MLB, ModeEtat.NBA, ModeEtat.NHL)

data class QuestionEtat(
    val etat: Etat,
    val type: TypeQ,
    val champs: List<ModeEtat> = emptyList(),
    val choix: List<Etat> = emptyList()
)

/** Les champs d'une fiche : les sports où l'État n'a aucune équipe sont simplement donnés (« Aucune »). */
fun champsPourEtat(e: Etat, modes: List<ModeEtat>): List<ModeEtat> =
    modes.filter { it.texte && !(it in SPORTS && equipesDe(e, it).isEmpty()) }

fun genererQuestionsEtats(tous: List<Etat>, nb: Int, modes: List<ModeEtat>): List<QuestionEtat> {
    val types = buildList {
        if (modes.any { it.texte }) { add(TypeQ.FICHE); add(TypeQ.FICHE) }
        if (ModeEtat.LOCALISATION in modes) add(TypeQ.CARTE)
        if (ModeEtat.DRAPEAU in modes) add(TypeQ.DRAPEAU)
    }
    if (types.isEmpty()) return emptyList()
    var pioche = tous.shuffled().toMutableList()
    fun suivant(): Etat {
        if (pioche.isEmpty()) pioche = tous.shuffled().toMutableList()
        return pioche.removeAt(0)
    }
    return List(nb) {
        val t = types.random()
        var e = suivant()
        if (t == TypeQ.FICHE) {
            // on cherche un État qui a au moins un champ à remplir
            var essais = 0
            while (champsPourEtat(e, modes).isEmpty() && essais < 100) { e = suivant(); essais++ }
        }
        when (t) {
            TypeQ.FICHE -> QuestionEtat(e, t, champsPourEtat(e, modes))
            TypeQ.DRAPEAU -> QuestionEtat(e, t, choix = (tous.filter { it.code != e.code }.shuffled().take(4) + e).shuffled())
            else -> QuestionEtat(e, t)
        }
    }
}

// ----------------------------------------------------------------------------
// CORRECTION
// ----------------------------------------------------------------------------

fun variantesSurnom(s: String): List<String> =
    listOf(s, s.removePrefix("The ").trim(), s.removeSuffix(" State").trim(), s.removePrefix("The ").removeSuffix(" State").trim())
        .filter { it.length > 2 }

fun valeurEtat(e: Etat, m: ModeEtat): String = when (m) {
    ModeEtat.NOM -> e.nom
    ModeEtat.CAPITALE -> e.capitale
    ModeEtat.ABREVIATION -> e.code
    ModeEtat.SURNOM -> e.surnoms.joinToString(" / ")
    ModeEtat.NFL, ModeEtat.MLB, ModeEtat.NBA, ModeEtat.NHL ->
        equipesDe(e, m).let { l -> if (l.isEmpty()) "Aucune" else l.joinToString(", ") { it.nom } }
    else -> ""
}

fun champJusteEtat(e: Etat, m: ModeEtat, reponse: String): Boolean = when (m) {
    ModeEtat.NOM -> (listOf(e.nom) + e.nomsAcceptes).any { correspondA(reponse, it) }
    ModeEtat.CAPITALE -> (listOf(e.capitale) + e.capitalesAcceptees).any { correspondA(reponse, it) }
    ModeEtat.ABREVIATION -> normaliser(reponse) == normaliser(e.code)
    ModeEtat.SURNOM -> e.surnoms.any { s -> variantesSurnom(s).any { correspondA(reponse, it) } }
    ModeEtat.NFL, ModeEtat.MLB, ModeEtat.NBA, ModeEtat.NHL -> {
        val equipes = equipesDe(e, m)
        val (ok, faux) = evaluerListe(equipes.map { it.variantes }, decouperListe(reponse.replace(" et ", ",")))
        faux == 0 && ok >= equipes.size
    }
    else -> false
}

fun libelleChampEtat(e: Etat, m: ModeEtat): String = when (m) {
    ModeEtat.NFL, ModeEtat.MLB, ModeEtat.NBA, ModeEtat.NHL -> {
        val n = equipesDe(e, m).size
        if (n > 1) "${m.libelleChamp}\n($n à citer, séparées par des virgules)" else m.libelleChamp
    }
    else -> m.libelleChamp
}

// ----------------------------------------------------------------------------
// FLUX : paramètres -> questions -> résultats
// ----------------------------------------------------------------------------

@Composable
fun QuizEtats(onRetour: () -> Unit) {
    val ctx = LocalContext.current
    val tous = remember { EtatsCache.obtenir(ctx) }
    var phase by remember { mutableStateOf(0) }
    var nbQuestions by remember { mutableStateOf(20f) }
    var modes by remember { mutableStateOf(ModeEtat.entries.toList()) }
    var questions by remember { mutableStateOf(listOf<QuestionEtat>()) }
    var index by remember { mutableStateOf(0) }
    var reponses by remember { mutableStateOf(listOf<Boolean>()) }
    var sauvee by remember { mutableStateOf(lirePartieEtats(ctx, tous)) }
    var confirmer by remember { mutableStateOf(false) }

    BackHandler(enabled = phase == 1) { confirmer = true }

    fun demarrer() {
        questions = genererQuestionsEtats(tous, nbQuestions.toInt(), modes)
        index = 0
        reponses = emptyList()
        effacerPartieEtats(ctx)
        sauvee = null
        phase = 1
    }

    if (confirmer) {
        DialogQuitter(
            onContinuer = { confirmer = false },
            onPause = {
                sauverPartieEtats(ctx, questions, index, reponses)
                sauvee = lirePartieEtats(ctx, tous)
                confirmer = false
                onRetour()
            },
            onAbandonner = {
                effacerPartieEtats(ctx)
                sauvee = null
                confirmer = false
                onRetour()
            }
        )
    }

    when (phase) {
        0 -> EcranParamsEtats(
            nbQuestions, { nbQuestions = it },
            modes, { modes = it },
            onDemarrer = { demarrer() },
            partie = sauvee,
            onReprendre = { pp ->
                questions = pp.questions
                index = pp.index
                reponses = pp.reponses
                phase = 1
            }
        )
        1 -> EcranQuizEtats(
            question = questions[index],
            total = questions.size,
            index = index,
            onRepondu = { correct ->
                reponses = reponses + correct
                if (index + 1 < questions.size) {
                    index++
                    sauverPartieEtats(ctx, questions, index, reponses)
                } else {
                    effacerPartieEtats(ctx)
                    sauvee = null
                    phase = 2
                }
            },
            onAbandon = { confirmer = true }
        )
        else -> EcranResultatsEtats(questions, reponses, onRef = { demarrer() }, onMenu = onRetour)
    }
}

@Composable
fun EcranParamsEtats(
    nbQuestions: Float,
    onNbQuestions: (Float) -> Unit,
    modes: List<ModeEtat>,
    onModes: (List<ModeEtat>) -> Unit,
    onDemarrer: () -> Unit,
    partie: PartieEtats?,
    onReprendre: (PartieEtats) -> Unit
) {
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        if (partie != null) {
            CarteReprise(
                "Question ${partie.index + 1} / ${partie.questions.size} · ${partie.reponses.count { it }} bonne(s) réponse(s)",
                onReprendre = { onReprendre(partie) }
            )
        }
        Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
            Column(Modifier.padding(20.dp)) {
                Text("Nombre de questions", fontWeight = FontWeight.Bold)
                Text("${nbQuestions.toInt()} questions", fontSize = 26.sp, fontWeight = FontWeight.Bold)
                Slider(
                    value = nbQuestions,
                    onValueChange = { onNbQuestions((it / 5f).toInt() * 5f) },
                    valueRange = 5f..50f,
                    steps = 9
                )
            }
        }
        Spacer(Modifier.height(16.dp))
        Text("Types de questions", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text(
            "Le drapeau et la carte de l'État te sont toujours donnés dans la fiche. " +
                "Les équipes sont celles de la saison en cours (Washington D.C. n'étant pas un État, ses équipes sont omises).",
            fontSize = 13.sp, color = Color.Gray
        )
        ModeEtat.entries.forEach { mode ->
            val actif = mode in modes
            Card(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable {
                        onModes(if (actif) { if (modes.size > 1) modes - mode else modes } else modes + mode)
                    },
                colors = CardDefaults.cardColors(
                    containerColor = if (actif) MaterialTheme.colorScheme.primaryContainer
                    else MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(
                    Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(if (actif) "☑" else "☐", fontSize = 18.sp)
                    Spacer(Modifier.width(12.dp))
                    Text(mode.label)
                }
            }
        }
        Spacer(Modifier.height(24.dp))
        Button(onClick = onDemarrer, Modifier.fillMaxWidth().height(56.dp)) {
            Text("Commencer", fontSize = 18.sp)
        }
        Spacer(Modifier.height(24.dp))
    }
}

// ----------------------------------------------------------------------------
// QUESTION
// ----------------------------------------------------------------------------

@Composable
fun EcranQuizEtats(
    question: QuestionEtat,
    total: Int,
    index: Int,
    onRepondu: (Boolean) -> Unit,
    onAbandon: () -> Unit
) {
    val e = question.etat
    val textes = ModeEtat.entries.filter { it.texte }
    var saisies by remember(question) {
        mutableStateOf<Map<ModeEtat, String>>(question.champs.associateWith { "" })
    }
    var verdicts by remember(question) { mutableStateOf<Map<ModeEtat, Boolean>>(emptyMap()) }
    var choixCarte by remember(question) { mutableStateOf<String?>(null) }
    var choixQcm by remember(question) { mutableStateOf<String?>(null) }
    var valide by remember(question) { mutableStateOf(false) }
    var correct by remember(question) { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Question ${index + 1} / $total", fontWeight = FontWeight.Bold)
            TextButton(onClick = onAbandon) { Text("Abandon") }
        }
        LinearProgressIndicator(progress = { (index + 1f) / total }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(12.dp))

        when (question.type) {
            TypeQ.CARTE -> {
                EnonceEtat("Localise l'État suivant sur la carte : ${e.nom}. (Pince pour zoomer)")
                Spacer(Modifier.height(8.dp))
                key(question) {
                    CarteFrance(
                        choix = choixCarte,
                        reponse = if (valide) e.code else null,
                        onChoix = { choixCarte = it },
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        fichier = CARTE_USA
                    )
                }
            }

            TypeQ.DRAPEAU -> Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                EnonceEtat("Quel est le drapeau de l'État suivant : ${e.nom} ?")
                Spacer(Modifier.height(8.dp))
                question.choix.chunked(2).forEach { ligne ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        ligne.forEach { c ->
                            val neutre = Color(0xFFBDBDBD)
                            val couleur = when {
                                valide && c.code == e.code -> Color(0xFF2E7D32)
                                valide && c.code == choixQcm -> Color(0xFFC62828)
                                !valide && c.code == choixQcm -> MaterialTheme.colorScheme.primary
                                else -> neutre
                            }
                            Box(
                                Modifier
                                    .weight(1f)
                                    .padding(vertical = 6.dp)
                                    .border(if (couleur == neutre) 1.dp else 4.dp, couleur, RoundedCornerShape(10.dp))
                                    .clickable(enabled = !valide) { choixQcm = c.code }
                                    .padding(10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Drapeau(c.iso2, hauteur = 80.dp)
                            }
                        }
                        if (ligne.size == 1) Spacer(Modifier.weight(1f))
                    }
                }
            }

            else -> LazyColumn(Modifier.weight(1f)) {
                item {
                    Card(
                        Modifier
                            .fillMaxWidth()
                            .border(4.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp))
                    ) {
                        Column(Modifier.padding(14.dp)) {
                            Text("Complète la fiche", fontSize = 19.sp, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(6.dp))
                            Drapeau(e.iso2, Modifier.align(Alignment.CenterHorizontally), 90.dp)
                            Spacer(Modifier.height(6.dp))
                            textes.forEach { mode ->
                                if (mode !in question.champs) {
                                    LigneFiche(mode.nom, valeurEtat(e, mode))
                                } else {
                                    ChampRempli(
                                        libelle = libelleChampEtat(e, mode),
                                        valeur = saisies[mode] ?: "",
                                        onSaisie = { nvx -> saisies = saisies.toMutableMap().apply { put(mode, nvx) } },
                                        verrouille = valide,
                                        verdict = verdicts[mode],
                                        multiligne = false
                                    )
                                    if (verdicts[mode] == false) {
                                        Text(
                                            "Attendu : ${valeurEtat(e, mode)}",
                                            fontSize = 13.sp,
                                            color = Color(0xFFC62828),
                                            modifier = Modifier.padding(start = 16.dp)
                                        )
                                    }
                                }
                            }
                            Spacer(Modifier.height(12.dp))
                            Text("Localisation", fontSize = 14.sp, color = Color.Gray)
                            CarteRegionMini(CARTE_USA, e.code, EtatsCache.obtenir(LocalContext.current).associate { it.code to it.nom }, "Capitale")
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(10.dp))

        if (!valide) {
            val pret = when (question.type) {
                TypeQ.CARTE -> choixCarte != null
                TypeQ.DRAPEAU -> choixQcm != null
                else -> question.champs.all { (saisies[it] ?: "").isNotBlank() }
            }
            Button(
                onClick = {
                    when (question.type) {
                        TypeQ.CARTE -> correct = choixCarte == e.code
                        TypeQ.DRAPEAU -> correct = choixQcm == e.code
                        else -> {
                            val v = question.champs.associateWith { m -> champJusteEtat(e, m, saisies[m] ?: "") }
                            verdicts = v
                            correct = v.values.all { it }
                        }
                    }
                    valide = true
                },
                modifier = Modifier.fillMaxWidth().height(54.dp),
                enabled = pret
            ) {
                Text(
                    if (question.type == TypeQ.CARTE && choixCarte == null) "Touche un État sur la carte" else "Valider",
                    fontSize = 18.sp
                )
            }
        } else {
            Text(
                if (correct) "Correct !" else "Faux",
                fontSize = 24.sp, fontWeight = FontWeight.Bold,
                color = if (correct) Color(0xFF2E7D32) else Color(0xFFC62828),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            if (question.type == TypeQ.CARTE && !correct) {
                val choisi = EtatsCache.obtenir(LocalContext.current).firstOrNull { it.code == choixCarte }
                if (choisi != null) {
                    Text(
                        "Tu as choisi : ${choisi.nom}",
                        fontSize = 14.sp, color = Color.Gray,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
            }
            Button(onClick = { onRepondu(correct) }, modifier = Modifier.fillMaxWidth().height(54.dp)) {
                Text(if (index + 1 < total) "Suite" else "Voir les résultats", fontSize = 18.sp)
            }
        }
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun EnonceEtat(texte: String) {
    Card(
        Modifier
            .fillMaxWidth()
            .border(4.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp))
    ) {
        Text(texte, fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(14.dp))
    }
}

// ----------------------------------------------------------------------------
// RÉSULTATS
// ----------------------------------------------------------------------------

@Composable
fun EcranResultatsEtats(
    questions: List<QuestionEtat>,
    reponses: List<Boolean>,
    onRef: () -> Unit,
    onMenu: () -> Unit
) {
    val score = reponses.count { it }
    val pct = if (reponses.isNotEmpty()) score * 100 / reponses.size else 0
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(24.dp))
        Text("Quiz terminé !", fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))
        Text(
            "$score / ${reponses.size}",
            fontSize = 56.sp, fontWeight = FontWeight.Bold,
            color = when {
                pct >= 80 -> Color(0xFF2E7D32)
                pct >= 50 -> Color(0xFFF9A825)
                else -> Color(0xFFC62828)
            }
        )
        Text("$pct % de réussite", fontSize = 18.sp)
        Spacer(Modifier.height(24.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Button(onClick = onRef, Modifier.weight(1f)) { Text("Refaire") }
            Spacer(Modifier.width(12.dp))
            OutlinedButton(onClick = onMenu, Modifier.weight(1f)) { Text("Menu") }
        }
        Spacer(Modifier.height(20.dp))
        questions.forEachIndexed { i, q ->
            val ok = reponses.getOrNull(i) ?: false
            Row(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                Text(if (ok) "✔" else "✘", fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(8.dp))
                Column {
                    val type = when (q.type) {
                        TypeQ.CARTE -> "Localisation"
                        TypeQ.DRAPEAU -> "Drapeau"
                        else -> "Fiche"
                    }
                    Text("$type - ${q.etat.nom}", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    if (!ok && q.type == TypeQ.FICHE) {
                        Text(
                            "Attendu : " + q.champs.joinToString(" | ") { "${it.nom} = ${valeurEtat(q.etat, it)}" },
                            fontSize = 12.sp, color = Color.Gray
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}

// ----------------------------------------------------------------------------
// FICHES (révision)
// ----------------------------------------------------------------------------

@Composable
fun EcranFichesEtats() {
    val ctx = LocalContext.current
    val tous = remember { EtatsCache.obtenir(ctx) }
    val noms = remember { tous.associate { it.code to it.nom } }
    var recherche by remember { mutableStateOf("") }
    var ouvert by remember { mutableStateOf<Etat?>(null) }
    BackHandler(enabled = ouvert != null) { ouvert = null }

    val e = ouvert
    if (e != null) {
        Column(
            Modifier
                .fillMaxSize()
                .balayageFiches(
                    onSuivante = { ouvert = voisine(tous, e, 1) },
                    onPrecedente = { ouvert = voisine(tous, e, -1) }
                )
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Drapeau(e.iso2, Modifier.align(Alignment.CenterHorizontally), 110.dp)
                    Spacer(Modifier.height(8.dp))
                    Text(e.nom, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Text("Abréviation : ${e.code}", fontSize = 15.sp)
                    Text("Capitale : ${e.capitale}", fontSize = 15.sp)
                    Text("Surnom : ${valeurEtat(e, ModeEtat.SURNOM)}", fontSize = 15.sp)
                    val villes = remember(e.code) { FaitsCache.villes(ctx, "faits_etats.json", e.code) }
                    if (villes.isNotEmpty()) Text("Plus grandes villes : ${villes.joinToString(", ")}", fontSize = 15.sp)
                    Spacer(Modifier.height(8.dp))
                    listOf(ModeEtat.NFL, ModeEtat.MLB, ModeEtat.NBA, ModeEtat.NHL).forEach {
                        Text("${it.nom} : ${valeurEtat(e, it)}", fontSize = 15.sp)
                    }
                    Spacer(Modifier.height(12.dp))
                    Text("Localisation", fontSize = 14.sp, color = Color.Gray)
                    CarteRegionMini(CARTE_USA, e.code, noms, "Capitale")
                }
            }
            BlocFaits("faits_etats.json", e.code)
            PiedFiche(onAleatoire = { ouvert = tous.random() })
        }
    } else {
        Column(Modifier.fillMaxSize().padding(16.dp)) {
            Text("Fiches : États américains", fontWeight = FontWeight.Bold, fontSize = 20.sp)
            TextField(
                value = recherche,
                onValueChange = { recherche = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Rechercher (État, capitale, abréviation)") },
                singleLine = true
            )
            Spacer(Modifier.height(8.dp))
            val q = normaliser(recherche)
            val filtres = tous.filter {
                q.isEmpty() || normaliser(it.nom).contains(q) || normaliser(it.capitale).contains(q) ||
                    normaliser(it.code) == q || it.nomsAcceptes.any { n -> normaliser(n).contains(q) }
            }
            LazyColumn(Modifier.weight(1f)) {
                items(filtres) { et ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { ouvert = et }
                    ) {
                        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Drapeau(et.iso2, hauteur = 36.dp)
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(et.nom, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                                Text("${et.code}\nCapitale : ${et.capitale}", fontSize = 13.sp, color = Color.Gray)
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Button(onClick = { ouvert = tous.random() }, Modifier.fillMaxWidth().height(52.dp)) { Text("Fiche aléatoire") }
        }
    }
}
