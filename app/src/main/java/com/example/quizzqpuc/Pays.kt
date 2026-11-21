// ============================================================================
// QUIZZ QPUC - Catégorie « Pays du monde »
// Données : assets/pays.json, carte : assets/carte_monde.json, drapeaux : assets/drapeaux/xx.png
// ============================================================================

package com.example.quizzqpuc

import android.content.Context
import android.graphics.BitmapFactory
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.json.JSONArray
import org.json.JSONObject
import java.util.Locale
import kotlin.math.abs
import kotlin.math.min

// ----------------------------------------------------------------------------
// DONNÉES
// ----------------------------------------------------------------------------

data class Monnaie(val code: String, val nom: String)

data class Pays(
    val iso3: String,
    val iso2: String,
    val nom: String,
    val nomsAcceptes: List<String>,
    val capitales: List<String>,
    val capitalesAcceptees: List<String>,
    val population: Long,
    val superficie: Double,
    val langues: List<String>,
    val monnaies: List<Monnaie>,
    val continent: String,
    val transcontinental: Boolean
)

val CONTINENTS = listOf("Afrique", "Amérique du Nord", "Amérique du Sud", "Asie", "Europe", "Océanie")

private fun JSONArray?.versListe(): List<String> =
    if (this == null) emptyList() else List(length()) { getString(it) }

fun chargerPays(ctx: Context): List<Pays> {
    val texte = ctx.assets.open("pays.json").bufferedReader().use { it.readText() }
    val j = JSONArray(texte)
    return List(j.length()) { i ->
        val o = j.getJSONObject(i)
        val ms = o.getJSONArray("m")
        Pays(
            iso3 = o.getString("i"),
            iso2 = o.getString("a"),
            nom = o.getString("n"),
            nomsAcceptes = o.optJSONArray("na").versListe(),
            capitales = o.getJSONArray("c").versListe(),
            capitalesAcceptees = o.optJSONArray("ca").versListe(),
            population = o.getLong("p"),
            superficie = o.getDouble("s"),
            langues = o.getJSONArray("l").versListe(),
            monnaies = List(ms.length()) { k ->
                val m = ms.getJSONObject(k)
                Monnaie(m.getString("code"), m.getString("nom"))
            },
            continent = o.getString("k"),
            transcontinental = o.has("tc")
        )
    }.sortedBy { normaliser(it.nom) }
}

object PaysCache {
    private var liste: List<Pays>? = null
    fun obtenir(ctx: Context): List<Pays> = liste ?: chargerPays(ctx.applicationContext).also { liste = it }
}

fun formaterNombre(n: Long): String = String.format(Locale.FRENCH, "%,d", n).replace(' ', ' ').replace(' ', ' ')

// ----------------------------------------------------------------------------
// MODES DE QUESTION
// ----------------------------------------------------------------------------

/**
 * texte = champ à remplir dans une fiche ; sinon type de question à part (carte, QCM).
 */
enum class ModePays(val label: String, val libelleChamp: String, val nom: String, val texte: Boolean) {
    NOM("Nom du pays", "Nom du pays", "Nom", true),
    CAPITALE("Capitale(s)", "Capitale(s)", "Capitale(s)", true),
    POPULATION("Nb d'habitants ($TOL_TXT)", "Habitants\n(ex : 5000000, $TOL_TXT)", "Habitants", true),
    LANGUES("Langue(s) officielle(s)", "Langue(s) officielle(s)", "Langue(s) officielle(s)", true),
    MONNAIE("Monnaie", "Monnaie", "Monnaie", true),
    LOCALISATION("Emplacement sur la carte", "", "Localisation", false),
    DRAPEAU("Drapeau (QCM)", "", "Drapeau", false),
    CONTINENT("Continent (QCM)", "", "Continent", false)
}

enum class TypeQ { FICHE, CARTE, DRAPEAU, CONTINENT }

data class QuestionPays(
    val pays: Pays,
    val type: TypeQ,
    val champs: List<ModePays> = emptyList(),
    val choix: List<Pays> = emptyList()
)

// drapeaux quasiment identiques : jamais proposés ensemble dans un QCM
private val JUMEAUX = listOf(setOf("IDN", "MCO"), setOf("ROU", "TCD"))

private fun trop_proches(a: Pays, b: Pays) = JUMEAUX.any { a.iso3 in it && b.iso3 in it }

