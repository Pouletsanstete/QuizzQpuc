// ============================================================================
// QUIZZ QPUC - Thème : Départements français
// ============================================================================
// Appli Android complète en UN SEUL fichier (MainActivity.kt), en Kotlin +
// Jetpack Compose. À coller dans un projet Android sur un service en ligne
// (Replit, Gitpod...) pour générer l'APK sans ordinateur.
//
// Fonctions :
//  - Thème : Culture générale > Départements français (extensible)
//  - Choix des modes : nom, numéro, localisation (carte schématique
//    cliquable), région, gentilé, habitants (±5%), superficie (±10%),
//    chef-lieu, sous-préfectures
//  - Réponse LIBRE avec correction souple (casse, accents, 1-2 fautes)
//  - Slider du nombre de questions (5 à 50, par pas de 5)
//  - Fiches de révision par département + résultats détaillés
//
// NOTE : population / superficie / sous-préfectures sont approximatives
// (INSEE ~2021). À vérifier et corriger dans DEPARTEMENTS avant une
// préparation QPUC sérieuse.
// ============================================================================

package com.example.quizzqpuc

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale
import kotlin.math.abs
import kotlin.math.sqrt
import kotlin.random.Random

// ----------------------------------------------------------------------------
// DONNÉES : 101 départements
// Numéro, Nom, Région, Chef-lieu, Gentilé, Habitants (milliers),
// Superficie (km²), Sous-préfectures, position carte (mapX, mapY en 0..1)
// ----------------------------------------------------------------------------

data class Departement(
    val numero: String,
    val nom: String,
    val region: String,
    val chefLieu: String,
    val gentile: String,
    val populationK: Int,   // en milliers d'habitants
    val superficieKm2: Int,
    val sousPrefs: List<String>,
    val mapX: Float,        // 0.0 = ouest, 1.0 = est (métropole)
    val mapY: Float         // 0.0 = nord, 1.0 = sud (métropole)
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
    "DOM" to "Département d'outre-mer"
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
// CORRECTION SOUPLE (mode QPUC) : normalisation + distance de Levenshtein
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
                m[j] + 1,                                       // suppression
                n[j - 1] + 1,                                   // insertion
                m[j - 1] + if (a[i - 1] == b[j - 1]) 0 else 1   // substitution
            )
        }
        val tmp = m; m = n; n = tmp
    }
    return m[b.length]
}

// Tolérance : distance faible selon la longueur, ou inclusion des mots
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
// MODES DE QUESTION
// ----------------------------------------------------------------------------

enum class Mode(val label: String) {
    NOM("Le nom (n° -> nom)"),
    NUMERO("Le numéro (nom -> n°)"),
    LOCALISATION("Emplacement sur la carte"),
    REGION("La région"),
    GENTILE("Le gentilé"),
    HABITANTS("Nb d'habitants (±5%)"),
    SUPERFICIE("Superficie (±10%)"),
    CHEF_LIEU("Le chef-lieu"),
    SOUS_PREF("Sous-préfectures")
}

enum class Ecran { ACCUEIL, QUIZ, RESULTATS, FICHES }

data class Question(val mode: Mode, val dept: Departement)

// ----------------------------------------------------------------------------
// ACTIVITÉ PRINCIPALE
// ----------------------------------------------------------------------------

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { QuizzQpucApp() }
    }
}

@Composable
fun QuizzQpucApp() {
    var ecran by remember { mutableStateOf(Ecran.ACCUEIL) }

    // Options choisies à l'accueil
    var nbQuestions by remember { mutableStateOf(20f) }
    var modesActifs by remember { mutableStateOf(Mode.entries.toList()) }

    // État du quiz en cours
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
                Ecran.ACCUEIL -> EcranAccueil(
                    nbQuestions,
                    { nbQuestions = it },
                    modesActifs,
                    { modesActifs = it },
                    onDemarrer = { demarrer() },
                    onFiches = { ecran = Ecran.FICHES }
                )
                Ecran.QUIZ -> EcranQuiz(
                    question = questions[index],
                    total = questions.size,
                    index = index,
                    onRepondu = { correct ->
                        reponses = reponses + correct
                        if (index + 1 < questions.size) index++ else ecran = Ecran.RESULTATS
                    },
                    onAbandon = { ecran = Ecran.ACCUEIL }
                )
                Ecran.RESULTATS -> EcranResultats(
                    questions, reponses,
                    onRef = { demarrer() },
                    onAccueil = { ecran = Ecran.ACCUEIL }
                )
                Ecran.FICHES -> EcranFiches(onRetour = { ecran = Ecran.ACCUEIL })
            }
        }
    }
}

