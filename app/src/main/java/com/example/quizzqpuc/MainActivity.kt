// ============================================================================
// QUIZZ QPUC v2 - Thème : Départements français
// ============================================================================

package com.example.quizzqpuc

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
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
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.rememberTextMeasurer
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
    Departement("01", "Ain", "ARA", "Bourg-en-Bresse", "Aindinois", 679, 5762, listOf("Belley", "Gex", "Nantua")),
    Departement("02", "Aisne", "HDF", "Laon", "Axonais", 523, 7369, listOf("Château-Thierry", "Saint-Quentin", "Soissons", "Vervins")),
    Departement("03", "Allier", "ARA", "Moulins", "Bourbonnais", 333, 7340, listOf("Montluçon", "Vichy")),
    Departement("04", "Alpes-de-Haute-Provence", "PACA", "Digne-les-Bains", "Bas-Alpins", 168, 6925, listOf("Barcelonnette", "Castellane", "Forcalquier")),
    Departement("05", "Hautes-Alpes", "PACA", "Gap", "Alpins", 143, 5549, listOf("Briançon")),
    Departement("06", "Alpes-Maritimes", "PACA", "Nice", "Maralpins", 1128, 4299, listOf("Grasse")),
    Departement("07", "Ardèche", "ARA", "Privas", "Ardéchois", 334, 5529, listOf("Largentière", "Tournon-sur-Rhône")),
    Departement("08", "Ardennes", "GE", "Charleville-Mézières", "Ardennais", 266, 5229, listOf("Rethel", "Sedan", "Vouziers")),
    Departement("09", "Ariège", "OCC", "Foix", "Ariégeois", 156, 4890, listOf("Pamiers", "Saint-Girons")),
    Departement("10", "Aube", "GE", "Troyes", "Aubois", 310, 6004, listOf("Bar-sur-Aube", "Nogent-sur-Seine")),
    Departement("11", "Aude", "OCC", "Carcassonne", "Audois", 380, 6139, listOf("Limoux", "Narbonne")),
    Departement("12", "Aveyron", "OCC", "Rodez", "Aveyronnais", 280, 8735, listOf("Millau", "Villefranche-de-Rouergue")),
    Departement("13", "Bouches-du-Rhône", "PACA", "Marseille", "Bucco-Rhodaniens", 2088, 5087, listOf("Aix-en-Provence", "Arles", "Istres")),
    Departement("14", "Calvados", "NOR", "Caen", "Calvadosiens", 709, 5548, listOf("Bayeux", "Lisieux", "Vire Normandie")),
    Departement("15", "Cantal", "ARA", "Aurillac", "Cantaliens", 144, 5726, listOf("Mauriac", "Saint-Flour")),
    Departement("16", "Charente", "NA", "Angoulême", "Charentais", 353, 5956, listOf("Cognac", "Confolens")),
    Departement("17", "Charente-Maritime", "NA", "La Rochelle", "Charentais-Maritimes", 672, 6864, listOf("Jonzac", "Rochefort", "Saint-Jean-d'Angély", "Saintes")),
    Departement("18", "Cher", "CVL", "Bourges", "Berruyers", 299, 7235, listOf("Saint-Amand-Montrond", "Vierzon")),
    Departement("19", "Corrèze", "NA", "Tulle", "Corréziens", 241, 5857, listOf("Brive-la-Gaillarde", "Ussel")),
    Departement("2A", "Corse-du-Sud", "COR", "Ajaccio", "Sud-Corses", 168, 4014, listOf("Sartène")),
    Departement("2B", "Haute-Corse", "COR", "Bastia", "Haut-Corses", 187, 4666, listOf("Calvi", "Corte")),
    Departement("21", "Côte-d'Or", "BFC", "Dijon", "Dijonnais", 540, 8763, listOf("Beaune", "Montbard")),
    Departement("22", "Côtes-d'Armor", "BRE", "Saint-Brieuc", "Costarmoricains", 612, 6878, listOf("Dinan", "Guingamp", "Lannion")),
    Departement("23", "Creuse", "NA", "Guéret", "Creusois", 116, 5565, listOf("Aubusson")),
    Departement("24", "Dordogne", "NA", "Périgueux", "Périgordins", 418, 9060, listOf("Bergerac", "Nontron", "Sarlat-la-Canéda")),
    Departement("25", "Doubs", "BFC", "Besançon", "Doubistes", 547, 5232, listOf("Montbéliard", "Pontarlier")),
    Departement("26", "Drôme", "ARA", "Valence", "Drômois", 524, 6530, listOf("Die", "Nyons")),
    Departement("27", "Eure", "NOR", "Évreux", "Eurois", 603, 6040, listOf("Bernay", "Les Andelys")),
    Departement("28", "Eure-et-Loir", "CVL", "Chartres", "Euréliens", 433, 5880, listOf("Châteaudun", "Dreux", "Nogent-le-Rotrou")),
    Departement("29", "Finistère", "BRE", "Quimper", "Finistériens", 933, 6733, listOf("Brest", "Châteaulin", "Morlaix")),
    Departement("30", "Gard", "OCC", "Nîmes", "Gardois", 771, 5853, listOf("Alès", "Le Vigan")),
    Departement("31", "Haute-Garonne", "OCC", "Toulouse", "Haut-Garonnais", 1471, 6309, listOf("Muret", "Saint-Gaudens")),
    Departement("32", "Gers", "OCC", "Auch", "Gersois", 193, 6257, listOf("Condom", "Mirande")),
    Departement("33", "Gironde", "NA", "Bordeaux", "Bordelais", 1690, 10024, listOf("Arcachon", "Blaye", "Langon", "Lesparre-Médoc", "Libourne")),
    Departement("34", "Hérault", "OCC", "Montpellier", "Héraultais", 1230, 6101, listOf("Béziers", "Lodève")),
    Departement("35", "Ille-et-Vilaine", "BRE", "Rennes", "Ille-Vilainais", 1121, 6775, listOf("Fougères", "Redon", "Saint-Malo")),
    Departement("36", "Indre", "CVL", "Châteauroux", "Castelroussins", 216, 6883, listOf("Issoudun", "La Châtre", "Le Blanc")),
    Departement("37", "Indre-et-Loire", "CVL", "Tours", "Tourangeaux", 619, 6127, listOf("Chinon", "Loches")),
    Departement("38", "Isère", "ARA", "Grenoble", "Isérois", 1299, 7431, listOf("La Tour-du-Pin", "Vienne")),
    Departement("39", "Jura", "BFC", "Lons-le-Saunier", "Jurassiens", 258, 4999, listOf("Dole", "Saint-Claude")),
    Departement("40", "Landes", "NA", "Mont-de-Marsan", "Landais", 434, 9243, listOf("Dax")),
    Departement("41", "Loir-et-Cher", "CVL", "Blois", "Blésois", 329, 6314, listOf("Romorantin-Lanthenay", "Vendôme")),
    Departement("42", "Loire", "ARA", "Saint-Étienne", "Stéphanois", 774, 4781, listOf("Montbrison", "Roanne")),
    Departement("43", "Haute-Loire", "ARA", "Le Puy-en-Velay", "Altiligériens", 229, 4977, listOf("Brioude", "Yssingeaux")),
    Departement("44", "Loire-Atlantique", "PDL", "Nantes", "Nantais", 1488, 7156, listOf("Châteaubriant", "Saint-Nazaire")),
    Departement("45", "Loiret", "CVL", "Orléans", "Orléanais", 691, 6775, listOf("Montargis", "Pithiviers")),
    Departement("46", "Lot", "OCC", "Cahors", "Lotois", 176, 5217, listOf("Figeac", "Gourdon")),
    Departement("47", "Lot-et-Garonne", "NA", "Agen", "Agenais", 334, 5362, listOf("Marmande", "Nérac", "Villeneuve-sur-Lot")),
    Departement("48", "Lozère", "OCC", "Mende", "Lozériens", 76, 5167, listOf("Florac Trois Rivières")),
    Departement("49", "Maine-et-Loire", "PDL", "Angers", "Angevins", 834, 7136, listOf("Cholet", "Saumur", "Segré-en-Anjou Bleu")),
    Departement("50", "Manche", "NOR", "Saint-Lô", "Manchois", 498, 5938, listOf("Avranches", "Cherbourg-en-Cotentin", "Coutances")),
    Departement("51", "Marne", "GE", "Châlons-en-Champagne", "Marnais", 563, 8162, listOf("Reims", "Vitry-le-François", "Épernay")),
    Departement("52", "Haute-Marne", "GE", "Chaumont", "Chaumontais", 168, 6211, listOf("Langres", "Saint-Dizier")),
    Departement("53", "Mayenne", "PDL", "Laval", "Lavallois", 305, 5175, listOf("Château-Gontier-sur-Mayenne", "Mayenne")),
    Departement("54", "Meurthe-et-Moselle", "GE", "Nancy", "Nancéiens", 732, 5246, listOf("Lunéville", "Toul", "Val de Briey")),
    Departement("55", "Meuse", "GE", "Bar-le-Duc", "Barisiens", 180, 6211, listOf("Commercy", "Verdun")),
    Departement("56", "Morbihan", "BRE", "Vannes", "Vannetais", 783, 6823, listOf("Lorient", "Pontivy")),
    Departement("57", "Moselle", "GE", "Metz", "Messins", 1051, 6216, listOf("Forbach", "Sarrebourg", "Sarreguemines", "Thionville")),
    Departement("58", "Nièvre", "BFC", "Nevers", "Nivernais", 201, 6817, listOf("Château-Chinon (Ville)", "Clamecy", "Cosne-Cours-sur-Loire")),
    Departement("59", "Nord", "HDF", "Lille", "Nordistes", 2616, 5743, listOf("Avesnes-sur-Helpe", "Cambrai", "Douai", "Dunkerque", "Valenciennes")),
    Departement("60", "Oise", "HDF", "Beauvais", "Oisiens", 830, 5860, listOf("Clermont", "Compiègne", "Senlis")),
    Departement("61", "Orne", "NOR", "Alençon", "Ornais", 275, 6103, listOf("Argentan", "Mortagne-au-Perche")),
    Departement("62", "Pas-de-Calais", "HDF", "Arras", "Pas-de-Calaisiens", 1458, 6671, listOf("Boulogne-sur-Mer", "Béthune", "Calais", "Lens", "Montreuil-sur-Mer", "Saint-Omer")),
    Departement("63", "Puy-de-Dôme", "ARA", "Clermont-Ferrand", "Clermontois", 664, 7970, listOf("Ambert", "Issoire", "Riom", "Thiers")),
    Departement("64", "Pyrénées-Atlantiques", "NA", "Pau", "Pyrénnéens-Atlantiques", 707, 7645, listOf("Bayonne", "Oloron-Sainte-Marie")),
    Departement("65", "Hautes-Pyrénées", "OCC", "Tarbes", "Haut-Pyrénéens", 231, 4464, listOf("Argelès-Gazost", "Bagnères-de-Bigorre")),
    Departement("66", "Pyrénées-Orientales", "OCC", "Perpignan", "Perpignanais", 497, 4116, listOf("Céret", "Prades")),
    Departement("67", "Bas-Rhin", "GE", "Strasbourg", "Strasbourgeois", 1164, 4775, listOf("Haguenau", "Molsheim", "Saverne", "Sélestat")),
    Departement("68", "Haut-Rhin", "GE", "Colmar", "Haut-Rhinois", 771, 3525, listOf("Altkirch", "Mulhouse", "Thann")),
    Departement("69", "Rhône", "ARA", "Lyon", "Lyonnais", 1915, 5316, listOf("Villefranche-sur-Saône")),
    Departement("70", "Haute-Saône", "BFC", "Vesoul", "Haut-Saônois", 233, 5360, listOf("Lure")),
    Departement("71", "Saône-et-Loire", "BFC", "Mâcon", "Mâconnais", 550, 8575, listOf("Autun", "Chalon-sur-Saône", "Charolles", "Louhans-Châteaurenaud")),
    Departement("72", "Sarthe", "PDL", "Le Mans", "Mancelles", 567, 6206, listOf("La Flèche", "Mamers")),
    Departement("73", "Savoie", "ARA", "Chambéry", "Savoyards", 448, 6171, listOf("Albertville", "Saint-Jean-de-Maurienne")),
    Departement("74", "Haute-Savoie", "ARA", "Annecy", "Haut-Savoyards", 861, 4388, listOf("Bonneville", "Saint-Julien-en-Genevois", "Thonon-les-Bains")),
    Departement("75", "Paris", "IDF", "Paris", "Parisiens", 2104, 105, emptyList()),
    Departement("76", "Seine-Maritime", "NOR", "Rouen", "Rouennais", 1261, 6328, listOf("Dieppe", "Le Havre")),
    Departement("77", "Seine-et-Marne", "IDF", "Melun", "Melunais", 1468, 5915, listOf("Fontainebleau", "Meaux", "Provins", "Torcy")),
    Departement("78", "Yvelines", "IDF", "Versailles", "Versaillais", 1485, 2284, listOf("Mantes-la-Jolie", "Rambouillet", "Saint-Germain-en-Laye")),
    Departement("79", "Deux-Sèvres", "NA", "Niort", "Niortais", 375, 5999, listOf("Bressuire", "Parthenay")),
    Departement("80", "Somme", "HDF", "Amiens", "Amiénois", 565, 6170, listOf("Abbeville", "Montdidier", "Péronne")),
    Departement("81", "Tarn", "OCC", "Albi", "Albigeois", 397, 5758, listOf("Castres")),
    Departement("82", "Tarn-et-Garonne", "OCC", "Montauban", "Montalbanais", 266, 3718, listOf("Castelsarrasin")),
    Departement("83", "Var", "PACA", "Toulon", "Toulonnais", 1119, 5975, listOf("Brignoles", "Draguignan")),
    Departement("84", "Vaucluse", "PACA", "Avignon", "Vauclusiens", 572, 3567, listOf("Apt", "Carpentras")),
    Departement("85", "Vendée", "PDL", "La Roche-sur-Yon", "Yonnais", 714, 6720, listOf("Fontenay-le-Comte", "Les Sables-d'Olonne")),
    Departement("86", "Vienne", "NA", "Poitiers", "Poitevins", 439, 6990, listOf("Châtellerault", "Montmorillon")),
    Departement("87", "Haute-Vienne", "NA", "Limoges", "Limougeauds", 373, 5520, listOf("Bellac", "Rochechouart")),
    Departement("88", "Vosges", "GE", "Épinal", "Vosgiens", 357, 5874, listOf("Neufchâteau", "Saint-Dié-des-Vosges")),
    Departement("89", "Yonne", "BFC", "Auxerre", "Icaunais", 332, 7427, listOf("Avallon", "Sens")),
    Departement("90", "Territoire de Belfort", "BFC", "Belfort", "Belfortains", 140, 609, emptyList()),
    Departement("91", "Essonne", "IDF", "Évry-Courcouronnes", "Essonniens", 1338, 1804, listOf("Palaiseau", "Étampes")),
    Departement("92", "Hauts-de-Seine", "IDF", "Nanterre", "Altoséquanais", 1655, 176, listOf("Antony", "Boulogne-Billancourt")),
    Departement("93", "Seine-Saint-Denis", "IDF", "Bobigny", "Séquano-Dionysiens", 1704, 236, listOf("Le Raincy", "Saint-Denis")),
    Departement("94", "Val-de-Marne", "IDF", "Créteil", "Créteilais", 1427, 245, listOf("L'Haÿ-les-Roses", "Nogent-sur-Marne")),
    Departement("95", "Val-d'Oise", "IDF", "Cergy", "Val-d'Oisiens", 1282, 1246, listOf("Argenteuil", "Sarcelles")),
    Departement("971", "Guadeloupe", "DOM", "Basse-Terre", "Guadeloupéens", 384, 1628, listOf("Pointe-à-Pitre")),
    Departement("972", "Martinique", "DOM", "Fort-de-France", "Martiniquais", 361, 1128, listOf("La Trinité", "Le Marin", "Saint-Pierre")),
    Departement("973", "Guyane", "DOM", "Cayenne", "Guyanais", 294, 83534, listOf("Saint-Georges", "Saint-Laurent-du-Maroni")),
    Departement("974", "La Réunion", "DOM", "Saint-Denis", "Réunionnais", 890, 2504, listOf("Saint-Benoît", "Saint-Paul", "Saint-Pierre")),
    Departement("976", "Mayotte", "DOM", "Mamoudzou", "Mahorais", 321, 374, emptyList())
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