fun genererQuestionsPays(tous: List<Pays>, nb: Int, modes: List<ModePays>): List<QuestionPays> {
    val champs = ModePays.entries.filter { it.texte && it in modes }
    val types = buildList {
        if (champs.isNotEmpty()) { add(TypeQ.FICHE); add(TypeQ.FICHE) }
        if (ModePays.LOCALISATION in modes) add(TypeQ.CARTE)
        if (ModePays.DRAPEAU in modes) add(TypeQ.DRAPEAU)
        if (ModePays.CONTINENT in modes) add(TypeQ.CONTINENT)
    }
    if (types.isEmpty()) return emptyList()
    var pioche = tous.shuffled().toMutableList()
    fun suivant(): Pays {
        if (pioche.isEmpty()) pioche = tous.shuffled().toMutableList()
        return pioche.removeAt(0)
    }
    return List(nb) {
        val t = types.random()
        var p = suivant()
        if (t == TypeQ.CONTINENT) {
            var essais = 0
            while (p.transcontinental && essais < 60) { p = suivant(); essais++ }
        }
        when (t) {
            TypeQ.FICHE -> QuestionPays(p, t, champs)
            TypeQ.DRAPEAU -> {
                val candidats = tous.filter { it.iso3 != p.iso3 && !trop_proches(it, p) }
                val memeContinent = candidats.filter { it.continent == p.continent }
                val autres = (if (memeContinent.size >= 4) memeContinent else candidats).shuffled().take(4)
                QuestionPays(p, t, choix = (autres + p).shuffled())
            }
            else -> QuestionPays(p, t)
        }
    }
}

// ----------------------------------------------------------------------------
// CORRECTION
// ----------------------------------------------------------------------------

private val ARTICLE = Regex("^(?:(?:le|la|les|un|une|du|des)\\s+|(?:l|d)['’]\\s*)", RegexOption.IGNORE_CASE)

fun sansArticle(s: String): String = s.trim().replace(ARTICLE, "")

/** Comparaison tolérante (accents, majuscules, petite faute, article ignoré). */
fun correspondA(reponse: String, attendu: String): Boolean =
    correspond(sansArticle(reponse), sansArticle(attendu))

private fun decouperReponses(saisie: String): List<String> =
    decouperListe(saisie.replace(" et ", ","))

/** Renvoie (nombre de bonnes réponses distinctes, nombre de réponses fausses ou en double). */
fun evaluerListe(attendus: List<List<String>>, saisie: List<String>): Pair<Int, Int> {
    val restants = attendus.toMutableList()
    var ok = 0
    var faux = 0
    for (s in saisie) {
        val i = restants.indexOfFirst { variantes ->
            variantes.any { correspondA(s, it) || correspondA(s.trimEnd('s'), it) }
        }
        if (i >= 0) {
            restants.removeAt(i)
            ok++
        } else faux++
    }
    return ok to faux
}

private val ALIAS_LANGUES = mapOf(
    "Chinois" to listOf("Mandarin"),
    "Néerlandais" to listOf("Flamand", "Hollandais"),
    "Persan" to listOf("Farsi"),
    "Ourdou" to listOf("Urdu"),
    "Filipino" to listOf("Tagalog"),
    "Bichelamar" to listOf("Bislama"),
    "Créole haïtien" to listOf("Créole", "Haïtien"),
    "Zoulou" to listOf("Zulu"),
    "Tétoum" to listOf("Tetum"),
    "Berbère" to listOf("Amazigh", "Tamazight"),
    "Roundi" to listOf("Kirundi", "Rundi"),
    "Swati" to listOf("Siswati"),
    "Tswana" to listOf("Setswana"),
    "Sotho du Sud" to listOf("Sesotho", "Sotho"),
    "Maldivien" to listOf("Divehi", "Dhivehi"),
    "Cingalais" to listOf("Singhalais"),
    "Biélorusse" to listOf("Belarusse"),
    "Kirghize" to listOf("Kirghiz"),
    "Tadjik" to listOf("Tajik"),
    "Azerbaïdjanais" to listOf("Azéri", "Azeri"),
    "Indonésien" to listOf("Bahasa", "Bahasa indonésien")
)

fun variantesMonnaie(m: Monnaie): List<String> {
    val sansParenthese = m.nom.replace(Regex("\\s*\\(.*?\\)"), "").trim()
    val extra = when (m.code) {
        "USD" -> listOf("dollar américain", "dollar us", "dollar", "dollar états-unien")
        "GBP" -> listOf("livre", "livre britannique")
        "XOF", "XAF" -> listOf("franc CFA", "CFA")
        "CNY" -> listOf("yuan", "renminbi")
        "ZWG" -> listOf("ZiG", "or du Zimbabwe")
        else -> emptyList()
    }
    return listOf(m.nom, sansParenthese, m.code) + extra
}

