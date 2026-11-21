// ============================================================================
// QUIZZ QPUC v2 - Thème : Départements français
// ============================================================================

package com.example.quizzqpuc

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.json.JSONArray
import org.json.JSONObject
import java.util.Locale
import kotlin.math.abs
import kotlin.math.min

// ----------------------------------------------------------------------------
// DONNÉES : 101 départements
// ----------------------------------------------------------------------------

data class Departement(
    val numero: String,
    val nom: String,
    val region: String,
    val chefLieu: String,
    val gentile: String,
    val populationK: Int,
    val superficieKm2: Int,
    val sousPrefs: List<String>,
    val imageUrl: String? = null
)

val REGION_Noms = mapOf(
    "ARA" to "Auvergne-Rhône-Alpes",
    "GE" to "Grand Est",
    "HDF" to "Hauts-de-France",
    "NA" to "Nouvelle-Aquitaine",
    "OCC" to "Occitanie",
    "PACA" to "Provence-Alpes-Côte d'Azur",
    "PDL" to "Pays de la Loire",
    "NOR" to "Normandie",
    "BRE" to "Bretagne",
    "CVL" to "Centre-Val de Loire",
    "BFC" to "Bourgogne-Franche-Comté",
    "IDF" to "Île-de-France",
    "COR" to "Corse",
    "DOM" to "Outre-mer"
)