/** Noms courts ou usuels acceptés en plus du nom officiel d'une commune. */
val ALIAS_VILLES = mapOf(
    "Vire Normandie" to listOf("Vire"),
    "Segré-en-Anjou Bleu" to listOf("Segré"),
    "Florac Trois Rivières" to listOf("Florac"),
    "Château-Chinon (Ville)" to listOf("Château-Chinon"),
    "Château-Gontier-sur-Mayenne" to listOf("Château-Gontier"),
    "Louhans-Châteaurenaud" to listOf("Louhans"),
    "Montreuil-sur-Mer" to listOf("Montreuil"),
    "Cherbourg-en-Cotentin" to listOf("Cherbourg"),
    "Saint-Dié-des-Vosges" to listOf("Saint-Dié"),
    "Tournon-sur-Rhône" to listOf("Tournon"),
    "Val de Briey" to listOf("Briey"),
    "Cergy" to listOf("Pontoise"),
)

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
    AUTRES_DEPTS(
        "Les autres départements de la région",
        "Autres départements de la région",
        "Autres départements de la région"
    ),
    GENTILE("Le gentilé", "Gentilé (les ...)", "Gentilé"),
    HABITANTS("Nb d'habitants ($TOL_TXT)", "Habitants\n(ex : 500000, $TOL_TXT)", "Habitants"),
    SUPERFICIE("Superficie ($TOL_TXT)", "Superficie (km²)\n($TOL_TXT)", "Superficie (km²)"),
    CHEF_LIEU("Le chef-lieu", "Chef-lieu", "Chef-lieu"),
    SOUS_PREF("Sous-préfectures", "Sous-préfecture(s)", "Sous-préfecture(s)"),
    LOCALISATION("La localisation (carte)", "Localisation", "Localisation")
}