// ----------------------------------------------------------------------------
// GÉNÉRATION DES QUESTIONS
// ----------------------------------------------------------------------------

fun genererQuestions(nb: Int, modes: List<Mode>): List<Question> {
    val liste = mutableListOf<Question>()
    repeat(nb) { i ->
        val mode = modes[i % modes.size]
        val dept = DEPARTEMENTS[Random.nextInt(DEPARTEMENTS.size)]
        liste.add(Question(mode, dept))
    }
    return liste
}

// ----------------------------------------------------------------------------
// ÉNONCÉS, BONNES RÉPONSES ET ÉVALUATION
// ----------------------------------------------------------------------------

fun enonce(q: Question): String = when (q.mode) {
    Mode.NOM -> "Quel est le nom du département n° ${q.dept.numero} ?"
    Mode.NUMERO -> "Quel est le numéro du département ${q.dept.nom} ?"
    Mode.LOCALISATION -> "Où se trouve ${q.dept.nom} ? Touche sa zone sur la carte."
    Mode.REGION -> "Dans quelle région se trouve ${q.dept.nom} (${q.dept.numero}) ?"
    Mode.GENTILE -> "Comment appelle-t-on les habitants de ${q.dept.nom} (${q.dept.numero}) ?"
    Mode.HABITANTS -> "Combien d'habitants dans ${q.dept.nom} (${q.dept.numero}) ? (±5%)"
    Mode.SUPERFICIE -> "Quelle est la superficie de ${q.dept.nom} (${q.dept.numero}) ? (±10%)"
    Mode.CHEF_LIEU -> "Quel est le chef-lieu de ${q.dept.nom} (${q.dept.numero}) ?"
    Mode.SOUS_PREF -> "Cite UNE sous-préfecture de ${q.dept.nom} (${q.dept.numero}) :"
}

fun bonneReponse(q: Question): String = when (q.mode) {
    Mode.NOM -> q.dept.nom
    Mode.NUMERO -> q.dept.numero
    Mode.LOCALISATION -> "${q.dept.numero} - ${q.dept.nom}"
    Mode.REGION -> REGION_Noms[q.dept.region] ?: q.dept.region
    Mode.GENTILE -> "Les ${q.dept.gentile}"
    Mode.HABITANTS -> "Environ ${q.dept.populationK * 1000} habitants"
    Mode.SUPERFICIE -> "Environ ${q.dept.superficieKm2} km²"
    Mode.CHEF_LIEU -> q.dept.chefLieu
    Mode.SOUS_PREF -> q.dept.sousPrefs.joinToString(", ")
}

fun evalueReponse(q: Question, reponse: String): Boolean = when (q.mode) {
    Mode.LOCALISATION -> true
    Mode.NOM -> correctFlechie(reponse, listOf(q.dept.nom))
    Mode.NUMERO -> normaliser(reponse) == normaliser(q.dept.numero)
    Mode.REGION -> correctFlechie(reponse, listOf(REGION_Noms[q.dept.region] ?: q.dept.region, q.dept.region))
    Mode.GENTILE -> correctFlechie(reponse, listOf(q.dept.gentile))
    Mode.HABITANTS -> {
        val v = reponse.filter { it.isDigit() }.toIntOrNull() ?: 0
        val cible = q.dept.populationK * 1000
        v > 0 && abs(v - cible) <= cible * 0.05
    }
    Mode.SUPERFICIE -> {
        val v = reponse.filter { it.isDigit() }.toIntOrNull() ?: 0
        val cible = q.dept.superficieKm2
        v > 0 && abs(v - cible) <= cible * 0.10
    }
    Mode.CHEF_LIEU -> correctFlechie(reponse, listOf(q.dept.chefLieu))
    Mode.SOUS_PREF -> q.dept.sousPrefs.isNotEmpty() && correctFlechie(reponse, q.dept.sousPrefs)
}

// ----------------------------------------------------------------------------
// ÉCRAN D'ACCUEIL (thème, modes, slider)
// ----------------------------------------------------------------------------