val DEPARTEMENTS = listOf(
    Departement("01", "Ain", "ARA", "Bourg-en-Bresse", "Aindinois", 652, 5762, listOf("Belley", "Gex", "Nantua", "Valserhône")),
    Departement("02", "Aisne", "HDF", "Laon", "Axonais", 531, 7369, listOf("Château-Thierry", "Saint-Quentin", "Soissons", "Vervins")),
    Departement("03", "Allier", "ARA", "Moulins", "Bourbonnais", 335, 7340, listOf("Commentry", "Montluçon", "Vichy")),
    Departement("04", "Alpes-de-Haute-Provence", "PACA", "Digne-les-Bains", "Bas-Alpins", 164, 6925, listOf("Barcelonnette", "Castellane", "Forcalquier")),
    Departement("05", "Hautes-Alpes", "PACA", "Gap", "Alpins", 142, 5549, listOf("Briançon")),
    Departement("06", "Alpes-Maritimes", "PACA", "Nice", "Maralpins", 1094, 4299, listOf("Grasse")),
    Departement("07", "Ardèche", "ARA", "Privas", "Ardéchois", 328, 5529, listOf("Tournon-sur-Rhône")),
    Departement("08", "Ardennes", "GE", "Charleville-Mézières", "Ardennais", 274, 5229, listOf("Rethel", "Sedan", "Vouziers")),
    Departement("09", "Ariège", "OCC", "Foix", "Ariégeois", 154, 4890, listOf("Pamiers", "Saint-Girons")),
    Departement("10", "Aube", "GE", "Troyes", "Aubois", 311, 6004, listOf("Bar-sur-Aube", "Nogent-sur-Seine")),
    Departement("11", "Aude", "OCC", "Carcassonne", "Audois", 376, 6139, listOf("Limoux", "Narbonne")),
    Departement("12", "Aveyron", "OCC", "Rodez", "Aveyronnais", 279, 8735, listOf("Millau", "Villefranche-de-Rouergue")),
    Departement("13", "Bouches-du-Rhône", "PACA", "Marseille", "Bucco-Rhodaniens", 2039, 5087, listOf("Aix-en-Provence", "Arles", "Istres")),
    Departement("14", "Calvados", "NOR", "Caen", "Calvadosiens", 697, 5548, listOf("Bayeux", "Falaise", "Lisieux", "Vire")),
    Departement("15", "Cantal", "ARA", "Aurillac", "Cantaliens", 144, 5726, listOf("Mauriac", "Saint-Flour")),
    Departement("16", "Charente", "NA", "Angoulême", "Charentais", 351, 5956, listOf("Cognac", "Confolens", "Ruffec")),
    Departement("17", "Charente-Maritime", "NA", "La Rochelle", "Charentais-Maritimes", 665, 6864, listOf("Jonzac", "Rochefort", "Saintes", "Saint-Jean-d'Angély")),
    Departement("18", "Cher", "CVL", "Bourges", "Berruyers", 300, 7235, listOf("Saint-Amand-Montrond", "Vierzon")),
    Departement("19", "Corrèze", "NA", "Tulle", "Corréziens", 240, 5857, listOf("Brive-la-Gaillarde", "Ussel")),
    Departement("2A", "Corse-du-Sud", "COR", "Ajaccio", "Sud-Corses", 158, 4014, listOf("Porto-Vecchio", "Sartène")),
    Departement("2B", "Haute-Corse", "COR", "Bastia", "Haut-Corses", 187, 4666, listOf("Calvi", "Corte")),
    Departement("21", "Côte-d'Or", "BFC", "Dijon", "Dijonnais", 537, 8763, listOf("Beaune", "Montbard", "Semur-en-Auxois")),
    Departement("22", "Côtes-d'Armor", "BRE", "Saint-Brieuc", "Costarmoricains", 604, 6878, listOf("Dinan", "Guingamp", "Lannion")),
    Departement("23", "Creuse", "NA", "Guéret", "Creusois", 117, 5565, listOf("Aubusson")),
    Departement("24", "Dordogne", "NA", "Périgueux", "Périgordins", 414, 9060, listOf("Bergerac", "Nontron", "Sarlat-la-Canéda")),
    Departement("25", "Doubs", "BFC", "Besançon", "Doubistes", 535, 5232, listOf("Montbéliard", "Pontarlier")),
    Departement("26", "Drôme", "ARA", "Valence", "Drômois", 519, 6530, listOf("Die", "Nyons")),
    Departement("27", "Eure", "NOR", "Évreux", "Eurois", 602, 6040, listOf("Les Andelys", "Bernay", "Louviers", "Pont-Audemer")),
    Departement("28", "Eure-et-Loir", "CVL", "Chartres", "Euréliens", 431, 5880, listOf("Châteaudun", "Dreux", "Nogent-le-Rotrou")),
    Departement("29", "Finistère", "BRE", "Quimper", "Finistériens", 919, 6733, listOf("Brest", "Châteaulin", "Morlaix")),
    Departement("30", "Gard", "OCC", "Nîmes", "Gardois", 751, 5853, listOf("Alès", "Le Vigan")),
    Departement("31", "Haute-Garonne", "OCC", "Toulouse", "Haut-Garonnais", 1404, 6309, listOf("Muret", "Saint-Gaudens")),
    Departement("32", "Gers", "OCC", "Auch", "Gersois", 192, 6257, listOf("Condom", "L'Isle-Jourdain", "Mirande")),
    Departement("33", "Gironde", "NA", "Bordeaux", "Bordelais", 1655, 10024, listOf("Blaye", "Langon", "Lesparre-Médoc", "Libourne")),
    Departement("34", "Hérault", "OCC", "Montpellier", "Héraultais", 1143, 6101, listOf("Béziers", "Lodève")),
    Departement("35", "Ille-et-Vilaine", "BRE", "Rennes", "Ille-Vilainais", 1074, 6775, listOf("Fougères", "Redon", "Saint-Malo")),
    Departement("36", "Indre", "CVL", "Châteauroux", "Castelroussins", 217, 6883, listOf("Le Blanc", "La Châtre", "Issoudun")),
    Departement("37", "Indre-et-Loire", "CVL", "Tours", "Tourangeaux", 611, 6127, listOf("Chinon", "Loches")),
    Departement("38", "Isère", "ARA", "Grenoble", "Isérois", 1275, 7431, listOf("La Tour-du-Pin", "Vienne")),
    Departement("39", "Jura", "BFC", "Lons-le-Saunier", "Jurassiens", 259, 4999, listOf("Dole", "Saint-Claude")),
    Departement("40", "Landes", "NA", "Mont-de-Marsan", "Landais", 420, 9243, listOf("Dax", "Saint-Sever")),
    Departement("41", "Loir-et-Cher", "CVL", "Blois", "Blésois", 333, 6314, listOf("Romorantin-Lanthenay", "Vendôme")),
    Departement("42", "Loire", "ARA", "Saint-Étienne", "Stéphanois", 767, 4781, listOf("Montbrison", "Roanne")),
    Departement("43", "Haute-Loire", "ARA", "Le Puy-en-Velay", "Altiligériens", 227, 4977, listOf("Brioude", "Yssingeaux")),
    Departement("44", "Loire-Atlantique", "PDL", "Nantes", "Nantais", 1459, 7156, listOf("Ancenis-Saint-Géréon", "Châteaubriant", "Saint-Nazaire")),
    Departement("45", "Loiret", "CVL", "Orléans", "Orléanais", 683, 6775, listOf("Montargis", "Pithiviers")),
    Departement("46", "Lot", "OCC", "Cahors", "Lotois", 172, 5217, listOf("Figeac", "Gourdon")),
    Departement("47", "Lot-et-Garonne", "NA", "Agen", "Agenais", 333, 5362, listOf("Marmande", "Nérac", "Villeneuve-sur-Lot")),
    Departement("48", "Lozère", "OCC", "Mende", "Lozériens", 76, 5167, listOf("Florac")),
    Departement("49", "Maine-et-Loire", "PDL", "Angers", "Angevins", 817, 7136, listOf("Baugé", "Cholet", "Saumur", "Segré")),
    Departement("50", "Manche", "NOR", "Saint-Lô", "Manchois", 494, 5938, listOf("Avranches", "Cherbourg-en-Cotentin", "Coutances", "Granville")),
    Departement("51", "Marne", "GE", "Châlons-en-Champagne", "Marnais", 556, 8162, listOf("Épernay", "Reims", "Sainte-Menehould", "Vitry-le-François")),
    Departement("52", "Haute-Marne", "GE", "Chaumont", "Chaumontais", 171, 6211, listOf("Langres", "Saint-Dizier")),
    Departement("53", "Mayenne", "PDL", "Laval", "Lavallois", 309, 5175, listOf("Château-Gontier", "Mayenne")),
    Departement("54", "Meurthe-et-Moselle", "GE", "Nancy", "Nancéiens", 734, 5246, listOf("Lunéville", "Toul")),
    Departement("55", "Meuse", "GE", "Bar-le-Duc", "Barisiens", 184, 6211, listOf("Commercy", "Verdun")),
    Departement("56", "Morbihan", "BRE", "Vannes", "Vannetais", 772, 6823, listOf("Lorient", "Ploërmel", "Pontivy")),
    Departement("57", "Moselle", "GE", "Metz", "Messins", 1049, 6216, listOf("Sarreguemines", "Sarrebourg", "Thionville")),
    Departement("58", "Nièvre", "BFC", "Nevers", "Nivernais", 203, 6817, listOf("Château-Chinon", "Clamecy", "Cosne-Cours-sur-Loire")),
    Departement("59", "Nord", "HDF", "Lille", "Nordistes", 2612, 5743, listOf("Avesnes-sur-Helpe", "Cambrai", "Douai", "Dunkerque", "Valenciennes")),
    Departement("60", "Oise", "HDF", "Beauvais", "Oisiens", 829, 5860, listOf("Compiègne", "Senlis")),
    Departement("61", "Orne", "NOR", "Alençon", "Ornais", 279, 6103, listOf("Argentan", "Domfront", "Mortagne-au-Perche")),
    Departement("62", "Pas-de-Calais", "HDF", "Arras", "Pas-de-Calaisiens", 1467, 6671, listOf("Béthune", "Boulogne-sur-Mer", "Calais", "Lens", "Montreuil", "Saint-Omer")),
    Departement("63", "Puy-de-Dôme", "ARA", "Clermont-Ferrand", "Clermontois", 668, 7970, listOf("Ambert", "Issoire", "Riom", "Thiers")),
    Departement("64", "Pyrénées-Atlantiques", "NA", "Pau", "Pyrénnéens-Atlantiques", 683, 7645, listOf("Bayonne", "Oloron-Sainte-Marie")),
    Departement("65", "Hautes-Pyrénées", "OCC", "Tarbes", "Haut-Pyrénéens", 231, 4464, listOf("Argelès-Gazost", "Bagnères-de-Bigorre")),
    Departement("66", "Pyrénées-Orientales", "OCC", "Perpignan", "Perpignanais", 487, 4116, listOf("Céret", "Prades")),
    Departement("67", "Bas-Rhin", "GE", "Strasbourg", "Strasbourgeois", 1137, 4775, listOf("Haguenau", "Molsheim", "Saverne", "Sélestat")),
    Departement("68", "Haut-Rhin", "GE", "Colmar", "Haut-Rhinois", 761, 3525, listOf("Altkirch", "Guebwiller", "Mulhouse", "Thann")),
    Departement("69", "Rhône", "ARA", "Lyon", "Lyonnais", 1881, 5316, listOf("Villefranche-sur-Saône")),
    Departement("70", "Haute-Saône", "BFC", "Vesoul", "Haut-Saônois", 236, 5360, listOf("Gray", "Lure")),
    Departement("71", "Saône-et-Loire", "BFC", "Mâcon", "Mâconnais", 543, 8575, listOf("Autun", "Chalon-sur-Saône", "Charolles", "Louhans")),
    Departement("72", "Sarthe", "PDL", "Le Mans", "Mancelles", 568, 6206, listOf("La Flèche", "Mamers")),
    Departement("73", "Savoie", "ARA", "Chambéry", "Savoyards", 440, 6171, listOf("Albertville", "Saint-Jean-de-Maurienne")),
    Departement("74", "Haute-Savoie", "ARA", "Annecy", "Haut-Savoyards", 837, 4388, listOf("Bonneville", "Saint-Julien-en-Genevois", "Thonon-les-Bains")),
    Departement("75", "Paris", "IDF", "Paris", "Parisiens", 2148, 105, emptyList()),
    Departement("76", "Seine-Maritime", "NOR", "Rouen", "Rouennais", 1255, 6328, listOf("Dieppe", "Le Havre")),
    Departement("77", "Seine-et-Marne", "IDF", "Melun", "Melunais", 1450, 5915, listOf("Fontainebleau", "Meaux", "Provins", "Torcy")),
    Departement("78", "Yvelines", "IDF", "Versailles", "Versaillais", 1451, 2284, listOf("Mantes-la-Jolie", "Rambouillet", "Saint-Germain-en-Laye")),
    Departement("79", "Deux-Sèvres", "NA", "Niort", "Niortais", 375, 5999, listOf("Bressuire", "Thouars")),
    Departement("80", "Somme", "HDF", "Amiens", "Amiénois", 570, 6170, listOf("Abbeville", "Montdidier", "Péronne")),
    Departement("81", "Tarn", "OCC", "Albi", "Albigeois", 393, 5758, listOf("Castres", "Gaillac")),
    Departement("82", "Tarn-et-Garonne", "OCC", "Montauban", "Montalbanais", 261, 3718, listOf("Castelsarrasin")),
    Departement("83", "Var", "PACA", "Toulon", "Toulonnais", 1081, 5975, listOf("Brignoles", "Draguignan")),
    Departement("84", "Vaucluse", "PACA", "Avignon", "Vauclusiens", 569, 3567, listOf("Apt", "Carpentras")),
    Departement("85", "Vendée", "PDL", "La Roche-sur-Yon", "Yonnais", 701, 6720, listOf("Fontenay-le-Comte", "Les Sables-d'Olonne")),
    Departement("86", "Vienne", "NA", "Poitiers", "Poitevins", 439, 6990, listOf("Châtellerault", "Chauvigny", "Montmorillon")),
    Departement("87", "Haute-Vienne", "NA", "Limoges", "Limougeauds", 373, 5520, listOf("Bellac", "Rochechouart", "Saint-Yrieix-la-Perche")),
    Departement("88", "Vosges", "GE", "Épinal", "Vosgiens", 364, 5874, listOf("Mirecourt", "Neufchâteau", "Remiremont", "Saint-Dié-des-Vosges")),
    Departement("89", "Yonne", "BFC", "Auxerre", "Icaunais", 336, 7427, listOf("Avallon", "Migennes", "Sens", "Tonnerre")),
    Departement("90", "Territoire de Belfort", "BFC", "Belfort", "Belfortains", 141, 609, listOf("Delle")),
    Departement("91", "Essonne", "IDF", "Évry-Courcouronnes", "Essonniens", 1305, 1804, listOf("Étampes", "Palaiseau")),
    Departement("92", "Hauts-de-Seine", "IDF", "Nanterre", "Altoséquanais", 1627, 176, listOf("Antony", "Boulogne-Billancourt")),
    Departement("93", "Seine-Saint-Denis", "IDF", "Bobigny", "Séquano-Dionysiens", 1664, 236, listOf("Le Raincy", "Montreuil")),
    Departement("94", "Val-de-Marne", "IDF", "Créteil", "Créteilais", 1427, 245, listOf("L'Haÿ-les-Roses", "Nogent-sur-Marne")),
    Departement("95", "Val-d'Oise", "IDF", "Cergy", "Val-d'Oisiens", 1261, 1246, listOf("Argenteuil", "Sarcelles")),
    Departement("971", "Guadeloupe", "DOM", "Basse-Terre", "Guadeloupéens", 379, 1628, listOf("Pointe-à-Pitre")),
    Departement("972", "Martinique", "DOM", "Fort-de-France", "Martiniquais", 357, 1128, listOf("Le Marin", "La Trinité")),
    Departement("973", "Guyane", "DOM", "Cayenne", "Guyanais", 292, 83534, listOf("Kourou", "Saint-Laurent-du-Maroni")),
    Departement("974", "La Réunion", "DOM", "Saint-Denis", "Réunionnais", 871, 2504, listOf("Saint-Benoît", "Saint-Paul", "Saint-Pierre")),
    Departement("976", "Mayotte", "DOM", "Mamoudzou", "Mahorais", 321, 374, listOf("Dzaoudzi"))
)

