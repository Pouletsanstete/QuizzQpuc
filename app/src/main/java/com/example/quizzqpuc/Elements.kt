// ============================================================================
// QUIZZ QPUC - Catégorie « Tableau périodique »
// Données : assets/elements.json, tableau : assets/carte_tableau.json (une case = un élément)
// ============================================================================

package com.example.quizzqpuc

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.json.JSONArray
import java.util.Locale
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

private const val CARTE_TABLEAU = "carte_tableau.json"

// ----------------------------------------------------------------------------
// DONNÉES
// ----------------------------------------------------------------------------

data class Element(
    val z: Int,
    val nom: String,
    val nomEn: String,
    val isotopes: List<String>,
    val nomsAcceptes: List<String>,
    val symbole: String,
    val masse: Double,
    val periode: Int,
    val groupe: Int,          // 0 = pas de groupe (blocs f : lanthanides et actinides)
    val famille: String,
    val etat: String,         // "S", "L", "G" ou "" (inconnu)
    val config: String
)

val FAMILLES = linkedMapOf(
    "alcalin" to "Métaux alcalins",
    "alcalino" to "Métaux alcalino-terreux",
    "transition" to "Métaux de transition",
    "pauvre" to "Métaux pauvres",
    "metalloide" to "Métalloïdes",
    "nonmetal" to "Autres non-métaux",
    "halogene" to "Halogènes",
    "noble" to "Gaz nobles",
    "lanthanide" to "Lanthanides",
    "actinide" to "Actinides"
)

private val COULEURS_FAMILLES = mapOf(
    "alcalin" to Color(0xFFFFAB91),
    "alcalino" to Color(0xFFFFCC80),
    "transition" to Color(0xFFFFF59D),
    "pauvre" to Color(0xFFC5E1A5),
    "metalloide" to Color(0xFF80CBC4),
    "nonmetal" to Color(0xFF81D4FA),
    "halogene" to Color(0xFFCE93D8),
    "noble" to Color(0xFFB39DDB),
    "lanthanide" to Color(0xFFF8BBD0),
    "actinide" to Color(0xFFEF9A9A)
)

val ETATS_MATIERE = mapOf("S" to "Solide", "L" to "Liquide", "G" to "Gazeux")

private fun JSONArray?.textes(): List<String> =
    if (this == null) emptyList() else List(length()) { getString(it) }

fun chargerElements(ctx: Context): List<Element> {
    val texte = ctx.assets.open("elements.json").bufferedReader().use { it.readText() }
    val j = JSONArray(texte)
    return List(j.length()) { i ->
        val o = j.getJSONObject(i)
        Element(
            z = o.getInt("z"),
            nom = o.getString("n"),
            nomEn = o.optString("en", ""),
            isotopes = o.optJSONArray("i").textes(),
            nomsAcceptes = o.optJSONArray("na").textes(),
            symbole = o.getString("s"),
            masse = o.getDouble("m"),
            periode = o.getInt("p"),
            groupe = o.getInt("g"),
            famille = o.getString("f"),
            etat = o.getString("e"),
            config = o.optString("c", "")
        )
    }.sortedBy { it.z }
}

object ElementsCache {
    private var liste: List<Element>? = null
    fun obtenir(ctx: Context): List<Element> = liste ?: chargerElements(ctx.applicationContext).also { liste = it }
}

fun formaterMasse(m: Double): String = String.format(Locale.FRENCH, "%.3f", m).trimEnd('0').trimEnd(',')

// ----------------------------------------------------------------------------
// MODES
// ----------------------------------------------------------------------------

enum class ModeElem(val label: String, val libelleChamp: String, val nom: String, val texte: Boolean) {
    NOM("Nom de l'élément", "Nom de l'élément", "Nom", true),
    SYMBOLE("Symbole (ex : Fe)", "Symbole\n(ex : Fe)", "Symbole", true),
    NUMERO("Numéro atomique", "Numéro atomique\n(Z)", "Numéro atomique", true),
    MASSE("Masse atomique (±1 %)", "Masse atomique (u)\n(±1 %)", "Masse atomique (u)", true),
    PERIODE("Période (ligne, de 1 à 7)", "Période\n(ligne, de 1 à 7)", "Période", true),
    GROUPE("Groupe (colonne, de 1 à 18)", "Groupe\n(colonne, de 1 à 18)", "Groupe", true),
    FAMILLE("Famille (QCM)", "", "Famille", false),
    ETAT("État à 25 °C (QCM)", "", "État à 25 °C", false),
    LOCALISATION("Emplacement dans le tableau", "", "Localisation", false)
}