fun parserPopulation(s: String): Long {
    val t = s.lowercase(Locale.FRENCH).trim()
    val mult = when {
        "milliard" in t -> 1_000_000_000.0
        "million" in t || Regex("\\d\\s*m$").containsMatchIn(t) -> 1_000_000.0
        "mille" in t || Regex("\\d\\s*k$").containsMatchIn(t) -> 1_000.0
        else -> 1.0
    }
    if (mult > 1.0) {
        val nombre = Regex("[0-9]+(?:[.,][0-9]+)?").find(t)?.value?.replace(',', '.')?.toDoubleOrNull()
            ?: return 0L
        return (nombre * mult).toLong()
    }
    return t.filter { it.isDigit() }.toLongOrNull() ?: 0L
}

fun dansToleranceL(v: Long, cible: Long): Boolean =
    v > 0 && abs(v - cible) <= cible * TOLERANCE_POURCENT / 100.0

fun attendusCapitales(p: Pays): List<List<String>> = p.capitales.map { c ->
    listOf(c) + when {
        p.capitales.size == 1 -> p.capitalesAcceptees
        c == "Le Cap" -> listOf("Cape Town")
        else -> emptyList()
    }
}

fun attendusLangues(p: Pays): List<List<String>> =
    p.langues.map { listOf(it) + (ALIAS_LANGUES[it] ?: emptyList()) }

/** Nombre de langues à citer : toutes, mais pas plus de 3. */
fun langueRequises(p: Pays) = min(p.langues.size, 3)

fun valeurPays(p: Pays, m: ModePays): String = when (m) {
    ModePays.NOM -> p.nom
    ModePays.CAPITALE -> p.capitales.joinToString(", ")
    ModePays.POPULATION -> formaterNombre(p.population)
    ModePays.LANGUES -> p.langues.joinToString(", ")
    ModePays.MONNAIE -> p.monnaies.joinToString(", ") { it.nom }
    ModePays.CONTINENT -> p.continent
    else -> ""
}

fun champJustePays(p: Pays, m: ModePays, reponse: String): Boolean = when (m) {
    ModePays.NOM -> (listOf(p.nom) + p.nomsAcceptes).any { correspondA(reponse, it) }
    ModePays.CAPITALE -> {
        val (ok, faux) = evaluerListe(attendusCapitales(p), decouperReponses(reponse))
        faux == 0 && ok >= p.capitales.size
    }
    ModePays.POPULATION -> dansToleranceL(parserPopulation(reponse), p.population)
    ModePays.LANGUES -> {
        val (ok, faux) = evaluerListe(attendusLangues(p), decouperReponses(reponse))
        faux == 0 && ok >= langueRequises(p)
    }
    ModePays.MONNAIE -> {
        val (ok, faux) = evaluerListe(p.monnaies.map { variantesMonnaie(it) }, decouperReponses(reponse))
        faux == 0 && ok >= 1
    }
    else -> false
}

/** Indication affichée sous le libellé du champ quand plusieurs réponses sont possibles. */
fun libelleChampPays(p: Pays, m: ModePays): String = when (m) {
    ModePays.CAPITALE ->
        if (p.capitales.size > 1) "Capitale(s)\n(${p.capitales.size} à citer, séparées par des virgules)"
        else "Capitale(s)"
    ModePays.LANGUES -> {
        val n = langueRequises(p)
        if (n > 1) "Langue(s) officielle(s)\n(${if (p.langues.size > n) "au moins " else ""}$n à citer, séparées par des virgules)"
        else "Langue(s) officielle(s)"
    }
    ModePays.MONNAIE ->
        if (p.monnaies.size > 1) "Monnaie\n(une seule suffit)" else "Monnaie"
    else -> m.libelleChamp
}

// ----------------------------------------------------------------------------
// DRAPEAU
// ----------------------------------------------------------------------------

@Composable
fun Drapeau(iso2: String, modifier: Modifier = Modifier, hauteur: Dp = 90.dp) {
    val ctx = LocalContext.current
    val image = remember(iso2) {
        try {
            ctx.assets.open("drapeaux/${iso2.lowercase(Locale.ROOT)}.png").use {
                BitmapFactory.decodeStream(it)
            }?.asImageBitmap()
        } catch (e: Exception) {
            null
        }
    }
    if (image != null) {
        Image(
            bitmap = image,
            contentDescription = "Drapeau",
            contentScale = ContentScale.Fit,
            modifier = modifier
                .height(hauteur)
                .aspectRatio(4f / 3f)
                .border(1.dp, Color(0xFFB0B0B0))
        )
    }
}