// ----------------------------------------------------------------------------
// CORRECTION SOUPLE
// ----------------------------------------------------------------------------

fun normaliser(s: String): String = s
    .lowercase(Locale.FRENCH)
    .replace(Regex("[àáâãäå]"), "a").replace("ç", "c")
    .replace(Regex("[èéêë]"), "e").replace(Regex("[ìíîï]"), "i")
    .replace("ñ", "n").replace(Regex("[òóôõö]"), "o").replace(Regex("[ùúûü]"), "u")
    .replace("œ", "oe").replace("ÿ", "y")
    .replace(Regex("[^a-z0-9]"), "")

fun levenshtein(a: String, b: String): Int {
    var m = IntArray(b.length + 1) { it }
    var n = IntArray(b.length + 1)
    for (i in 1..a.length) {
        n[0] = i
        for (j in 1..b.length) {
            n[j] = minOf(
                m[j] + 1,
                n[j - 1] + 1,
                m[j - 1] + if (a[i - 1] == b[j - 1]) 0 else 1
            )
        }
        val tmp = m; m = n; n = tmp
    }
    return m[b.length]
}

fun correspond(reponse: String, attendu: String): Boolean {
    val r = normaliser(reponse)
    val a = normaliser(attendu)
    if (r.isEmpty()) return false
    val tol = when {
        a.length <= 4 -> 0
        a.length <= 8 -> 1
        else -> 2
    }
    val dist = levenshtein(r, a)
    return dist <= tol ||
        (a.length > 6 && r.length > 6 && (r.contains(a) || a.contains(r)) && dist <= 3)
}