enum class Ecran { MENU, LISTE_QUIZ, PARAMS, QUIZ, RESULTATS, FICHES_CHOIX, FICHES, FICHES_PAYS, FICHES_ETATS }

data class Question(
    val dept: Departement,
    val carte: Boolean,
    val champs: List<Mode> = emptyList()
)

// dispo = false : catégorie en chantier, cachée dans le menu (le code reste là)
val CATEGORIES = listOf(
    Categorie("departements", "Départements français", true),
    Categorie("pays", "Pays du monde", true),
    Categorie("rois", "Rois de France", false),
    Categorie("usa", "États américains", true)
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
    val ctx = LocalContext.current
    var ecran by remember { mutableStateOf(Ecran.MENU) }
    var categorie by remember { mutableStateOf(CATEGORIES[0]) }

    var nbQuestions by remember { mutableStateOf(20f) }
    var modesActifs by remember { mutableStateOf(Mode.entries.toList()) }

    var questions by remember { mutableStateOf(listOf<Question>()) }
    var index by remember { mutableStateOf(0) }
    var reponses by remember { mutableStateOf(listOf<Boolean>()) }
    var partieSauvee by remember { mutableStateOf(lirePartieDepts(ctx)) }
    var confirmerQuitter by remember { mutableStateOf(false) }

    fun demarrer() {
        questions = genererQuestions(nbQuestions.toInt(), modesActifs)
        index = 0
        reponses = emptyList()
        effacerPartieDepts(ctx)
        partieSauvee = null
        ecran = Ecran.QUIZ
    }

    // Bouton retour du téléphone : remonte d'un écran ; pendant une partie, demande confirmation.
    BackHandler(enabled = ecran != Ecran.MENU) {
        when (ecran) {
            Ecran.QUIZ -> confirmerQuitter = true
            Ecran.LISTE_QUIZ, Ecran.FICHES_CHOIX -> ecran = Ecran.MENU
            Ecran.PARAMS, Ecran.RESULTATS -> ecran = Ecran.LISTE_QUIZ
            Ecran.FICHES, Ecran.FICHES_PAYS, Ecran.FICHES_ETATS -> ecran = Ecran.FICHES_CHOIX
            Ecran.MENU -> {}
        }
    }

    MaterialTheme {
        Surface(Modifier.fillMaxSize()) {
            // marges : les boutons ne passent plus sous la barre de navigation ni sous le clavier
            Box(Modifier.fillMaxSize().systemBarsPadding().imePadding()) {
                when (ecran) {
                    Ecran.MENU -> EcranMenu(
                        onJouer = { ecran = Ecran.LISTE_QUIZ },
                        onFiches = { ecran = Ecran.FICHES_CHOIX }
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
                            onDemarrer = { demarrer() },
                            partie = partieSauvee,
                            onReprendre = { p ->
                                questions = p.questions
                                index = p.index
                                reponses = p.reponses
                                ecran = Ecran.QUIZ
                            }
                        )
                        "pays" -> QuizPays(onRetour = { ecran = Ecran.LISTE_QUIZ })
                        "usa" -> QuizEtats(onRetour = { ecran = Ecran.LISTE_QUIZ })
                        else -> {}
                    }
                    Ecran.QUIZ -> EcranQuiz(
                        question = questions[index],
                        total = questions.size,
                        index = index,
                        onRepondu = { correct ->
                            reponses = reponses + correct
                            if (index + 1 < questions.size) {
                                index++
                                sauverPartieDepts(ctx, questions, index, reponses)
                            } else {
                                effacerPartieDepts(ctx)
                                partieSauvee = null
                                ecran = Ecran.RESULTATS
                            }
                        },
                        onAbandon = { confirmerQuitter = true }
                    )
                    Ecran.RESULTATS -> EcranResultats(
                        questions, reponses,
                        onRef = { demarrer() },
                        onMenu = { ecran = Ecran.LISTE_QUIZ }
                    )
                    Ecran.FICHES_CHOIX -> EcranChoixFiches(
                        onRetour = { ecran = Ecran.MENU },
                        onDepartements = { ecran = Ecran.FICHES },
                        onPays = { ecran = Ecran.FICHES_PAYS },
                        onEtats = { ecran = Ecran.FICHES_ETATS }
                    )
                    Ecran.FICHES -> EcranFiches(onRetour = { ecran = Ecran.FICHES_CHOIX })
                    Ecran.FICHES_PAYS -> EcranFichesPays(onRetour = { ecran = Ecran.FICHES_CHOIX })
                    Ecran.FICHES_ETATS -> EcranFichesEtats()
                }
            }
        }
        if (confirmerQuitter) {
            DialogQuitter(
                onContinuer = { confirmerQuitter = false },
                onPause = {
                    sauverPartieDepts(ctx, questions, index, reponses)
                    partieSauvee = lirePartieDepts(ctx)
                    confirmerQuitter = false
                    ecran = Ecran.LISTE_QUIZ
                },
                onAbandonner = {
                    effacerPartieDepts(ctx)
                    partieSauvee = null
                    confirmerQuitter = false
                    ecran = Ecran.LISTE_QUIZ
                }
            )
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
        Text("Choisis un quiz", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        CATEGORIES.filter { it.dispo }.forEach { c ->
            Button(
                onClick = { onChoix(c) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .padding(vertical = 4.dp)
            ) {
                Text(c.titre, fontSize = 18.sp)
            }
            Spacer(Modifier.height(4.dp))
        }
    }
}

// ----------------------------------------------------------------------------
// PARAMÈTRES DU QUIZ DÉPARTEMENTS
// ----------------------------------------------------------------------------

/** Champs qui permettent de reconnaître un département : l'un d'eux reste toujours affiché. */
val CHAMPS_IDENTIFIANTS = listOf(Mode.NUMERO, Mode.NOM, Mode.CHEF_LIEU, Mode.GENTILE)

/**
 * Champs à remplir pour un département.
 * - Paris n'a pas de sous-préfecture : ce champ n'est pas demandé.
 * - Si tous les champs identifiants sont demandés, l'un d'eux (au hasard) reste affiché,
 *   sinon on ne saurait pas de quel département on parle.
 */
fun champsPour(d: Departement, modes: List<Mode>): List<Mode> {
    val champs = modes.filter { it != Mode.LOCALISATION && !(it == Mode.SOUS_PREF && d.sousPrefs.isEmpty()) }
    val resteAffiche = CHAMPS_IDENTIFIANTS.any { it !in champs }
    return if (resteAffiche) champs else champs - CHAMPS_IDENTIFIANTS.random()
}

/** Les autres départements de la même région. */
fun autresDepartements(d: Departement): List<Departement> =
    DEPARTEMENTS.filter { it.region == d.region && it.numero != d.numero }

/** Découpe « Yvelines, Essonne ; 92 » en réponses séparées. */
fun decouperListe(saisie: String): List<String> =
    saisie.split(',', ';', '\n').map { it.trim() }.filter { it.isNotEmpty() }

/**
 * Corrige la liste des autres départements de la région (ordre libre, nom ou numéro accepté).
 * Renvoie les départements manquants et le nombre de réponses en trop.
 */
fun verifierAutresDepts(d: Departement, saisie: String): Pair<List<Departement>, Int> {
    val restants = autresDepartements(d).toMutableList()
    var enTrop = 0
    decouperListe(saisie).forEach { rep ->
        val i = restants.indexOfFirst { normaliser(rep) == normaliser(it.numero) || correspond(rep, it.nom) }
        if (i >= 0) restants.removeAt(i) else enTrop++
    }
    return restants to enTrop
}

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
    onDemarrer: () -> Unit,
    partie: PartieDepts? = null,
    onReprendre: (PartieDepts) -> Unit = {}
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
        Text("Champs à remplir dans la fiche", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text(
            "Un champ qui permet d'identifier le département (n°, nom, chef-lieu ou gentilé) " +
                "reste toujours affiché.",
            fontSize = 13.sp, color = Color.Gray
        )

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
    verdict: Boolean? = null,
    multiligne: Boolean = false
) {
    TextField(
        value = valeur,
        onValueChange = { if (!verrouille) onSaisie(it) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        label = { Text(libelle, fontSize = 13.sp) },
        singleLine = !multiligne,
        minLines = if (multiligne) 3 else 1,
        maxLines = if (multiligne) 8 else 1,
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
    Mode.AUTRES_DEPTS -> autresDepartements(d).joinToString(", ") { it.nom }
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
        val i = restantes.indexOfFirst { att -> (listOf(att) + (ALIAS_VILLES[att] ?: emptyList())).any { correspond(rep, it) } }
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
    Mode.AUTRES_DEPTS -> verifierAutresDepts(d, reponse).let { (manquants, enTrop) -> manquants.isEmpty() && enTrop == 0 }
    Mode.GENTILE -> correctFlechie(reponse, listOf(d.gentile))
    Mode.HABITANTS -> dansTolerance(reponse.filter { it.isDigit() }.toIntOrNull() ?: 0, d.populationK * 1000)
    Mode.SUPERFICIE -> dansTolerance(reponse.filter { it.isDigit() }.toIntOrNull() ?: 0, d.superficieKm2)
    Mode.CHEF_LIEU -> correctFlechie(reponse, listOf(d.chefLieu) + (ALIAS_VILLES[d.chefLieu] ?: emptyList()))
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
    var detailAutres by remember(question) { mutableStateOf("") }
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
                    "Localise le département ${question.dept.numero} ${question.dept.nom} sur la carte. (Pince pour zoomer)",
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
                                Mode.NUMERO, Mode.NOM, Mode.REGION, Mode.AUTRES_DEPTS, Mode.GENTILE,
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
                                    val autres = mode == Mode.AUTRES_DEPTS
                                    ChampRempli(
                                        libelle = if (autres) {
                                            "${mode.libelleChamp}\n(${autresDepartements(question.dept).size} à citer, " +
                                                "séparés par des virgules)"
                                        } else mode.libelleChamp,
                                        valeur = saisies[mode] ?: "",
                                        onSaisie = { nvx ->
                                            saisies = saisies.toMutableMap().apply { put(mode, nvx) }
                                        },
                                        verrouille = verdicts.isNotEmpty(),
                                        verdict = verdicts[mode],
                                        multiligne = autres
                                    )
                                    if (verdicts[mode] == false) {
                                        Text(
                                            if (autres) detailAutres
                                            else "Attendu : ${valeurFiche(question.dept, mode)}",
                                            fontSize = 13.sp,
                                            color = Color(0xFFC62828),
                                            modifier = Modifier.padding(start = 16.dp)
                                        )
                                    }
                                }
                            }
                            // Localisation toujours donnée dans la fiche (sauf question de localisation)
                            Spacer(Modifier.height(12.dp))
                            Text("Localisation", fontSize = 14.sp, color = Color.Gray)
                            CarteFranceMini(question.dept.numero)
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
                        if (Mode.AUTRES_DEPTS in question.champs) {
                            val (manquants, enTrop) = verifierAutresDepts(question.dept, saisies[Mode.AUTRES_DEPTS] ?: "")
                            detailAutres = buildString {
                                if (manquants.isNotEmpty()) append("Manque : ${manquants.joinToString(", ") { it.nom }}")
                                if (manquants.isNotEmpty() && enTrop > 0) append("\n")
                                if (enTrop > 0) append("En trop ou mal écrit : $enTrop réponse(s)")
                            }
                        }
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

    val largeurBoite: Float get() = maxX - minX
    val centreX: Float get() = (minX + maxX) / 2f
    val centreY: Float get() = (minY + maxY) / 2f

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
    val contoursRegions: Path,
    val chefsLieux: Map<String, Offset> = emptyMap()
)

fun ajouterAnneau(chemin: Path, a: FloatArray) {
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
fun lireAnneau(j: JSONArray): FloatArray {
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

fun chargerCarte(ctx: Context, fichier: String = "carte_france.json"): CarteGeo {
    val texte = ctx.assets.open(fichier).bufferedReader().use { it.readText() }
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
    val chefs = HashMap<String, Offset>()
    val cl = racine.optJSONArray("cl")
    if (cl != null) {
        for (i in 0 until cl.length()) {
            val e = cl.getJSONArray(i)
            chefs[e.getString(0)] = Offset(e.getInt(1).toFloat(), e.getInt(2).toFloat())
        }
    }
    return CarteGeo(racine.getInt("w").toFloat(), racine.getInt("h").toFloat(), formes, contours, chefs)
}

/** La carte n'est lue qu'une fois, puis gardée en mémoire. */
object CarteCache {
    private val cartes = HashMap<String, CarteGeo>()
    fun obtenir(ctx: Context, fichier: String = "carte_france.json"): CarteGeo =
        cartes.getOrPut(fichier) { chargerCarte(ctx.applicationContext, fichier) }
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
    modifier: Modifier = Modifier,
    fichier: String = "carte_france.json"
) {
    val contexte = LocalContext.current
    val geo = remember(fichier) { CarteCache.obtenir(contexte, fichier) }
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
            dessinerCarte(geo, k, tx, ty, 0.6f * px, 1.6f * px) { numero ->
                when {
                    reponse != null && numero == reponse -> VERT_BON
                    reponse != null && numero == choix -> ROUGE_FAUX
                    reponse == null && numero == choix -> BLEU_CHOIX
                    else -> GRIS_DEPT
                }
            }
        }
    }
}

/**
 * Dessine la carte. Les épaisseurs de trait (en pixels écran) sont divisées par k :
 * le trait garde la même taille à l'écran quel que soit le zoom.
 */
private fun DrawScope.dessinerCarte(
    geo: CarteGeo,
    k: Float,
    tx: Float,
    ty: Float,
    traitDept: Float,
    traitRegion: Float,
    couleurDe: (String) -> Color
) {
    withTransform({
        translate(tx, ty)
        scale(k, k, Offset.Zero)
    }) {
        geo.departements.forEach { d -> drawPath(d.chemin, couleurDe(d.numero)) }
        geo.departements.forEach { d -> drawPath(d.chemin, GRIS_LIMITE, style = Stroke(width = traitDept / k)) }
        drawPath(
            geo.contoursRegions, Color.Black,
            style = Stroke(width = traitRegion / k, join = StrokeJoin.Round)
        )
    }
}

/**
 * Mini-carte de la fiche : département en bleu, point rouge = chef-lieu,
 * zoom (pincer) et déplacement, numéros / noms des départements voisins en zoomant.
 */
@Composable
fun CarteFranceMini(numero: String, modifier: Modifier = Modifier) {
    val noms = remember { DEPARTEMENTS.associate { it.numero to it.nom } }
    CarteRegionMini("carte_france.json", numero, noms, "Chef-lieu", modifier)
}

/** Mini-carte générique (départements français, États américains...) : voir CarteFranceMini. */
@Composable
fun CarteRegionMini(
    fichier: String,
    numero: String,
    noms: Map<String, String>,
    repere: String,
    modifier: Modifier = Modifier
) {
    val contexte = LocalContext.current
    val geo = remember(fichier) { CarteCache.obtenir(contexte, fichier) }
    val vue = remember(numero) { VueZoom() }
    val tm = rememberTextMeasurer()
    Column(modifier.fillMaxWidth()) {
        Canvas(
            Modifier
                .fillMaxWidth()
                .height(240.dp)
                .clipToBounds()
                .zoomPanMini(vue, geo.largeur, geo.hauteur, ZOOM_MAX)
        ) {
            val k = min(size.width / geo.largeur, size.height / geo.hauteur) * vue.zoom
            val tx = (size.width - geo.largeur * k) / 2f + vue.decalage.x
            val ty = (size.height - geo.hauteur * k) / 2f + vue.decalage.y
            val px = 1.dp.toPx()
            dessinerCarte(geo, k, tx, ty, 0.5f * px, 1.1f * px) { n ->
                if (n == numero) BLEU_CHOIX else GRIS_DEPT
            }
            // numéros / noms des voisins (celui de la fiche n'est jamais écrit : ce pourrait être la réponse)
            if (vue.zoom >= 2f) {
                geo.departements.forEach { d ->
                    if (d.numero != numero) {
                        val sx = tx + d.centreX * k
                        val sy = ty + d.centreY * k
                        if (sx > 0f && sx < size.width && sy > 0f && sy < size.height) {
                            etiquette(
                                tm, listOf(noms[d.numero] ?: d.numero, d.numero),
                                sx, sy, d.largeurBoite * k, 9.sp, Color(0xFF263238)
                            )
                        }
                    }
                }
            }
            // le cercle aide à repérer les petits départements (Paris, Hauts-de-Seine...)
            geo.departements.firstOrNull { it.numero == numero }?.let { d ->
                if (vue.zoom < 3f) {
                    drawCircle(
                        Color(0xFFFF6F00),
                        radius = 10.dp.toPx(),
                        center = Offset(tx + d.centreX * k, ty + d.centreY * k),
                        style = Stroke(width = 1.8f * px)
                    )
                }
            }
            geo.chefsLieux[numero]?.let { c ->
                val centre = Offset(tx + c.x * k, ty + c.y * k)
                drawCircle(Color.White, radius = 5.dp.toPx(), center = centre)
                drawCircle(Color(0xFFD32F2F), radius = 3.6f.dp.toPx(), center = centre)
            }
        }
        LegendeCarte(vue, repere)
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
    BackHandler(enabled = deptOuvert != null) { deptOuvert = null }

    val d = deptOuvert
    if (d != null) {
        Column(
            Modifier
                .fillMaxSize()
                .balayageFiches(
                    onSuivante = { deptOuvert = voisine(DEPARTEMENTS, d, 1) },
                    onPrecedente = { deptOuvert = voisine(DEPARTEMENTS, d, -1) }
                )
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    if (d.imageUrl != null) ImageUrl(d.imageUrl, "Illustration")
                    Text("${d.numero} - ${d.nom}", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Text("Région : ${REGION_Noms[d.region] ?: d.region}", fontSize = 15.sp)
                    Text(
                        "Autres départements de la région : ${valeurFiche(d, Mode.AUTRES_DEPTS)}",
                        fontSize = 15.sp
                    )
                    Text("Chef-lieu : ${d.chefLieu}", fontSize = 15.sp)
                    Text("Gentilé : les ${d.gentile}", fontSize = 15.sp)
                    Text("Population : environ ${d.populationK * 1000} hab.", fontSize = 15.sp)
                    Text("Superficie (km²) : ${d.superficieKm2}", fontSize = 15.sp)
                    if (d.sousPrefs.isNotEmpty())
                        Text("Sous-préfecture(s) : ${d.sousPrefs.joinToString(", ")}", fontSize = 15.sp)
                    Spacer(Modifier.height(12.dp))
                    Text("Localisation", fontSize = 14.sp, color = Color.Gray)
                    CarteFranceMini(d.numero)
                }
            }
            PiedFiche(onAleatoire = { deptOuvert = DEPARTEMENTS.random() })
        }
    } else {
        Column(Modifier.fillMaxSize().padding(16.dp)) {
            Text("Fiches : départements", fontWeight = FontWeight.Bold, fontSize = 20.sp)
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
            LazyColumn(Modifier.weight(1f)) {
                items(filtres) { dep ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { deptOuvert = dep }
                    ) {
                        Column(Modifier.padding(12.dp)) {
                            Text("${dep.numero} - ${dep.nom}", fontSize = 17.sp, fontWeight = FontWeight.Bold)
                            Text("${REGION_Noms[dep.region] ?: dep.region}\nChef-lieu : ${dep.chefLieu}", fontSize = 13.sp, color = Color.Gray)
                        }
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Button(onClick = { deptOuvert = DEPARTEMENTS.random() }, Modifier.fillMaxWidth().height(52.dp)) { Text("Fiche aléatoire") }
        }
    }
}
