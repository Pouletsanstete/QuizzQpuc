// ============================================================================
// « Le saviez-vous ? » : fun facts (et grandes villes) affichés sur les fiches.
// Données : assets/faits_elements.json, faits_depts.json, faits_etats.json
// ============================================================================

package com.example.quizzqpuc

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.json.JSONObject

object FaitsCache {
    private val cache = HashMap<String, JSONObject>()

    fun obtenir(ctx: Context, fichier: String): JSONObject = cache.getOrPut(fichier) {
        try {
            JSONObject(ctx.applicationContext.assets.open(fichier).bufferedReader().use { it.readText() })
        } catch (e: Exception) {
            JSONObject()
        }
    }

    /** Fun facts d'une entrée (élément, département ou État) ; liste vide si absents. */
    fun faits(ctx: Context, fichier: String, cle: String): List<String> {
        val o = obtenir(ctx, fichier)
        val a = o.optJSONArray(cle) ?: o.optJSONObject(cle)?.optJSONArray("ff") ?: return emptyList()
        return List(a.length()) { a.getString(it) }
    }

    fun villes(ctx: Context, fichier: String, cle: String): List<String> {
        val a = obtenir(ctx, fichier).optJSONObject(cle)?.optJSONArray("v") ?: return emptyList()
        return List(a.length()) { a.getString(it) }
    }
}

@Composable
fun BlocFaits(fichier: String, cle: String) {
    val ctx = LocalContext.current
    val faits = remember(fichier, cle) { FaitsCache.faits(ctx, fichier, cle) }
    if (faits.isEmpty()) return
    Spacer(Modifier.height(12.dp))
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text("Le saviez-vous ?", fontSize = 17.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
            faits.forEach {
                Text("• $it", fontSize = 15.sp, modifier = Modifier.padding(vertical = 3.dp))
            }
        }
    }
}