fun correctFlechie(reponse: String, candidats: List<String>) =
    candidats.any { correspond(reponse, it) }

    // ----------------------------------------------------------------------------
// MODES ET NAVIGATION
// ----------------------------------------------------------------------------

// Tolérance acceptée sur le nombre d'habitants et sur la superficie
const val TOLERANCE_POURCENT = 10
const val TOL_TXT = "±$TOLERANCE_POURCENT %"

/**
 * label        : texte dans l'écran des paramètres
 * libelleChamp : étiquette du champ de saisie (peut contenir un retour à la ligne)
 * nom          : intitulé court, utilisé pour les lignes déjà remplies et les résultats
 */
enum class Mode(val label: String, val libelleChamp: String, val nom: String) {
    NOM("Le nom (n° -> nom)", "Nom du département", "Nom"),
    NUMERO("Le numéro (nom -> n°)", "Numéro", "Numéro"),
    REGION("La région", "Région", "Région"),
    GENTILE("Le gentilé", "Gentilé (les ...)", "Gentilé"),
    HABITANTS("Nb d'habitants ($TOL_TXT)", "Habitants\n(ex : 500000, $TOL_TXT)", "Habitants"),
    SUPERFICIE("Superficie ($TOL_TXT)", "Superficie (km²)\n($TOL_TXT)", "Superficie (km²)"),
    CHEF_LIEU("Le chef-lieu", "Chef-lieu", "Chef-lieu"),
    SOUS_PREF("Sous-préfectures", "Sous-préfecture(s)", "Sous-préfecture(s)"),
    LOCALISATION("La localisation (carte)", "Localisation", "Localisation")
}

enum class Ecran { MENU, LISTE_QUIZ, PARAMS, QUIZ, RESULTATS, FICHES }

data class Question(
    val dept: Departement,
    val carte: Boolean,
    val champs: List<Mode> = emptyList()
)

val CATEGORIES = listOf(
    Categorie("departements", "Départements français", true),
    Categorie("rois", "Rois de France", false),
    Categorie("usa", "États américains", false),
    Categorie("capitales", "Capitales du monde", false)
)

data class Categorie(val id: String, val titre: String, val dispo: Boolean)

// ----------------------------------------------------------------------------
// ACTIVITÉ + NAVIGATION
// ----------------------------------------------------------------------------

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { QuizzQpucApp() }
    }
}

@Composable
fun QuizzQpucApp() {
    var ecran by remember { mutableStateOf(Ecran.MENU) }
    var categorie by remember { mutableStateOf(CATEGORIES[0]) }

    var nbQuestions by remember { mutableStateOf(20f) }
    var modesActifs by remember { mutableStateOf(Mode.entries.toList()) }

    var questions by remember { mutableStateOf(listOf<Question>()) }
    var index by remember { mutableStateOf(0) }
    var reponses by remember { mutableStateOf(listOf<Boolean>()) }

    fun demarrer() {
        questions = genererQuestions(nbQuestions.toInt(), modesActifs)
        index = 0
        reponses = emptyList()
        ecran = Ecran.QUIZ
    }

    MaterialTheme {
        Surface(Modifier.fillMaxSize()) {
            when (ecran) {
                Ecran.MENU -> EcranMenu(
                    onJouer = { ecran = Ecran.LISTE_QUIZ },
                    onFiches = { ecran = Ecran.FICHES }
                )
                Ecran.LISTE_QUIZ -> EcranListeQuiz(
                    onRetour = { ecran = Ecran.MENU },
                    onChoix = { c ->
                        categorie = c
                        if (c.dispo) ecran = Ecran.PARAMS
                    }
                )
                Ecran.PARAMS -> when (categorie.id) {
                    "departements" -> EcranParamsDepartements(
                        nbQuestions,
                        { nbQuestions = it },
                        modesActifs,
                        { modesActifs = it },
                        onDemarrer = { demarrer() }
                    )
                    else -> {}
                }
                Ecran.QUIZ -> EcranQuiz(
                    question = questions[index],
                    total = questions.size,
                    index = index,
                    onRepondu = { correct ->
                        reponses = reponses + correct
                        if (index + 1 < questions.size) index++ else ecran = Ecran.RESULTATS
                    },
                    onAbandon = { ecran = Ecran.LISTE_QUIZ }
                )
                Ecran.RESULTATS -> EcranResultats(
                    questions, reponses,
                    onRef = { demarrer() },
                    onMenu = { ecran = Ecran.LISTE_QUIZ }
                )
                Ecran.FICHES -> EcranFiches(onRetour = { ecran = Ecran.MENU })
            }
        }
    }
}