enum class TypeElem { FICHE, CARTE, FAMILLE, ETAT }

data class QuestionElem(
    val element: Element,
    val type: TypeElem,
    val champs: List<ModeElem> = emptyList()
)

private val IDENTIFIANTS = listOf(ModeElem.NOM, ModeElem.SYMBOLE, ModeElem.NUMERO)

/**
 * Champs à remplir d'une fiche. Au moins un identifiant (nom, symbole ou numéro) reste affiché ;
 * la masse des éléments instables et le groupe des blocs f ne sont pas demandés.
 */
fun champsPourElem(e: Element, modes: List<ModeElem>): List<ModeElem> {
    val champs = modes.filter {
        it.texte && !(it == ModeElem.MASSE && e.z > 103) && !(it == ModeElem.GROUPE && e.groupe == 0)
    }
    val resteAffiche = IDENTIFIANTS.any { it !in champs }
    return if (resteAffiche) champs else champs - IDENTIFIANTS.random()
}

private fun eligible(t: TypeElem, e: Element, modes: List<ModeElem>) = when (t) {
    TypeElem.FAMILLE -> e.z <= 103 && e.symbole != "Po" && e.symbole != "At"   // classement discuté pour ces deux-là
    TypeElem.ETAT -> e.etat.isNotEmpty()
    TypeElem.FICHE -> champsPourElem(e, modes).isNotEmpty()
    TypeElem.CARTE -> true
}

fun genererQuestionsElem(tous: List<Element>, nb: Int, modes: List<ModeElem>): List<QuestionElem> {
    val types = buildList {
        if (modes.any { it.texte }) { add(TypeElem.FICHE); add(TypeElem.FICHE) }
        if (ModeElem.LOCALISATION in modes) add(TypeElem.CARTE)
        if (ModeElem.FAMILLE in modes) add(TypeElem.FAMILLE)
        if (ModeElem.ETAT in modes) add(TypeElem.ETAT)
    }
    if (types.isEmpty()) return emptyList()
    var pioche = tous.shuffled().toMutableList()
    fun suivant(): Element {
        if (pioche.isEmpty()) pioche = tous.shuffled().toMutableList()
        return pioche.removeAt(0)
    }
    return List(nb) {
        val t = types.random()
        var e = suivant()
        var essais = 0
        while (!eligible(t, e, modes) && essais < 200) { e = suivant(); essais++ }
        when (t) {
            TypeElem.FICHE -> QuestionElem(e, t, champsPourElem(e, modes))
            else -> QuestionElem(e, t)
        }
    }
}

// ----------------------------------------------------------------------------
// CORRECTION
// ----------------------------------------------------------------------------

fun valeurElem(e: Element, m: ModeElem): String = when (m) {
    ModeElem.NOM -> e.nom
    ModeElem.SYMBOLE -> e.symbole
    ModeElem.NUMERO -> e.z.toString()
    ModeElem.MASSE -> formaterMasse(e.masse)
    ModeElem.PERIODE -> e.periode.toString()
    ModeElem.GROUPE -> if (e.groupe == 0) "aucun (bloc f)" else e.groupe.toString()
    ModeElem.FAMILLE -> FAMILLES[e.famille] ?: e.famille
    ModeElem.ETAT -> ETATS_MATIERE[e.etat] ?: "Inconnu"
    else -> ""
}

fun masseJuste(reponse: String, e: Element): Boolean {
    val v = reponse.replace(',', '.').filter { it.isDigit() || it == '.' }.toDoubleOrNull() ?: return false
    return abs(v - e.masse) <= max(0.5, e.masse * 0.01)
}

fun champJusteElem(e: Element, m: ModeElem, reponse: String): Boolean = when (m) {
    ModeElem.NOM -> (listOf(e.nom, e.nomEn) + e.nomsAcceptes).filter { it.isNotEmpty() }.any { correspondA(reponse, it) }
    ModeElem.SYMBOLE -> normaliser(reponse) == normaliser(e.symbole)
    ModeElem.NUMERO -> reponse.filter { it.isDigit() }.toIntOrNull() == e.z
    ModeElem.MASSE -> masseJuste(reponse, e)
    ModeElem.PERIODE -> reponse.filter { it.isDigit() }.toIntOrNull() == e.periode
    ModeElem.GROUPE -> reponse.filter { it.isDigit() }.toIntOrNull() == e.groupe
    else -> false
}

// ----------------------------------------------------------------------------
// FLUX : paramètres -> questions -> résultats
// ----------------------------------------------------------------------------