@Composable
fun EcranAccueil(
    nbQuestions: Float,
    onNbQuestions: (Float) -> Unit,
    modesActifs: List<Mode>,
    onModesActifs: (List<Mode>) -> Unit,
    onDemarrer: () -> Unit,
    onFiches: () -> Unit
) {
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(16.dp))
        Text("Quizz QPUC", fontSize = 32.sp, fontWeight = FontWeight.Bold)
        Text("Thème : Départements français", fontSize = 16.sp, color = Color.Gray)
        Spacer(Modifier.height(24.dp))

        Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
            Column(Modifier.padding(20.dp)) {
                Text("Nombre de questions", fontWeight = FontWeight.Bold)
                Text("${nbQuestions.toInt()} questions", fontSize = 26.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.CenterHorizontally))
                Slider(
                    value = nbQuestions,
                    onValueChange = { onNbQuestions((it / 5f).toInt() * 5f) },
                    valueRange = 5f..50f,
                    steps = 9   // pas de 5 : 5, 10, 15, 20, 25, 30, 35, 40, 45, 50
                )
            }
        }

        Spacer(Modifier.height(16.dp))
        Text("Ce qu'il faut savoir", fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Start))

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
            Text("Commencer le quiz", fontSize = 18.sp)
        }
        OutlinedButton(onClick = onFiches, Modifier.fillMaxWidth().height(56.dp)) {
            Text("Revoir les fiches", fontSize = 18.sp)
        }
        Spacer(Modifier.height(24.dp))
    }
}

// ----------------------------------------------------------------------------
// ÉCRAN QUIZ
// ----------------------------------------------------------------------------