// ----------------------------------------------------------------------------
// MENU GÉNÉRAL
// ----------------------------------------------------------------------------

@Composable
fun EcranMenu(onJouer: () -> Unit, onFiches: () -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Quizz QPUC", fontSize = 36.sp, fontWeight = FontWeight.Bold)
        Text("Entraînement aux quiz de culture générale", fontSize = 15.sp, color = Color.Gray)
        Spacer(Modifier.height(48.dp))
        Button(onClick = onJouer, Modifier.fillMaxWidth().height(64.dp)) {
            Text("Jouer", fontSize = 22.sp)
        }
        Spacer(Modifier.height(16.dp))
        OutlinedButton(onClick = onFiches, Modifier.fillMaxWidth().height(64.dp)) {
            Text("Fiches", fontSize = 22.sp)
        }
    }
}

@Composable
fun EcranListeQuiz(onRetour: () -> Unit, onChoix: (Categorie) -> Unit) {
    Column(Modifier.fillMaxSize().padding(20.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onRetour) { Text("← Menu") }
            Text("Choisis un quiz", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(12.dp))
        CATEGORIES.forEach { c ->
            val titre = if (c.dispo) c.titre else "${c.titre} (à venir...)"
            Button(
                onClick = { onChoix(c) },
                enabled = c.dispo,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .padding(vertical = 4.dp)
            ) {
                Text(titre, fontSize = 18.sp)
            }
            Spacer(Modifier.height(4.dp))
        }
    }
}

// ----------------------------------------------------------------------------
// PARAMÈTRES DU QUIZ DÉPARTEMENTS
// ----------------------------------------------------------------------------

/** Champs à remplir pour un département (Paris n'a pas de sous-préfecture, par exemple). */
fun champsPour(d: Departement, modes: List<Mode>): List<Mode> =
    modes.filter { it != Mode.LOCALISATION && !(it == Mode.SOUS_PREF && d.sousPrefs.isEmpty()) }

fun genererQuestions(nb: Int, modes: List<Mode>): List<Question> {
    val liste = mutableListOf<Question>()
    val avecChamps = modes.any { it != Mode.LOCALISATION }
    repeat(nb) { i ->
        val carte = Mode.LOCALISATION in modes && (!avecChamps || i % 5 == 0)
        if (carte) {
            liste.add(Question(DEPARTEMENTS.random(), true, emptyList()))
        } else {
            // on ne tire que des départements pour lesquels il reste au moins un champ à remplir
            val dept = DEPARTEMENTS.filter { champsPour(it, modes).isNotEmpty() }.random()
            liste.add(Question(dept, false, champsPour(dept, modes)))
        }
    }
    return liste
}