// ----------------------------------------------------------------------------
// CARTE DU MONDE
// ----------------------------------------------------------------------------

private const val ZOOM_MAX_MONDE = 40f
private val MONDE_PAYS = Color(0xFFC8C8C8)
private val MONDE_FOND = Color(0xFFE2E2E2)
private val MONDE_LIMITE = Color(0xFF8C8C8C)
private val MONDE_BLEU = Color(0xFF42A5F5)
private val MONDE_VERT = Color(0xFF66BB6A)
private val MONDE_ROUGE = Color(0xFFEF5350)

class Marqueur(val iso3: String, val x: Float, val y: Float)

class MondeGeo(
    val largeur: Float,
    val hauteur: Float,
    val formes: List<FormeDept>,
    val fond: Path,
    val continents: Path,
    val marqueurs: List<Marqueur>,
    val centres: Map<String, Offset>
)

fun chargerMonde(ctx: Context): MondeGeo {
    val texte = ctx.assets.open("carte_monde.json").bufferedReader().use { it.readText() }
    val racine = JSONObject(texte)
    val d = racine.getJSONArray("d")
    val formes = ArrayList<FormeDept>(d.length())
    val centres = HashMap<String, Offset>()
    for (i in 0 until d.length()) {
        val e = d.getJSONArray(i)
        val anneauxJson = e.getJSONArray(1)
        if (anneauxJson.length() == 0) continue
        val anneaux = List(anneauxJson.length()) { lireAnneau(anneauxJson.getJSONArray(it)) }
        val code = e.getString(0)
        formes.add(FormeDept(code, anneaux))
        // point de repère : centre de la boîte du plus grand anneau (évite l'Alaska ou Hawaï pour les États-Unis)
        var meilleur = -1f
        for (a in anneaux) {
            var x0 = Float.MAX_VALUE; var y0 = Float.MAX_VALUE
            var x1 = -Float.MAX_VALUE; var y1 = -Float.MAX_VALUE
            var k = 0
            while (k < a.size) {
                if (a[k] < x0) x0 = a[k]
                if (a[k] > x1) x1 = a[k]
                if (a[k + 1] < y0) y0 = a[k + 1]
                if (a[k + 1] > y1) y1 = a[k + 1]
                k += 2
            }
            val surface = (x1 - x0) * (y1 - y0)
            if (surface > meilleur) {
                meilleur = surface
                centres[code] = Offset((x0 + x1) / 2f, (y0 + y1) / 2f)
            }
        }
    }
    val fond = Path().apply { fillType = PathFillType.EvenOdd }
    val x = racine.getJSONArray("x")
    for (i in 0 until x.length()) ajouterAnneau(fond, lireAnneau(x.getJSONArray(i)))
    val continents = Path()
    val r = racine.getJSONArray("r")
    for (i in 0 until r.length()) ajouterAnneau(continents, lireAnneau(r.getJSONArray(i)))
    val m = racine.getJSONArray("m")
    val marqueurs = List(m.length()) { i ->
        val e = m.getJSONArray(i)
        Marqueur(e.getString(0), e.getInt(1).toFloat(), e.getInt(2).toFloat())
    }
    marqueurs.forEach { centres[it.iso3] = Offset(it.x, it.y) }
    return MondeGeo(
        racine.getInt("w").toFloat(), racine.getInt("h").toFloat(),
        formes, fond, continents, marqueurs, centres
    )
}

object MondeCache {
    private var geo: MondeGeo? = null
    fun obtenir(ctx: Context): MondeGeo = geo ?: chargerMonde(ctx.applicationContext).also { geo = it }
}

private fun limiterMonde(pan: Offset, k: Float, lw: Float, lh: Float, geo: MondeGeo): Offset {
    val debX = geo.largeur * k - lw
    val debY = geo.hauteur * k - lh
    return Offset(
        if (debX <= 0f) 0f else pan.x.coerceIn(-debX / 2f, debX / 2f),
        if (debY <= 0f) 0f else pan.y.coerceIn(-debY / 2f, debY / 2f)
    )
}