@Composable
fun QuizElements(onRetour: () -> Unit) {
    val ctx = LocalContext.current
    val tous = remember { ElementsCache.obtenir(ctx) }
    var phase by remember { mutableStateOf(0) }
    var nbQuestions by remember { mutableStateOf(20f) }
    var modes by remember { mutableStateOf(ModeElem.entries.toList()) }
    var questions by remember { mutableStateOf(listOf<QuestionElem>()) }
    var index by remember { mutableStateOf(0) }
    var reponses by remember { mutableStateOf(listOf<Boolean>()) }
    var sauvee by remember { mutableStateOf(lirePartieElem(ctx, tous)) }
    var confirmer by remember { mutableStateOf(false) }

    BackHandler(enabled = phase == 1) { confirmer = true }

    fun demarrer() {
        questions = genererQuestionsElem(tous, nbQuestions.toInt(), modes)
        index = 0
        reponses = emptyList()
        effacerPartieElem(ctx)
        sauvee = null
        phase = 1
    }

    if (confirmer) {
        DialogQuitter(
            onContinuer = { confirmer = false },
            onPause = {
                sauverPartieElem(ctx, questions, index, reponses)
                sauvee = lirePartieElem(ctx, tous)
                confirmer = false
                onRetour()
            },
            onAbandonner = {
                effacerPartieElem(ctx)
                sauvee = null
                confirmer = false
                onRetour()
            }
        )
    }

    when (phase) {
        0 -> EcranParamsElem(
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
        1 -> EcranQuizElem(
            question = questions[index],
            total = questions.size,
            index = index,
            onRepondu = { correct ->
                reponses = reponses + correct
                if (index + 1 < questions.size) {
                    index++
                    sauverPartieElem(ctx, questions, index, reponses)
                } else {
                    effacerPartieElem(ctx)
                    sauvee = null
                    phase = 2
                }
            },
            onAbandon = { confirmer = true }
        )
        else -> EcranResultatsElem(questions, reponses, onRef = { demarrer() }, onMenu = onRetour)
    }
}

@Composable
fun EcranParamsElem(
    nbQuestions: Float,
    onNbQuestions: (Float) -> Unit,
    modes: List<ModeElem>,
    onModes: (List<ModeElem>) -> Unit,
    onDemarrer: () -> Unit,
    partie: PartieElem?,
    onReprendre: (PartieElem) -> Unit
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
            "Dans une fiche, au moins un identifiant (nom, symbole ou numéro) reste toujours affiché. " +
                "Les éléments instables n'ont pas de question sur la masse.",
            fontSize = 13.sp, color = Color.Gray
        )
        ModeElem.entries.forEach { mode ->
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
fun EcranQuizElem(
    question: QuestionElem,
    total: Int,
    index: Int,
    onRepondu: (Boolean) -> Unit,
    onAbandon: () -> Unit
) {
    val e = question.element
    val textes = ModeElem.entries.filter { it.texte }
    var saisies by remember(question) {
        mutableStateOf<Map<ModeElem, String>>(question.champs.associateWith { "" })
    }
    var verdicts by remember(question) { mutableStateOf<Map<ModeElem, Boolean>>(emptyMap()) }
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
            TypeElem.CARTE -> {
                EnonceElem("Place l'élément suivant dans le tableau périodique : ${e.nom}. (Pince pour zoomer)")
                Spacer(Modifier.height(8.dp))
                key(question) {
                    CarteFrance(
                        choix = choixCarte,
                        reponse = if (valide) e.symbole else null,
                        onChoix = { choixCarte = it },
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        fichier = CARTE_TABLEAU
                    )
                }
            }

            TypeElem.FAMILLE, TypeElem.ETAT -> Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                val estFamille = question.type == TypeElem.FAMILLE
                EnonceElem(
                    if (estFamille) "À quelle famille appartient l'élément suivant : ${e.nom} (${e.symbole}) ?"
                    else "Quel est l'état de l'élément suivant à 25 °C : ${e.nom} (${e.symbole}) ?"
                )
                Spacer(Modifier.height(8.dp))
                val options = if (estFamille) FAMILLES.entries.map { it.key to it.value }
                else ETATS_MATIERE.entries.map { it.key to it.value }
                val bonne = if (estFamille) e.famille else e.etat
                options.forEach { (code, libelle) ->
                    val neutre = Color(0xFFBDBDBD)
                    val couleur = when {
                        valide && code == bonne -> Color(0xFF2E7D32)
                        valide && code == choixQcm -> Color(0xFFC62828)
                        !valide && code == choixQcm -> MaterialTheme.colorScheme.primary
                        else -> neutre
                    }
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .border(if (couleur == neutre) 1.dp else 4.dp, couleur, RoundedCornerShape(10.dp))
                            .clickable(enabled = !valide) { choixQcm = code }
                            .padding(14.dp)
                    ) {
                        Text(libelle, fontSize = 17.sp)
                    }
                }
                if (valide) {
                    Spacer(Modifier.height(12.dp))
                    TableauMini(e.symbole)
                }
            }

            TypeElem.FICHE -> LazyColumn(Modifier.weight(1f)) {
                item {
                    Card(
                        Modifier
                            .fillMaxWidth()
                            .border(4.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp))
                    ) {
                        Column(Modifier.padding(14.dp)) {
                            Text("Complète la fiche", fontSize = 19.sp, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(6.dp))
                            textes.forEach { mode ->
                                if (mode !in question.champs) {
                                    // les infos que l'on ne demande pas sont données (sauf celles qui sortent du cadre)
                                    if (!(mode == ModeElem.MASSE && e.z > 103)) LigneFiche(mode.nom, valeurElem(e, mode))
                                } else {
                                    ChampRempli(
                                        libelle = mode.libelleChamp,
                                        valeur = saisies[mode] ?: "",
                                        onSaisie = { nvx -> saisies = saisies.toMutableMap().apply { put(mode, nvx) } },
                                        verrouille = valide,
                                        verdict = verdicts[mode],
                                        multiligne = false
                                    )
                                    if (verdicts[mode] == false) {
                                        Text(
                                            "Attendu : ${valeurElem(e, mode)}",
                                            fontSize = 13.sp,
                                            color = Color(0xFFC62828),
                                            modifier = Modifier.padding(start = 16.dp)
                                        )
                                    }
                                }
                            }
                            LigneFiche("Famille", valeurElem(e, ModeElem.FAMILLE))
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(10.dp))

        if (!valide) {
            val pret = when (question.type) {
                TypeElem.CARTE -> choixCarte != null
                TypeElem.FAMILLE, TypeElem.ETAT -> choixQcm != null
                TypeElem.FICHE -> question.champs.all { (saisies[it] ?: "").isNotBlank() }
            }
            Button(
                onClick = {
                    when (question.type) {
                        TypeElem.CARTE -> correct = choixCarte == e.symbole
                        TypeElem.FAMILLE -> correct = choixQcm == e.famille
                        TypeElem.ETAT -> correct = choixQcm == e.etat
                        TypeElem.FICHE -> {
                            val v = question.champs.associateWith { m -> champJusteElem(e, m, saisies[m] ?: "") }
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
                    if (question.type == TypeElem.CARTE && choixCarte == null) "Touche une case du tableau" else "Valider",
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
            if (question.type == TypeElem.CARTE && !correct) {
                val choisi = ElementsCache.obtenir(LocalContext.current).firstOrNull { it.symbole == choixCarte }
                if (choisi != null) {
                    Text(
                        "Tu as choisi : ${choisi.nom} (${choisi.symbole})",
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
private fun EnonceElem(texte: String) {
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
fun EcranResultatsElem(
    questions: List<QuestionElem>,
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
                        TypeElem.CARTE -> "Localisation"
                        TypeElem.FAMILLE -> "Famille"
                        TypeElem.ETAT -> "État"
                        TypeElem.FICHE -> "Fiche"
                    }
                    Text("$type - ${q.element.nom} (${q.element.symbole})", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    if (!ok && q.type == TypeElem.FAMILLE) {
                        Text("Attendu : ${valeurElem(q.element, ModeElem.FAMILLE)}", fontSize = 12.sp, color = Color.Gray)
                    }
                    if (!ok && q.type == TypeElem.ETAT) {
                        Text("Attendu : ${valeurElem(q.element, ModeElem.ETAT)}", fontSize = 12.sp, color = Color.Gray)
                    }
                    if (!ok && q.type == TypeElem.FICHE) {
                        Text(
                            "Attendu : " + q.champs.joinToString(" | ") { "${it.nom} = ${valeurElem(q.element, it)}" },
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
// TABLEAU COLORÉ (fiches de révision) : symboles dans les cases, zoom, famille en couleur
// ----------------------------------------------------------------------------

@Composable
fun TableauMini(symbole: String, modifier: Modifier = Modifier) {
    val contexte = LocalContext.current
    val geo = remember { CarteCache.obtenir(contexte, CARTE_TABLEAU) }
    val elements = remember { ElementsCache.obtenir(contexte) }
    val couleurs = remember { elements.associate { it.symbole to (COULEURS_FAMILLES[it.famille] ?: Color.LightGray) } }
    val tm = rememberTextMeasurer()
    Canvas(
        modifier
            .fillMaxWidth()
            .aspectRatio(geo.largeur / geo.hauteur)
    ) {
        val k = min(size.width / geo.largeur, size.height / geo.hauteur)
        val tx = (size.width - geo.largeur * k) / 2f
        val ty = (size.height - geo.hauteur * k) / 2f
        val px = 1.dp.toPx()
        dessinerCarte(geo, k, tx, ty, 0.4f * px, 0f) { c -> couleurs[c] ?: Color.LightGray }
        geo.departements.forEach { f ->
            val w = f.largeurBoite * k
            etiquette(tm, listOf(f.numero), tx + f.centreX * k, ty + f.centreY * k, w, (w * 0.42f).toSp(), Color(0xFF212121))
        }
        // l'élément de la fiche est entouré en rouge
        geo.departements.firstOrNull { it.numero == symbole }?.let { f ->
            drawRect(
                Color(0xFFD32F2F),
                topLeft = Offset(tx + (f.centreX - f.largeurBoite / 2f) * k, ty + (f.centreY - f.hauteurBoite / 2f) * k),
                size = androidx.compose.ui.geometry.Size(f.largeurBoite * k, f.hauteurBoite * k),
                style = Stroke(width = 2.5f * px)
            )
        }
    }
}

// ----------------------------------------------------------------------------
// FICHES (révision)
// ----------------------------------------------------------------------------

@Composable
fun EcranFichesElem() {
    val ctx = LocalContext.current
    val tous = remember { ElementsCache.obtenir(ctx) }
    var recherche by remember { mutableStateOf("") }
    var ouvert by remember { mutableStateOf<Element?>(null) }
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
                    Text("${e.nom} (${e.symbole})", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Text("En anglais : ${e.nomEn}", fontSize = 15.sp, color = Color.Gray)
                    Spacer(Modifier.height(8.dp))
                    Text("Numéro atomique : ${e.z}", fontSize = 15.sp)
                    Text(
                        "Masse atomique : ${formaterMasse(e.masse)} u" + if (e.z > 103) " (isotope le plus stable)" else "",
                        fontSize = 15.sp
                    )
                    Text("Famille : ${valeurElem(e, ModeElem.FAMILLE)}", fontSize = 15.sp)
                    Text("Période : ${e.periode}", fontSize = 15.sp)
                    Text("Groupe : ${valeurElem(e, ModeElem.GROUPE)}", fontSize = 15.sp)
                    if (e.etat.isNotEmpty()) Text("État à 25 °C : ${valeurElem(e, ModeElem.ETAT)}", fontSize = 15.sp)
                    if (e.isotopes.isNotEmpty()) {
                        Text(
                            (if (e.isotopes.first().contains("demi-vie")) "Isotope le plus stable : " else "Isotopes naturels : ") +
                                e.isotopes.joinToString(", "),
                            fontSize = 15.sp
                        )
                    }
                    if (e.config.isNotEmpty()) Text("Configuration électronique : ${e.config}", fontSize = 15.sp)
                    Spacer(Modifier.height(12.dp))
                    TableauMini(e.symbole)
                }
            }
            PiedFiche(onAleatoire = { ouvert = tous.random() })
        }
    } else {
        Column(Modifier.fillMaxSize().padding(16.dp)) {
            Text("Fiches : tableau périodique", fontWeight = FontWeight.Bold, fontSize = 20.sp)
            TextField(
                value = recherche,
                onValueChange = { recherche = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Rechercher (nom, symbole, numéro)") },
                singleLine = true
            )
            Spacer(Modifier.height(8.dp))
            val q = normaliser(recherche)
            val filtres = tous.filter {
                q.isEmpty() || normaliser(it.nom).contains(q) || normaliser(it.nomEn).contains(q) || normaliser(it.symbole) == q ||
                    it.z.toString() == recherche.trim() || it.nomsAcceptes.any { n -> normaliser(n).contains(q) }
            }
            LazyColumn(Modifier.weight(1f)) {
                items(filtres) { el ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { ouvert = el }
                    ) {
                        Column(Modifier.padding(12.dp)) {
                            Text("${el.z} - ${el.nom} (${el.symbole})", fontSize = 17.sp, fontWeight = FontWeight.Bold)
                            Text(
                                "Famille : ${valeurElem(el, ModeElem.FAMILLE)}",
                                fontSize = 13.sp, color = Color.Gray
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Button(onClick = { ouvert = tous.random() }, Modifier.fillMaxWidth().height(52.dp)) { Text("Fiche aléatoire") }
        }
    }
}