@Composable
fun EcranParamsDepartements(
    nbQuestions: Float,
    onNbQuestions: (Float) -> Unit,
    modesActifs: List<Mode>,
    onModesActifs: (List<Mode>) -> Unit,
    onDemarrer: () -> Unit
) {
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
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
        Text("Champs à remplir dans la fiche", fontSize = 18.sp, fontWeight = FontWeight.Bold)

        Mode.entries.forEach { mode ->
            val actif = mode in modesActifs
            Card(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable {
                        onModesActifs(
                            if (actif) {
                                if (modesActifs.size > 1) modesActifs - mode else modesActifs
                            } else modesActifs + mode
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
// FICHE RÉUTILISABLE (questions ET révisions)
// ----------------------------------------------------------------------------

@Composable
fun ChampRempli(
    libelle: String,
    valeur: String,
    onSaisie: (String) -> Unit,
    verrouille: Boolean,
    verdict: Boolean? = null
) {
    TextField(
        value = valeur,
        onValueChange = { if (!verrouille) onSaisie(it) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        label = { Text(libelle, fontSize = 13.sp) },
        singleLine = true,
        enabled = !verrouille,
        isError = verdict == false,
        supportingText = {
            when (verdict) {
                true -> Text("Juste !", color = Color(0xFF2E7D32), fontSize = 12.sp)
                false -> Text("Faux", color = Color(0xFFC62828), fontSize = 12.sp)
                null -> {}
            }
        }
    )
}

@Composable
fun LigneFiche(libelle: String, valeur: String) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp)
    ) {
        Text(libelle, fontSize = 14.sp, color = Color.Gray, modifier = Modifier.width(150.dp))
        Text(valeur, fontSize = 15.sp, modifier = Modifier.weight(1f))
    }
}

fun valeurFiche(d: Departement, mode: Mode): String = when (mode) {
    Mode.NOM -> d.nom
    Mode.NUMERO -> d.numero
    Mode.REGION -> REGION_Noms[d.region] ?: d.region
    Mode.GENTILE -> d.gentile
    Mode.HABITANTS -> (d.populationK * 1000).toString()
    Mode.SUPERFICIE -> d.superficieKm2.toString()
    Mode.CHEF_LIEU -> d.chefLieu
    Mode.SOUS_PREF -> d.sousPrefs.joinToString(", ").ifEmpty { "Aucune" }
    Mode.LOCALISATION -> ""
}

/**
 * Corrige les réponses « sous-préfectures » : une case par sous-préfecture, dans n'importe quel ordre.
 * Chaque réponse doit correspondre à une sous-préfecture différente. Renvoie un verdict par case.
 */
fun verdictsSousPrefs(attendues: List<String>, reponses: List<String>): List<Boolean> {
    val restantes = attendues.toMutableList()
    return reponses.map { rep ->
        val i = restantes.indexOfFirst { correspond(rep, it) }
        if (i >= 0) {
            restantes.removeAt(i)
            true
        } else false
    }
}

/** Vrai si `v` est à moins de TOLERANCE_POURCENT % de `cible`. */
fun dansTolerance(v: Int, cible: Int): Boolean =
    v > 0 && abs(v - cible) <= cible * (TOLERANCE_POURCENT / 100.0)

fun champJuste(mode: Mode, d: Departement, reponse: String): Boolean = when (mode) {
    Mode.NOM -> correctFlechie(reponse, listOf(d.nom))
    Mode.NUMERO -> normaliser(reponse) == normaliser(d.numero)
    Mode.REGION -> correctFlechie(reponse, listOf(REGION_Noms[d.region] ?: d.region))
    Mode.GENTILE -> correctFlechie(reponse, listOf(d.gentile))
    Mode.HABITANTS -> dansTolerance(reponse.filter { it.isDigit() }.toIntOrNull() ?: 0, d.populationK * 1000)
    Mode.SUPERFICIE -> dansTolerance(reponse.filter { it.isDigit() }.toIntOrNull() ?: 0, d.superficieKm2)
    Mode.CHEF_LIEU -> correctFlechie(reponse, listOf(d.chefLieu))
    Mode.SOUS_PREF -> false      // corrigé case par case par verdictsSousPrefs()
    Mode.LOCALISATION -> false
}

// ----------------------------------------------------------------------------
// ÉCRAN QUIZ : fiche à trous (ou carte pour la localisation)
// ----------------------------------------------------------------------------

@Composable
fun EcranQuiz(
    question: Question,
    total: Int,
    index: Int,
    onRepondu: (Boolean) -> Unit,
    onAbandon: () -> Unit
) {
    var saisies by remember(question) {
        mutableStateOf<Map<Mode, String>>(question.champs.associateWith { "" })
    }
    // une case par sous-préfecture du département
    var saisiesSP by remember(question) {
        mutableStateOf(List(question.dept.sousPrefs.size) { "" })
    }
    var verdictsSP by remember(question) { mutableStateOf<List<Boolean>>(emptyList()) }
    var verdicts by remember(question) { mutableStateOf<Map<Mode, Boolean>>(emptyMap()) }
    var choixCarte by remember(question) { mutableStateOf<String?>(null) }
    var carteValidee by remember(question) { mutableStateOf(false) }
    var correct by remember(question) { mutableStateOf(false) }
    val aValider = verdicts.isNotEmpty() || carteValidee

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Question ${index + 1} / $total", fontWeight = FontWeight.Bold)
            TextButton(onClick = onAbandon) { Text("Abandon") }
        }
        LinearProgressIndicator(
            progress = { (index + 1f) / total },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))

        if (question.carte) {
            Card(
                Modifier
                    .fillMaxWidth()
                    .border(4.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp))
            ) {
                Text(
                    "Où se trouve ${question.dept.nom} ? Touche son département sur la carte (pince pour zoomer).",
                    fontSize = 18.sp, fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(14.dp)
                )
            }
            Spacer(Modifier.height(8.dp))
            key(question) {
                CarteFrance(
                    choix = choixCarte,
                    reponse = if (carteValidee) question.dept.numero else null,
                    onChoix = { choixCarte = it },
                    modifier = Modifier.weight(1f).fillMaxWidth()
                )
            }
        } else {
            LazyColumn(Modifier.weight(1f)) {
                item {
                    Card(
                        Modifier
                            .fillMaxWidth()
                            .border(4.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp))
                    ) {
                        Column(Modifier.padding(14.dp)) {
                            Text("Complète la fiche", fontSize = 19.sp, fontWeight = FontWeight.Bold)
                            if (question.dept.imageUrl != null) {
                                ImageUrl(question.dept.imageUrl, "Illustration")
                            }
                            val tous = listOf(
                                Mode.NUMERO, Mode.NOM, Mode.REGION, Mode.GENTILE,
                                Mode.HABITANTS, Mode.SUPERFICIE, Mode.CHEF_LIEU, Mode.SOUS_PREF
                            )
                            tous.forEach { mode ->
                                if (mode !in question.champs) {
                                    LigneFiche(mode.nom, valeurFiche(question.dept, mode))
                                } else if (mode == Mode.SOUS_PREF) {
                                    val n = question.dept.sousPrefs.size
                                    for (i in 0 until n) {
                                        ChampRempli(
                                            libelle = if (n == 1) mode.libelleChamp
                                            else "${mode.libelleChamp} (${i + 1}/$n)",
                                            valeur = saisiesSP.getOrElse(i) { "" },
                                            onSaisie = { nvx ->
                                                saisiesSP = saisiesSP.toMutableList().also { it[i] = nvx }
                                            },
                                            verrouille = verdicts.isNotEmpty(),
                                            verdict = verdictsSP.getOrNull(i)
                                        )
                                    }
                                    if (verdicts[mode] == false) {
                                        Text(
                                            "Attendu : ${valeurFiche(question.dept, mode)}",
                                            fontSize = 13.sp,
                                            color = Color(0xFFC62828),
                                            modifier = Modifier.padding(start = 16.dp)
                                        )
                                    }
                                } else {
                                    ChampRempli(
                                        libelle = mode.libelleChamp,
                                        valeur = saisies[mode] ?: "",
                                        onSaisie = { nvx ->
                                            saisies = saisies.toMutableMap().apply { put(mode, nvx) }
                                        },
                                        verrouille = verdicts.isNotEmpty(),
                                        verdict = verdicts[mode]
                                    )
                                    if (verdicts[mode] == false) {
                                        Text(
                                            "Attendu : ${valeurFiche(question.dept, mode)}",
                                            fontSize = 13.sp,
                                            color = Color(0xFFC62828),
                                            modifier = Modifier.padding(start = 16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(10.dp))

        if (!aValider) {
            Button(
                onClick = {
                    if (question.carte) {
                        carteValidee = true
                        correct = choixCarte == question.dept.numero
                    } else {
                        val vsp = verdictsSousPrefs(question.dept.sousPrefs, saisiesSP)
                        verdictsSP = vsp
                        val nouveaux = question.champs.associateWith { m ->
                            if (m == Mode.SOUS_PREF) vsp.all { it }
                            else champJuste(m, question.dept, saisies[m] ?: "")
                        }
                        verdicts = nouveaux
                        correct = nouveaux.values.all { it }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(54.dp),
                enabled = if (question.carte) choixCarte != null
                else question.champs.all { m ->
                    if (m == Mode.SOUS_PREF) saisiesSP.all { it.isNotBlank() }
                    else (saisies[m] ?: "").isNotBlank()
                }
            ) {
                Text(
                    if (question.carte && choixCarte == null) "Touche un département sur la carte" else "Valider",
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
            if (question.carte && !correct) {
                val choisi = DEPARTEMENTS.firstOrNull { it.numero == choixCarte }
                if (choisi != null) {
                    Text(
                        "Tu as choisi : ${choisi.numero} ${choisi.nom}",
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

// ----------------------------------------------------------------------------
// CARTE : vrais contours des départements (asset carte_france.json).
// Régions entourées de noir, départements en gris, aucun nom affiché.
// Un appui choisit le département touché ; la validation se fait avec le bouton.
// ----------------------------------------------------------------------------

private const val ZOOM_MAX = 12f
private val GRIS_DEPT = Color(0xFFC8C8C8)
private val GRIS_LIMITE = Color(0xFF8C8C8C)
private val BLEU_CHOIX = Color(0xFF42A5F5)
private val VERT_BON = Color(0xFF66BB6A)
private val ROUGE_FAUX = Color(0xFFEF5350)

/** Un département : ses anneaux (x0,y0,x1,y1,...), son tracé et sa boîte englobante. */
class FormeDept(val numero: String, val anneaux: List<FloatArray>) {
    val chemin = Path()
    private val minX: Float
    private val minY: Float
    private val maxX: Float
    private val maxY: Float

    init {
        chemin.fillType = PathFillType.EvenOdd   // les enclaves (trous) restent vides
        var x0 = Float.MAX_VALUE
        var y0 = Float.MAX_VALUE
        var x1 = -Float.MAX_VALUE
        var y1 = -Float.MAX_VALUE
        for (a in anneaux) {
            ajouterAnneau(chemin, a)
            var i = 0
            while (i < a.size) {
                if (a[i] < x0) x0 = a[i]
                if (a[i] > x1) x1 = a[i]
                if (a[i + 1] < y0) y0 = a[i + 1]
                if (a[i + 1] > y1) y1 = a[i + 1]
                i += 2
            }
        }
        minX = x0; minY = y0; maxX = x1; maxY = y1
    }

    /** Point dans le département ? (règle pair/impair sur tous les anneaux) */
    fun contient(x: Float, y: Float): Boolean {
        if (x < minX || x > maxX || y < minY || y > maxY) return false
        var dedans = false
        for (a in anneaux) {
            val n = a.size / 2
            var j = n - 1
            for (i in 0 until n) {
                val xi = a[2 * i]
                val yi = a[2 * i + 1]
                val xj = a[2 * j]
                val yj = a[2 * j + 1]
                if ((yi > y) != (yj > y) && x < (xj - xi) * (y - yi) / (yj - yi) + xi) dedans = !dedans
                j = i
            }
        }
        return dedans
    }
}

class CarteGeo(
    val largeur: Float,
    val hauteur: Float,
    val departements: List<FormeDept>,
    val contoursRegions: Path
)

private fun ajouterAnneau(chemin: Path, a: FloatArray) {
    if (a.size < 6) return
    chemin.moveTo(a[0], a[1])
    var i = 2
    while (i < a.size) {
        chemin.lineTo(a[i], a[i + 1])
        i += 2
    }
    chemin.close()
}

/** Anneau encodé en différences : [x0, y0, dx1, dy1, dx2, dy2, ...] */
private fun lireAnneau(j: JSONArray): FloatArray {
    val a = FloatArray(j.length())
    var x = 0
    var y = 0
    var i = 0
    while (i < a.size) {
        x += j.getInt(i)
        y += j.getInt(i + 1)
        a[i] = x.toFloat()
        a[i + 1] = y.toFloat()
        i += 2
    }
    return a
}

fun chargerCarte(ctx: Context): CarteGeo {
    val texte = ctx.assets.open("carte_france.json").bufferedReader().use { it.readText() }
    val racine = JSONObject(texte)
    val d = racine.getJSONArray("d")
    val formes = ArrayList<FormeDept>(d.length())
    for (i in 0 until d.length()) {
        val e = d.getJSONArray(i)
        val anneauxJson = e.getJSONArray(1)
        formes.add(FormeDept(e.getString(0), List(anneauxJson.length()) { lireAnneau(anneauxJson.getJSONArray(it)) }))
    }
    val contours = Path()
    val r = racine.getJSONArray("r")
    for (i in 0 until r.length()) ajouterAnneau(contours, lireAnneau(r.getJSONArray(i)))
    return CarteGeo(racine.getInt("w").toFloat(), racine.getInt("h").toFloat(), formes, contours)
}

/** La carte n'est lue qu'une fois, puis gardée en mémoire. */
object CarteCache {
    private var geo: CarteGeo? = null
    fun obtenir(ctx: Context): CarteGeo = geo ?: chargerCarte(ctx.applicationContext).also { geo = it }
}

/** Garde la carte dans la vue : pas de bord vide quand on zoome, centrée quand elle est plus petite. */
private fun limiter(pan: Offset, k: Float, lw: Float, lh: Float, geo: CarteGeo): Offset {
    val debX = geo.largeur * k - lw
    val debY = geo.hauteur * k - lh
    return Offset(
        if (debX <= 0f) 0f else pan.x.coerceIn(-debX / 2f, debX / 2f),
        if (debY <= 0f) 0f else pan.y.coerceIn(-debY / 2f, debY / 2f)
    )
}

/**
 * choix   : numéro du département touché (en bleu)
 * reponse : numéro de la bonne réponse, uniquement après validation (en vert ; le choix faux passe en rouge)
 */
@Composable
fun CarteFrance(
    choix: String?,
    reponse: String?,
    onChoix: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val contexte = LocalContext.current
    val geo = remember { CarteCache.obtenir(contexte) }
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
                        val zoomApres = (zoom * zoomGeste).coerceIn(1f, ZOOM_MAX)
                        val kApres = fit * zoomApres
                        // point de la carte sous les doigts avant le zoom...
                        val mx = (centre.x - ((lw - geo.largeur * kAvant) / 2f + decalage.x)) / kAvant
                        val my = (centre.y - ((lh - geo.hauteur * kAvant) / 2f + decalage.y)) / kAvant
                        // ...qu'on garde sous les doigts après le zoom, puis on applique le déplacement
                        val panX = centre.x - mx * kApres - (lw - geo.largeur * kApres) / 2f + pan.x
                        val panY = centre.y - my * kApres - (lh - geo.hauteur * kApres) / 2f + pan.y
                        zoom = zoomApres
                        decalage = limiter(Offset(panX, panY), kApres, lw, lh, geo)
                    }
                }
                .pointerInput(geo) {
                    detectTapGestures { p ->
                        if (reponseActuelle != null) return@detectTapGestures
                        val lw = taille.width.toFloat()
                        val lh = taille.height.toFloat()
                        if (lw <= 0f || lh <= 0f) return@detectTapGestures
                        val k = min(lw / geo.largeur, lh / geo.hauteur) * zoom
                        val x = (p.x - ((lw - geo.largeur * k) / 2f + decalage.x)) / k
                        val y = (p.y - ((lh - geo.hauteur * k) / 2f + decalage.y)) / k
                        geo.departements.firstOrNull { it.contient(x, y) }?.let { onChoixActuel(it.numero) }
                    }
                }
        ) {
            val k = min(size.width / geo.largeur, size.height / geo.hauteur) * zoom
            val tx = (size.width - geo.largeur * k) / 2f + decalage.x
            val ty = (size.height - geo.hauteur * k) / 2f + decalage.y
            val px = 1.dp.toPx()
            withTransform({
                translate(tx, ty)
                scale(k, k, Offset.Zero)
            }) {
                geo.departements.forEach { d ->
                    val couleur = when {
                        reponse != null && d.numero == reponse -> VERT_BON
                        reponse != null && d.numero == choix -> ROUGE_FAUX
                        reponse == null && d.numero == choix -> BLEU_CHOIX
                        else -> GRIS_DEPT
                    }
                    drawPath(d.chemin, couleur)
                }
                // épaisseurs divisées par k : le trait garde la même taille à l'écran quel que soit le zoom
                geo.departements.forEach { d ->
                    drawPath(d.chemin, GRIS_LIMITE, style = Stroke(width = 0.6f * px / k))
                }
                drawPath(
                    geo.contoursRegions, Color.Black,
                    style = Stroke(width = 1.6f * px / k, join = StrokeJoin.Round)
                )
            }
        }
    }
}

// ----------------------------------------------------------------------------
// IMAGE PAR URL (prêt pour les drapeaux des prochaines catégories)
// ----------------------------------------------------------------------------

@Composable
fun ImageUrl(url: String, description: String) {
    coil.compose.AsyncImage(
        model = url,
        contentDescription = description,
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .padding(vertical = 4.dp)
    )
}

// ----------------------------------------------------------------------------
// RÉSULTATS
// ----------------------------------------------------------------------------

@Composable
fun EcranResultats(
    questions: List<Question>,
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
                    Text(
                        if (q.carte) "Localisation - ${q.dept.numero} ${q.dept.nom}"
                        else "${q.dept.numero} ${q.dept.nom}",
                        fontSize = 14.sp, fontWeight = FontWeight.Bold
                    )
                    if (!ok) {
                        Text(
                            "Attendu : " + if (q.carte) "${q.dept.numero} - ${q.dept.nom}"
                            else q.champs.joinToString(" | ") { "${it.nom} = ${valeurFiche(q.dept, it)}" },
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
// FICHES
// ----------------------------------------------------------------------------

@Composable
fun EcranFiches(onRetour: () -> Unit) {
    var recherche by remember { mutableStateOf("") }
    var deptOuvert by remember { mutableStateOf<Departement?>(null) }

    val d = deptOuvert
    if (d != null) {
        Column(Modifier.fillMaxSize().padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = { deptOuvert = null }) { Text("← Fiches") }
            }
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    if (d.imageUrl != null) ImageUrl(d.imageUrl, "Illustration")
                    Text("${d.numero} - ${d.nom}", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Text("Région : ${REGION_Noms[d.region] ?: d.region}", fontSize = 15.sp)
                    Text("Chef-lieu : ${d.chefLieu}", fontSize = 15.sp)
                    Text("Gentilé : les ${d.gentile}", fontSize = 15.sp)
                    Text("Population : environ ${d.populationK * 1000} hab.", fontSize = 15.sp)
                    Text("Superficie (km²) : ${d.superficieKm2}", fontSize = 15.sp)
                    if (d.sousPrefs.isNotEmpty())
                        Text("Sous-préfecture(s) : ${d.sousPrefs.joinToString(", ")}", fontSize = 15.sp)
                }
            }
        }
    } else {
        Column(Modifier.fillMaxSize().padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = onRetour) { Text("← Menu") }
                Text("Fiches", fontWeight = FontWeight.Bold, fontSize = 20.sp)
            }
            TextField(
                value = recherche,
                onValueChange = { recherche = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Rechercher (nom, numéro, région)") },
                singleLine = true
            )
            Spacer(Modifier.height(8.dp))
            val filtres = DEPARTEMENTS.filter {
                it.nom.contains(recherche, ignoreCase = true) ||
                    it.numero.contains(recherche) ||
                    (REGION_Noms[it.region]?.contains(recherche, ignoreCase = true) == true)
            }
            LazyColumn(Modifier.fillMaxSize()) {
                items(filtres) { dep ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { deptOuvert = dep }
                    ) {
                        Column(Modifier.padding(12.dp)) {
                            Text("${dep.numero} - ${dep.nom}", fontSize = 17.sp, fontWeight = FontWeight.Bold)
                            Text("${REGION_Noms[dep.region] ?: dep.region} · chef-lieu : ${dep.chefLieu}", fontSize = 13.sp, color = Color.Gray)
                        }
                    }
                }
            }
        }
    }
}