private fun DrawScope.dessinerMonde(
    geo: MondeGeo,
    k: Float,
    tx: Float,
    ty: Float,
    px: Float,
    rayonMarqueur: Float,
    couleurDe: (String) -> Color
) {
    withTransform({
        translate(tx, ty)
        scale(k, k, Offset.Zero)
    }) {
        drawPath(geo.fond, MONDE_FOND)
        geo.formes.forEach { f -> drawPath(f.chemin, couleurDe(f.numero)) }
        geo.formes.forEach { f -> drawPath(f.chemin, MONDE_LIMITE, style = Stroke(width = 0.5f * px / k)) }
        drawPath(geo.continents, Color.Black, style = Stroke(width = 1.2f * px / k, join = StrokeJoin.Round))
        // micro-États : pastilles toujours visibles, à la même taille quel que soit le zoom
        geo.marqueurs.forEach { m ->
            val c = Offset(m.x, m.y)
            drawCircle(couleurDe(m.iso3), radius = rayonMarqueur / k, center = c)
            drawCircle(Color.Black, radius = rayonMarqueur / k, center = c, style = Stroke(width = 1f * px / k))
        }
    }
}

/**
 * choix   : code du pays touché (en bleu)
 * reponse : bonne réponse, uniquement après validation (en vert ; le choix faux passe en rouge)
 */
@Composable
fun CarteMonde(
    choix: String?,
    reponse: String?,
    onChoix: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val contexte = LocalContext.current
    val geo = remember { MondeCache.obtenir(contexte) }
    var zoom by remember { mutableStateOf(1f) }
    var decalage by remember { mutableStateOf(Offset.Zero) }
    var taille by remember { mutableStateOf(IntSize.Zero) }
    val reponseActuelle by rememberUpdatedState(reponse)
    val onChoixActuel by rememberUpdatedState(onChoix)

    Box(
        modifier
            .background(Color(0xFFF4F7FB), RoundedCornerShape(12.dp))
            .border(1.dp, Color(0xFFB0C4DE), RoundedCornerShape(12.dp))
            .clipToBounds()
    ) {
        Canvas(
            Modifier
                .fillMaxSize()
                .onSizeChanged { taille = it }
                .pointerInput(geo) {
                    detectTransformGestures { centre, pan, zoomGeste, _ ->
                        val lw = taille.width.toFloat()
                        val lh = taille.height.toFloat()
                        if (lw <= 0f || lh <= 0f) return@detectTransformGestures
                        val fit = min(lw / geo.largeur, lh / geo.hauteur)
                        val kAvant = fit * zoom
                        val zoomApres = (zoom * zoomGeste).coerceIn(1f, ZOOM_MAX_MONDE)
                        val kApres = fit * zoomApres
                        val mx = (centre.x - ((lw - geo.largeur * kAvant) / 2f + decalage.x)) / kAvant
                        val my = (centre.y - ((lh - geo.hauteur * kAvant) / 2f + decalage.y)) / kAvant
                        val panX = centre.x - mx * kApres - (lw - geo.largeur * kApres) / 2f + pan.x
                        val panY = centre.y - my * kApres - (lh - geo.hauteur * kApres) / 2f + pan.y
                        zoom = zoomApres
                        decalage = limiterMonde(Offset(panX, panY), kApres, lw, lh, geo)
                    }
                }
                .pointerInput(geo) {
                    detectTapGestures { p ->
                        if (reponseActuelle != null) return@detectTapGestures
                        val lw = taille.width.toFloat()
                        val lh = taille.height.toFloat()
                        if (lw <= 0f || lh <= 0f) return@detectTapGestures
                        val k = min(lw / geo.largeur, lh / geo.hauteur) * zoom
                        val ox = (lw - geo.largeur * k) / 2f + decalage.x
                        val oy = (lh - geo.hauteur * k) / 2f + decalage.y
                        // 1) une pastille de micro-État touchée (zone de toucher généreuse : 20 dp)
                        val zone = 20.dp.toPx()
                        val pastille = geo.marqueurs
                            .map { m ->
                                val dx = ox + m.x * k - p.x
                                val dy = oy + m.y * k - p.y
                                m to (dx * dx + dy * dy)
                            }
                            .filter { it.second <= zone * zone }
                            .minByOrNull { it.second }
                        if (pastille != null) {
                            onChoixActuel(pastille.first.iso3)
                            return@detectTapGestures
                        }
                        // 2) sinon le pays dont la forme contient le point
                        val x = (p.x - ox) / k
                        val y = (p.y - oy) / k
                        geo.formes.firstOrNull { it.contient(x, y) }?.let { onChoixActuel(it.numero) }
                    }
                }
        ) {
            val k = min(size.width / geo.largeur, size.height / geo.hauteur) * zoom
            val tx = (size.width - geo.largeur * k) / 2f + decalage.x
            val ty = (size.height - geo.hauteur * k) / 2f + decalage.y
            dessinerMonde(geo, k, tx, ty, 1.dp.toPx(), 7.dp.toPx()) { code ->
                when {
                    reponse != null && code == reponse -> MONDE_VERT
                    reponse != null && code == choix -> MONDE_ROUGE
                    reponse == null && code == choix -> MONDE_BLEU
                    else -> MONDE_PAYS
                }
            }
        }
    }
}