@Composable
fun EcranQuiz(
    question: Question,
    total: Int,
    index: Int,
    onRepondu: (Boolean) -> Unit,
    onAbandon: () -> Unit
) {
    var reponse by remember(question) { mutableStateOf("") }
    var verdict by remember(question) { mutableStateOf<Boolean?>(null) }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Question ${index + 1} / $total", fontWeight = FontWeight.Bold)
            TextButton(onClick = onAbandon) { Text("Abandon") }
        }
                LinearProgressIndicator(progress = { (index + 1f) / total }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(20.dp))

        Card(
            Modifier
                .fillMaxWidth()
                .border(4.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp))
        ) {
            Column(Modifier.padding(20.dp)) {
                Text(enonce(question), fontSize = 22.sp, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(Modifier.height(20.dp))

        // Carte interactive uniquement pour la localisation
        if (question.mode == Mode.LOCALISATION) {
            CarteFrance(deptATrouver = question.dept, verrouille = verdict != null, onTouche = { verdict = it })
        } else {
            TextField(
                value = reponse,
                onValueChange = { reponse = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Ta réponse") },
                singleLine = true,
                enabled = verdict == null
            )
        }

        Spacer(Modifier.height(16.dp))

        when (verdict) {
            null -> {
                if (question.mode != Mode.LOCALISATION) {
                    Button(
                        onClick = { verdict = evalueReponse(question, reponse) },
                        Modifier.fillMaxWidth().height(52.dp),
                        enabled = reponse.isNotBlank()
                    ) { Text("Valider", fontSize = 18.sp) }
                }
            }
            else -> {
                val couleur = if (verdict == true) Color(0xFF2E7D32) else Color(0xFFC62828)
                Text(
                    if (verdict == true) "Correct !" else "Faux",
                    fontSize = 26.sp, color = couleur, fontWeight = FontWeight.Bold
                )
                Text("Réponse : ${bonneReponse(question)}", fontSize = 17.sp, color = couleur)
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = { onRepondu(verdict == true) },
                    Modifier.fillMaxWidth().height(52.dp)
                ) {
                    Text(if (index + 1 < total) "Suite" else "Voir les résultats", fontSize = 18.sp)
                }
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}

// ----------------------------------------------------------------------------
// CARTE DE FRANCE SCHÉMATIQUE (zones cliquables)
// ----------------------------------------------------------------------------
// Chaque département métropolitain est dessiné à sa position approximative
// (mapX, mapY) sous forme de petite pastille. Le joueur touche l'écran :
// on prend le département le plus proche du toucher, avec une tolérance
// (6 % de la carte). Simple mais parfait pour travailler l'emplacement.

@Composable
fun CarteFrance(deptATrouver: Departement, verrouille: Boolean, onTouche: (Boolean) -> Unit) {
    var infoTap by remember { mutableStateOf("Touche la zone où tu penses qu'il se trouve :") }
    var couleurZone by remember { mutableStateOf(Color(0xFF90A4AE)) }

    BoxWithConstraints(
        Modifier
            .fillMaxWidth()
            .height(460.dp)
            .background(Color(0xFFDFF0FF), RoundedCornerShape(12.dp))
            .border(2.dp, couleurZone, RoundedCornerShape(12.dp))
            .pointerInput(deptATrouver.numero, verrouille) {
                if (verrouille) return@pointerInput
                detectTapGestures { offset ->
                    val x = offset.x / size.width
                    val y = offset.y / size.height
                    // On compare aux positions relatives utilisées pour dessiner
                    val rx = x / 0.92f
                    val ry = (y - 0.10f) / 0.72f
                    val metropole = DEPARTEMENTS.filter { it.mapY < 0.96f }
                    val proche = metropole.minByOrNull { d ->
                        val dx = d.mapX - rx; val dy = d.mapY - ry
                        dx * dx + dy * dy
                    }
                    if (proche != null) {
                        val dist = sqrt((proche.mapX - rx) * (proche.mapX - rx) + (proche.mapY - ry) * (proche.mapY - ry))
                        if (dist > 0.06f) {
                            infoTap = "Assez loin d'un département, réessaie..."
                        } else if (proche.numero == deptATrouver.numero) {
                            infoTap = "Correct ! Tu as trouvé ${deptATrouver.nom}."
                            couleurZone = Color(0xFF66BB6A)
                            onTouche(true)
                        } else {
                            infoTap = "Faux : tu as touché ${proche.nom} (${proche.numero})."
                            couleurZone = Color(0xFFEF5350)
                            onTouche(false)
                        }
                    }
                }
            }
            .padding(6.dp)
    ) {
        Column(Modifier.padding(8.dp)) {
            Text("FRANCE (carte schématique)", fontSize = 12.sp, color = Color.Gray)
            Text(infoTap, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
        val w = maxWidth
        val h = maxHeight
        DEPARTEMENTS
            .filter { it.mapY < 0.96f }
            .forEach { d ->
                Box(
                    Modifier
                                                .offset(x = w * (d.mapX * 0.92f), y = h * (0.10f + d.mapY * 0.72f))
                        .size(width = 14.dp, height = 14.dp)
                        .background(
                            if (d.numero == deptATrouver.numero) Color(0xFF1565C0)
                            else couleurZone,
                            RoundedCornerShape(3.dp)
                        )
                )
            }
        Text(
            "Outre-mer (971-976) : pas sur cette carte",
            Modifier.align(Alignment.BottomStart).padding(6.dp),
            fontSize = 11.sp, color = Color.Gray
        )
    }
}

// ----------------------------------------------------------------------------
// RÉSULTATS
// ----------------------------------------------------------------------------

@Composable
fun EcranResultats(
    questions: List<Question>,
    reponses: List<Boolean>,
    onRef: () -> Unit,
    onAccueil: () -> Unit
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
            Button(onClick = onRef, Modifier.weight(1f)) { Text("Refaire un quiz") }
            Spacer(Modifier.width(12.dp))
            OutlinedButton(onClick = onAccueil, Modifier.weight(1f)) { Text("Menu") }
        }
        Spacer(Modifier.height(20.dp))

        questions.forEachIndexed { i, q ->
            val ok = reponses.getOrNull(i) ?: false
            Row(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                Text(if (ok) "✔" else "✘", fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(8.dp))
                Column {
                    Text(
                        "${q.mode.label} - ${q.dept.numero} ${q.dept.nom}",
                        fontSize = 14.sp, fontWeight = FontWeight.Bold
                    )
                    if (!ok) {
                        Text("Réponse attendue : ${bonneReponse(q)}", fontSize = 13.sp, color = Color.Gray)
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
    val filtres = DEPARTEMENTS.filter {
        it.nom.contains(recherche, ignoreCase = true) ||
            it.numero.contains(recherche) ||
            (REGION_Noms[it.region]?.contains(recherche, ignoreCase = true) == true)
    }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onRetour) { Text("← Retour") }
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
        LazyColumn(Modifier.fillMaxSize()) {
            items(filtres) { d ->
                Card(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Column(Modifier.padding(12.dp)) {
                        Text("${d.numero} - ${d.nom}", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text("Région : ${REGION_Noms[d.region] ?: d.region}", fontSize = 14.sp)
                        Text("Chef-lieu : ${d.chefLieu}", fontSize = 14.sp)
                        Text("Gentilé : les ${d.gentile}", fontSize = 14.sp)
                        Text("Population : environ ${d.populationK * 1000} hab. · Superficie : ${d.superficieKm2} km²", fontSize = 14.sp)
                        if (d.sousPrefs.isNotEmpty()) {
                            Text("Sous-préfectures : ${d.sousPrefs.joinToString(", ")}", fontSize = 14.sp)
                        }
                    }
                }
            }
        }
    }
}
