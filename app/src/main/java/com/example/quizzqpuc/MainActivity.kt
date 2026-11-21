// ============================================================================
// QUIZZ QPUC v2 - Thème : Départements français
// ============================================================================

package com.example.quizzqpuc

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.sqrt
import kotlin.random.Random

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
    val mapX: Float,
    val mapY: Float,
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

val REGION_Couleurs = mapOf(
    "ARA" to Color(0xFFDCEDC8),
    "GE" to Color(0xFFFFF9C4),
    "HDF" to Color(0xFFFFE0B2),
    "NA" to Color(0xFFE1BEE7),
    "OCC" to Color(0xFFFFCCBC),
    "PACA" to Color(0xFFB2EBF2),
    "PDL" to Color(0xFFC8E6C9),
    "NOR" to Color(0xFFD1C4E9),
    "BRE" to Color(0xFFB3E5FC),
    "CVL" to Color(0xFFFFF59D),
    "BFC" to Color(0xFFFFCDD2),
    "IDF" to Color(0xFFCFD8DC),
    "COR" to Color(0xFFB2DFDB),
    "DOM" to Color(0xFFF5F5F5)
)

val DEPARTEMENTS = listOf(
    Departement("01", "Ain", "ARA", "Bourg-en-Bresse", "Aindinois", 652, 5762, listOf("Belley", "Gex", "Nantua", "Valserhône"), 0.83f, 0.70f),
    Departement("02", "Aisne", "HDF", "Laon", "Axonais", 531, 7369, listOf("Château-Thierry", "Saint-Quentin", "Soissons", "Vervins"), 0.80f, 0.36f),
    Departement("03", "Allier", "ARA", "Moulins", "Bourbonnais", 335, 7340, listOf("Commentry", "Montluçon", "Vichy"), 0.76f, 0.63f),
    Departement("04", "Alpes-de-Haute-Provence", "PACA", "Digne-les-Bains", "Bas-Alpins", 164, 6925, listOf("Barcelonnette", "Castellane", "Forcalquier"), 0.87f, 0.88f),
    Departement("05", "Hautes-Alpes", "PACA", "Gap", "Alpins", 142, 5549, listOf("Briançon"), 0.89f, 0.83f),
    Departement("06", "Alpes-Maritimes", "PACA", "Nice", "Maralpins", 1094, 4299, listOf("Grasse"), 0.91f, 0.92f),
    Departement("07", "Ardèche", "ARA", "Privas", "Ardéchois", 328, 5529, listOf("Tournon-sur-Rhône"), 0.82f, 0.80f),
    Departement("08", "Ardennes", "GE", "Charleville-Mézières", "Ardennais", 274, 5229, listOf("Rethel", "Sedan", "Vouziers"), 0.85f, 0.26f),
    Departement("09", "Ariège", "OCC", "Foix", "Ariégeois", 154, 4890, listOf("Pamiers", "Saint-Girons"), 0.63f, 0.89f),
    Departement("10", "Aube", "GE", "Troyes", "Aubois", 311, 6004, listOf("Bar-sur-Aube", "Nogent-sur-Seine"), 0.84f, 0.45f),
    Departement("11", "Aude", "OCC", "Carcassonne", "Audois", 376, 6139, listOf("Limoux", "Narbonne"), 0.70f, 0.90f),
    Departement("12", "Aveyron", "OCC", "Rodez", "Aveyronnais", 279, 8735, listOf("Millau", "Villefranche-de-Rouergue"), 0.72f, 0.81f),
    Departement("13", "Bouches-du-Rhône", "PACA", "Marseille", "Bucco-Rhodaniens", 2039, 5087, listOf("Aix-en-Provence", "Arles", "Istres"), 0.85f, 0.94f),
    Departement("14", "Calvados", "NOR", "Caen", "Calvadosiens", 697, 5548, listOf("Bayeux", "Falaise", "Lisieux", "Vire"), 0.28f, 0.33f),
    Departement("15", "Cantal", "ARA", "Aurillac", "Cantaliens", 144, 5726, listOf("Mauriac", "Saint-Flour"), 0.73f, 0.72f),
    Departement("16", "Charente", "NA", "Angoulême", "Charentais", 351, 5956, listOf("Cognac", "Confolens", "Ruffec"), 0.44f, 0.74f),
    Departement("17", "Charente-Maritime", "NA", "La Rochelle", "Charentais-Maritimes", 665, 6864, listOf("Jonzac", "Rochefort", "Saintes", "Saint-Jean-d'Angély"), 0.40f, 0.77f),
    Departement("18", "Cher", "CVL", "Bourges", "Berruyers", 300, 7235, listOf("Saint-Amand-Montrond", "Vierzon"), 0.72f, 0.56f),
    Departement("19", "Corrèze", "NA", "Tulle", "Corréziens", 240, 5857, listOf("Brive-la-Gaillarde", "Ussel"), 0.61f, 0.75f),
    Departement("2A", "Corse-du-Sud", "COR", "Ajaccio", "Sud-Corses", 158, 4014, listOf("Porto-Vecchio", "Sartène"), 0.97f, 0.96f),
    Departement("2B", "Haute-Corse", "COR", "Bastia", "Haut-Corses", 187, 4666, listOf("Calvi", "Corte"), 0.99f, 0.90f),
    Departement("21", "Côte-d'Or", "BFC", "Dijon", "Dijonnais", 537, 8763, listOf("Beaune", "Montbard", "Semur-en-Auxois"), 0.86f, 0.55f),
    Departement("22", "Côtes-d'Armor", "BRE", "Saint-Brieuc", "Costarmoricains", 604, 6878, listOf("Dinan", "Guingamp", "Lannion"), 0.13f, 0.18f),
    Departement("23", "Creuse", "NA", "Guéret", "Creusois", 117, 5565, listOf("Aubusson"), 0.65f, 0.70f),
    Departement("24", "Dordogne", "NA", "Périgueux", "Périgordins", 414, 9060, listOf("Bergerac", "Nontron", "Sarlat-la-Canéda"), 0.51f, 0.79f),
    Departement("25", "Doubs", "BFC", "Besançon", "Doubistes", 535, 5232, listOf("Montbéliard", "Pontarlier"), 0.94f, 0.64f),
    Departement("26", "Drôme", "ARA", "Valence", "Drômois", 519, 6530, listOf("Die", "Nyons"), 0.84f, 0.84f),
    Departement("27", "Eure", "NOR", "Évreux", "Eurois", 602, 6040, listOf("Les Andelys", "Bernay", "Louviers", "Pont-Audemer"), 0.42f, 0.25f),
    Departement("28", "Eure-et-Loir", "CVL", "Chartres", "Euréliens", 431, 5880, listOf("Châteaudun", "Dreux", "Nogent-le-Rotrou"), 0.52f, 0.44f),
    Departement("29", "Finistère", "BRE", "Quimper", "Finistériens", 919, 6733, listOf("Brest", "Châteaulin", "Morlaix"), 0.04f, 0.20f),
    Departement("30", "Gard", "OCC", "Nîmes", "Gardois", 751, 5853, listOf("Alès", "Le Vigan"), 0.76f, 0.88f),
    Departement("31", "Haute-Garonne", "OCC", "Toulouse", "Haut-Garonnais", 1404, 6309, listOf("Muret", "Saint-Gaudens"), 0.58f, 0.87f),
    Departement("32", "Gers", "OCC", "Auch", "Gersois", 192, 6257, listOf("Condom", "L'Isle-Jourdain", "Mirande"), 0.55f, 0.82f),
    Departement("33", "Gironde", "NA", "Bordeaux", "Bordelais", 1655, 10024, listOf("Blaye", "Langon", "Lesparre-Médoc", "Libourne"), 0.44f, 0.82f),
    Departement("34", "Hérault", "OCC", "Montpellier", "Héraultais", 1143, 6101, listOf("Béziers", "Lodève"), 0.71f, 0.90f),
    Departement("35", "Ille-et-Vilaine", "BRE", "Rennes", "Ille-Vilainais", 1074, 6775, listOf("Fougères", "Redon", "Saint-Malo"), 0.16f, 0.22f),
    Departement("36", "Indre", "CVL", "Châteauroux", "Castelroussins", 217, 6883, listOf("Le Blanc", "La Châtre", "Issoudun"), 0.66f, 0.63f),
    Departement("37", "Indre-et-Loire", "CVL", "Tours", "Tourangeaux", 611, 6127, listOf("Chinon", "Loches"), 0.57f, 0.60f),
    Departement("38", "Isère", "ARA", "Grenoble", "Isérois", 1275, 7431, listOf("La Tour-du-Pin", "Vienne"), 0.87f, 0.76f),
    Departement("39", "Jura", "BFC", "Lons-le-Saunier", "Jurassiens", 259, 4999, listOf("Dole", "Saint-Claude"), 0.92f, 0.62f),
    Departement("40", "Landes", "NA", "Mont-de-Marsan", "Landais", 420, 9243, listOf("Dax", "Saint-Sever"), 0.48f, 0.85f),
    Departement("41", "Loir-et-Cher", "CVL", "Blois", "Blésois", 333, 6314, listOf("Romorantin-Lanthenay", "Vendôme"), 0.62f, 0.55f),
    Departement("42", "Loire", "ARA", "Saint-Étienne", "Stéphanois", 767, 4781, listOf("Montbrison", "Roanne"), 0.78f, 0.74f),
    Departement("43", "Haute-Loire", "ARA", "Le Puy-en-Velay", "Altiligériens", 227, 4977, listOf("Brioude", "Yssingeaux"), 0.77f, 0.78f),
    Departement("44", "Loire-Atlantique", "PDL", "Nantes", "Nantais", 1459, 7156, listOf("Ancenis-Saint-Géréon", "Châteaubriant", "Saint-Nazaire"), 0.31f, 0.48f),
    Departement("45", "Loiret", "CVL", "Orléans", "Orléanais", 683, 6775, listOf("Montargis", "Pithiviers"), 0.72f, 0.50f),
    Departement("46", "Lot", "OCC", "Cahors", "Lotois", 172, 5217, listOf("Figeac", "Gourdon"), 0.63f, 0.78f),
    Departement("47", "Lot-et-Garonne", "NA", "Agen", "Agenais", 333, 5362, listOf("Marmande", "Nérac", "Villeneuve-sur-Lot"), 0.51f, 0.83f),
    Departement("48", "Lozère", "OCC", "Mende", "Lozériens", 76, 5167, listOf("Florac"), 0.74f, 0.85f),
    Departement("49", "Maine-et-Loire", "PDL", "Angers", "Angevins", 817, 7136, listOf("Baugé", "Cholet", "Saumur", "Segré"), 0.29f, 0.44f),
    Departement("50", "Manche", "NOR", "Saint-Lô", "Manchois", 494, 5938, listOf("Avranches", "Cherbourg-en-Cotentin", "Coutances", "Granville"), 0.17f, 0.20f),
    Departement("51", "Marne", "GE", "Châlons-en-Champagne", "Marnais", 556, 8162, listOf("Épernay", "Reims", "Sainte-Menehould", "Vitry-le-François"), 0.84f, 0.38f),
    Departement("52", "Haute-Marne", "GE", "Chaumont", "Chaumontais", 171, 6211, listOf("Langres", "Saint-Dizier"), 0.83f, 0.49f),
    Departement("53", "Mayenne", "PDL", "Laval", "Lavallois", 309, 5175, listOf("Château-Gontier", "Mayenne"), 0.26f, 0.36f),
    Departement("54", "Meurthe-et-Moselle", "GE", "Nancy", "Nancéiens", 734, 5246, listOf("Lunéville", "Toul"), 0.88f, 0.34f),
    Departement("55", "Meuse", "GE", "Bar-le-Duc", "Barisiens", 184, 6211, listOf("Commercy", "Verdun"), 0.87f, 0.41f),
    Departement("56", "Morbihan", "BRE", "Vannes", "Vannetais", 772, 6823, listOf("Lorient", "Ploërmel", "Pontivy"), 0.12f, 0.28f),
    Departement("57", "Moselle", "GE", "Metz", "Messins", 1049, 6216, listOf("Sarreguemines", "Sarrebourg", "Thionville"), 0.90f, 0.32f),
    Departement("58", "Nièvre", "BFC", "Nevers", "Nivernais", 203, 6817, listOf("Château-Chinon", "Clamecy", "Cosne-Cours-sur-Loire"), 0.78f, 0.60f),
    Departement("59", "Nord", "HDF", "Lille", "Nordistes", 2612, 5743, listOf("Avesnes-sur-Helpe", "Cambrai", "Douai", "Dunkerque", "Valenciennes"), 0.80f, 0.24f),
    Departement("60", "Oise", "HDF", "Beauvais", "Oisiens", 829, 5860, listOf("Compiègne", "Senlis"), 0.72f, 0.34f),
    Departement("61", "Orne", "NOR", "Alençon", "Ornais", 279, 6103, listOf("Argentan", "Domfront", "Mortagne-au-Perche"), 0.36f, 0.34f),
    Departement("62", "Pas-de-Calais", "HDF", "Arras", "Pas-de-Calaisiens", 1467, 6671, listOf("Béthune", "Boulogne-sur-Mer", "Calais", "Lens", "Montreuil", "Saint-Omer"), 0.70f, 0.26f),
    Departement("63", "Puy-de-Dôme", "ARA", "Clermont-Ferrand", "Clermontois", 668, 7970, listOf("Ambert", "Issoire", "Riom", "Thiers"), 0.75f, 0.71f),
    Departement("64", "Pyrénées-Atlantiques", "NA", "Pau", "Pyrénnéens-Atlantiques", 683, 7645, listOf("Bayonne", "Oloron-Sainte-Marie"), 0.49f, 0.89f),
    Departement("65", "Hautes-Pyrénées", "OCC", "Tarbes", "Haut-Pyrénéens", 231, 4464, listOf("Argelès-Gazost", "Bagnères-de-Bigorre"), 0.52f, 0.88f),
    Departement("66", "Pyrénées-Orientales", "OCC", "Perpignan", "Perpignanais", 487, 4116, listOf("Céret", "Prades"), 0.66f, 0.93f),
    Departement("67", "Bas-Rhin", "GE", "Strasbourg", "Strasbourgeois", 1137, 4775, listOf("Haguenau", "Molsheim", "Saverne", "Sélestat"), 0.94f, 0.52f),
    Departement("68", "Haut-Rhin", "GE", "Colmar", "Haut-Rhinois", 761, 3525, listOf("Altkirch", "Guebwiller", "Mulhouse", "Thann"), 0.92f, 0.58f),
    Departement("69", "Rhône", "ARA", "Lyon", "Lyonnais", 1881, 5316, listOf("Villefranche-sur-Saône"), 0.80f, 0.72f),
    Departement("70", "Haute-Saône", "BFC", "Vesoul", "Haut-Saônois", 236, 5360, listOf("Gray", "Lure"), 0.91f, 0.53f),
    Departement("71", "Saône-et-Loire", "BFC", "Mâcon", "Mâconnais", 543, 8575, listOf("Autun", "Chalon-sur-Saône", "Charolles", "Louhans"), 0.83f, 0.66f),
    Departement("72", "Sarthe", "PDL", "Le Mans", "Mancelles", 568, 6206, listOf("La Flèche", "Mamers"), 0.34f, 0.45f),
    Departement("73", "Savoie", "ARA", "Chambéry", "Savoyards", 440, 6171, listOf("Albertville", "Saint-Jean-de-Maurienne"), 0.86f, 0.78f),
    Departement("74", "Haute-Savoie", "ARA", "Annecy", "Haut-Savoyards", 837, 4388, listOf("Bonneville", "Saint-Julien-en-Genevois", "Thonon-les-Bains"), 0.88f, 0.71f),
    Departement("75", "Paris", "IDF", "Paris", "Parisiens", 2148, 105, emptyList(), 0.74f, 0.42f),
    Departement("76", "Seine-Maritime", "NOR", "Rouen", "Rouennais", 1255, 6328, listOf("Dieppe", "Le Havre"), 0.54f, 0.20f),
    Departement("77", "Seine-et-Marne", "IDF", "Melun", "Melunais", 1450, 5915, listOf("Fontainebleau", "Meaux", "Provins", "Torcy"), 0.77f, 0.45f),
    Departement("78", "Yvelines", "IDF", "Versailles", "Versaillais", 1451, 2284, listOf("Mantes-la-Jolie", "Rambouillet", "Saint-Germain-en-Laye"), 0.71f, 0.43f),
    Departement("79", "Deux-Sèvres", "NA", "Niort", "Niortais", 375, 5999, listOf("Bressuire", "Thouars"), 0.37f, 0.55f),
    Departement("80", "Somme", "HDF", "Amiens", "Amiénois", 570, 6170, listOf("Abbeville", "Montdidier", "Péronne"), 0.68f, 0.30f),
    Departement("81", "Tarn", "OCC", "Albi", "Albigeois", 393, 5758, listOf("Castres", "Gaillac"), 0.65f, 0.84f),
    Departement("82", "Tarn-et-Garonne", "OCC", "Montauban", "Montalbanais", 261, 3718, listOf("Castelsarrasin"), 0.59f, 0.82f),
    Departement("83", "Var", "PACA", "Toulon", "Toulonnais", 1081, 5975, listOf("Brignoles", "Draguignan"), 0.88f, 0.93f),
    Departement("84", "Vaucluse", "PACA", "Avignon", "Vauclusiens", 569, 3567, listOf("Apt", "Carpentras"), 0.83f, 0.87f),
    Departement("85", "Vendée", "PDL", "La Roche-sur-Yon", "Yonnais", 701, 6720, listOf("Fontenay-le-Comte", "Les Sables-d'Olonne"), 0.32f, 0.52f),
    Departement("86", "Vienne", "NA", "Poitiers", "Poitevins", 439, 6990, listOf("Châtellerault", "Chauvigny", "Montmorillon"), 0.41f, 0.60f),
    Departement("87", "Haute-Vienne", "NA", "Limoges", "Limougeauds", 373, 5520, listOf("Bellac", "Rochechouart", "Saint-Yrieix-la-Perche"), 0.56f, 0.70f),
    Departement("88", "Vosges", "GE", "Épinal", "Vosgiens", 364, 5874, listOf("Mirecourt", "Neufchâteau", "Remiremont", "Saint-Dié-des-Vosges"), 0.89f, 0.46f),
    Departement("89", "Yonne", "BFC", "Auxerre", "Icaunais", 336, 7427, listOf("Avallon", "Migennes", "Sens", "Tonnerre"), 0.82f, 0.52f),
    Departement("90", "Territoire de Belfort", "BFC", "Belfort", "Belfortains", 141, 609, listOf("Delle"), 0.93f, 0.61f),
    Departement("91", "Essonne", "IDF", "Évry-Courcouronnes", "Essonniens", 1305, 1804, listOf("Étampes", "Palaiseau"), 0.74f, 0.45f),
    Departement("92", "Hauts-de-Seine", "IDF", "Nanterre", "Altoséquanais", 1627, 176, listOf("Antony", "Boulogne-Billancourt"), 0.73f, 0.43f),
    Departement("93", "Seine-Saint-Denis", "IDF", "Bobigny", "Séquano-Dionysiens", 1664, 236, listOf("Le Raincy", "Montreuil"), 0.75f, 0.42f),
    Departement("94", "Val-de-Marne", "IDF", "Créteil", "Créteilais", 1427, 245, listOf("L'Haÿ-les-Roses", "Nogent-sur-Marne"), 0.76f, 0.44f),
    Departement("95", "Val-d'Oise", "IDF", "Cergy", "Val-d'Oisiens", 1261, 1246, listOf("Argenteuil", "Sarcelles"), 0.71f, 0.38f),
    Departement("971", "Guadeloupe", "DOM", "Basse-Terre", "Guadeloupéens", 379, 1628, listOf("Pointe-à-Pitre"), 0.06f, 0.98f),
    Departement("972", "Martinique", "DOM", "Fort-de-France", "Martiniquais", 357, 1128, listOf("Le Marin", "La Trinité"), 0.12f, 0.99f),
    Departement("973", "Guyane", "DOM", "Cayenne", "Guyanais", 292, 83534, listOf("Kourou", "Saint-Laurent-du-Maroni"), 0.18f, 0.99f),
    Departement("974", "La Réunion", "DOM", "Saint-Denis", "Réunionnais", 871, 2504, listOf("Saint-Benoît", "Saint-Paul", "Saint-Pierre"), 0.24f, 0.99f),
    Departement("976", "Mayotte", "DOM", "Mamoudzou", "Mahorais", 321, 374, listOf("Dzaoudzi"), 0.30f, 0.99f)
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

enum class Mode(val label: String, val libelleChamp: String) {
    NOM("Le nom (n° -> nom)", "Nom du département"),
    NUMERO("Le numéro (nom -> n°)", "Numéro"),
    REGION("La région", "Région"),
    GENTILE("Le gentilé", "Gentilé (les ...)"),
    HABITANTS("Nb d'habitants (±5%)", "Habitants (ex : 500000)"),
    SUPERFICIE("Superficie (±10%)", "Superficie km²"),
    CHEF_LIEU("Le chef-lieu", "Chef-lieu"),
    SOUS_PREF("Sous-préfectures", "Une sous-préfecture"),
    LOCALISATION("La localisation (carte)", "Localisation")
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

fun genererQuestions(nb: Int, modes: List<Mode>): List<Question> {
    val liste = mutableListOf<Question>()
    repeat(nb) { i ->
        val dept = DEPARTEMENTS[Random.nextInt(DEPARTEMENTS.size)]
        val champs = modes.filter { it != Mode.LOCALISATION }
        val carte = Mode.LOCALISATION in modes && (champs.isEmpty() || i % 5 == 0)
        liste.add(
            if (carte) Question(dept, true, emptyList())
            else Question(dept, false, champs)
        )
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
        Text(libelle, fontSize = 14.sp, color = Color.Gray, modifier = Modifier.width(130.dp))
        Text(valeur, fontSize = 15.sp)
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
    Mode.SOUS_PREF -> d.sousPrefs.firstOrNull() ?: ""
    Mode.LOCALISATION -> ""
}

fun champJuste(mode: Mode, d: Departement, reponse: String): Boolean = when (mode) {
    Mode.NOM -> correctFlechie(reponse, listOf(d.nom))
    Mode.NUMERO -> normaliser(reponse) == normaliser(d.numero)
    Mode.REGION -> correctFlechie(reponse, listOf(REGION_Noms[d.region] ?: d.region))
    Mode.GENTILE -> correctFlechie(reponse, listOf(d.gentile))
    Mode.HABITANTS -> {
        val v = reponse.filter { it.isDigit() }.toIntOrNull() ?: 0
        val cible = d.populationK * 1000
        v > 0 && abs(v - cible) <= cible * 0.05
    }
    Mode.SUPERFICIE -> {
        val v = reponse.filter { it.isDigit() }.toIntOrNull() ?: 0
        val cible = d.superficieKm2
        v > 0 && abs(v - cible) <= cible * 0.10
    }
    Mode.CHEF_LIEU -> correctFlechie(reponse, listOf(d.chefLieu))
    Mode.SOUS_PREF -> d.sousPrefs.isNotEmpty() && correctFlechie(reponse, d.sousPrefs)
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
        mutableStateOf(question.champs.associateWith { "" } as MutableMap<Mode, String>)
    }
    var verdicts by remember(question) { mutableStateOf<Map<Mode, Boolean>>(emptyMap()) }
    var verdictCarte by remember(question) { mutableStateOf<Boolean?>(null) }
    var correct by remember(question) { mutableStateOf(false) }
    val aValider = verdicts.isNotEmpty() || verdictCarte != null

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

        LazyColumn(Modifier.weight(1f)) {
            item {
                Card(
                    Modifier
                        .fillMaxWidth()
                        .border(4.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp))
                ) {
                    Column(Modifier.padding(14.dp)) {
                        if (question.carte) {
                            Text(
                                "Où se trouve ${question.dept.nom} ? Pince pour zoomer, puis touche sa zone.",
                                fontSize = 18.sp, fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(8.dp))
                            CarteFranceLocked(
                                deptRevele = if (verdictCarte != null) question.dept else null,
                                onTapDept = { d -> if (verdictCarte == null) verdictCarte = (d.numero == question.dept.numero) }
                            )
                        } else {
                            Text("Complète la fiche", fontSize = 19.sp, fontWeight = FontWeight.Bold)
                            if (question.dept.imageUrl != null) {
                                ImageUrl(question.dept.imageUrl, "Illustration")
                            }
                            val tous = listOf(
                                Mode.NUMERO, Mode.NOM, Mode.REGION, Mode.GENTILE,
                                Mode.HABITANTS, Mode.SUPERFICIE, Mode.CHEF_LIEU, Mode.SOUS_PREF
                            )
                            tous.forEach { mode ->
                                if (mode in question.champs) {
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
                                } else {
                                    LigneFiche(mode.libelleChamp, valeurFiche(question.dept, mode))
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(10.dp))

        when {
            !aValider -> {
                Button(
                    onClick = {
                        if (question.carte) {
                            verdictCarte?.let {
                                correct = it
                            }
                        } else {
                            val nouveaux = question.champs.associateWith {
                                champJuste(it, question.dept, saisies[it] ?: "")
                            }
                            verdicts = nouveaux
                            correct = nouveaux.values.all { it }
                        }
                    },
                    Modifier.fillMaxWidth().height(54.dp),
                    enabled = if (question.carte) verdictCarte != null
                    else question.champs.all { (saisies[it] ?: "").isNotBlank() }
                ) {
                    Text(if (question.carte && verdictCarte == null) "Touche la carte pour répondre" else "Valider", fontSize = 18.sp)
                }
            }
            index + 1 < total -> {
                val ok = if (question.carte) verdictCarte == true else verdicts.values.all { it }
                Text(
                    if (ok) "Correct !" else "Faux",
                    fontSize = 24.sp, fontWeight = FontWeight.Bold,
                    color = if (ok) Color(0xFF2E7D32) else Color(0xFFC62828),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                Button(
                    onClick = { onRepondu(correct) },
                    Modifier.fillMaxWidth().height(54.dp)
                ) { Text("Suite", fontSize = 18.sp) }
            }
            else -> {
                Button(
                    onClick = { onRepondu(correct) },
                    Modifier.fillMaxWidth().height(54.dp)
                ) { Text("Voir les résultats", fontSize = 18.sp) }
            }
        }
        Spacer(Modifier.height(16.dp))
    }
}

// ----------------------------------------------------------------------------
// CARTE : zones de régions en fond, zoom/pan à deux doigts, départements
// invisibles puis révélation après réponse
// ----------------------------------------------------------------------------

@Composable
fun CarteFranceLocked(
    deptRevele: Departement?,
    onTapDept: (Departement) -> Unit = {}
) {
    var echelle by remember { mutableStateOf(1f) }
    var decalage by remember { mutableStateOf(Offset.Zero) }
    var taille by remember { mutableStateOf(IntSize.Zero) }

    Box(
        Modifier
            .fillMaxWidth()
            .height(520.dp)
            .background(Color(0xFFEAF3FB), RoundedCornerShape(12.dp))
            .border(2.dp, Color(0xFFB0C4DE), RoundedCornerShape(12.dp))
            .onSizeChanged { taille = it }
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    echelle = (echelle * zoom).coerceIn(1f, 10f)
                    val maxDX = (size.width * (echelle - 1f)) / 2f
                    val maxDY = (size.height * (echelle - 1f)) / 2f
                    if (echelle > 1f) {
                        decalage = Offset(
                            (decalage.x + pan.x).coerceIn(-maxDX, maxDX),
                            (decalage.y + pan.y).coerceIn(-maxDY, maxDY)
                        )
                    } else {
                        decalage = Offset.Zero
                    }
                }
            }
            .pointerInput(deptRevele) {
                detectTapGestures { offset ->
                    if (taille == IntSize.Zero || deptRevele != null) return@detectTapGestures
                    val px = (offset.x - size.width / 2f - decalage.x) / echelle + size.width / 2f
                    val py = (offset.y - size.height / 2f - decalage.y) / echelle + size.height / 2f
                    val rx = px / size.width / 0.92f
                    val ry = (py / size.height - 0.12f) / 0.70f
                    val metropole = DEPARTEMENTS.filter { it.mapY < 0.96f }
                    val proche = metropole.minByOrNull { d ->
                        val dx = d.mapX - rx; val dy = d.mapY - ry
                        dx * dx + dy * dy
                    }
                    if (proche != null) {
                        val dist = sqrt(
                            (proche.mapX - rx) * (proche.mapX - rx) +
                                (proche.mapY - ry) * (proche.mapY - ry)
                        )
                        if (dist > 0.035f / echelle) {
                            // tap trop loin : on ignore, l'utilisateur zoome
                        } else {
                            onTapDept(proche)
                        }
                    }
                }
            }
            .graphicsLayer {
                scaleX = echelle
                scaleY = echelle
                translationX = decalage.x
                translationY = decalage.y
            }
    ) {
        BoxWithConstraints(Modifier.fillMaxSize()) {
            val w = maxWidth
            val h = maxHeight
            Column(Modifier.padding(6.dp)) {
                Text("FRANCE - zones de régions", fontSize = 11.sp, color = Color.Gray)
            }
            REGION_Couleurs.forEach { (reg, couleur) ->
                val deps = DEPARTEMENTS.filter { it.region == reg && it.mapY < 0.96f }
                if (deps.size >= 1) {
                    val minX = deps.minOf { it.mapX }; val maxX = deps.maxOf { it.mapX }
                    val minY = deps.minOf { it.mapY }; val maxY = deps.maxOf { it.mapY }
                    Box(
                        Modifier
                            .offset(x = w * (minX * 0.92f - 0.02f), y = h * (0.12f + minY * 0.70f - 0.025f))
                            .width(w * ((maxX - minX + 0.04f) * 0.92f))
                            .height(h * ((maxY - minY + 0.05f) * 0.70f) + 24.dp)
                            .background(couleur.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                            .border(1.dp, couleur, RoundedCornerShape(10.dp))
                    ) {
                        Text(
                            REGION_Noms[reg] ?: reg,
                            fontSize = 10.sp,
                            color = Color(0xFF37474F),
                            modifier = Modifier.padding(start = 6.dp, top = 2.dp)
                        )
                    }
                }
            }
            if (deptRevele != null && deptRevele.mapY < 0.96f) {
                Box(
                    Modifier
                        .offset(x = w * (deptRevele.mapX * 0.92f) - 9.dp, y = h * (0.12f + deptRevele.mapY * 0.70f) - 9.dp)
                        .size(18.dp)
                        .background(Color(0xFF1B5E20), CircleShape)
                        .border(2.dp, Color.White, CircleShape)
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
                            else q.champs.joinToString(" | ") { "${it.libelleChamp} = ${valeurFiche(q.dept, it)}" },
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

    if (deptOuvert != null) {
        val d = deptOuvert
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
                    Text("Superficie : ${d.superficieKm2} km²", fontSize = 15.sp)
                    if (d.sousPrefs.isNotEmpty())
                        Text("Sous-préfectures : ${d.sousPrefs.joinToString(", ")}", fontSize = 15.sp)
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