/** Petite carte non interactive : le pays est en bleu, entouré d'un cercle rouge. */
@Composable
fun CarteMondeMini(iso3: String, modifier: Modifier = Modifier) {
    val contexte = LocalContext.current
    val geo = remember { MondeCache.obtenir(contexte) }
    Canvas(
        modifier
            .fillMaxWidth()
            .aspectRatio(geo.largeur / geo.hauteur)
            .background(Color(0xFFF4F7FB))
            .clipToBounds()
    ) {
        val k = min(size.width / geo.largeur, size.height / geo.hauteur)
        val tx = (size.width - geo.largeur * k) / 2f
        val ty = (size.height - geo.hauteur * k) / 2f
        val px = 1.dp.toPx()
        dessinerMonde(geo, k, tx, ty, px, 4.dp.toPx()) { c -> if (c == iso3) MONDE_BLEU else MONDE_PAYS }
        geo.centres[iso3]?.let { c ->
            drawCircle(
                Color(0xFFD32F2F),
                radius = 11.dp.toPx(),
                center = Offset(tx + c.x * k, ty + c.y * k),
                style = Stroke(width = 1.8f * px)
            )
        }
    }
}

// ----------------------------------------------------------------------------
// FLUX DU QUIZ : paramètres -> questions -> résultats
// ----------------------------------------------------------------------------

@Composable
fun QuizPays(onRetour: () -> Unit) {
    val ctx = LocalContext.current
    val tous = remember { PaysCache.obtenir(ctx) }
    var phase by remember { mutableStateOf(0) }       // 0 paramètres, 1 quiz, 2 résultats
    var nbQuestions by remember { mutableStateOf(20f) }
    var modes by remember { mutableStateOf(ModePays.entries.toList()) }
    var questions by remember { mutableStateOf(listOf<QuestionPays>()) }
    var index by remember { mutableStateOf(0) }
    var reponses by remember { mutableStateOf(listOf<Boolean>()) }

    fun demarrer() {
        questions = genererQuestionsPays(tous, nbQuestions.toInt(), modes)
        index = 0
        reponses = emptyList()
        phase = 1
    }

    when (phase) {
        0 -> EcranParamsPays(
            nbQuestions, { nbQuestions = it },
            modes, { modes = it },
            onDemarrer = { demarrer() },
            onRetour = onRetour
        )
        1 -> EcranQuizPays(
            question = questions[index],
            total = questions.size,
            index = index,
            onRepondu = { correct ->
                reponses = reponses + correct
                if (index + 1 < questions.size) index++ else phase = 2
            },
            onAbandon = onRetour
        )
        else -> EcranResultatsPays(questions, reponses, onRef = { demarrer() }, onMenu = onRetour)
    }
}

@Composable
fun EcranParamsPays(
    nbQuestions: Float,
    onNbQuestions: (Float) -> Unit,
    modes: List<ModePays>,
    onModes: (List<ModePays>) -> Unit,
    onDemarrer: () -> Unit,
    onRetour: () -> Unit
) {
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        TextButton(onClick = onRetour) { Text("← Quiz") }
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
            "Les champs cochés « fiche » sont à remplir ensemble ; le drapeau et la carte du pays " +
                "te sont toujours donnés dans la fiche.",
            fontSize = 13.sp, color = Color.Gray
        )
        ModePays.entries.forEach { mode ->
            val actif = mode in modes
            Card(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable {
                        onModes(
                            if (actif) {
                                if (modes.size > 1) modes - mode else modes
                            } else modes + mode
                        )
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
// ÉCRAN DE QUESTION
// ----------------------------------------------------------------------------

@Composable
fun EcranQuizPays(
    question: QuestionPays,
    total: Int,
    index: Int,
    onRepondu: (Boolean) -> Unit,
    onAbandon: () -> Unit
) {
    val p = question.pays
    val tous = ModePays.entries.filter { it.texte }
    var saisies by remember(question) {
        mutableStateOf<Map<ModePays, String>>(question.champs.associateWith { "" })
    }
    var verdicts by remember(question) { mutableStateOf<Map<ModePays, Boolean>>(emptyMap()) }
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
                EnonceQuestion("Localise le pays « ${p.nom} » sur la carte. (Pince pour zoomer)")
                Spacer(Modifier.height(8.dp))
                key(question) {
                    CarteMonde(
                        choix = choixCarte,
                        reponse = if (valide) p.iso3 else null,
                        onChoix = { choixCarte = it },
                        modifier = Modifier.weight(1f).fillMaxWidth()
                    )
                }
            }

            TypeQ.DRAPEAU -> Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                EnonceQuestion("Quel est le drapeau de « ${p.nom} » ?")
                Spacer(Modifier.height(8.dp))
                question.choix.chunked(2).forEach { ligne ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        ligne.forEach { c ->
                            val couleur = when {
                                valide && c.iso3 == p.iso3 -> Color(0xFF2E7D32)
                                valide && c.iso3 == choixQcm -> Color(0xFFC62828)
                                !valide && c.iso3 == choixQcm -> MaterialTheme.colorScheme.primary
                                else -> Color(0xFFBDBDBD)
                            }
                            val epais = if (couleur == Color(0xFFBDBDBD)) 1.dp else 4.dp
                            Box(
                                Modifier
                                    .weight(1f)
                                    .padding(vertical = 6.dp)
                                    .border(epais, couleur, RoundedCornerShape(10.dp))
                                    .clickable(enabled = !valide) { choixQcm = c.iso3 }
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

            TypeQ.CONTINENT -> Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                EnonceQuestion("Sur quel continent se trouve « ${p.nom} » ?")
                Spacer(Modifier.height(8.dp))
                CONTINENTS.forEach { c ->
                    val couleur = when {
                        valide && c == p.continent -> Color(0xFF2E7D32)
                        valide && c == choixQcm -> Color(0xFFC62828)
                        !valide && c == choixQcm -> MaterialTheme.colorScheme.primary
                        else -> Color(0xFFBDBDBD)
                    }
                    val epais = if (couleur == Color(0xFFBDBDBD)) 1.dp else 4.dp
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 5.dp)
                            .border(epais, couleur, RoundedCornerShape(10.dp))
                            .clickable(enabled = !valide) { choixQcm = c }
                            .padding(16.dp)
                    ) {
                        Text(c, fontSize = 18.sp)
                    }
                }
            }

            TypeQ.FICHE -> LazyColumn(Modifier.weight(1f)) {
                item {
                    Card(
                        Modifier
                            .fillMaxWidth()
                            .border(4.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp))
                    ) {
                        Column(Modifier.padding(14.dp)) {
                            Text("Complète la fiche", fontSize = 19.sp, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(6.dp))
                            Drapeau(p.iso2, Modifier.align(Alignment.CenterHorizontally), 90.dp)
                            Spacer(Modifier.height(6.dp))
                            tous.forEach { mode ->
                                if (mode !in question.champs) {
                                    LigneFiche(mode.nom, valeurPays(p, mode))
                                } else {
                                    ChampRempli(
                                        libelle = libelleChampPays(p, mode),
                                        valeur = saisies[mode] ?: "",
                                        onSaisie = { nvx -> saisies = saisies.toMutableMap().apply { put(mode, nvx) } },
                                        verrouille = valide,
                                        verdict = verdicts[mode],
                                        multiligne = false
                                    )
                                    if (verdicts[mode] == false) {
                                        Text(
                                            "Attendu : ${valeurPays(p, mode)}",
                                            fontSize = 13.sp,
                                            color = Color(0xFFC62828),
                                            modifier = Modifier.padding(start = 16.dp)
                                        )
                                    }
                                }
                            }
                            LigneFiche("Continent", p.continent)
                            Spacer(Modifier.height(12.dp))
                            Text("Localisation", fontSize = 14.sp, color = Color.Gray)
                            CarteMondeMini(p.iso3)
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(10.dp))

        if (!valide) {
            val pret = when (question.type) {
                TypeQ.CARTE -> choixCarte != null
                TypeQ.DRAPEAU, TypeQ.CONTINENT -> choixQcm != null
                TypeQ.FICHE -> question.champs.all { (saisies[it] ?: "").isNotBlank() }
            }
            Button(
                onClick = {
                    when (question.type) {
                        TypeQ.CARTE -> correct = choixCarte == p.iso3
                        TypeQ.DRAPEAU -> correct = choixQcm == p.iso3
                        TypeQ.CONTINENT -> correct = choixQcm == p.continent
                        TypeQ.FICHE -> {
                            val v = question.champs.associateWith { m -> champJustePays(p, m, saisies[m] ?: "") }
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
                    if (question.type == TypeQ.CARTE && choixCarte == null) "Touche un pays sur la carte" else "Valider",
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
                val choisi = PaysCache.obtenir(LocalContext.current).firstOrNull { it.iso3 == choixCarte }
                if (choisi != null) {
                    Text(
                        "Tu as choisi : ${choisi.nom}",
                        fontSize = 14.sp, color = Color.Gray,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
            }
            Button(
                onClick = { onRepondu(correct) },
                modifier = Modifier.fillMaxWidth().height(54.dp)
            ) {
                Text(if (index + 1 < total) "Suite" else "Voir les résultats", fontSize = 18.sp)
            }
        }
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun EnonceQuestion(texte: String) {
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
fun EcranResultatsPays(
    questions: List<QuestionPays>,
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
                        TypeQ.CONTINENT -> "Continent"
                        TypeQ.FICHE -> "Fiche"
                    }
                    Text("$type - ${q.pays.nom}", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    if (!ok && q.type == TypeQ.CONTINENT) {
                        Text("Attendu : ${q.pays.continent}", fontSize = 12.sp, color = Color.Gray)
                    }
                    if (!ok && q.type == TypeQ.FICHE) {
                        Text(
                            "Attendu : " + q.champs.joinToString(" | ") { "${it.nom} = ${valeurPays(q.pays, it)}" },
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
fun EcranChoixFiches(onRetour: () -> Unit, onDepartements: () -> Unit, onPays: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(20.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onRetour) { Text("← Menu") }
            Text("Fiches", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(12.dp))
        Button(onClick = onDepartements, Modifier.fillMaxWidth().height(60.dp)) {
            Text("Départements français", fontSize = 18.sp)
        }
        Spacer(Modifier.height(12.dp))
        Button(onClick = onPays, Modifier.fillMaxWidth().height(60.dp)) {
            Text("Pays du monde", fontSize = 18.sp)
        }
    }
}

@Composable
fun EcranFichesPays(onRetour: () -> Unit) {
    val ctx = LocalContext.current
    val tous = remember { PaysCache.obtenir(ctx) }
    var recherche by remember { mutableStateOf("") }
    var ouvert by remember { mutableStateOf<Pays?>(null) }

    val p = ouvert
    if (p != null) {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
            TextButton(onClick = { ouvert = null }) { Text("← Fiches") }
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Drapeau(p.iso2, Modifier.align(Alignment.CenterHorizontally), 110.dp)
                    Spacer(Modifier.height(8.dp))
                    Text(p.nom, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Text("Continent : ${p.continent}", fontSize = 15.sp)
                    Text("Capitale(s) : ${valeurPays(p, ModePays.CAPITALE)}", fontSize = 15.sp)
                    Text("Population : environ ${valeurPays(p, ModePays.POPULATION)} hab.", fontSize = 15.sp)
                    Text("Superficie (km²) : ${formaterNombre(p.superficie.toLong())}", fontSize = 15.sp)
                    Text("Langue(s) officielle(s) : ${valeurPays(p, ModePays.LANGUES)}", fontSize = 15.sp)
                    Text("Monnaie : ${valeurPays(p, ModePays.MONNAIE)}", fontSize = 15.sp)
                    Spacer(Modifier.height(12.dp))
                    Text("Localisation", fontSize = 14.sp, color = Color.Gray)
                    CarteMondeMini(p.iso3)
                }
            }
        }
    } else {
        Column(Modifier.fillMaxSize().padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = onRetour) { Text("← Fiches") }
                Text("Pays du monde", fontWeight = FontWeight.Bold, fontSize = 20.sp)
            }
            TextField(
                value = recherche,
                onValueChange = { recherche = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Rechercher (pays, capitale, continent)") },
                singleLine = true
            )
            Spacer(Modifier.height(8.dp))
            val q = normaliser(recherche)
            val filtres = tous.filter {
                q.isEmpty() || normaliser(it.nom).contains(q) || normaliser(it.continent).contains(q) ||
                    it.capitales.any { c -> normaliser(c).contains(q) } ||
                    it.nomsAcceptes.any { n -> normaliser(n).contains(q) }
            }
            LazyColumn(Modifier.fillMaxSize()) {
                items(filtres) { pays ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { ouvert = pays }
                    ) {
                        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Drapeau(pays.iso2, hauteur = 36.dp)
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(pays.nom, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                                Text(
                                    "${pays.continent} · capitale : ${pays.capitales.joinToString(", ")}",
                                    fontSize = 13.sp, color = Color.Gray
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
